package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Convert;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;

/**
 * Entitetsklasse for oppgitte arbeidsforhold.
 * <p>
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 * <p>
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */
public class OppgittArbeidsforholdDto implements OppgittPeriodeInntekt {

    private Intervall periode;

    private BigDecimal inntekt;

    public OppgittArbeidsforholdDto() {
        // hibernate
    }

    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    @Override
    public Intervall getPeriode() {
        return periode;
    }

    @Override
    public BigDecimal getInntekt() {
        return inntekt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittArbeidsforholdDto)) return false;

        OppgittArbeidsforholdDto that = (OppgittArbeidsforholdDto) o;

        return
            Objects.equals(periode, that.periode) &&
            Objects.equals(inntekt, that.inntekt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, inntekt);
    }

    @Override
    public String toString() {
        return "OppgittArbeidsforholdImpl{" +
            "periode=" + periode +
            "inntekt=" + inntekt +
                '}';
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    void setInntekt(BigDecimal inntekt) {
        this.inntekt = inntekt;
    }
}
