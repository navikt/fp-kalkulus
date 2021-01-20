package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
@FagsakYtelseTypeRef("SVP")
public class MapBeregningAktiviteterFraVLTilRegelK14 implements MapBeregningAktiviteterFraVLTilRegel {

    @Override
    public AktivitetStatusModell mapForSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input) {
        LocalDate opptjeningSkjæringstidspunkt = input.getSkjæringstidspunktOpptjening();

        AktivitetStatusModell modell = new AktivitetStatusModell();
        modell.setSkjæringstidspunktForOpptjening(opptjeningSkjæringstidspunkt);

        var relevanteAktiviteter = input.getOpptjeningAktiviteterForBeregning();

        if (!relevanteAktiviteter.isEmpty()) {
            relevanteAktiviteter.forEach(opptjeningsperiode -> modell.leggTilEllerOppdaterAktivPeriode(lagAktivPeriode(input.getInntektsmeldinger(), opptjeningsperiode, relevanteAktiviteter)));
        }

        return modell;
    }

    private AktivPeriode lagAktivPeriode(Collection<InntektsmeldingDto> inntektsmeldinger,
                                         OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode,
                                         Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> relevanteAktiviteter) {
        Aktivitet aktivitetType = MapOpptjeningAktivitetTypeFraVLTilRegel.map(opptjeningsperiode.getOpptjeningAktivitetType());
        var gjeldendePeriode = opptjeningsperiode.getPeriode();
        var regelPeriode = Periode.of(gjeldendePeriode.getFomDato(), gjeldendePeriode.getTomDato());
        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return AktivPeriode.forFrilanser(regelPeriode);
        } else if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            var opptjeningArbeidsgiverAktørId = opptjeningsperiode.getArbeidsgiverAktørId();
            var opptjeningArbeidsgiverOrgnummer = opptjeningsperiode.getArbeidsgiverOrgNummer();
            var opptjeningArbeidsforhold = Optional.ofNullable(opptjeningsperiode.getArbeidsforholdId()).orElse(InternArbeidsforholdRefDto.nullRef());
            return LagAktivPeriodeForArbeidstakerFelles.lagAktivPeriodeForArbeidstaker(inntektsmeldinger, regelPeriode, opptjeningArbeidsgiverAktørId,
                opptjeningArbeidsgiverOrgnummer, opptjeningArbeidsforhold, relevanteAktiviteter);
        } else {
            return AktivPeriode.forAndre(aktivitetType, regelPeriode);
        }
    }

}
