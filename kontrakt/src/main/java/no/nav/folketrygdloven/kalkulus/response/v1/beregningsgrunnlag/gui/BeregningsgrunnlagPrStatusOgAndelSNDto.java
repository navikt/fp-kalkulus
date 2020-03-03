package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrStatusOgAndelSNDto extends BeregningsgrunnlagPrStatusOgAndelDto {

    @JsonProperty("pgiSnitt")
    private BigDecimal pgiSnitt;

    @JsonProperty("pgiVerdier")
    private List<PgiDto> pgiVerdier;

    @JsonProperty("næringer")
    private List<EgenNæringDto> næringer;

    public BeregningsgrunnlagPrStatusOgAndelSNDto() {
        super();
        // trengs for deserialisering av JSON
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
