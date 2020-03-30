package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;

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

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class ForeldrepengerGrunnlag extends YtelsespesifiktGrunnlagDto {

    public static final String YTELSE_TYPE = "FP";

    protected ForeldrepengerGrunnlag() {
        // default ctor
    }

    public ForeldrepengerGrunnlag(@Valid @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "verdien ${validatedValue} må være <= {value}") @Digits(integer = 3, fraction = 2) BigDecimal dekningsgrad, @Valid @NotNull Boolean kvalifisererTilBesteberegning) {
        super(dekningsgrad, kvalifisererTilBesteberegning);
    }

    public static String getYtelseType() {
        return YTELSE_TYPE;
    }

}
