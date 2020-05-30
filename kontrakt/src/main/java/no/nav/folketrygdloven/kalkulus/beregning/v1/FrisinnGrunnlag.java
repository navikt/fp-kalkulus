package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class FrisinnGrunnlag extends YtelsespesifiktGrunnlagDto {

    public static final String YTELSE_TYPE = "FRISINN";
    /**
     * Er det søkt ytelse for frilansaktivitet
     */
    @JsonProperty("søkerYtelseForFrilans")
    @Valid
    @NotNull
    @Deprecated // Fjernes herfra når vi har gått over til å bruke frisinnPerioder
    private Boolean søkerYtelseForFrilans;

    /**
     * Er det søkt ytelse for næringsinntekt
     */
    @JsonProperty("søkerYtelseForNæring")
    @Valid
    @NotNull
    @Deprecated // Fjernes herfra når vi har gått over til å bruke frisinnPerioder
    private Boolean søkerYtelseForNæring;

    @Valid
    @JsonProperty("perioderMedSøkerInfo")
    @Size(max = 20)
    private List<PeriodeMedSøkerInfoDto> perioderMedSøkerInfo;

    protected FrisinnGrunnlag() {
        // default ctor
    }

    public FrisinnGrunnlag(@Valid @NotNull Boolean søkerYtelseForFrilans, @Valid @NotNull Boolean søkerYtelseForNæring) {
        this.søkerYtelseForFrilans = søkerYtelseForFrilans;
        this.søkerYtelseForNæring = søkerYtelseForNæring;
    }

    public FrisinnGrunnlag(@Valid @Size(max = 20) List<PeriodeMedSøkerInfoDto> perioderMedSøkerInfo) {
        this.perioderMedSøkerInfo = perioderMedSøkerInfo;
    }

    public FrisinnGrunnlag medPerioderMedSøkerInfo(List<PeriodeMedSøkerInfoDto> perioderMedSøkerInfo) {
        this.perioderMedSøkerInfo = perioderMedSøkerInfo;
        return this;
    }

    public List<PeriodeMedSøkerInfoDto> getPerioderMedSøkerInfo() {
        return perioderMedSøkerInfo;
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
                "perioderMedSøkerInfo=" + perioderMedSøkerInfo +
                '}';
    }
}
