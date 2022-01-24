package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.svp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelingUtenRefusjonskravTjeneste;

@ApplicationScoped
@FagsakYtelseTypeRef("SVP")
public class SVPFordelBeregningsgrunnlagTjeneste implements FordelBeregningsgrunnlagTjeneste {

    private OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste;
    private boolean fordelingUtenKravEnabled;

    public SVPFordelBeregningsgrunnlagTjeneste() {
        // CDI
    }

    @Inject
    public SVPFordelBeregningsgrunnlagTjeneste(OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste) {
        this.omfordelTjeneste = omfordelTjeneste;
        this.fordelingUtenKravEnabled = KonfigurasjonVerdi.get("FORDELING_UTEN_KRAV", false);
    }

    @Override
    public BeregningsgrunnlagRegelResultat omfordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var resultatFraOmfordeling = omfordelTjeneste.omfordel(input, input.getBeregningsgrunnlag());
        if (fordelingUtenKravEnabled) {
            resultatFraOmfordeling = new BeregningsgrunnlagRegelResultat(OmfordelingUtenRefusjonskravTjeneste.omfordel(resultatFraOmfordeling.getBeregningsgrunnlag(), input.getYtelsespesifiktGrunnlag()),
                    resultatFraOmfordeling.getRegelsporinger().orElse(null));
        }
        return new BeregningsgrunnlagRegelResultat(resultatFraOmfordeling.getBeregningsgrunnlag(),
                resultatFraOmfordeling.getRegelsporinger().orElse(null));
    }

}
