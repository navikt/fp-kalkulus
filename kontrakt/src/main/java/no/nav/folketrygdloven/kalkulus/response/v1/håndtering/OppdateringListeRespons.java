package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
public class OppdateringListeRespons {

    @JsonProperty(value = "oppdateringer")
    @Valid
    @NotNull
    private List<OppdateringPrRequest> oppdateringer;

    public OppdateringListeRespons() {
    }

    public OppdateringListeRespons(@Valid @NotNull List<OppdateringPrRequest> oppdateringer) {
        this.oppdateringer = oppdateringer;
    }

    public List<OppdateringPrRequest> getOppdateringer() {
        return oppdateringer;
    }
}
