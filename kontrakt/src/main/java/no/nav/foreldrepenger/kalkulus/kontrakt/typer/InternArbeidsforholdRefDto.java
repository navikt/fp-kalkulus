package no.nav.foreldrepenger.kalkulus.kontrakt.typer;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Denne referansen blir generet av Abakus og er en intern referanse som blir brukt internt av alle kombonenter i
 */
public record InternArbeidsforholdRefDto(@Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-]+$", message = "Abakusreferanse ${validatedValue} matcher ikke tillatt pattern '{regexp}'")
                                         @NotNull String abakusReferanse) {

    public String getAbakusReferanse() {
        return abakusReferanse;
    }

}
