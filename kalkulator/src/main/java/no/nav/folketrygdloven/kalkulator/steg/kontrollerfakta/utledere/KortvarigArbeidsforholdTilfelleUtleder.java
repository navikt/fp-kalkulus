package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


/**
 * Utleder for fakta om beregning tilfeller for kortvarige/tidsbegrensede aktiviteter
 *
 * Ein arbeidsaktivitet er regnet som kortvarig om den oppfyller følgende kriterier
 *  - Starter før skjæringstidspunktet for beregning
 *  - Slutter etter skjæringstidspunktet for beregning
 *  - Har varighet under 6 måneder
 *
 */
@ApplicationScoped
@FagsakYtelseTypeRef("FP")
@FagsakYtelseTypeRef("SVP")
@FaktaOmBeregningTilfelleRef("VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD")
public class KortvarigArbeidsforholdTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        return utledTilfelleForKortvarigeArbeidsforhold(input, beregningsgrunnlagGrunnlag);
    }

    protected Optional<FaktaOmBeregningTilfelle> utledTilfelleForKortvarigeArbeidsforhold(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        return KortvarigArbeidsforholdTjeneste.harKortvarigeArbeidsforholdOgErIkkeSN(beregningsgrunnlag, input.getIayGrunnlag()) ?
            Optional.of(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD) : Optional.empty();
    }
}
