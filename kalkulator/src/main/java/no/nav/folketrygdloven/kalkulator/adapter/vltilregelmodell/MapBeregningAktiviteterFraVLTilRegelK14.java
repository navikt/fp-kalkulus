package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.tidsserie.LocalDateInterval;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
@FagsakYtelseTypeRef("SVP")
public class MapBeregningAktiviteterFraVLTilRegelK14 implements MapBeregningAktiviteterFraVLTilRegel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapBeregningAktiviteterFraVLTilRegelK14.class);

    @Override
    public AktivitetStatusModell mapForSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input) {
        LocalDate opptjeningSkjæringstidspunkt = input.getSkjæringstidspunktOpptjening();

        AktivitetStatusModell modell = new AktivitetStatusModell();
        modell.setSkjæringstidspunktForOpptjening(opptjeningSkjæringstidspunkt);

        var relevanteAktiviteter = input.getOpptjeningAktiviteterForBeregning();

        if (!relevanteAktiviteter.isEmpty()) {
            relevanteAktiviteter.forEach(opptjeningsperiode -> modell.leggTilEllerOppdaterAktivPeriode(lagAktivPeriode(input.getInntektsmeldinger(),
                    input.getIayGrunnlag(),
                    opptjeningsperiode,
                    input.getSkjæringstidspunktOpptjening())));
        }

        return modell;
    }

    private AktivPeriode lagAktivPeriode(Collection<InntektsmeldingDto> inntektsmeldinger,
                                         InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                         OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode,
                                         LocalDate skjæringstidspunktOpptjening) {
        Aktivitet aktivitetType = MapOpptjeningAktivitetTypeFraVLTilRegel.map(opptjeningsperiode.getOpptjeningAktivitetType());
        var gjeldendePeriode = opptjeningsperiode.getPeriode();
        var regelPeriode = Periode.of(gjeldendePeriode.getFomDato(), gjeldendePeriode.getTomDato());
        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return AktivPeriode.forFrilanser(regelPeriode);
        } else if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            var opptjeningArbeidsgiverAktørId = opptjeningsperiode.getArbeidsgiverAktørId();
            var opptjeningArbeidsgiverOrgnummer = opptjeningsperiode.getArbeidsgiverOrgNummer();
            var opptjeningArbeidsforhold = opptjeningsperiode.getArbeidsforholdId();
            var arbeidsgiver = opptjeningsperiode.getArbeidsgiver().orElseThrow(() -> new IllegalStateException("Forventer arbeidsgiver"));
            var yrkesaktiviteter = iayGrunnlag
                    .getAktørArbeidFraRegister()
                    .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                    .orElse(Collections.emptyList());
            var gjeldendeTOM = finnTomdatoTaHensynTilPermisjon(yrkesaktiviteter, opptjeningsperiode, skjæringstidspunktOpptjening, arbeidsgiver);
            if (gjeldendeTOM.isBefore(skjæringstidspunktOpptjening) && !gjeldendeTOM.equals(gjeldendePeriode.getTomDato())) {
                LOGGER.info("FT-687459: Utledet tom {} på arbeidsgiver {} og referanse {}. Opprinnelig tom var {}", gjeldendeTOM,
                        opptjeningsperiode.getArbeidsgiver(),
                        opptjeningsperiode.getArbeidsforholdId(),
                        gjeldendePeriode.getTomDato());
            }
            var arbeidsperiode = Periode.of(gjeldendePeriode.getFomDato(), gjeldendeTOM);
            return LagAktivPeriodeForArbeidstakerFelles.lagAktivPeriodeForArbeidstaker(inntektsmeldinger, arbeidsperiode, opptjeningArbeidsgiverAktørId,
                opptjeningArbeidsgiverOrgnummer, opptjeningArbeidsforhold);
        } else {
            return AktivPeriode.forAndre(aktivitetType, regelPeriode);
        }
    }

    private LocalDate finnTomdatoTaHensynTilPermisjon(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                      OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode,
                                                      LocalDate skjæringstidspunktOpptjening,
                                                      Arbeidsgiver arbeidsgiver) {
        var relevantYrkesaktivitet = yrkesaktiviteter
                .stream()
                .filter(ya -> ya.gjelderFor(
                        arbeidsgiver,
                        opptjeningsperiode.getArbeidsforholdId()))
                .findFirst();
        Optional<LocalDate> sisteDagFørPermisjonStart = relevantYrkesaktivitet.flatMap(ya -> finnSisteDagFørPermisjonsstart(skjæringstidspunktOpptjening, opptjeningsperiode, ya));
        if (sisteDagFørPermisjonStart.isPresent() && sisteDagFørPermisjonStart.get().isBefore(opptjeningsperiode.getPeriode().getFomDato())) {
            // Kan skje pga opptjening ikke hensyntar permisjon, og ingenting stopper aareg fra å ha permisjon hele arbeidsperioden
            return opptjeningsperiode.getPeriode().getFomDato();
        }
        return sisteDagFørPermisjonStart.orElse(opptjeningsperiode.getPeriode().getTomDato());
    }

    private Optional<LocalDate> finnSisteDagFørPermisjonsstart(LocalDate skjæringstidspunktOpptjening, OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode, YrkesaktivitetDto relevantYrkesaktivitet) {
        return relevantYrkesaktivitet.getPermisjoner()
                .stream()
                .filter(p -> p.getPeriode().getTomDato() != null && !p.getPeriode().getTomDato().equals(LocalDateInterval.TIDENES_ENDE)
                        && p.getProsentsats() != null && p.getProsentsats().compareTo(BigDecimal.valueOf(100)) == 0 &&
                        p.getPeriode().inkluderer(skjæringstidspunktOpptjening)
                        && p.getPeriode().overlapper(opptjeningsperiode.getPeriode()))
                .map(PermisjonDto::getPeriode)
                .map(Intervall::getFomDato)
                .min(Comparator.naturalOrder())
                .map(d -> d.minusDays(1));
    }
}
