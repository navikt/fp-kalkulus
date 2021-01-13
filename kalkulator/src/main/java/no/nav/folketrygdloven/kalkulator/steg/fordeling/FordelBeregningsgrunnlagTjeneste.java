package no.nav.folketrygdloven.kalkulator.steg.fordeling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;

@ApplicationScoped
public class FordelBeregningsgrunnlagTjeneste {

    private OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste;

    public FordelBeregningsgrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public FordelBeregningsgrunnlagTjeneste(OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste) {
        this.omfordelTjeneste = omfordelTjeneste;
    }

    public BeregningsgrunnlagRegelResultat omfordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var resultatFraOmfordeling = omfordelTjeneste.omfordel(input, input.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(resultatFraOmfordeling.getBeregningsgrunnlag(),
                resultatFraOmfordeling.getRegelsporinger().orElse(null));
    }

}
