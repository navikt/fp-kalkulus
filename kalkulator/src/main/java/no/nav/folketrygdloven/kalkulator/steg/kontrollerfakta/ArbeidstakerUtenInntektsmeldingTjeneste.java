package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

public class ArbeidstakerUtenInntektsmeldingTjeneste {

    private ArbeidstakerUtenInntektsmeldingTjeneste() {
        // Hide constructor
    }

    public static Collection<BeregningsgrunnlagPrStatusOgAndelDto> finnArbeidstakerAndelerUtenInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                              InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        if (!harArbeidstakerandel(beregningsgrunnlag)) {
            return Collections.emptyList();
        }

        List<InntektsmeldingDto> inntektsmeldinger = inntektArbeidYtelseGrunnlag.getInntektsmeldinger().stream()
                .flatMap(im -> im.getAlleInntektsmeldinger().stream())
                .collect(Collectors.toList());

        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> a.getAktivitetStatus().erArbeidstaker() && a.getArbeidsgiver().isPresent())
                .filter(a -> inntektsmeldinger.stream().noneMatch(im -> a.gjelderInntektsmeldingFor(im.getArbeidsgiver(), im.getArbeidsforholdRef())))
                .collect(Collectors.toList());

    }

    private static boolean harArbeidstakerandel(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .anyMatch(andel -> andel.getAktivitetStatus().erArbeidstaker());
    }

}
