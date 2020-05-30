package no.nav.folketrygdloven.kalkulus.response.v1;


import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.beregning.v1.AksjonspunktMedTilstandDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Vilkårsavslagsårsak;


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

    @JsonProperty(value = "vilkarOppfylt")
    @Valid
    private Boolean vilkarOppfylt;

    @JsonProperty("vilkårsavslagsårsak")
    @Valid
    private Vilkårsavslagsårsak vilkårsavslagsårsak;

    @JsonProperty("vilkårsperioder")
    @Valid
    private List<Vilkårsperiode> vilkårsperioder;

    public TilstandResponse() {
        // default ctor
    }

    public TilstandResponse(@JsonProperty(value = "aksjonspunktMedTilstandDto") @Valid List<AksjonspunktMedTilstandDto> aksjonspunktMedTilstandDto) {
        this.aksjonspunktMedTilstandDto = aksjonspunktMedTilstandDto;
    }

    public static TilstandResponse TOM_RESPONSE() {
        return new TilstandResponse(Collections.emptyList());
    }

    public TilstandResponse medVilkårResultat(boolean resultat) {
        vilkarOppfylt = resultat;
        return this;
    }

    public TilstandResponse medVilkårsperioder(List<Vilkårsperiode> vilkår) {
        vilkårsperioder = vilkår;
        return this;
    }

    public TilstandResponse medVilkårsavslagsårsak(Vilkårsavslagsårsak vilkårsavslagsårsak) {
        this.vilkårsavslagsårsak = vilkårsavslagsårsak;
        return this;
    }

    @AssertTrue(message = "Krever vilkårsavslagsårsak når vilkåret ikke er oppfylt")
    boolean sjekkOmHarAvslagsårsak() {
        if (vilkarOppfylt != null && !vilkarOppfylt) {
            return vilkårsavslagsårsak != null;
        }
        return true;
    }

    public List<AksjonspunktMedTilstandDto> getAksjonspunktMedTilstandDto() {
        return aksjonspunktMedTilstandDto;
    }

    public Boolean getVilkarOppfylt() {
        return vilkarOppfylt;
    }

    public Vilkårsavslagsårsak getVilkårsavslagsårsak() {
        return vilkårsavslagsårsak;
    }

    public List<Vilkårsperiode> getVilkårsperioder() {
        return vilkårsperioder;
    }
}
