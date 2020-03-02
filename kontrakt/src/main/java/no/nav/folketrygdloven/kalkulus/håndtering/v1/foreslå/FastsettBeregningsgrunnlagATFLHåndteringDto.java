package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsattePerioderTidsbegrensetDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettBeregningsgrunnlagATFLHåndteringDto extends HåndterBeregningDto {

    public static final String IDENT_TYPE = "5038";

    @Valid
    @Size(max = 100)
    private List<InntektPrAndelDto> inntektPrAndelList;

    @Min(0)
    @Max(100 * 1000 * 1000)
    private Integer inntektFrilanser;

    @Valid
    @Size(max = 100)
    private List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder;


    FastsettBeregningsgrunnlagATFLHåndteringDto() {
        // For Jackson
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }


    public FastsettBeregningsgrunnlagATFLHåndteringDto(List<InntektPrAndelDto> inntektPrAndelList, Integer inntektFrilanser) { // NOSONAR
        this.inntektPrAndelList = new ArrayList<>(inntektPrAndelList);
        this.inntektFrilanser = inntektFrilanser;
    }

    public FastsettBeregningsgrunnlagATFLHåndteringDto(Integer inntektFrilanser) { // NOSONAR
        this.inntektFrilanser = inntektFrilanser;
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
