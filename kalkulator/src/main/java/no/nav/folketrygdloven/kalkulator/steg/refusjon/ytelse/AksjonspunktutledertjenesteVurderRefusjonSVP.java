package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AksjonspunktutlederRefusjonEtterSluttdato;
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

        Collection<YrkesaktivitetDto> yrkesaktiviteter = input.getIayGrunnlag().getAktørArbeidFraRegister()
                .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                .orElse(Collections.emptyList());
        // Skal ikke gi aksjonspunkt enda, ønsker analyse på hvor mange slike saker vi har
        AksjonspunktutlederRefusjonEtterSluttdato.harRefusjonEtterSisteDatoIArbeidsforhold(yrkesaktiviteter, input.getKoblingReferanse().getKoblingUuid(), periodisertMedRefusjonOgGradering);

        return aksjonspunkter;
    }
}
