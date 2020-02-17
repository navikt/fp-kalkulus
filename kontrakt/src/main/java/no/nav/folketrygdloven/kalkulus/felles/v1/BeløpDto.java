package no.nav.folketrygdloven.kalkulus.felles.v1;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class BeløpDto {

    @JsonProperty(value = "verdi")
    @Valid
    @DecimalMin(value = "0.00", message = "verdi ${validatedValue} må være >= {value}")
    private BigDecimal verdi;

    protected BeløpDto() {
        // default ctor
    }

    public BeløpDto(@Valid @DecimalMin(value = "0.00", message = "verdi ${validatedValue} må være >= {value}") BigDecimal verdi) {
        this.verdi = verdi;
    }

    public BigDecimal getVerdi() {
        return verdi;
    }
}
