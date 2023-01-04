package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.Set;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

class RullTilbakeBeregningsgrunnlagFelles implements RullTilbakeBeregningsgrunnlag {

    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    public RullTilbakeBeregningsgrunnlagFelles(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    @Override
    public void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, Set<Long> rullTilbakeKoblinger) {
        if (BeregningsgrunnlagTilstand.finnFørste().erFør(tilstand)) {
                beregningsgrunnlagRepository.reaktiverForrigeGrunnlagForKoblinger(rullTilbakeKoblinger, tilstand);
        }
    }
}
