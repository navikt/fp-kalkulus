package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

class RullTilbakeBeregningsgrunnlagFelles implements RullTilbakeBeregningsgrunnlag {

    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    public RullTilbakeBeregningsgrunnlagFelles(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    @Override
    public void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, Long rullTilbakeKobling) {
        if (BeregningsgrunnlagTilstand.finnFørste().erFør(tilstand)) {
                beregningsgrunnlagRepository.reaktiverForrigeGrunnlagForKoblinger(rullTilbakeKobling, tilstand);
        }
    }
}
