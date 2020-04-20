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
    private final boolean søkerYtelseForFrilans;
    private final boolean søkerYtelseForNæring;

    public FrisinnGrunnlag(boolean søkerYtelseForFrilans, boolean søkerYtelseForNæring) {
        this.søkerYtelseForFrilans = søkerYtelseForFrilans;
        this.søkerYtelseForNæring = søkerYtelseForNæring;
    }

    public static String getYtelseType() {
        return YTELSE_TYPE;
    }

    public boolean getSøkerYtelseForFrilans() {
        return søkerYtelseForFrilans;
    }

    public boolean getSøkerYtelseForNæring() {
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
