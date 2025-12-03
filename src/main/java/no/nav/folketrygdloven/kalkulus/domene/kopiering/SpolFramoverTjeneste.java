package no.nav.folketrygdloven.kalkulus.domene.kopiering;

import static no.nav.folketrygdloven.kalkulus.domene.kopiering.KanKopierBeregningsgrunnlag.kanKopiereForrigeGrunnlagAvklartIStegUt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;

public final class SpolFramoverTjeneste {

    private SpolFramoverTjeneste() {
        // Skjul
    }


    /**
     * Spoler grunnlaget framover en tilstand om dette er mulig.
     *
     * @param avklaringsbehov          avklaringsbehov som er utledet i steget
     * @param nyttGrunnlag             nytt grunnlag som er opprettet i steget
     * @param forrigeGrunnlagFraSteg   forrige grunnlag fra steget
     * @param forrigeGrunnlagFraStegUt forrige grunnlag fra steg ut
     * @return Builder for grunnlag som det skal spoles fram til
     */
    public static Optional<BeregningsgrunnlagGrunnlagDto> finnGrunnlagDetSkalSpolesTil(Collection<BeregningAvklaringsbehovResultat> avklaringsbehov,
                                                                                       BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                                       Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraSteg,
                                                                                       Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraStegUt) {

        boolean kanSpoleFramHeleGrunnlaget = kanKopiereForrigeGrunnlagAvklartIStegUt(avklaringsbehov, nyttGrunnlag, forrigeGrunnlagFraSteg);

        // Denne trengs intill vi får løst TFP-6324
        var harSammeTilfeller = harSammeTilfeller(nyttGrunnlag, forrigeGrunnlagFraStegUt);

        return harSammeTilfeller && kanSpoleFramHeleGrunnlaget ? forrigeGrunnlagFraStegUt : Optional.empty();
    }

    private static boolean harSammeTilfeller(BeregningsgrunnlagGrunnlagDto nyttGrunnlag, Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraStegUt) {
        var nyeTilfeller = nyttGrunnlag.getBeregningsgrunnlagHvisFinnes().map(BeregningsgrunnlagDto::getFaktaOmBeregningTilfeller).orElse(List.of());
        var tilfellerFraForrigeGrunnlagFraStegUt = forrigeGrunnlagFraStegUt.map(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag).map(BeregningsgrunnlagDto::getFaktaOmBeregningTilfeller).orElse(
            List.of());
        return nyeTilfeller.size() == tilfellerFraForrigeGrunnlagFraStegUt.size() && nyeTilfeller.containsAll(tilfellerFraForrigeGrunnlagFraStegUt);
    }
}
