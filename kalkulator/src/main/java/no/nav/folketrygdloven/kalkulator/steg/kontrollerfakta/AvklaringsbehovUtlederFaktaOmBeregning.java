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
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class AvklaringsbehovUtlederFaktaOmBeregning {

    protected FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    public AvklaringsbehovUtlederFaktaOmBeregning() {
        // for CDI proxy
    }

    @Inject
    public AvklaringsbehovUtlederFaktaOmBeregning(FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste) {
        this.faktaOmBeregningTilfelleTjeneste = faktaOmBeregningTilfelleTjeneste;
    }

    public FaktaOmBeregningAvklaringsbehovResultat utledAvklaringsbehovFor(FaktaOmBeregningInput input,
                                                                       BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                       boolean erOverstyrt) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");

        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = faktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAvklaringsbehov(input, beregningsgrunnlagGrunnlag);

        if (erOverstyrt) {
            return new FaktaOmBeregningAvklaringsbehovResultat(singletonList(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.OVERSTYRING_AV_BEREGNINGSGRUNNLAG)),
                faktaOmBeregningTilfeller);
        }
        if (faktaOmBeregningTilfeller.isEmpty()) {
            return FaktaOmBeregningAvklaringsbehovResultat.INGEN_AKSJONSPUNKTER;
        }
        return new FaktaOmBeregningAvklaringsbehovResultat(singletonList(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_FAKTA_FOR_ATFL_SN)),
            faktaOmBeregningTilfeller);
    }
}
