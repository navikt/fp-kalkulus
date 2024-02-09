package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrStatusOgAndelSNDto extends BeregningsgrunnlagPrStatusOgAndelDto {

    @Valid
    @JsonProperty("pgiSnitt")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal pgiSnitt;

    @Valid
    @JsonProperty("pgiVerdier")
    @Size
    private List<PgiDto> pgiVerdier;

    @Valid
    @JsonProperty("næringer")
    @Size
    private List<EgenNæringDto> næringer;

    public BeregningsgrunnlagPrStatusOgAndelSNDto() {
        super();
    }

    public BigDecimal getPgiSnitt() {
        return pgiSnitt;
    }

    public void setPgiSnitt(BigDecimal pgiSnitt) {
        this.pgiSnitt = pgiSnitt;
    }

    public List<PgiDto> getPgiVerdier() {
        return pgiVerdier;
    }

    public void setPgiVerdier(List<PgiDto> pgiVerdier) {
        this.pgiVerdier = pgiVerdier;
    }

    public List<EgenNæringDto> getNæringer() {
        return næringer;
    }

    public void setNæringer(List<EgenNæringDto> næringer) {
        this.næringer = næringer;
    }
}
