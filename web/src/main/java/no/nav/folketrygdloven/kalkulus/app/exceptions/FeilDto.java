package no.nav.folketrygdloven.kalkulus.app.exceptions;

import java.io.Serializable;
import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class FeilDto implements Serializable {

    @JsonProperty(value = "feilmelding", required = true)
    @NotNull
    @Size(max = 2000)
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{P}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String feilmelding;

    @JsonInclude(value = Include.NON_EMPTY)
    @JsonProperty(value = "feltFeil")
    @Valid
    @Size(max = 20)
    private Collection<FeltFeilDto> feltFeil;

    @JsonProperty(value = "type", required = true)
    @Valid
    private FeilType type;

    public FeilDto(FeilType type, String feilmelding) {
        this.type = type;
        this.feilmelding = feilmelding;
    }

    public FeilDto(String feilmelding) {
        this(FeilType.GENERELL_FEIL, feilmelding);
    }

    public FeilDto(String feilmelding, Collection<FeltFeilDto> feltFeil) {
        this(feilmelding);
        this.feltFeil = feltFeil;
    }

    protected FeilDto() {
        //
    }

    public String getFeilmelding() {
        return feilmelding;
    }

    public Collection<FeltFeilDto> getFeltFeil() {
        return feltFeil;
    }

    public FeilType getType() {
        return type;
    }
}

