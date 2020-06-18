package no.nav.folketrygdloven.kalkulator.ytelse.omp;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.jetbrains.annotations.NotNull;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.KortvarigArbeidsforholdTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;


@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class KortvarigArbeidsforholdTilfelleUtlederOMP extends KortvarigArbeidsforholdTilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        if (harRefusjonPåSkjæringstidspunktet(input)) {
           return Optional.empty();
        }
        return utledTilfelleForKortvarigeArbeidsforhold(input, beregningsgrunnlagGrunnlag);
    }

    private boolean harRefusjonPåSkjæringstidspunktet(BeregningsgrunnlagInput input) {
        List<InntektsmeldingDto> inntektsmeldinger = finnInntektsmeldinger(input);
        return harMinstEnInntektsmeldingMedRefusjonFraSkjæringstidspunktet(input, inntektsmeldinger);
    }

    private boolean harMinstEnInntektsmeldingMedRefusjonFraSkjæringstidspunktet(BeregningsgrunnlagInput input, List<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream().anyMatch(im -> harInntektsmeldingRefusjonFraStart(im) || harEndringIRefusjonFraSkjæringstidspunktet(input, im));
    }

    private List<InntektsmeldingDto> finnInntektsmeldinger(BeregningsgrunnlagInput input) {
        return input.getIayGrunnlag().getInntektsmeldinger()
                .map(InntektsmeldingAggregatDto::getInntektsmeldingerSomSkalBrukes)
                .orElse(List.of());
    }

    private boolean harEndringIRefusjonFraSkjæringstidspunktet(BeregningsgrunnlagInput input, InntektsmeldingDto im) {
        return im.getEndringerRefusjon().stream().anyMatch(er -> er.getFom().equals(input.getSkjæringstidspunktForBeregning()) && !er.getRefusjonsbeløp().erNullEllerNulltall());
    }

    private boolean harInntektsmeldingRefusjonFraStart(InntektsmeldingDto im) {
        return im.getRefusjonBeløpPerMnd() != null
                && !im.getRefusjonBeløpPerMnd().erNullEllerNulltall();
    }
}
