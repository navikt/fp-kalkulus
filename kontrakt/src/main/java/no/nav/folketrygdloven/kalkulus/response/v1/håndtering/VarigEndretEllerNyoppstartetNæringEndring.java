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
public class VarigEndretEllerNyoppstartetNæringEndring {

    @JsonProperty(value = "erVarigEndretNaeringEndring")
    @Valid
    private ToggleEndring erVarigEndretNaeringEndring;

    @JsonProperty(value = "erNyoppstartetNaeringEndring")
    @Valid
    private ToggleEndring erNyoppstartetNaeringEndring;

    public VarigEndretEllerNyoppstartetNæringEndring() {
    }

    private VarigEndretEllerNyoppstartetNæringEndring(ToggleEndring erVarigEndretNaeringEndring, ToggleEndring erNyoppstartetNaeringEndring) {
        this.erVarigEndretNaeringEndring = erVarigEndretNaeringEndring;
        this.erNyoppstartetNaeringEndring = erNyoppstartetNaeringEndring;
    }

    public static VarigEndretEllerNyoppstartetNæringEndring forVarigEndretNæring(ToggleEndring erVarigEndretNaeringEndring) {
        return new VarigEndretEllerNyoppstartetNæringEndring(erVarigEndretNaeringEndring, null);
    }

    public static VarigEndretEllerNyoppstartetNæringEndring forNyoppstartetNæring(ToggleEndring erNyoppstartetNaeringEndring) {
        return new VarigEndretEllerNyoppstartetNæringEndring(null, erNyoppstartetNaeringEndring);
    }

    public ToggleEndring getErVarigEndretNaeringEndring() {
        return erVarigEndretNaeringEndring;
    }

    public ToggleEndring getErNyoppstartetNaeringEndring() {
        return erNyoppstartetNaeringEndring;
    }
}
