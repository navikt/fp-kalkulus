package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
@FagsakYtelseTypeRef("PSB")
public class MapBeregningAktiviteterFraVLTilRegelK9 implements MapBeregningAktiviteterFraVLTilRegel {

    @Override
    public AktivitetStatusModell mapForSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input) {
        LocalDate opptjeningSkjæringstidspunkt = input.getSkjæringstidspunktOpptjening();

        AktivitetStatusModell modell = new AktivitetStatusModell();
        modell.setSkjæringstidspunktForOpptjening(opptjeningSkjæringstidspunkt);

        var relevanteAktiviteter = input.getOpptjeningAktiviteterForBeregningK9();

        if (!relevanteAktiviteter.isEmpty()) {
            var relevantYrkesaktivitet = input.getIayGrunnlag().getAktørArbeidFraRegister()
                    .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                    .orElse(Collections.emptyList());
            List<AktivPeriode> aktivePerioder = relevanteAktiviteter.stream()
                    .flatMap(opptjeningsperiode ->
                            lagAktivPeriode(
                                    input.getInntektsmeldinger(),
                                    relevantYrkesaktivitet,
                                    opptjeningsperiode,
                                    relevanteAktiviteter,
                                    input.getSkjæringstidspunktOpptjening())
                            .stream()
                    ).collect(Collectors.toList());
            aktivePerioder.forEach(modell::leggTilEllerOppdaterAktivPeriode);
        }

        return modell;
    }

    private Optional<AktivPeriode> lagAktivPeriode(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                   Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                   OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode,
                                                   Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> relevanteAktiviteter,
                                                   LocalDate skjæringstidspunktOpptjening) {
        Aktivitet aktivitetType = MapOpptjeningAktivitetTypeFraVLTilRegel.map(opptjeningsperiode.getOpptjeningAktivitetType());
        var gjeldendePeriode = opptjeningsperiode.getPeriode();
        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return Optional.of(AktivPeriode.forFrilanser(
                    Periode.of(gjeldendePeriode.getFomDato(), gjeldendePeriode.getTomDato())));
        } else if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            Arbeidsgiver arbeidsgiver = opptjeningsperiode.getArbeidsgiver().orElseThrow(() -> new IllegalStateException("Forventer arbeidsgiver"));

            // Hvis vi ikke har relevante aktiviteter på stp uten permisjon må vi ignorere permisjonsperioder for å få aktiviteter til beregning.
            boolean finnesAktivitePåSTPOmPermisjonHensyntas = finnesAktivitetPåSTPUtenPermisjon(yrkesaktiviteter, opptjeningsperiode, skjæringstidspunktOpptjening, gjeldendePeriode, relevanteAktiviteter);
            LocalDate tomDato = finnesAktivitePåSTPOmPermisjonHensyntas
                    ? finnTomdatoTaHensynTilPermisjon(yrkesaktiviteter, opptjeningsperiode, skjæringstidspunktOpptjening, gjeldendePeriode, arbeidsgiver)
                    : gjeldendePeriode.getTomDato();
            if (!tomDato.isBefore(gjeldendePeriode.getFomDato())) {
                return Optional.of(lagAktivPeriodeForArbeidstaker(inntektsmeldinger,
                        Periode.of(gjeldendePeriode.getFomDato(), tomDato),
                        arbeidsgiver,
                        opptjeningsperiode.getArbeidsforholdId(),
                        relevanteAktiviteter));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(AktivPeriode.forAndre(
                    aktivitetType,
                    Periode.of(gjeldendePeriode.getFomDato(), gjeldendePeriode.getTomDato())));
        }
    }

    private boolean finnesAktivitetPåSTPUtenPermisjon(Collection<YrkesaktivitetDto> yrkesaktiviteter, OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode, LocalDate skjæringstidspunktOpptjening, Intervall gjeldendePeriode, Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> relevanteAktiviteter) {
        List<LocalDate> alleTOMDatoerHensyntattPermisjon = relevanteAktiviteter.stream().map(ra -> {
            Arbeidsgiver arbeidsgiver = opptjeningsperiode.getArbeidsgiver().orElseThrow(() -> new IllegalStateException("Forventer arbeidsgiver"));
            return finnTomdatoTaHensynTilPermisjon(yrkesaktiviteter, opptjeningsperiode, skjæringstidspunktOpptjening, gjeldendePeriode, arbeidsgiver);
        }).collect(Collectors.toList());
        return alleTOMDatoerHensyntattPermisjon.stream().anyMatch(tom -> !tom.isBefore(skjæringstidspunktOpptjening));
    }

    private long finnAntallAktiviteterPåSTP(Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> relevanteAktiviteter, LocalDate skjæringstidspunktOpptjening) {
        return relevanteAktiviteter.stream().filter(akt -> akt.getPeriode().inkluderer(skjæringstidspunktOpptjening)).count();
    }

    protected static AktivPeriode lagAktivPeriodeForArbeidstaker(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                 Periode gjeldendePeriode,
                                                                 Arbeidsgiver arbeidsgiver,
                                                                 InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                 Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> alleAktiviteter) {
        if (arbeidsgiver.erAktørId()) {
            return lagAktivePerioderForArbeidstakerHosPrivatperson(arbeidsgiver.getIdentifikator(), gjeldendePeriode);
        } else if (arbeidsgiver.getErVirksomhet()) {
            return lagAktivePerioderForArbeidstakerHosVirksomhet(inntektsmeldinger, gjeldendePeriode, arbeidsgiver.getIdentifikator(), arbeidsforholdRef, alleAktiviteter);
        } else {
            throw new IllegalStateException("Må ha en arbeidsgiver som enten er aktør eller virksomhet når aktivitet er " + Aktivitet.ARBEIDSTAKERINNTEKT);
        }
    }

    private LocalDate finnTomdatoTaHensynTilPermisjon(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                      OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode,
                                                      LocalDate skjæringstidspunktOpptjening,
                                                      Intervall gjeldendePeriode,
                                                      Arbeidsgiver arbeidsgiver) {
        var relevantYrkesaktivitet = yrkesaktiviteter
                .stream()
                .filter(ya -> ya.gjelderFor(
                        arbeidsgiver,
                        opptjeningsperiode.getArbeidsforholdId())
                ).findFirst()
                .orElseThrow(() -> new IllegalStateException("Forventer å finne yrkesaktivitet for arbeidstakerinntekt"));
        Optional<LocalDate> sisteDagFørPermisjonStart = finnSisteDagFørPermisjonsstart(skjæringstidspunktOpptjening, opptjeningsperiode, relevantYrkesaktivitet);
        return sisteDagFørPermisjonStart.orElse(gjeldendePeriode.getTomDato());
    }

    private Optional<LocalDate> finnSisteDagFørPermisjonsstart(LocalDate skjæringstidspunktOpptjening, OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode, YrkesaktivitetDto relevantYrkesaktivitet) {
        return relevantYrkesaktivitet.getPermisjoner()
                .stream()
                .filter(p -> p.getProsentsats() != null && p.getProsentsats().compareTo(BigDecimal.valueOf(100)) == 0 &&
                        p.getPeriode().inkluderer(skjæringstidspunktOpptjening)
                        && p.getPeriode().overlapper(opptjeningsperiode.getPeriode()))
                .map(PermisjonDto::getPeriode)
                .map(Intervall::getFomDato)
                .min(Comparator.naturalOrder())
                .map(d -> d.minusDays(1));
    }

    private static AktivPeriode lagAktivePerioderForArbeidstakerHosPrivatperson(String aktørId, Periode gjeldendePeriode) {
        return AktivPeriode.forArbeidstakerHosPrivatperson(gjeldendePeriode, aktørId);
    }

    private static AktivPeriode lagAktivePerioderForArbeidstakerHosVirksomhet(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                              Periode gjeldendePeriode,
                                                                              String opptjeningArbeidsgiverOrgnummer,
                                                                              InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                              Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> alleAktiviteter) {
        if (harInntektsmeldingForArbeidsforhold(inntektsmeldinger, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef)) {
            return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef.getReferanse());
        } else {
            if (harInntektsmeldingForSpesifiktArbeidVedSkjæringstidspunktet(inntektsmeldinger, alleAktiviteter, opptjeningArbeidsgiverOrgnummer)) {
                // Her mangler vi inntektsmelding fra minst ett arbeidsforhold. Disse andelene får Id for at de ikke skal behandles som aggregat-andeler.
                return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef.getReferanse());
            }
            return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, null);
        }
    }

    /**
     * Sjekker om det er mottatt inntektsmelding for et spesifikt arbeidsforhold og et gitt orgnummer
     * og om dette spesifikke arbeidsforholdet er aktivt på skjæringstidspunktet for opptjening.
     * <p>
     * Tilpassningen er gjort for å støtte caset der omsorgspenger kun mottar inntektsmeldinger for arbeidsforhold der man har fravær (https://jira.adeo.no/browse/TSF-1153)
     *
     * @param inntektsmeldinger               Innteksmeldinger
     * @param alleAktiviteter                 Alle aktiviteter
     * @param opptjeningArbeidsgiverOrgnummer Orgnnummer for arbeidsaktivitet
     * @return Boolean som sier om det er motttatt inntektsmelding for et spesifikt arbeidsforhold som er aktivt på skjæringstidspunktet
     */
    private static boolean harInntektsmeldingForSpesifiktArbeidVedSkjæringstidspunktet(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                                       Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> alleAktiviteter,
                                                                                       String opptjeningArbeidsgiverOrgnummer) {
        return alleAktiviteter.stream()
                .filter(a -> a.getArbeidsgiverOrgNummer() != null && a.getArbeidsgiverOrgNummer().equals(opptjeningArbeidsgiverOrgnummer))
                .anyMatch(a -> OpptjeningAktivitetType.ARBEID.equals(a.getType()) && inntektsmeldinger.stream()
                        .anyMatch(im -> im.getArbeidsgiver().getIdentifikator().equals(a.getArbeidsgiverOrgNummer())
                                && im.gjelderForEtSpesifiktArbeidsforhold()
                                && im.getArbeidsforholdRef().gjelderFor(a.getArbeidsforholdId())));
    }

    private static boolean harInntektsmeldingForArbeidsforhold(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                               String orgnummer,
                                                               InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (!arbeidsforholdRef.gjelderForSpesifiktArbeidsforhold()) {
            return false;
        } else {
            return inntektsmeldinger.stream()
                    .anyMatch(im -> im.gjelderForEtSpesifiktArbeidsforhold()
                            && Objects.equals(im.getArbeidsgiver().getOrgnr(), orgnummer)
                            && Objects.equals(im.getArbeidsforholdRef().getReferanse(), arbeidsforholdRef.getReferanse()));
        }
    }
}
