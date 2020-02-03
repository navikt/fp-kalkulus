package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittFrilansDto {

    @JsonProperty
    @Valid
    @NotNull
    private Boolean harInntektFraFosterhjem;

    @JsonProperty
    @Valid
    @NotNull
    private Boolean erNyoppstartet;

    @JsonProperty
    @Valid
    @NotNull
    private Boolean harNærRelasjon;

    public OppgittFrilansDto(@Valid @NotNull Boolean harInntektFraFosterhjem, @Valid @NotNull Boolean erNyoppstartet, @Valid @NotNull Boolean harNærRelasjon) {
        this.harInntektFraFosterhjem = harInntektFraFosterhjem;
        this.erNyoppstartet = erNyoppstartet;
        this.harNærRelasjon = harNærRelasjon;
    }

    public Boolean getHarInntektFraFosterhjem() {
        return harInntektFraFosterhjem;
    }

    public Boolean getErNyoppstartet() {
        return erNyoppstartet;
    }

    public Boolean getHarNærRelasjon() {
        return harNærRelasjon;
    }
}
