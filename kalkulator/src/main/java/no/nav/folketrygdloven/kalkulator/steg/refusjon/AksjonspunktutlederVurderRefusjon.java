package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public final class AksjonspunktutlederVurderRefusjon {

    private AksjonspunktutlederVurderRefusjon() {
        // Skjuler default
    }

    public static boolean skalHaAksjonspunktVurderRefusjonskrav(BeregningsgrunnlagInput input, BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        if (!(input instanceof VurderRefusjonBeregningsgrunnlagInput)) {
            throw new IllegalStateException("Har ikke korrekt input for å vurdere aksjsonspunkt i vurder_refusjon steget");
        }
        VurderRefusjonBeregningsgrunnlagInput vurderInput = (VurderRefusjonBeregningsgrunnlagInput) input;
        Optional<BeregningsgrunnlagGrunnlagDto> orginaltBGGrunnlag = vurderInput.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        if (orginaltBGGrunnlag.isEmpty() || orginaltBGGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag).isEmpty()) {
            return false;
        }
        BeregningsgrunnlagDto orginaltBG = orginaltBGGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag).get();
        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjonIUtbetaltPeriode = AndelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(periodisertMedRefusjonOgGradering, orginaltBG);

        return !andelerMedØktRefusjonIUtbetaltPeriode.isEmpty();
    }
}
