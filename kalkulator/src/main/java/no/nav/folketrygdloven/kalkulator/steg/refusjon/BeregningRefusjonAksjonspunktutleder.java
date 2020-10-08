package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
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
@ApplicationScoped
public class BeregningRefusjonAksjonspunktutleder {
    private AndelerMedØktRefusjonTjeneste andelerMedØktRefusjonTjeneste;

    @Inject
    public BeregningRefusjonAksjonspunktutleder(AndelerMedØktRefusjonTjeneste andelerMedØktRefusjonTjeneste) {
        this.andelerMedØktRefusjonTjeneste = andelerMedØktRefusjonTjeneste;
    }

    public BeregningRefusjonAksjonspunktutleder() {
        // CDI
    }

    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input) {
        List<BeregningAksjonspunktResultat> aksjonspunkter = new ArrayList<>();

        if (skalHaAksjonspunktVurderRefusjonskrav(input)) {
            aksjonspunkter.add(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.VURDER_REFUSJONSKRAV));
        }

        return aksjonspunkter;
    }

    private boolean skalHaAksjonspunktVurderRefusjonskrav(BeregningsgrunnlagInput input) {
        Optional<BeregningsgrunnlagDto> orginaltBGOpt = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjonIUtbetaltPeriode = andelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(input);
        if (orginaltBGOpt.isEmpty() || andelerMedØktRefusjonIUtbetaltPeriode.isEmpty()) {
            return false;
        }

        // Vi skal ikke opprette aksjonspunkt hvis en andel alltid har hatt refusjon, men refusjonskravet har økt, da vi ikke har løsning for disse.
        // Dette skal løses i https://jira.adeo.no/browse/TFP-3795
        BeregningsgrunnlagDto orginaltBG = orginaltBGOpt.get();
        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjonOgTidligereRefusjon =
                BeregningRefusjonTjeneste.finnAndelerMedØktRefusjonMedTidligereRefusjon(andelerMedØktRefusjonIUtbetaltPeriode, orginaltBG);

        // Vi vet at det finnes andeler med økt refusjon, vi skal ha aksjonspunkt hvis ingen av disse tidligere har hatt refusjon
        return andelerMedØktRefusjonOgTidligereRefusjon.isEmpty();
    }

}
