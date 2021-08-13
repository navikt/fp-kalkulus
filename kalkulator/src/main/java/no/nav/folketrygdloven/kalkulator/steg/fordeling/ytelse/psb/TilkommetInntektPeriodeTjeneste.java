package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public class TilkommetInntektPeriodeTjeneste {

    private final FinnTilkommetInntektTjeneste finnTilkommetInntektTjeneste = new FinnTilkommetInntektTjeneste();
    private final OpprettPerioderOgAndelerForTilkommetInntekt opprettPerioderTjeneste = new OpprettPerioderOgAndelerForTilkommetInntekt();

    public BeregningsgrunnlagDto splittPerioderVedTilkommetInntekt(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        PleiepengerSyktBarnGrunnlag psbGrunnlag = input.getYtelsespesifiktGrunnlag();
        List<AktivitetDto> tilkomneAktiviteter = finnTilkommetInntektTjeneste.finnAktiviteterMedTilkommetInntekt(
                beregningsgrunnlag,
                input.getIayGrunnlag(),
                psbGrunnlag.getUtbetalingsgradPrAktivitet());
        return opprettPerioderTjeneste.opprettPerioderOgAndeler(beregningsgrunnlag, tilkomneAktiviteter);
    }

}
