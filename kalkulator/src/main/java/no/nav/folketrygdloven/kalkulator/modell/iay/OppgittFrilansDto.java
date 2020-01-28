package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class OppgittFrilansDto {

    private OppgittOpptjeningDto oppgittOpptjening;
    private boolean harInntektFraFosterhjem;
    private boolean erNyoppstartet;
    private boolean harNærRelasjon;
    private List<OppgittFrilansoppdragDto> frilansoppdrag;


    public OppgittFrilansDto() {
    }

    public OppgittFrilansDto(boolean harInntektFraFosterhjem, boolean erNyoppstartet, boolean harNærRelasjon) {
        this.harInntektFraFosterhjem = harInntektFraFosterhjem;
        this.erNyoppstartet = erNyoppstartet;
        this.harNærRelasjon = harNærRelasjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittFrilansDto)) return false;
        OppgittFrilansDto that = (OppgittFrilansDto) o;
        return harInntektFraFosterhjem == that.harInntektFraFosterhjem &&
            erNyoppstartet == that.erNyoppstartet &&
            harNærRelasjon == that.harNærRelasjon &&
            Objects.equals(oppgittOpptjening, that.oppgittOpptjening) &&
            Objects.equals(frilansoppdrag, that.frilansoppdrag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgittOpptjening, harInntektFraFosterhjem, erNyoppstartet, harNærRelasjon, frilansoppdrag);
    }

    @Override
    public String toString() {
        return "FrilansEntitet{" +
            "oppgittOpptjening=" + oppgittOpptjening +
            ", harInntektFraFosterhjem=" + harInntektFraFosterhjem +
            ", erNyoppstartet=" + erNyoppstartet +
            ", harNærRelasjon=" + harNærRelasjon +
            ", frilansoppdrag=" + frilansoppdrag +
            '}';
    }

    void setOppgittOpptjening(OppgittOpptjeningDto oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    public boolean getHarInntektFraFosterhjem() {
        return harInntektFraFosterhjem;
    }

    public void setHarInntektFraFosterhjem(boolean harInntektFraFosterhjem) {
        this.harInntektFraFosterhjem = harInntektFraFosterhjem;
    }

    public void setErNyoppstartet(boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }

    public void setHarNærRelasjon(boolean harNærRelasjon) {
        this.harNærRelasjon = harNærRelasjon;
    }

    public boolean getErNyoppstartet() {
        return erNyoppstartet;
    }

    public boolean getHarNærRelasjon() {
        return harNærRelasjon;
    }

    public List<OppgittFrilansoppdragDto> getFrilansoppdrag() {
        if (frilansoppdrag != null) {
            return Collections.unmodifiableList(frilansoppdrag);
        }
        return Collections.emptyList();
    }

    public void setFrilansoppdrag(List<OppgittFrilansoppdragDto> frilansoppdrag) {
        this.frilansoppdrag = frilansoppdrag;
    }
}
