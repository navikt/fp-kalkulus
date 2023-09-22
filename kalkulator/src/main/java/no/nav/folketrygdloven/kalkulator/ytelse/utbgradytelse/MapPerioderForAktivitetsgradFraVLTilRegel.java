package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;


public class MapPerioderForAktivitetsgradFraVLTilRegel extends MapPerioderForUtbetalingsgradFraVLTilRegel {

    @Override
    protected Utbetalingsgrad mapUttakPeriode(PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgrad, LocalDate skjæringstidspunkt) {
        Periode periode;
        Intervall utbetalingsgradPeriode = periodeMedUtbetalingsgrad.getPeriode();
        if (utbetalingsgradPeriode.getFomDato().isBefore(skjæringstidspunkt)) {
            periode = Periode.of(skjæringstidspunkt, utbetalingsgradPeriode.getTomDato());
        } else {
            periode = Periode.of(utbetalingsgradPeriode.getFomDato(), utbetalingsgradPeriode.getTomDato());
        }
        var aktivitetsgrad = periodeMedUtbetalingsgrad.getAktivitetsgrad();
        return new Utbetalingsgrad(periode, BigDecimal.valueOf(100).subtract(aktivitetsgrad.get()));
    }

}
