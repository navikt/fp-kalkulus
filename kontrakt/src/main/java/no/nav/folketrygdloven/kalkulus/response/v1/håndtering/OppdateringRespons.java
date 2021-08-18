package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Beskriver hvilke endringer som er gjort på beregningsgrunnlaget ved løst avklaringsbehov
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class OppdateringRespons {

    @JsonProperty(value = "beregningsgrunnlagEndring")
    @Valid
    private BeregningsgrunnlagEndring beregningsgrunnlagEndring;

    @JsonProperty(value = "faktaOmBeregningVurderinger")
    @Valid
    private FaktaOmBeregningVurderinger faktaOmBeregningVurderinger;

    @JsonProperty(value = "refusjonoverstyringEndring")
    @Valid
    private RefusjonoverstyringEndring refusjonoverstyringEndring;

    public OppdateringRespons() {
    }

    public OppdateringRespons(@Valid BeregningsgrunnlagEndring beregningsgrunnlagEndring,
                              @Valid FaktaOmBeregningVurderinger faktaOmBeregningVurderinger) {
        this.beregningsgrunnlagEndring = beregningsgrunnlagEndring;
        this.faktaOmBeregningVurderinger = faktaOmBeregningVurderinger;
    }

    public OppdateringRespons(@Valid RefusjonoverstyringEndring refusjonoverstyringEndring) {
        this.refusjonoverstyringEndring = refusjonoverstyringEndring;
    }

    public OppdateringRespons(@Valid BeregningsgrunnlagEndring beregningsgrunnlagEndring) {
        this.beregningsgrunnlagEndring = beregningsgrunnlagEndring;
    }

    public static OppdateringRespons TOM_RESPONS() {
        return new OppdateringRespons();
    }

    public BeregningsgrunnlagEndring getBeregningsgrunnlagEndring() {
        return beregningsgrunnlagEndring;
    }

    public FaktaOmBeregningVurderinger getFaktaOmBeregningVurderinger() {
        return faktaOmBeregningVurderinger;
    }

    public RefusjonoverstyringEndring getRefusjonoverstyringEndring() {
        return refusjonoverstyringEndring;
    }

}
