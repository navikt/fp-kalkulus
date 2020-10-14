package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class AksjonspunktUtlederFaktaOmBeregning {

    protected FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    public AksjonspunktUtlederFaktaOmBeregning() {
        // for CDI proxy
    }

    @Inject
    public AksjonspunktUtlederFaktaOmBeregning(FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste) {
        this.faktaOmBeregningTilfelleTjeneste = faktaOmBeregningTilfelleTjeneste;
    }

    public FaktaOmBeregningAksjonspunktResultat utledAksjonspunkterFor(FaktaOmBeregningInput input,
                                                                       BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                       boolean erOverstyrt) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");

        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = faktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAksjonspunkt(input, beregningsgrunnlagGrunnlag);

        if (erOverstyrt) {
            return new FaktaOmBeregningAksjonspunktResultat(singletonList(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.OVERSTYRING_AV_BEREGNINGSGRUNNLAG)),
                faktaOmBeregningTilfeller);
        }
        if (faktaOmBeregningTilfeller.isEmpty()) {
            return FaktaOmBeregningAksjonspunktResultat.INGEN_AKSJONSPUNKTER;
        }
        return new FaktaOmBeregningAksjonspunktResultat(singletonList(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN)),
            faktaOmBeregningTilfeller);
    }
}
