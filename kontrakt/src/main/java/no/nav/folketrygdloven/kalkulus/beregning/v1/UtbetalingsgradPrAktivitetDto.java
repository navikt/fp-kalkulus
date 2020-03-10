package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class UtbetalingsgradPrAktivitetDto {

    @JsonProperty(value = "utbetalingsgradArbeidsforholdDto", required = true)
    @Valid
    @NotNull
    private UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto;

    @JsonProperty(value = "periodeMedUtbetalingsgrad", required = true)
    @Valid
    @NotNull
    @NotEmpty
    private List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad;

    public UtbetalingsgradPrAktivitetDto() {
    }

    public UtbetalingsgradPrAktivitetDto(@Valid @NotNull UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto, @Valid @NotNull @NotEmpty List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad) {
        this.utbetalingsgradArbeidsforholdDto = utbetalingsgradArbeidsforholdDto;
        this.periodeMedUtbetalingsgrad = periodeMedUtbetalingsgrad;
    }

    public List<PeriodeMedUtbetalingsgradDto> getPeriodeMedUtbetalingsgrad() {
        return periodeMedUtbetalingsgrad;
    }

    public UtbetalingsgradArbeidsforholdDto getUtbetalingsgradArbeidsforholdDto() {
        return utbetalingsgradArbeidsforholdDto;
    }

}
