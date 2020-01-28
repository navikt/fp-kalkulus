package no.nav.folketrygdloven.kalkulator.modell.opptjening;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class OpptjeningAktivitetDto {

    private DatoIntervallEntitet periode;
    private OpptjeningAktivitetType aktivitetType;
    private ReferanseType aktivitetReferanseType = ReferanseType.UDEFINERT;
    private String aktivitetReferanse;
    private OpptjeningAktivitetKlassifiseringDto klassifisering;

    OpptjeningAktivitetDto() {
        // fur hibernate
    }

    public OpptjeningAktivitetDto(LocalDate fom, LocalDate tom, OpptjeningAktivitetType aktivitetType,
                                  OpptjeningAktivitetKlassifiseringDto klassifisering) {
        this(fom, tom, aktivitetType, klassifisering, null, null);
    }

    public OpptjeningAktivitetDto(LocalDate fom, LocalDate tom, OpptjeningAktivitetType aktivitetType,
                                  OpptjeningAktivitetKlassifiseringDto klassifisering, String aktivitetReferanse, ReferanseType aktivitetReferanseType) {
        Objects.requireNonNull(fom, "fom"); //$NON-NLS-1$
        Objects.requireNonNull(tom, "tom"); //$NON-NLS-1$
        Objects.requireNonNull(aktivitetType, "aktivitetType"); //$NON-NLS-1$
        Objects.requireNonNull(klassifisering, "klassifisering"); //$NON-NLS-1$
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);

        this.aktivitetType = aktivitetType;
        this.klassifisering = klassifisering;

        if (aktivitetReferanse != null) {
            Objects.requireNonNull(aktivitetReferanseType, "aktivitetReferanseType");
            this.aktivitetReferanse = aktivitetReferanse;
            this.aktivitetReferanseType = aktivitetReferanseType;
        }
    }

    /** copy constructor - kun data uten metadata som aktiv/endretAv etc. */
    public OpptjeningAktivitetDto(OpptjeningAktivitetDto annen) {

        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(annen.getFom(), annen.getTom());
        this.aktivitetReferanse = annen.getAktivitetReferanse();
        this.aktivitetType = annen.getAktivitetType();
        this.klassifisering = annen.getKlassifisering();
        this.aktivitetReferanseType = annen.getAktivitetReferanseType() == null ? ReferanseType.UDEFINERT
            : annen.getAktivitetReferanseType();

    }

    public LocalDate getFom() {
        return periode.getFomDato();
    }

    public LocalDate getTom() {
        return periode.getTomDato();
    }

    public String getAktivitetReferanse() {
        return aktivitetReferanse;
    }

    public ReferanseType getAktivitetReferanseType() {
        return ReferanseType.UDEFINERT.equals(aktivitetReferanseType) ? null : aktivitetReferanseType;
    }

    public OpptjeningAktivitetType getAktivitetType() {
        return aktivitetType;
    }

    public OpptjeningAktivitetKlassifiseringDto getKlassifisering() {
        return klassifisering;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }

        OpptjeningAktivitetDto other = (OpptjeningAktivitetDto) obj;
        return Objects.equals(periode, other.periode)
            && Objects.equals(aktivitetType, other.aktivitetType)
            && Objects.equals(aktivitetReferanse, other.aktivitetReferanse)
            && Objects.equals(aktivitetReferanseType, other.aktivitetReferanseType)
        // tar ikke med klassifisering, da det ikke er del av dette objektets identitet
        ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, aktivitetType, aktivitetReferanse, aktivitetReferanseType);
    }

    public String getIndexKey() {
        return IndexKey.createKey(periode, aktivitetType, aktivitetReferanse, aktivitetReferanseType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "<aktivitetType=" + aktivitetType //$NON-NLS-1$
            + (aktivitetReferanse == null ? "" : ", aktivitetReferanse[" + aktivitetReferanseType + "]=" + aktivitetReferanse) //$NON-NLS-1$ //$NON-NLS-2$
            + ", klassifisering=" + klassifisering
            + " [" + periode.getFomDato() + ", " + periode.getTomDato() + "]" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + ">"; //$NON-NLS-1$
    }

}
