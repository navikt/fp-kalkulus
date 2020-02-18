package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastsattePerioderTidsbegrensetDto {

    @JsonProperty("periodeFom")
    @Valid
    @NotNull
    private LocalDate periodeFom;

    @JsonProperty("periodeTom")
    @Valid
    @NotNull
    private LocalDate periodeTom;

    @JsonProperty("fastsatteTidsbegrensedeAndeler")
    @Valid
    @NotNull
    private List<FastsatteAndelerTidsbegrensetDto> fastsatteTidsbegrensedeAndeler;

    public FastsattePerioderTidsbegrensetDto(@Valid @NotNull LocalDate periodeFom, @Valid @NotNull LocalDate periodeTom, @Valid @NotNull List<FastsatteAndelerTidsbegrensetDto> fastsatteTidsbegrensedeAndeler) {
        this.periodeFom = periodeFom;
        this.periodeTom = periodeTom;
        this.fastsatteTidsbegrensedeAndeler = fastsatteTidsbegrensedeAndeler;
    }

    public LocalDate getPeriodeFom() {
        return periodeFom;
    }

    public LocalDate getPeriodeTom() {
        return periodeTom;
    }

    public List<FastsatteAndelerTidsbegrensetDto> getFastsatteTidsbegrensedeAndeler() {
        return fastsatteTidsbegrensedeAndeler;
    }
}
