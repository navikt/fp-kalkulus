package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Objects;


public class OppgittFrilansDto {

    private OppgittOpptjeningDto oppgittOpptjening;
    private boolean erNyoppstartet;


    public OppgittFrilansDto() {
    }

    public OppgittFrilansDto(boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittFrilansDto)) return false;
        OppgittFrilansDto that = (OppgittFrilansDto) o;
        return erNyoppstartet == that.erNyoppstartet && Objects.equals(oppgittOpptjening, that.oppgittOpptjening);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgittOpptjening, erNyoppstartet);
    }

    @Override
    public String toString() {
        return "FrilansEntitet{" +
            "oppgittOpptjening=" + oppgittOpptjening +
            ", erNyoppstartet=" + erNyoppstartet +
            '}';
    }

    void setOppgittOpptjening(OppgittOpptjeningDto oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    public void setErNyoppstartet(boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }


    public boolean getErNyoppstartet() {
        return erNyoppstartet;
    }

}
