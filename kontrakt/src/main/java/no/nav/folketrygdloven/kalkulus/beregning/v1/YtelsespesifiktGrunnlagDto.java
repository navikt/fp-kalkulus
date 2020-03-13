package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.AvklarAktiviteterHåndteringDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "ytelseType", defaultImpl = Void.class)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PleiepengerSyktBarnGrunnlag.class, name = PleiepengerSyktBarnGrunnlag.YTELSE_TYPE),
})
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public abstract class YtelsespesifiktGrunnlagDto {

    @JsonProperty(value = "dekningsgrad")
    @Valid
    @DecimalMin(value = "0.00", message = "stillingsprosent ${validatedValue} må være >= {value}")
    @DecimalMax(value = "100.00", message = "stillingsprosent ${validatedValue} må være <= {value}")
    private BigDecimal dekningsgrad;

    @JsonProperty(value = "kvalifisererTilBesteberegning")
    @Valid
    @NotNull
    private Boolean kvalifisererTilBesteberegning = false;

    protected YtelsespesifiktGrunnlagDto() {
        // default ctor
    }

    public YtelsespesifiktGrunnlagDto(@Valid @DecimalMin(value = "0.00", message = "stillingsprosent ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "stillingsprosent ${validatedValue} må være <= {value}") BigDecimal dekningsgrad) {
        this.dekningsgrad = dekningsgrad;
    }


    private YtelsespesifiktGrunnlagDto medBesteberegning() {
        this.kvalifisererTilBesteberegning = true;
        return this;
    }

    public BigDecimal getDekningsgrad() {
        return dekningsgrad;
    }

    public Boolean getKvalifisererTilBesteberegning() {
        return kvalifisererTilBesteberegning;
    }
    
}
