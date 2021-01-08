package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;


import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AksjonspunktutlederVurderRefusjon;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AksjonspunkutledertjenesteVurderRefusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;

@ApplicationScoped
@FagsakYtelseTypeRef("SVP")
public class AksjonspunktutledertjenesteVurderRefusjonSVP implements AksjonspunkutledertjenesteVurderRefusjon {

    @Inject
    public AksjonspunktutledertjenesteVurderRefusjonSVP() {
    }

    @Override
    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        List<BeregningAksjonspunktResultat> aksjonspunkter = new ArrayList<>();

        if (AksjonspunktutlederVurderRefusjon.skalHaAksjonspunktVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering)) {
            aksjonspunkter.add(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunkt.VURDER_REFUSJONSKRAV));
        }

        return aksjonspunkter;
    }
}
