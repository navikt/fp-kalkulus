package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektEndring {

    @JsonProperty(value = "fraInntekt")
    @Valid
    private BigDecimal fraInntekt;

    @JsonProperty(value = "tilInntekt")
    @NotNull
    @Valid
    private BigDecimal tilInntekt;

    public InntektEndring() {
        // For Json deserialisering
    }

    public InntektEndring(@Valid BigDecimal fraInntekt, @Valid @NotNull BigDecimal tilInntekt) {
        this.fraInntekt = fraInntekt;
        this.tilInntekt = tilInntekt;
    }

    public BigDecimal getFraInntekt() {
        return fraInntekt;
    }

    public BigDecimal getTilInntekt() {
        return tilInntekt;
    }
}
