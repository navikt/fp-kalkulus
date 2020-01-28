package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class RefusjonDto {

    private Beløp refusjonsbeløpMnd;
    private LocalDate fom;

    public RefusjonDto() {
    }

    public RefusjonDto(BigDecimal refusjonsbeløpMnd, LocalDate fom) {
        this.refusjonsbeløpMnd = refusjonsbeløpMnd == null ? null : new Beløp(refusjonsbeløpMnd);
        this.fom = fom;
    }

    RefusjonDto(RefusjonDto refusjon) {
        this.refusjonsbeløpMnd = refusjon.getRefusjonsbeløp();
        this.fom = refusjon.getFom();
    }

    public String getIndexKey() {
        return IndexKey.createKey(fom, refusjonsbeløpMnd);
    }

    public Beløp getRefusjonsbeløp() {
        return refusjonsbeløpMnd;
    }

    public LocalDate getFom() {
        return fom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof RefusjonDto)) return false;
        RefusjonDto that = (RefusjonDto) o;
        return Objects.equals(refusjonsbeløpMnd, that.refusjonsbeløpMnd) &&
            Objects.equals(fom, that.fom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refusjonsbeløpMnd, fom);
    }
}
