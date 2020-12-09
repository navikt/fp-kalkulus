package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.FaktaOmBeregningTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class AksjonspunktUtlederFaktaOmBeregningFRISINN extends AksjonspunktUtlederFaktaOmBeregning {

    public AksjonspunktUtlederFaktaOmBeregningFRISINN() {
        // CDI
    }

    @Inject
    public AksjonspunktUtlederFaktaOmBeregningFRISINN(FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste) {
        super(faktaOmBeregningTilfelleTjeneste);
    }

    @Override
    public FaktaOmBeregningAksjonspunktResultat utledAksjonspunkterFor(FaktaOmBeregningInput input,
                                                                       BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                       boolean erOverstyrt) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = faktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAksjonspunkt(input, beregningsgrunnlagGrunnlag);
        if (faktaOmBeregningTilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
           return new FaktaOmBeregningAksjonspunktResultat(singletonList(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN)),
                    List.of(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON));
        }
        return FaktaOmBeregningAksjonspunktResultat.INGEN_AKSJONSPUNKTER;
    }
}
