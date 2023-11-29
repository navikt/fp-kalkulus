package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;


public class TilkommetInntektTjeneste {

    private static final boolean GRADERING_MOT_INNTEKT_ENABLED = KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false);

    private final TilkommetInntektPeriodeTjeneste periodeTjeneste = new TilkommetInntektPeriodeTjeneste();

    public BeregningsgrunnlagDto vurderTilkommetInntekt(BeregningsgrunnlagInput input) {
        if (!GRADERING_MOT_INNTEKT_ENABLED) {
            return input.getBeregningsgrunnlag();
        } else {
            return periodeTjeneste.splittPerioderVedTilkommetInntekt(input, input.getBeregningsgrunnlag());
        }
    }

}
