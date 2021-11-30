package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutlederVurderRefusjon;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutledertjenesteVurderRefusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@ApplicationScoped
@FagsakYtelseTypeRef("PSB")
@FagsakYtelseTypeRef("PPN")
public class AvklaringsbehovutledertjenesteVurderRefusjonPleiepenger implements AvklaringsbehovutledertjenesteVurderRefusjon {

    @Inject
    public AvklaringsbehovutledertjenesteVurderRefusjonPleiepenger() {
    }

    @Override
    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {

        if (!KonfigurasjonVerdi.get("VURDER_REFUSJON_PSB",false)) {
            return Collections.emptyList();
        }

        List<BeregningAvklaringsbehovResultat> avklaringsbehov = new ArrayList<>();

        if (AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering)) {
            avklaringsbehov.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV));
        }
        return avklaringsbehov;
    }
}
