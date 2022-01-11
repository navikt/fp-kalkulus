package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;


import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutledertjenesteVurderRefusjon;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class AvklaringsbehovutledertjenesteVurderRefusjonFRISINN implements AvklaringsbehovutledertjenesteVurderRefusjon {

    @Inject
    public AvklaringsbehovutledertjenesteVurderRefusjonFRISINN() {
    }

    @Override
    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        return Collections.emptyList();
    }
}
