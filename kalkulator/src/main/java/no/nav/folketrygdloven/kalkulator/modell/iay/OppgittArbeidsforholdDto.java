package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Convert;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Entitetsklasse for oppgitte arbeidsforhold.
 * <p>
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 * <p>
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */
public class OppgittArbeidsforholdDto {

    private Intervall periode;

    @Convert(converter = ArbeidType.KodeverdiConverter.class)
    private ArbeidType arbeidType;

    public OppgittArbeidsforholdDto() {
        // hibernate
    }

    public String getIndexKey() {
        return IndexKey.createKey(periode, arbeidType);
    }

    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    public Intervall getPeriode() {
        return periode;
    }


    public ArbeidType getArbeidType() {
        return arbeidType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittArbeidsforholdDto)) return false;

        OppgittArbeidsforholdDto that = (OppgittArbeidsforholdDto) o;

        return
            Objects.equals(periode, that.periode) &&
            Objects.equals(arbeidType, that.arbeidType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidType);
    }

    @Override
    public String toString() {
        return "OppgittArbeidsforholdImpl{" +
            "periode=" + periode +
            ", arbeidType=" + arbeidType +
            '}';
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    void setArbeidType(ArbeidType arbeidType) {
        this.arbeidType = arbeidType;
    }


}
