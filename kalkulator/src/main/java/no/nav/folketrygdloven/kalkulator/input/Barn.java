package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/** Barn som inngår i foreldrepengergrunnlaget for ytelsen. */
public class Barn {
    private LocalDate fødselsdato;
    private Optional<LocalDate> dødsdato;

    public Barn(LocalDate fødselsDato) {
        this(fødselsDato, Optional.empty());
    }

    public Barn(LocalDate fødselsDato, LocalDate dødsdato) {
        this.fødselsdato = Objects.requireNonNull(fødselsDato, "fødselsDato");
        this.dødsdato = Optional.of(Objects.requireNonNull(dødsdato, "dødsdato"));
    }

    public Barn(LocalDate fødselsDato, Optional<LocalDate> dødsdato) {
        this.fødselsdato = Objects.requireNonNull(fødselsDato, "fødselsDato");
        this.dødsdato =  Objects.requireNonNull(dødsdato, "dødsdato");
    }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }

    public Optional<LocalDate> getDødsdato() {
        return dødsdato;
    }

}
