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
@FagsakYtelseTypeRef("OMP")
public class AksjonspunktutledertjenesteVurderRefusjonOMP implements AksjonspunkutledertjenesteVurderRefusjon {

    @Inject
    public AksjonspunktutledertjenesteVurderRefusjonOMP() {
    }

    @Override
    public List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        return Collections.emptyList();
    }
}
