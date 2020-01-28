package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class FastsettBGTidsbegrensetArbeidsforholdDto {


    @Valid
    @Size(max = 100)
    private List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder;

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Integer frilansInntekt;


    FastsettBGTidsbegrensetArbeidsforholdDto() {
        // For Jackson
    }

    public FastsettBGTidsbegrensetArbeidsforholdDto(List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder, Integer frilansInntekt) { // NOSONAR
        this.fastsatteTidsbegrensedePerioder = new ArrayList<>(fastsatteTidsbegrensedePerioder);
        this.frilansInntekt = frilansInntekt;
    }

    public List<FastsattePerioderTidsbegrensetDto> getFastsatteTidsbegrensedePerioder() {
        return fastsatteTidsbegrensedePerioder;
    }

    public Integer getFrilansInntekt() {
        return frilansInntekt;
    }
}
