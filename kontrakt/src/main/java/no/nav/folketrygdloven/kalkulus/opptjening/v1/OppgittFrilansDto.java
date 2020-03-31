package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittFrilansDto {

    @JsonProperty
    @Valid
    @NotNull
    private Boolean erNyoppstartet;

    public OppgittFrilansDto() {
        // default ctor
    }

    public OppgittFrilansDto(@Valid @NotNull Boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }

    public Boolean getErNyoppstartet() {
        return erNyoppstartet;
    }

}
