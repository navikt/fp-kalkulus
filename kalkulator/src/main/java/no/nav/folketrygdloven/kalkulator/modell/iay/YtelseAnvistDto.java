package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.diff.IndexKey;

public class YtelseAnvistDto {

    private Intervall anvistPeriode;
    private Beløp beløp;
    private Beløp dagsats;
    private Stillingsprosent utbetalingsgradProsent;

    public YtelseAnvistDto() {
        // hibernate
    }

    public YtelseAnvistDto(YtelseAnvistDto ytelseAnvist) {
        this.anvistPeriode = Intervall.fraOgMedTilOgMed(ytelseAnvist.getAnvistFOM(), ytelseAnvist.getAnvistTOM());
        this.beløp = ytelseAnvist.getBeløp().orElse(null);
        this.dagsats = ytelseAnvist.getDagsats().orElse(null);
        this.utbetalingsgradProsent = ytelseAnvist.getUtbetalingsgradProsent().orElse(null);
    }

    public String getIndexKey() {
        return IndexKey.createKey(this.anvistPeriode);
    }

    public LocalDate getAnvistFOM() {
        return anvistPeriode.getFomDato();
    }

    public LocalDate getAnvistTOM() {
        return anvistPeriode.getTomDato();
    }

    public Optional<Stillingsprosent> getUtbetalingsgradProsent() {
        return Optional.ofNullable(utbetalingsgradProsent);
    }

    public Optional<Beløp> getBeløp() {
        return Optional.ofNullable(beløp);
    }

    public Optional<Beløp> getDagsats() {
        return Optional.ofNullable(dagsats);
    }

    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    void setDagsats(Beløp dagsats) {
        this.dagsats = dagsats;
    }

    void setAnvistPeriode(Intervall periode) {
        this.anvistPeriode = periode;
    }

    void setUtbetalingsgradProsent(Stillingsprosent utbetalingsgradProsent) {
        this.utbetalingsgradProsent = utbetalingsgradProsent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof YtelseAnvistDto)) return false;
        YtelseAnvistDto that = (YtelseAnvistDto) o;
        return Objects.equals(anvistPeriode, that.anvistPeriode) &&
            Objects.equals(beløp, that.beløp) &&
            Objects.equals(dagsats, that.dagsats) &&
            Objects.equals(utbetalingsgradProsent, that.utbetalingsgradProsent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anvistPeriode, beløp, dagsats, utbetalingsgradProsent);
    }

    @Override
    public String toString() {
        return "YtelseAnvistEntitet{" +
            "periode=" + anvistPeriode +
            ", beløp=" + beløp +
            ", dagsats=" + dagsats +
            ", utbetalingsgradProsent=" + utbetalingsgradProsent +
            '}';
    }
}
