package no.nav.folketrygdloven.kalkulus.response.v1.regelinput;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class Saksnummer {

    @Valid
    @JsonProperty(value = "saksnummer")
    @NotNull
    private String saksnummer;


    public Saksnummer() {
    }

    public Saksnummer(@JsonProperty(value = "saksnummer") String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public String getSaksnummer() {
        return saksnummer;
    }
}
