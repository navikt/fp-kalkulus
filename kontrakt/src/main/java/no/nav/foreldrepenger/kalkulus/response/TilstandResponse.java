package no.nav.foreldrepenger.kalkulus.response;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.kalkulus.response.vilkår.VilkårResponse;

/**
 * Beskriver hvilke avklaringsbehov som må løses av K9 eller FPSAK for at beregningen kan fortsette
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class TilstandResponse implements KalkulusRespons {

    @JsonProperty(value = "eksternReferanse")
    @Valid
    private UUID eksternReferanse;

    @JsonProperty(value = "avklaringsbehovMedTilstandDto")
    private List<@Valid AvklaringsbehovMedTilstandDto> avklaringsbehovMedTilstandDto;

	@JsonProperty("vilkårResultat")
	@Valid
	private VilkårResponse vilkårResultat;

    public TilstandResponse() {
        // default ctor
    }

	public TilstandResponse(@Valid UUID eksternReferanse,
	                        List<@Valid AvklaringsbehovMedTilstandDto> avklaringsbehovMedTilstandDto,
                            @Valid VilkårResponse vilkårResultat) {
		this.eksternReferanse = eksternReferanse;
		this.avklaringsbehovMedTilstandDto = avklaringsbehovMedTilstandDto;
		this.vilkårResultat = vilkårResultat;
	}

    public List<AvklaringsbehovMedTilstandDto> getAvklaringsbehovMedTilstandDto() {
        return avklaringsbehovMedTilstandDto;
    }

    @Override
    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public VilkårResponse getVilkårResultat() {
		return vilkårResultat;
	}
}
