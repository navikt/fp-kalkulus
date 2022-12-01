package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class VurderNyttInntektsforholdDto {

    @Valid
    @JsonProperty(value = "vurderInntektsforholdPerioder")
    @Size(max = 50)
    @NotNull
    private List<VurderInntektsforholdPeriodeDto> vurderInntektsforholdPerioder = new ArrayList<>();

    public VurderNyttInntektsforholdDto() {
    }

    public VurderNyttInntektsforholdDto(List<VurderInntektsforholdPeriodeDto> vurderInntektsforholdPerioder) {
        this.vurderInntektsforholdPerioder = vurderInntektsforholdPerioder;
    }

    public List<VurderInntektsforholdPeriodeDto> getVurderInntektsforholdPerioder() {
        return vurderInntektsforholdPerioder;
    }

    public void setVurderInntektsforholdPerioder(List<VurderInntektsforholdPeriodeDto> vurderInntektsforholdPerioder) {
        this.vurderInntektsforholdPerioder = vurderInntektsforholdPerioder;
    }
}
