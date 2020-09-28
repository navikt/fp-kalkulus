package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingSomIkkeKommerDto;

public class ArbeidstakerUtenInntektsmeldingTjeneste {

    private ArbeidstakerUtenInntektsmeldingTjeneste() {
        // Hide constructor
    }

    public static Collection<BeregningsgrunnlagPrStatusOgAndelDto> finnArbeidstakerAndelerUtenInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                              InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        if (!harArbeidstakerandel(beregningsgrunnlag)) {
            return Collections.emptyList();
        }

        List<InntektsmeldingSomIkkeKommerDto> manglendeInntektsmeldinger = inntektArbeidYtelseGrunnlag.getInntektsmeldingerSomIkkeKommer();
        if (manglendeInntektsmeldinger.isEmpty()) {
            return Collections.emptyList();
        }
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIFørstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList();
        return finnAndelerSomManglerIM(andelerIFørstePeriode, manglendeInntektsmeldinger);
    }

    private static boolean harArbeidstakerandel(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .anyMatch(andel -> andel.getAktivitetStatus().erArbeidstaker());
    }

    private static Collection<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelerSomManglerIM(Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                                            List<InntektsmeldingSomIkkeKommerDto> manglendeInntektsmeldinger) {
        return andeler.stream()
            .filter(a -> a.getBgAndelArbeidsforhold().isPresent())
            .filter(a -> matchAndelMedInntektsmeldingSomIkkeKommer(manglendeInntektsmeldinger, a))
            .collect(Collectors.toList());
    }

    private static boolean matchAndelMedInntektsmeldingSomIkkeKommer(List<InntektsmeldingSomIkkeKommerDto> manglendeInntektsmeldinger,
                                                                     BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return manglendeInntektsmeldinger.stream()
            .anyMatch(im -> andel.gjelderSammeArbeidsforhold(im.getArbeidsgiver(), im.getRef()));
    }
}
