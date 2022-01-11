package no.nav.folketrygdloven.kalkulator.steg.fordeling;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;

@ApplicationScoped
@FagsakYtelseTypeRef()
public class FordelBeregningsgrunnlagTjenesteImpl implements FordelBeregningsgrunnlagTjeneste {

    private OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste;

    public FordelBeregningsgrunnlagTjenesteImpl() {
        // CDI
    }

    @Inject
    public FordelBeregningsgrunnlagTjenesteImpl(OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste) {
        this.omfordelTjeneste = omfordelTjeneste;
    }

    @Override
    public BeregningsgrunnlagRegelResultat omfordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var resultatFraOmfordeling = omfordelTjeneste.omfordel(input, input.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(resultatFraOmfordeling.getBeregningsgrunnlag(),
                resultatFraOmfordeling.getRegelsporinger().orElse(null));
    }

}
