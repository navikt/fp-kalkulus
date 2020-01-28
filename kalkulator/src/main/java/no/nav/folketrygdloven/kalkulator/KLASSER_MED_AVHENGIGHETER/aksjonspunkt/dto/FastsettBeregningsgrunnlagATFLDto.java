package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class FastsettBeregningsgrunnlagATFLDto {


    @Valid
    @Size(max = 100)
    private List<InntektPrAndelDto> inntektPrAndelList;

    @Min(0)
    @Max(100 * 1000 * 1000)
    private Integer inntektFrilanser;

    @Valid
    @Size(max = 100)
    private List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder;


    FastsettBeregningsgrunnlagATFLDto() {
        // For Jackson
    }


    public FastsettBeregningsgrunnlagATFLDto(List<InntektPrAndelDto> inntektPrAndelList, Integer inntektFrilanser) { // NOSONAR
        this.inntektPrAndelList = new ArrayList<>(inntektPrAndelList);
        this.inntektFrilanser = inntektFrilanser;
    }

    public FastsettBeregningsgrunnlagATFLDto(Integer inntektFrilanser) { // NOSONAR
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
