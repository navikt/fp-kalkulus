package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsattePerioderTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettBeregningsgrunnlagATFLHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "5038";

    @JsonProperty(value = "inntektPrAndelList")
    @Valid
    @Size(max = 100)
    private List<InntektPrAndelDto> inntektPrAndelList;

    @JsonProperty(value = "inntektFrilanser")
    @Valid
    @Min(0)
    @Max(100 * 1000 * 1000)
    private Integer inntektFrilanser;

    @JsonProperty(value = "fastsatteTidsbegrensedePerioder")
    @Valid
    @Size(max = 100)
    private List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder;


    protected FastsettBeregningsgrunnlagATFLHåndteringDto() {
        // For Jackson
    }
    public FastsettBeregningsgrunnlagATFLHåndteringDto(@Valid @Size(max = 100) List<InntektPrAndelDto> inntektPrAndelList,
                                                       @JsonProperty(value = "inntektFrilanser") @Valid @Min(0) @Max(100 * 1000 * 1000) Integer inntektFrilanser,
                                                       @JsonProperty(value = "fastsatteTidsbegrensedePerioder") @Valid @Size(max = 100) List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder) {
        super(HåndteringKode.fraKode(IDENT_TYPE));
        this.inntektPrAndelList = inntektPrAndelList;
        this.inntektFrilanser = inntektFrilanser;
        this.fastsatteTidsbegrensedePerioder = fastsatteTidsbegrensedePerioder;
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }

    public Integer getInntektFrilanser() {
        return inntektFrilanser;
    }

    public List<InntektPrAndelDto> getInntektPrAndelList() {
        return inntektPrAndelList;
    }

    public List<FastsattePerioderTidsbegrensetDto> getFastsatteTidsbegrensedePerioder() {
        return fastsatteTidsbegrensedePerioder;
    }

    public boolean tidsbegrensetInntektErFastsatt() {
        return fastsatteTidsbegrensedePerioder != null && !fastsatteTidsbegrensedePerioder.isEmpty();
    }

}
