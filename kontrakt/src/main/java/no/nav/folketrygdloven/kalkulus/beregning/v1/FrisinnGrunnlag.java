package no.nav.folketrygdloven.kalkulus.beregning.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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

    /**
     * Er det søkt ytelse for frilansaktivitet
     */
    @Valid
    @NotNull
    private final Boolean søkerYtelseForFrilans;

    /**
     * Er det søkt ytelse for næringsinntekt
     */
    @Valid
    @NotNull
    private final Boolean søkerYtelseForNæring;

    public FrisinnGrunnlag(Boolean søkerYtelseForFrilans, Boolean søkerYtelseForNæring) {
        this.søkerYtelseForFrilans = søkerYtelseForFrilans;
        this.søkerYtelseForNæring = søkerYtelseForNæring;
    }

    public static String getYtelseType() {
        return YTELSE_TYPE;
    }

    public Boolean getSøkerYtelseForFrilans() {
        return søkerYtelseForFrilans;
    }

    public Boolean getSøkerYtelseForNæring() {
        return søkerYtelseForNæring;
    }

    @Override
    public String toString() {
        return "FrisinnGrunnlag{" +
                "søkerYtelseForFrilans=" + søkerYtelseForFrilans +
                ", søkerYtelseForNæring=" + søkerYtelseForNæring +
                '}';
    }
}
