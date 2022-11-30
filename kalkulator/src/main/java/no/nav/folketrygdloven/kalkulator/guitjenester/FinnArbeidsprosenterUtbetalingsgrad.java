package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class FinnArbeidsprosenterUtbetalingsgrad implements FinnArbeidsprosenter {

    @Override
    public List<BigDecimal> finnArbeidsprosenterIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                         YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                         Intervall periode) {
        var utbetalingsgrad = UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel(andel, periode, ytelsespesifiktGrunnlag, true);
        return List.of(BigDecimal.valueOf(100).subtract(utbetalingsgrad));
    }


}
