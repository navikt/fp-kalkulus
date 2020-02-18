package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.time.LocalDateTime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AksjonspunktMedTilstandDto {

    @JsonProperty(value = "beregningAksjonspunktDefinisjon")
    @Valid
    @NotNull
    private BeregningAksjonspunkt beregningAksjonspunktDefinisjon;

    @JsonProperty(value = "beregningAksjonspunktDefinisjon")
    @Valid
    @NotNull
    private BeregningVenteårsak venteårsak;

    @JsonProperty(value = "beregningAksjonspunktDefinisjon")
    @Valid
    @NotNull
    private LocalDateTime ventefrist;

    public AksjonspunktMedTilstandDto() {
        // default ctor
    }

    public AksjonspunktMedTilstandDto(@Valid @NotNull BeregningAksjonspunkt beregningAksjonspunktDefinisjon, @Valid @NotNull BeregningVenteårsak venteårsak, @Valid @NotNull LocalDateTime ventefrist) {
        this.beregningAksjonspunktDefinisjon = beregningAksjonspunktDefinisjon;
        this.venteårsak = venteårsak;
        this.ventefrist = ventefrist;
    }

    public BeregningAksjonspunkt getBeregningAksjonspunktDefinisjon() {
        return beregningAksjonspunktDefinisjon;
    }

    public BeregningVenteårsak getVenteårsak() {
        return venteårsak;
    }

    public LocalDateTime getVentefrist() {
        return ventefrist;
    }
}
