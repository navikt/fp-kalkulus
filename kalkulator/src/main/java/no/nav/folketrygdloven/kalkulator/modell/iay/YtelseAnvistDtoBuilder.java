package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

public class YtelseAnvistDtoBuilder {
    private final YtelseAnvistDto ytelseAnvist;

    YtelseAnvistDtoBuilder(YtelseAnvistDto ytelseAnvist) {
        this.ytelseAnvist = ytelseAnvist;
    }

    public static YtelseAnvistDtoBuilder ny() {
        return new YtelseAnvistDtoBuilder(new YtelseAnvistDto());
    }

    public YtelseAnvistDtoBuilder medBeløp(BigDecimal beløp) {
        if (beløp != null) {
            this.ytelseAnvist.setBeløp(Beløp.fra(beløp));
        }
        return this;
    }

    public YtelseAnvistDtoBuilder medDagsats(BigDecimal dagsats) {
        if (dagsats != null) {
            this.ytelseAnvist.setDagsats(Beløp.fra(dagsats));
        }
        return this;
    }

    public YtelseAnvistDtoBuilder medAnvistPeriode(Intervall intervallEntitet){
        this.ytelseAnvist.setAnvistPeriode(intervallEntitet);
        return this;
    }

    public YtelseAnvistDtoBuilder medAnvisteAndeler(List<AnvistAndel> anvisteAndeler){
        this.ytelseAnvist.setAnvisteAndeler(anvisteAndeler);
        return this;
    }


    public YtelseAnvistDtoBuilder medUtbetalingsgradProsent(BigDecimal utbetalingsgradProsent) {
        if (utbetalingsgradProsent != null) {
            this.ytelseAnvist.setUtbetalingsgradProsent(new Stillingsprosent(utbetalingsgradProsent));
        }
        return this;
    }

    public YtelseAnvistDto build() {
        return ytelseAnvist;
    }

}
