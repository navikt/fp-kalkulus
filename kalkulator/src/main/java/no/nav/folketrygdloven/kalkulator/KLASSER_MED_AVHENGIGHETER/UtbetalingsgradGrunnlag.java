package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public abstract class UtbetalingsgradGrunnlag {

    private List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;

    public UtbetalingsgradGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
    }

    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        return utbetalingsgradPrAktivitet;
    }

}
