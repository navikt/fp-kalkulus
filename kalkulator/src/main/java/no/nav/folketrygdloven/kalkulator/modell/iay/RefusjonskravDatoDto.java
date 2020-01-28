package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class RefusjonskravDatoDto {

    private Arbeidsgiver arbeidsgiver;

    private LocalDate førsteDagMedRefusjonskrav;

    private LocalDate førsteInnsendingAvRefusjonskrav;

    RefusjonskravDatoDto() {
    }

    public RefusjonskravDatoDto(Arbeidsgiver arbeidsgiver, LocalDate førsteDagMedRefusjonskrav, LocalDate førsteInnsendingAvRefusjonskrav) {
        this.arbeidsgiver = arbeidsgiver;
        this.førsteDagMedRefusjonskrav = førsteDagMedRefusjonskrav;
        this.førsteInnsendingAvRefusjonskrav = førsteInnsendingAvRefusjonskrav;
    }

    public RefusjonskravDatoDto(RefusjonskravDatoDto refusjonskravDato) {
        this.arbeidsgiver = refusjonskravDato.getArbeidsgiver();
        this.førsteDagMedRefusjonskrav = refusjonskravDato.førsteDagMedRefusjonskrav;
        this.førsteInnsendingAvRefusjonskrav = refusjonskravDato.førsteInnsendingAvRefusjonskrav;
    }

    /**
     * Virksomheten som har sendt inn inntektsmeldingen
     *
     * @return {@link Virksomhet}
     */
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }


    public Optional<LocalDate> getFørsteDagMedRefusjonskrav() {
        return Optional.ofNullable(førsteDagMedRefusjonskrav);
    }

    public LocalDate getFørsteInnsendingAvRefusjonskrav() {
        return førsteInnsendingAvRefusjonskrav;
    }
}
