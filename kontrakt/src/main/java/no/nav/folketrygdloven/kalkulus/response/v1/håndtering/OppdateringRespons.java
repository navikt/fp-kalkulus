package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Beskriver hvilke endringer som er gjort på beregningsgrunnlaget ved løst aksjonspunkt
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class OppdateringRespons {

    @JsonProperty(value = "beregningsgrunnlagEndring")
    @Valid
    private BeregningsgrunnlagEndring beregningsgrunnlagEndring;

    public BeregningsgrunnlagEndring getBeregningsgrunnlagEndring() {
        return beregningsgrunnlagEndring;
    }

    public void setBeregningsgrunnlagEndring(BeregningsgrunnlagEndring beregningsgrunnlagEndring) {
        this.beregningsgrunnlagEndring = beregningsgrunnlagEndring;
    }
}
