package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AksjonspunktMedTilstandDto {

    @JsonProperty(value = "beregningAksjonspunktDefinisjon")
    @Valid
    @NotNull
    private AksjonspunktDefinisjon beregningAksjonspunktDefinisjon;

    @JsonProperty(value = "venteårsak")
    @Valid
    @NotNull
    private BeregningVenteårsak venteårsak;

    @JsonProperty(value = "ventefrist")
    @Valid
    @NotNull
    private LocalDateTime ventefrist;

    public AksjonspunktMedTilstandDto() {
        // default ctor
    }

    public AksjonspunktMedTilstandDto(@Valid @NotNull AksjonspunktDefinisjon beregningAksjonspunktDefinisjon, @Valid @NotNull BeregningVenteårsak venteårsak, @Valid @NotNull LocalDateTime ventefrist) {
        this.beregningAksjonspunktDefinisjon = beregningAksjonspunktDefinisjon;
        this.venteårsak = venteårsak;
        this.ventefrist = ventefrist;
    }

    public AksjonspunktDefinisjon getBeregningAksjonspunktDefinisjon() {
        return beregningAksjonspunktDefinisjon;
    }

    public BeregningVenteårsak getVenteårsak() {
        return venteårsak;
    }

    public LocalDateTime getVentefrist() {
        return ventefrist;
    }

    @Override
    public String toString() {
        return "AksjonspunktMedTilstandDto{" +
                "beregningAksjonspunktDefinisjon=" + beregningAksjonspunktDefinisjon +
                ", venteårsak=" + venteårsak +
                ", ventefrist=" + ventefrist +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AksjonspunktMedTilstandDto that = (AksjonspunktMedTilstandDto) o;
        return Objects.equals(beregningAksjonspunktDefinisjon, that.beregningAksjonspunktDefinisjon) &&
                Objects.equals(venteårsak, that.venteårsak) &&
                Objects.equals(ventefrist, that.ventefrist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningAksjonspunktDefinisjon, venteårsak, ventefrist);
    }
}
