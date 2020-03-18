package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonEndring {

    @JsonProperty(value = "fraRefusjon")
    @Valid
    private BigDecimal fraRefusjon;

    @JsonProperty(value = "tilRefusjon")
    @NotNull
    @Valid
    private BigDecimal tilRefusjon;

    public RefusjonEndring() {
        // For Json deserialisering
    }

    public RefusjonEndring(@Valid BigDecimal fraRefusjon, @Valid @NotNull BigDecimal tilRefusjon) {
        this.fraRefusjon = fraRefusjon;
        this.tilRefusjon = tilRefusjon;
    }

    public BigDecimal getFraRefusjon() {
        return fraRefusjon;
    }

    public BigDecimal getTilRefusjon() {
        return tilRefusjon;
    }
}
