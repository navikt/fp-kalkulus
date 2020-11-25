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
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingSomIkkeKommerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;

public class LønnsendringTjeneste {

    private LønnsendringTjeneste() {
        // Skjul
    }

    public static boolean brukerHarHattLønnsendringOgManglerInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        List<YrkesaktivitetDto> aktiviteterMedLønnsendringUtenIM = finnAlleAktiviteterMedLønnsendringUtenInntektsmelding(beregningsgrunnlag, iayGrunnlag);
        return !aktiviteterMedLønnsendringUtenIM.isEmpty();
    }

    public static List<YrkesaktivitetDto> finnAlleAktiviteterMedLønnsendringUtenInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var manglendeInntektsmeldinger = iayGrunnlag.getInntektsmeldingerSomIkkeKommer();
        if (manglendeInntektsmeldinger.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();

        Optional<AktørArbeidDto> aktørArbeid = iayGrunnlag.getAktørArbeidFraRegister();

        List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidstakerAndeler = alleArbeidstakerandeler(beregningsgrunnlag);

        if (aktørArbeid.isEmpty() || arbeidstakerAndeler.isEmpty()) {
            return Collections.emptyList();
        }
        // Alle arbeidstakerandeler har samme beregningsperiode, kan derfor ta fra den første
        LocalDate beregningsperiodeFom = arbeidstakerAndeler.get(0).getBeregningsperiodeFom();
        LocalDate beregningsperiodeTom = arbeidstakerAndeler.get(0).getBeregningsperiodeTom();
        if (beregningsperiodeFom == null || beregningsperiodeTom == null) {
            return Collections.emptyList();
        }

        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid).før(skjæringstidspunkt);
        Collection<YrkesaktivitetDto> aktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringIBeregningsperioden(filter, beregningsperiodeFom, beregningsperiodeTom, skjæringstidspunkt);
        if (aktiviteterMedLønnsendring.isEmpty()) {
            return Collections.emptyList();
        }
        return aktiviteterMedLønnsendring.stream()
            .filter(ya -> ya.getArbeidsgiver() != null && ya.getArbeidsgiver().getIdentifikator() != null)
            .filter(ya -> finnesKorresponderendeBeregningsgrunnlagsandel(arbeidstakerAndeler, ya))
            .filter(ya -> matchYrkesaktivitetMedInntektsmeldingSomIkkeKommer(manglendeInntektsmeldinger, ya))
            .collect(Collectors.toList());
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

    private static boolean matchYrkesaktivitetMedInntektsmeldingSomIkkeKommer(List<InntektsmeldingSomIkkeKommerDto> manglendeInntektsmeldinger, YrkesaktivitetDto yrkesaktivitet) {
        return manglendeInntektsmeldinger.stream()
            .anyMatch(im -> yrkesaktivitet.gjelderFor(im.getArbeidsgiver(), im.getRef()));
    }

    private static Collection<YrkesaktivitetDto> finnAktiviteterMedLønnsendringIBeregningsperioden(YrkesaktivitetFilterDto filter, LocalDate beregningsperiodeFom, LocalDate beregningsperiodeTom, LocalDate skjæringstidspunkt) {
        return filter.getYrkesaktiviteterForBeregning()
            .stream()
            .filter(ya -> !ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER.equals(ya.getArbeidType())
                && !ArbeidType.FRILANSER.equals(ya.getArbeidType()))
            .filter(ya -> filter.getAnsettelsesPerioder(ya).stream()
                .anyMatch(ap -> ap.getPeriode().inkluderer(skjæringstidspunkt)))
            .filter(ya -> harAvtalerMedLønnsendringIBeregningsgrunnlagperioden(filter.getAktivitetsAvtalerForArbeid(ya), beregningsperiodeFom, beregningsperiodeTom))
            .collect(Collectors.toList());
    }

    private static boolean harAvtalerMedLønnsendringIBeregningsgrunnlagperioden(Collection<AktivitetsAvtaleDto> aktivitetsAvtaler, LocalDate beregningsperiodeFom, LocalDate beregningsperiodeTom) {
        return aktivitetsAvtaler
                .stream()
                .filter(aa -> aa.getSisteLønnsendringsdato() != null)
                .filter(aa -> aa.getSisteLønnsendringsdato().equals(beregningsperiodeFom) || aa.getSisteLønnsendringsdato().isAfter(beregningsperiodeFom))
                .filter(aa -> aa.getSisteLønnsendringsdato().equals(beregningsperiodeTom) || aa.getSisteLønnsendringsdato().isBefore(beregningsperiodeTom))
                .count() > 0;
    }
}
