package no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class BeregningAktivitetAggregatMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private List<BeregningAktivitetMigreringDto> aktiviteter;
    @Valid
    @NotNull
    private LocalDate skjæringstidspunktOpptjening;

    public BeregningAktivitetAggregatMigreringDto(List<BeregningAktivitetMigreringDto> aktiviteter, LocalDate skjæringstidspunktOpptjening) {
        this.aktiviteter = aktiviteter;
        this.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
    }

    public List<BeregningAktivitetMigreringDto> getAktiviteter() {
        return aktiviteter;
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }
}
