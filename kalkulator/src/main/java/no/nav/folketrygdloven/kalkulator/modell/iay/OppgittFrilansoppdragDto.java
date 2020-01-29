package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;


public class OppgittFrilansoppdragDto {

    private OppgittFrilansDto frilans;
    private String oppdragsgiver;
    private Intervall periode;


    OppgittFrilansoppdragDto() {
    }

    public OppgittFrilansoppdragDto(String oppdragsgiver, Intervall periode) {
        this.oppdragsgiver = oppdragsgiver;
        this.periode = periode;
    }

    public String getIndexKey() {
        return IndexKey.createKey(periode, oppdragsgiver);
    }

    void setOppgittOpptjening(OppgittFrilansDto frilans) {
        this.frilans = frilans;
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittFrilansoppdragDto)) return false;
        OppgittFrilansoppdragDto that = (OppgittFrilansoppdragDto) o;
        return Objects.equals(frilans, that.frilans) &&
            Objects.equals(oppdragsgiver, that.oppdragsgiver) &&
            Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frilans, oppdragsgiver, periode);
    }

    @Override
    public String toString() {
        return "FrilansoppdragEntitet{" +
            "frilans=" + frilans +
            ", oppdragsgiver='" + oppdragsgiver + '\'' +
            ", periode=" + periode +
            '}';
    }

    public Intervall getPeriode() {
        return periode;
    }

    public String getOppdragsgiver() {
        return oppdragsgiver;
    }

    // FIXME (OJR) kan ikke ha mutators
    public void setFrilans(OppgittFrilansDto frilans) {
        this.frilans = frilans;
    }
}
