package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class RefusjonskravDatoDto {

    private Arbeidsgiver arbeidsgiver;

    private LocalDate førsteDagMedRefusjonskrav;

    private LocalDate førsteInnsendingAvRefusjonskrav;

    private boolean harRefusjonFraStart = false;

    RefusjonskravDatoDto() {
    }

    /**
     * @param arbeidsgiver arbeidsgiver med refusjon
     * @param førsteDagMedRefusjonskrav første dag med refusjon som oppgitt i inntektsmelding
     * @param førsteInnsendingAvRefusjonskrav første innsendelse av refusjonskrav
     * @param harRefusjonFraStart Angir om første dag med refusjon er lik første dag med ytelse
     */
    public RefusjonskravDatoDto(Arbeidsgiver arbeidsgiver, LocalDate førsteDagMedRefusjonskrav, LocalDate førsteInnsendingAvRefusjonskrav, boolean harRefusjonFraStart) {
        this.arbeidsgiver = arbeidsgiver;
        this.førsteDagMedRefusjonskrav = førsteDagMedRefusjonskrav;
        this.førsteInnsendingAvRefusjonskrav = førsteInnsendingAvRefusjonskrav;
        this.harRefusjonFraStart = harRefusjonFraStart;
    }

    public RefusjonskravDatoDto(RefusjonskravDatoDto refusjonskravDato) {
        this.arbeidsgiver = refusjonskravDato.getArbeidsgiver();
        this.førsteDagMedRefusjonskrav = refusjonskravDato.førsteDagMedRefusjonskrav;
        this.førsteInnsendingAvRefusjonskrav = refusjonskravDato.førsteInnsendingAvRefusjonskrav;
        this.harRefusjonFraStart = refusjonskravDato.harRefusjonFraStart;
    }

    /**
     * Virksomheten som har sendt inn inntektsmeldingen
     *
     * @return {@link Arbeidsgiver}
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

    public boolean harRefusjonFraStart() {
        return harRefusjonFraStart;
    }
}
