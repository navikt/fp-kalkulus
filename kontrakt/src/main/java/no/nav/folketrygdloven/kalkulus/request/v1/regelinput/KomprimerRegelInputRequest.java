package no.nav.folketrygdloven.kalkulus.request.v1.regelinput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class KomprimerRegelInputRequest {

    @JsonProperty(value = "saksnummer")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "antall")
    @Valid
    @NotNull
    private int antall;

    public KomprimerRegelInputRequest() {
    }

    @JsonCreator
    public KomprimerRegelInputRequest(@JsonProperty(value = "saksnummer") String saksnummer,
                                      @JsonProperty(value = "antall") int antall) {
        this.saksnummer = saksnummer;
        this.antall = antall;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public int getAntall() {
        return antall;
    }
}
