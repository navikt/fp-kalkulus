package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;

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
            aksjonspunkter.add(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.VURDER_REFUSJONSKRAV));
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
        if (andelerMedØktRefusjonIUtbetaltPeriode.isEmpty()) {
            return false;
        }

        // Vi skal ikke opprette aksjonspunkt hvis en andel alltid har hatt refusjon, men refusjonskravet har økt, da vi ikke har løsning for disse.
        // Dette skal løses i https://jira.adeo.no/browse/TFP-3795
        List<BeregningRefusjonOverstyringDto> orginaleOverstyringer = orginaltBGGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getRefusjonOverstyringer)
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList());
        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjonOgTidligereRefusjon =
                BeregningRefusjonTjeneste.finnAndelerMedØktRefusjonMedTidligereRefusjonIkkeTidligereVurdert(andelerMedØktRefusjonIUtbetaltPeriode, orginaltBG, orginaleOverstyringer);

        // Vi vet at det finnes andeler med økt refusjon, vi skal ha aksjonspunkt hvis ingen av disse tidligere har hatt refusjon
        return andelerMedØktRefusjonOgTidligereRefusjon.isEmpty();
    }
}
