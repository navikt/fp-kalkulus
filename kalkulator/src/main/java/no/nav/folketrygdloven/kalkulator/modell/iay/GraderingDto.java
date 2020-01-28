package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class GraderingDto implements Comparable<GraderingDto> {

    private DatoIntervallEntitet periode;
    private Stillingsprosent arbeidstidProsent;

    GraderingDto() {
    }

    public GraderingDto(DatoIntervallEntitet periode, BigDecimal arbeidstidProsent) {
        this.arbeidstidProsent = new Stillingsprosent(Objects.requireNonNull(arbeidstidProsent,  "arbeidstidProsent"));
        this.periode = periode;
    }

    public GraderingDto(DatoIntervallEntitet periode, Stillingsprosent arbeidstidProsent) {
        this.arbeidstidProsent = arbeidstidProsent;
        this.periode = periode;
    }

    public GraderingDto(LocalDate fom, LocalDate tom, BigDecimal arbeidstidProsent) {
        this(tom == null ? DatoIntervallEntitet.fraOgMed(fom) : DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom), new Stillingsprosent(Objects.requireNonNull(arbeidstidProsent,  "arbeidstidProsent")));
    }

    GraderingDto(GraderingDto gradering) {
        this(gradering.getPeriode(), gradering.getArbeidstidProsent());
    }

    public String getIndexKey() {
        return IndexKey.createKey(periode);
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    /**
     * En arbeidstaker kan kombinere foreldrepenger med deltidsarbeid.
     *
     * Når arbeidstakeren jobber deltid, utgjør foreldrepengene differansen mellom deltidsarbeidet og en 100 prosent stilling.
     * Det er ingen nedre eller øvre grense for hvor mye eller lite arbeidstakeren kan arbeide.
     *
     * Eksempel
     * Arbeidstaker A har en 100 % stilling og arbeider fem dager i uken. Arbeidstakeren ønsker å arbeide to dager i uken i
     * foreldrepengeperioden.
     * Arbeidstids- prosenten blir da 40 %.
     *
     * Arbeidstaker B har en 80 % stilling og arbeider fire dager i uken. Arbeidstakeren ønsker å arbeide to dager i uken i
     * foreldrepengeperioden.
     * Arbeidstidprosenten blir også her 40 %.
     *
     * @return prosentsats
     */
    public Stillingsprosent getArbeidstidProsent() {
        return arbeidstidProsent;
    }

    @Override
    public String toString() {
        return "GraderingEntitet{" +
            "periode=" + periode +
            ", arbeidstidProsent=" + arbeidstidProsent +
            '}';
    }

    @Override
    public int compareTo(GraderingDto o) {
        return o == null ? 1 : this.getPeriode().compareTo(o.getPeriode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof GraderingDto)) return false;
        GraderingDto that = (GraderingDto) o;
        return Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }
}
