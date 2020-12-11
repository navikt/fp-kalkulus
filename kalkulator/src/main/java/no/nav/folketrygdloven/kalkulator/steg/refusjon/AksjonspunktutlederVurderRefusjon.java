package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;

/**
 * Tjeneste for å utlede aksjonspunkter i steg for å vurdere refusjonskrav
 * Hvis det har kommet nye inntektsmeldinger siden forrige beregningsgrunnlag og disse leder til mindre
 * utbetaling til bruker skal det opprettes aksjonspunkt for å vurdere når disse refusjonskravene skal tas med.
 */
public final class AksjonspunktutlederVurderRefusjon {

    private AksjonspunktutlederVurderRefusjon() {
        // Skjuler default konstruktør
    }

    public static List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        List<BeregningAksjonspunktResultat> aksjonspunkter = new ArrayList<>();

        if (skalHaAksjonspunktVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering)) {
            aksjonspunkter.add(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunkt.VURDER_REFUSJONSKRAV));
        }

        return aksjonspunkter;
    }

    private static boolean skalHaAksjonspunktVurderRefusjonskrav(BeregningsgrunnlagInput input, BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
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
