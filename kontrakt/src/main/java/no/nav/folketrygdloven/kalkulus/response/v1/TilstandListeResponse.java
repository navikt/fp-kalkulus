package no.nav.folketrygdloven.kalkulus.response.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Beskriver hvilke avklaringsbehov som må løses av K9 eller FPSAK for at beregningen kan fortsette
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class TilstandListeResponse {

    @JsonProperty(value = "tilstand")
    @NotEmpty
    @Valid
    private List<TilstandResponse> tilstand;

    @JsonProperty(value = "trengerNyInput")
    @Valid
    private Boolean trengerNyInput;


    public TilstandListeResponse() {
        // default ctor
    }

    public TilstandListeResponse(@JsonProperty(value = "tilstand") @Valid @NotNull @NotEmpty List<TilstandResponse> tilstand) {
        this.tilstand = List.copyOf(tilstand);
    }

    public TilstandListeResponse(@Valid Boolean trengerNyInput) {
        this.trengerNyInput = trengerNyInput;
    }

    @AssertTrue(message = "Sjekk tilstandresponer")
    boolean sjekkOmHarAvslagsårsak() {
        return tilstand.stream().allMatch(t -> t.sjekkOmHarAvslagsårsak());
    }

    public List<TilstandResponse> getTilstand() {
        return tilstand;
    }

    public boolean trengerNyInput() { return trengerNyInput != null && trengerNyInput; }


}
