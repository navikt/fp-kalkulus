package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.NaturalYtelseType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.tid.DatoIntervallEntitet;

public class NaturalYtelseDto {

    private DatoIntervallEntitet periode;
    private Beløp beloepPerMnd;

    private NaturalYtelseType type = NaturalYtelseType.UDEFINERT;

    NaturalYtelseDto() {
    }

    public NaturalYtelseDto(LocalDate fom, LocalDate tom, BigDecimal beloepPerMnd, NaturalYtelseType type) {
        this.beloepPerMnd = new Beløp(beloepPerMnd);
        this.type = type;
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    public NaturalYtelseDto(DatoIntervallEntitet datoIntervall, BigDecimal beloepPerMnd, NaturalYtelseType type) {
        this.beloepPerMnd = new Beløp(beloepPerMnd);
        this.type = type;
        this.periode = datoIntervall;
    }

    NaturalYtelseDto(NaturalYtelseDto naturalYtelse) {
        this.periode = naturalYtelse.getPeriode();
        this.beloepPerMnd = naturalYtelse.getBeloepPerMnd();
        this.type = naturalYtelse.getType();
    }

    public String getIndexKey() {
        return IndexKey.createKey(type, periode);
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public Beløp getBeloepPerMnd() {
        return beloepPerMnd;
    }

    public NaturalYtelseType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof NaturalYtelseDto))
            return false;
        NaturalYtelseDto that = (NaturalYtelseDto) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, type);
    }

    @Override
    public String toString() {
        return "NaturalYtelseEntitet{" +
            "periode=" + periode +
            ", beloepPerMnd=" + beloepPerMnd +
            ", type=" + type +
            '}';
    }
}
