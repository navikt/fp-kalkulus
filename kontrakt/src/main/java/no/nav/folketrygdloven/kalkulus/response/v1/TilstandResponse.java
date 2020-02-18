package no.nav.folketrygdloven.kalkulus.response.v1;


import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.beregning.v1.AksjonspunktMedTilstandDto;


/**
 * Beskriver hvilke aksjonspunkter som må løses av K9 eller FPSAK for at beregningen kan fortsette
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class TilstandResponse {

    @JsonProperty(value = "aksjonspunktMedTilstandDto")
    @Valid
    private List<AksjonspunktMedTilstandDto> aksjonspunktMedTilstandDto;

    public TilstandResponse() {
        // default ctor
    }

    @JsonCreator
    public TilstandResponse(@Valid List<AksjonspunktMedTilstandDto> aksjonspunktMedTilstandDto) {
        this.aksjonspunktMedTilstandDto = aksjonspunktMedTilstandDto;
    }

    public List<AksjonspunktMedTilstandDto> getAksjonspunktMedTilstandDto() {
        return aksjonspunktMedTilstandDto;
    }

    public static TilstandResponse TOM_RESPONSE() {
        return new TilstandResponse(Collections.emptyList());
    }
}
