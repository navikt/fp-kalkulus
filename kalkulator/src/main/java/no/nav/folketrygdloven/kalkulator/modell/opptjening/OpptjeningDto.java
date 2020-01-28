package no.nav.folketrygdloven.kalkulator.modell.opptjening;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Entitet som representerer Opptjening. Denne har også et sett med {@link OpptjeningAktivitetDto}.
 * Grafen her er immutable og tillater ikke endring av data elementer annet enn metadata (aktiv flagg osv.)
 *
 */
public class OpptjeningDto {

    private boolean aktiv = true;
    private DatoIntervallEntitet opptjeningPeriode;
    private Long id;
    private List<OpptjeningAktivitetDto> opptjeningAktivitet = new ArrayList<>();
    private String opptjentPeriode;
    private long versjon;

    public OpptjeningDto(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom, "opptjeningsperiodeFom");
        Objects.requireNonNull(tom, "opptjeningsperiodeTom");
        this.opptjeningPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    /** copy-constructor. */
    public OpptjeningDto(OpptjeningDto annen) {
        this.opptjeningPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(annen.getFom(), annen.getTom());
        this.opptjentPeriode = annen.getOpptjentPeriode() == null ? null : annen.getOpptjentPeriode().toString();
        this.opptjeningAktivitet
                .addAll(annen.getOpptjeningAktivitet().stream().map(oa -> new OpptjeningAktivitetDto(oa)).collect(Collectors.toList()));
        // kopierer ikke data som ikke er relevante (aktiv, versjon, id, etc)

    }

    OpptjeningDto() {
        // for hibernate
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OpptjeningDto)) {
            return false;
        }
        OpptjeningDto other = (OpptjeningDto) obj;
        return Objects.equals(this.getFom(), other.getFom())
                && Objects.equals(this.getTom(), other.getTom());
    }

    public Boolean getAktiv() {
        return aktiv;
    }

    public LocalDate getFom() {
        return opptjeningPeriode.getFomDato();
    }

    public Long getId() {
        return id;
    }

    public List<OpptjeningAktivitetDto> getOpptjeningAktivitet() {
        // alle returnerte data fra denne klassen skal være immutable
        return Collections.unmodifiableList(opptjeningAktivitet);
    }

    public Period getOpptjentPeriode() {
        return opptjentPeriode == null ? null : Period.parse(opptjentPeriode);
    }

    public LocalDate getTom() {
        return opptjeningPeriode.getTomDato();
    }

    /** fom/tom opptjening er gjort. */
    public DatoIntervallEntitet getOpptjeningPeriode() {
        return opptjeningPeriode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(opptjeningPeriode);
    }

    public void setInaktiv() {
        if (aktiv) {
            this.aktiv = false;
        }
        // else - can never go back
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "id=" + id + ", " //$NON-NLS-2$ //$NON-NLS-3$
            + "opptjeningsperiodeFom=" + opptjeningPeriode.getFomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "opptjeningsperiodeTom=" + opptjeningPeriode.getTomDato() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + (opptjentPeriode == null ? "" : ", opptjentPeriode=" + opptjentPeriode) //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    void setOpptjeningAktivitet(Collection<OpptjeningAktivitetDto> opptjeningAktivitet) {
        this.opptjeningAktivitet.clear();
        this.opptjeningAktivitet.addAll(opptjeningAktivitet);
    }

    void setOpptjentPeriode(Period opptjentPeriode) {
        this.opptjentPeriode = opptjentPeriode == null ? null : opptjentPeriode.toString();
    }

}
