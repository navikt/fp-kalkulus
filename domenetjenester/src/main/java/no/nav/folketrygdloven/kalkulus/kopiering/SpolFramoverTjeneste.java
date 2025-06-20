package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.kopiering.KanKopierBeregningsgrunnlag.kanKopiereForrigeGrunnlagAvklartIStegUt;

import java.util.Collection;
import java.util.Optional;

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
        return kanSpoleFramHeleGrunnlaget ? forrigeGrunnlagFraStegUt : Optional.empty();
    }
}
