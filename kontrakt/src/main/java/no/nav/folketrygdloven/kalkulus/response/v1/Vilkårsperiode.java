package no.nav.folketrygdloven.kalkulus.response.v1;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Vilkårsavslagsårsak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class Vilkårsperiode {

    @JsonProperty("vilkårsavslagsårsak")
    @Valid
    private Vilkårsavslagsårsak vilkårsavslagsårsak;

    @JsonProperty("erVilkårOppfylt")
    @Valid
    private boolean erVilkårOppfylt;

    @JsonProperty("vilkårsperiode")
    @Valid
    private Periode vilkårsperiode;

    public Vilkårsperiode() {
        //
    }

    public Vilkårsperiode(@Valid Vilkårsavslagsårsak vilkårsavslagsårsak, @Valid boolean erVilkårOppfylt, @Valid Periode vilkårsperiode) {
        this.vilkårsavslagsårsak = vilkårsavslagsårsak;
        this.erVilkårOppfylt = erVilkårOppfylt;
        this.vilkårsperiode = vilkårsperiode;
    }

    public Vilkårsavslagsårsak getVilkårsavslagsårsak() {
        return vilkårsavslagsårsak;
    }

    public boolean isErVilkårOppfylt() {
        return erVilkårOppfylt;
    }

    public Periode getVilkårsperiode() {
        return vilkårsperiode;
    }
}
