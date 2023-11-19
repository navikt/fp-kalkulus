package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektPeriodeTjeneste;


public class FordelBeregningsgrunnlagTjenesteUtbGrad implements FordelBeregningsgrunnlagTjeneste {

    private static final boolean GRADERING_MOT_INNTEKT_ENABLED = KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false);

    private final TilkommetInntektPeriodeTjeneste periodeTjeneste = new TilkommetInntektPeriodeTjeneste();
    private final OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste = new OmfordelBeregningsgrunnlagTjeneste();

    @Override
    public BeregningsgrunnlagRegelResultat omfordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var resultatFraOmfordeling = omfordelTjeneste.omfordel(input, input.getBeregningsgrunnlag());
        if (!GRADERING_MOT_INNTEKT_ENABLED) {
            return new BeregningsgrunnlagRegelResultat(resultatFraOmfordeling.getBeregningsgrunnlag(),
                    resultatFraOmfordeling.getRegelsporinger().orElse(null));
        } else {
            var splittetTilkommetInntektBg = periodeTjeneste.splittPerioderVedTilkommetInntekt(input, resultatFraOmfordeling.getBeregningsgrunnlag());
            return new BeregningsgrunnlagRegelResultat(splittetTilkommetInntektBg,
                    resultatFraOmfordeling.getRegelsporinger().orElse(null));
        }
    }

}
