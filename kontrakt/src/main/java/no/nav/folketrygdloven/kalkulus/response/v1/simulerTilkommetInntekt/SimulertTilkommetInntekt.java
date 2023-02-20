package no.nav.folketrygdloven.kalkulus.response.v1.simulerTilkommetInntekt;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.BeregningsgrunnlagPrReferanse;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class SimulertTilkommetInntekt {

    @JsonProperty(value = "antallSakerMedAksjonspunkt")
    @Valid
    @Min(0)
    @Max(1000000)
    private long antallSakerMedAksjonspunkt;

    @JsonProperty(value = "antallSakerMedReduksjon")
    @Valid
    @Min(0)
    @Max(1000000)
    private long antallSakerMedReduksjon;


    @JsonProperty(value = "antallSakerSimulert")
    @Valid
    @Min(0)
    @Max(1000000)
    private long antallSakerSimulert;

    public SimulertTilkommetInntekt(long antallSakerMedAksjonspunkt, long antallSakerMedReduksjon, long antallSakerSimulert) {
        this.antallSakerMedAksjonspunkt = antallSakerMedAksjonspunkt;
        this.antallSakerMedReduksjon = antallSakerMedReduksjon;
        this.antallSakerSimulert = antallSakerSimulert;
    }

    public SimulertTilkommetInntekt() {
    }

    public long getAntallSakerMedAksjonspunkt() {
        return antallSakerMedAksjonspunkt;
    }

    public long getAntallSakerSimulert() {
        return antallSakerSimulert;
    }

    public long getAntallSakerMedReduksjon() {
        return antallSakerMedReduksjon;
    }
}
