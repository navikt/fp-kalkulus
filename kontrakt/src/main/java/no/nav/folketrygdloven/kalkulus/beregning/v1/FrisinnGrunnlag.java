package no.nav.folketrygdloven.kalkulus.beregning.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class FrisinnGrunnlag extends YtelsespesifiktGrunnlagDto {

    public static final String YTELSE_TYPE = "FRISINN";

    public FrisinnGrunnlag() {
        // default ctor
    }

    public static String getYtelseType() {
        return YTELSE_TYPE;
    }

    @Override
    public String toString() {
        return "FrisinnGrunnlag{" +
                YTELSE_TYPE +
                '}';
    }

}
