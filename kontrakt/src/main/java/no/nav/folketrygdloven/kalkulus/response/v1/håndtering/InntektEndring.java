package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.typer.DiffBeløp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektEndring {

    @JsonProperty(value = "fraInntekt")
    @Valid
    private DiffBeløp fraInntekt;

    @JsonProperty(value = "tilInntekt")
    @NotNull
    @Valid
    private DiffBeløp tilInntekt;

    public InntektEndring() {
        // For Json deserialisering
    }

    public InntektEndring(@Valid DiffBeløp fraInntekt, @Valid @NotNull DiffBeløp tilInntekt) {
        this.fraInntekt = fraInntekt;
        this.tilInntekt = tilInntekt;
    }

    public InntektEndring(@Valid BigDecimal fraInntekt, @Valid @NotNull BigDecimal tilInntekt) {
        this.fraInntekt = DiffBeløp.fra(fraInntekt);
        this.tilInntekt = DiffBeløp.fra(tilInntekt);
    }

    public DiffBeløp getFraInntekt() {
        return fraInntekt;
    }

    public DiffBeløp getTilInntekt() {
        return tilInntekt;
    }
}
