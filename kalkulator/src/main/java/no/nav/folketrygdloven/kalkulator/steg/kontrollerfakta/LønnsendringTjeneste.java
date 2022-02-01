package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

public class LønnsendringTjeneste {

    private LønnsendringTjeneste() {
        // Skjul
    }

    public static boolean brukerHarHattLønnsendringISisteMånedOgManglerInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger) {
        List<YrkesaktivitetDto> aktiviteterMedLønnsendringUtenIM = finnAktiviteterMedLønnsendringEtterFørsteDagISisteMåned(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger);
        return !aktiviteterMedLønnsendringUtenIM.isEmpty();
    }

    public static boolean brukerHarHattLønnsendringIHeleBeregningsperiodenOgManglerInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger) {
        List<YrkesaktivitetDto> aktiviteter = finnAktiviteterMedLønnsendringUtenInntektsmeldingIHeleBeregningsperioden(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger);
        return !aktiviteter.isEmpty();

    }

    /**
     * Finner aktiviteter som har lønnsendring etter den første dagen i siste måned før skjæringstidspunktet
     *
     * @param beregningsgrunnlag Beregningsgrunnlag
     * @param iayGrunnlag        InntektArbeidYtelseGrunnlag
     * @return Liste med aktiviteter som har lønnsendring
     */
    public static List<YrkesaktivitetDto> finnAktiviteterMedLønnsendringEtterFørsteDagISisteMåned(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                                  Collection<InntektsmeldingDto> inntektsmeldinger) {
        LocalDate stpBeregning = beregningsgrunnlag.getSkjæringstidspunkt();
        // Vi teller ikkje med første dag, siden man då har ein heil måned med inntekt å beregne fra
        Intervall sisteMåned = Intervall.fraOgMedTilOgMed(stpBeregning.minusMonths(1).withDayOfMonth(2), stpBeregning);
        return finnAktiviteterMedLønnsendringUtenInntektsmelding(beregningsgrunnlag, iayGrunnlag, sisteMåned, inntektsmeldinger);
    }

    /**
     * Finner aktiviteter som har lønnsendring i beregningsperioden
     *
     * @param beregningsgrunnlag Beregningsgrunnlag
     * @param iayGrunnlag        InntektArbeidYtelseGrunnlag
     * @return Liste med aktiviteter som har lønnsendring
     */
    public static List<YrkesaktivitetDto> finnAktiviteterMedLønnsendringUtenInntektsmeldingIHeleBeregningsperioden(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                                   InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                                                   Collection<InntektsmeldingDto> inntektsmeldinger) {
        LocalDate stpBeregning = beregningsgrunnlag.getSkjæringstidspunkt();
        Intervall beregningsperiode = new BeregningsperiodeTjeneste().fastsettBeregningsperiodeForATFLAndeler(stpBeregning);
        return finnAktiviteterMedLønnsendringUtenInntektsmelding(beregningsgrunnlag, iayGrunnlag, Intervall.fraOgMedTilOgMed(beregningsperiode.getFomDato(), stpBeregning), inntektsmeldinger);
    }

    public static List<YrkesaktivitetDto> finnAktiviteterMedLønnsendringUtenInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                            InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                            Intervall periode,
                                                                                            Collection<InntektsmeldingDto> inntektsmeldinger) {
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();

        Optional<AktørArbeidDto> aktørArbeid = iayGrunnlag.getAktørArbeidFraRegister();

        List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidstakerAndeler = alleArbeidstakerandeler(beregningsgrunnlag);

        if (aktørArbeid.isEmpty() || arbeidstakerAndeler.isEmpty()) {
            return Collections.emptyList();
        }

        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid).før(skjæringstidspunkt);
        Collection<YrkesaktivitetDto> aktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringIPerioden(filter, periode, skjæringstidspunkt);
        if (aktiviteterMedLønnsendring.isEmpty()) {
            return Collections.emptyList();
        }
        return aktiviteterMedLønnsendring.stream()
                .filter(ya -> ya.getArbeidsgiver() != null && ya.getArbeidsgiver().getIdentifikator() != null)
                .filter(ya -> finnesKorresponderendeBeregningsgrunnlagsandel(arbeidstakerAndeler, ya))
                .filter(ya -> manglerInntektsmelding(inntektsmeldinger, ya))
                .collect(Collectors.toList());
    }

    private static boolean manglerInntektsmelding(Collection<InntektsmeldingDto> inntektsmeldinger, YrkesaktivitetDto ya) {
        return inntektsmeldinger.stream().noneMatch(im -> ya.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()));
    }

    private static boolean finnesKorresponderendeBeregningsgrunnlagsandel(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                          YrkesaktivitetDto a) {
        return andeler.stream()
                .anyMatch(andel -> andel.gjelderSammeArbeidsforhold(a.getArbeidsgiver(), a.getArbeidsforholdRef()));
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> alleArbeidstakerandeler(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList).flatMap(Collection::stream)
                .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
                .collect(Collectors.toList());
    }

    private static Collection<YrkesaktivitetDto> finnAktiviteterMedLønnsendringIPerioden(YrkesaktivitetFilterDto filter, Intervall periode, LocalDate skjæringstidspunkt) {
        return filter.getYrkesaktiviteterForBeregning()
                .stream()
                .filter(ya -> !ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER.equals(ya.getArbeidType())
                        && !ArbeidType.FRILANSER.equals(ya.getArbeidType()))
                .filter(ya -> filter.getAnsettelsesPerioder(ya).stream()
                        .anyMatch(ap -> ap.getPeriode().inkluderer(skjæringstidspunkt)))
                .filter(ya -> harAvtalerMedLønnsendringIPerioden(filter.getAktivitetsAvtalerForArbeid(ya), periode))
                .collect(Collectors.toList());
    }

    private static boolean harAvtalerMedLønnsendringIPerioden(Collection<AktivitetsAvtaleDto> aktivitetsAvtaler, Intervall periode) {
        return aktivitetsAvtaler
                .stream()
                .filter(aa -> aa.getSisteLønnsendringsdato() != null)
                .filter(aa -> aa.getSisteLønnsendringsdato().equals(periode.getFomDato())
                        || aa.getSisteLønnsendringsdato().isAfter(periode.getFomDato()))
                .anyMatch(aa -> aa.getSisteLønnsendringsdato().equals(periode.getTomDato())
                        || aa.getSisteLønnsendringsdato().isBefore(periode.getTomDato()));
    }
}
