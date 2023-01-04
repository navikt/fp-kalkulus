package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;


class RullTilbakeTilFordel implements RullTilbakeBeregningsgrunnlag {

    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private final AvklaringsbehovTjeneste avklaringsbehovTjeneste;


    public RullTilbakeTilFordel(BeregningsgrunnlagRepository beregningsgrunnlagRepository, AvklaringsbehovTjeneste avklaringsbehovTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }

    @Override
    public void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, Set<Long> rullTilbakeKoblinger, boolean skalKjøreSteget) {
        if (skalKjøreSteget) {
            beregningsgrunnlagRepository.reaktiverForrigeGrunnlagForKoblinger(rullTilbakeKoblinger, tilstand);
        } else {
            var koblingerMedAvklaringsbehovIStegUt = finnKoblingerMedAvklaringsbehovIStegUt(tilstand, rullTilbakeKoblinger);
            beregningsgrunnlagRepository.reaktiverSisteMedTilstand(tilstand, koblingerMedAvklaringsbehovIStegUt);

            var koblingerUtenAvklaringsbehovIStegUt = rullTilbakeKoblinger.stream().filter(k -> !koblingerMedAvklaringsbehovIStegUt.contains(k)).collect(Collectors.toSet());
            beregningsgrunnlagRepository.reaktiverForrigeGrunnlagForKoblinger(koblingerUtenAvklaringsbehovIStegUt, tilstand);
        }
    }


    private Set<Long> finnKoblingerMedAvklaringsbehovIStegUt(BeregningsgrunnlagTilstand tilstand, Set<Long> rullTilbakeKoblinger) {
        return avklaringsbehovTjeneste.hentAlleAvklaringsbehovForKoblinger(rullTilbakeKoblinger).stream()
                .filter(a -> MapStegTilTilstand.mapTilStegUtTilstand(a.getStegFunnet()).map(tilstand::equals).orElse(false))
                .map(AvklaringsbehovEntitet::getKoblingId)
                .collect(Collectors.toSet());
    }


}
