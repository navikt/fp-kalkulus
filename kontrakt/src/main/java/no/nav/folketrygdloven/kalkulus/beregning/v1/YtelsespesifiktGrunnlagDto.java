package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "ytelseType", defaultImpl = Void.class)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PleiepengerSyktBarnGrunnlag.class, name = PleiepengerSyktBarnGrunnlag.YTELSE_TYPE),
        @JsonSubTypes.Type(value = OmsorgspengerGrunnlag.class, name = OmsorgspengerGrunnlag.YTELSE_TYPE),
        @JsonSubTypes.Type(value = ForeldrepengerGrunnlag.class, name = ForeldrepengerGrunnlag.YTELSE_TYPE),
        @JsonSubTypes.Type(value = SvangerskapspengerGrunnlag.class, name = SvangerskapspengerGrunnlag.YTELSE_TYPE),
        @JsonSubTypes.Type(value = FrisinnGrunnlag.class, name = FrisinnGrunnlag.YTELSE_TYPE),
})
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public abstract class YtelsespesifiktGrunnlagDto {

    @JsonProperty(value = "dekningsgrad")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "100.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal dekningsgrad = BigDecimal.valueOf(100);

    @JsonProperty(value = "kvalifisererTilBesteberegning")
    @Valid
    @NotNull
    private Boolean kvalifisererTilBesteberegning = false;

    protected YtelsespesifiktGrunnlagDto() {
        // default ctor
    }

    public YtelsespesifiktGrunnlagDto(@Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 3, fraction = 2) BigDecimal dekningsgrad, @Valid @NotNull Boolean kvalifisererTilBesteberegning) {
        this.dekningsgrad = dekningsgrad;
        this.kvalifisererTilBesteberegning = kvalifisererTilBesteberegning;
    }

    public BigDecimal getDekningsgrad() {
        return dekningsgrad;
    }

    public Boolean getKvalifisererTilBesteberegning() {
        return kvalifisererTilBesteberegning;
    }

    @Override
    public String toString() {
        return "YtelsespesifiktGrunnlagDto{" +
                "dekningsgrad=" + dekningsgrad +
                ", kvalifisererTilBesteberegning=" + kvalifisererTilBesteberegning +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YtelsespesifiktGrunnlagDto that = (YtelsespesifiktGrunnlagDto) o;
        return Objects.equals(dekningsgrad, that.dekningsgrad) &&
                Objects.equals(kvalifisererTilBesteberegning, that.kvalifisererTilBesteberegning);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dekningsgrad, kvalifisererTilBesteberegning);
    }
}
