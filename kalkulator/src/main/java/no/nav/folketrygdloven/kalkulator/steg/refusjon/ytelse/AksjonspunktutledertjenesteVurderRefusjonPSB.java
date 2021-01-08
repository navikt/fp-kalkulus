package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;


import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AksjonspunkutledertjenesteVurderRefusjon;

@ApplicationScoped
@FagsakYtelseTypeRef("PSB")
public class AksjonspunktutledertjenesteVurderRefusjonPSB implements AksjonspunkutledertjenesteVurderRefusjon {

    @Inject
    public AksjonspunktutledertjenesteVurderRefusjonPSB() {
    }

    @Override
    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        return Collections.emptyList();
    }
}
