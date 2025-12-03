package no.nav.folketrygdloven.kalkulus.domene.tjeneste.beregningsgrunnlag;

import no.nav.folketrygdloven.kalkulus.domene.beregning.MapStegTilTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;


class RullTilbakeTilFastsattInn implements RullTilbakeBeregningsgrunnlag {

    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private final AvklaringsbehovTjeneste avklaringsbehovTjeneste;


    public RullTilbakeTilFastsattInn(BeregningsgrunnlagRepository beregningsgrunnlagRepository, AvklaringsbehovTjeneste avklaringsbehovTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }

    @Override
    public void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, Long rullTilbakeKobling) {
            var harAvklaringsbehovIStegUt = harKoblingAvklaringsbehovIStegUt(tilstand, rullTilbakeKobling);
            beregningsgrunnlagRepository.reaktiverSisteMedTilstand(tilstand, rullTilbakeKobling);

            if (!harAvklaringsbehovIStegUt) {
                beregningsgrunnlagRepository.reaktiverForrigeGrunnlagForKoblinger(rullTilbakeKobling, tilstand);
            }
    }


    private boolean harKoblingAvklaringsbehovIStegUt(BeregningsgrunnlagTilstand tilstand, Long rullTilbakeKobling) {
        return avklaringsbehovTjeneste.hentAlleAvklaringsbehovForKobling(rullTilbakeKobling).stream()
                .anyMatch(a -> MapStegTilTilstand.mapTilStegUtTilstand(a.getStegFunnet()).map(tilstand::equals).orElse(false));
    }


}
