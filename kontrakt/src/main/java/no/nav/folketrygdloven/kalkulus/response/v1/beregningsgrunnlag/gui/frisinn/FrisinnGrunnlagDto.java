package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FrisinnGrunnlagDto extends YtelsespesifiktGrunnlagDto {
    public static final String YTELSETYPE = "FRISINN";

    @JsonProperty("opplysningerFL")
    @Valid
    private SøknadsopplysningerDto opplysningerFL;

    @JsonProperty("opplysningerSN")
    @Valid
    private SøknadsopplysningerDto opplysningerSN;

    @Valid
    @JsonProperty("perioderSøktFor")
    @NotNull
    @Size(min = 1)
    private List<OpplystPeriodeDto> perioderSøktFor;

    public FrisinnGrunnlagDto() {
        super();
    }

    public SøknadsopplysningerDto getOpplysningerFL() {
        return opplysningerFL;
    }

    public SøknadsopplysningerDto getOpplysningerSN() {
        return opplysningerSN;
    }

    public void setOpplysningerSN(SøknadsopplysningerDto opplysningerSN) {
        this.opplysningerSN = opplysningerSN;
    }

    public void setOpplysningerFL(SøknadsopplysningerDto opplysningerFL) {
        this.opplysningerFL = opplysningerFL;
    }

    public List<OpplystPeriodeDto> getPerioderSøktFor() {
        return perioderSøktFor;
    }

    public void setPerioderSøktFor(List<OpplystPeriodeDto> perioderSøktFor) {
        this.perioderSøktFor = perioderSøktFor;
    }
}

