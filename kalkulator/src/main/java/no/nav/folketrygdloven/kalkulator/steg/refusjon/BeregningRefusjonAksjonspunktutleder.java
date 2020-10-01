package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
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

        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjon = andelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(input);

        boolean finnesAndelerMedØktRefusjon = !andelerMedØktRefusjon.isEmpty();

        if (finnesAndelerMedØktRefusjon) {
            aksjonspunkter.add(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.VURDER_REFUSJONSKRAV));
        }

        return aksjonspunkter;
    }

}
