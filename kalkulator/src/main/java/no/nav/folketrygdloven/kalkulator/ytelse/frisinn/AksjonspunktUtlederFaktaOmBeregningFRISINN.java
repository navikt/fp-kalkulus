package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FaktaOmBeregningTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAksjonspunktResultat;
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
    public FaktaOmBeregningAksjonspunktResultat utledAksjonspunkterFor(BeregningsgrunnlagInput input,
                                                                       BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                       boolean erOverstyrt) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = faktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAksjonspunkt(input, beregningsgrunnlagGrunnlag);
        if (faktaOmBeregningTilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            throw new IllegalStateException("Kan ikke behandle FRISINN-ytelse for AT og FL i samme organisajon.");
        }
        return FaktaOmBeregningAksjonspunktResultat.INGEN_AKSJONSPUNKTER;
    }
}
