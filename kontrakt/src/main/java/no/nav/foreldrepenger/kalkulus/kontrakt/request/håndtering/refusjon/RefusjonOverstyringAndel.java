package no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.refusjon;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;

public record RefusjonOverstyringAndel(@Valid @NotNull
                                       @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
                                       String arbeidsgiverIdent,

                                       @Valid
                                       @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
                                       String interntArbeidsforholdId,

                                       @Valid @NotNull @Size(min = 1, max = 200)
                                       List<@Valid RefusjonOverstyringPeriode> perioder,

                                       @Valid @NotNull
                                       RefusjonKilde kilde) {


    public record RefusjonOverstyringPeriode(@NotNull
                                             @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
                                             String fom,

                                             @NotNull
                                             @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
                                             String tom,

                                             @Valid @NotNull
                                             @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
                                             @DecimalMax(value = "178956970.00", message = "verdien ${validatedValue} må være <= {value}")
                                             @Digits(integer = 9, fraction = 2)
                                             BigDecimal refusjonsbeløpPrMnd,

                                             @Valid
                                             RefusjonUtfallÅrsak refusjonsUtfallÅrsak) {
    }
}
