package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class OmsorgspengerGrunnlag extends YtelsespesifiktGrunnlagDto {

    public static final String YTELSE_TYPE = "OMP";


    protected OmsorgspengerGrunnlag() {
        // default ctor
    }

    public OmsorgspengerGrunnlag(@Valid @DecimalMin(value = "0.00", message = "stillingsprosent ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "stillingsprosent ${validatedValue} må være <= {value}") BigDecimal dekningsgrad) {
        super(dekningsgrad);
    }

    public static String getYtelseType() {
        return YTELSE_TYPE;
    }
}
