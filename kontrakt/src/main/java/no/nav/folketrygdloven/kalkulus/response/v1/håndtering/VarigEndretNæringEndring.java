package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class VarigEndretNæringEndring {

    @JsonProperty(value = "erVarigEndretNaeringEndring")
    @Valid
    private ToggleEndring erVarigEndretNaeringEndring;

    public VarigEndretNæringEndring() {
    }

    public VarigEndretNæringEndring(ToggleEndring erVarigEndretNaeringEndring) {
        this.erVarigEndretNaeringEndring = erVarigEndretNaeringEndring;
    }

    public ToggleEndring getErVarigEndretNaeringEndring() {
        return erVarigEndretNaeringEndring;
    }

}
