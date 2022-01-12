package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class VurderMottarYtelseDto {

    @Valid
    @JsonProperty(value = "erFrilans")
    private boolean erFrilans;

    @Valid
    @JsonProperty(value = "frilansMottarYtelse")
    private Boolean frilansMottarYtelse;

    @Valid
    @JsonProperty(value = "frilansInntektPrMnd")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal frilansInntektPrMnd;

    @Valid
    @JsonProperty(value = "arbeidstakerAndelerUtenIM")
    @Size
    private List<ArbeidstakerUtenInntektsmeldingAndelDto> arbeidstakerAndelerUtenIM = new ArrayList<>();

    public boolean getErFrilans() {
        return erFrilans;
    }

    public void setErFrilans(boolean erFrilans) {
        this.erFrilans = erFrilans;
    }

    public Boolean getFrilansMottarYtelse() {
        return frilansMottarYtelse;
    }

    public BigDecimal getFrilansInntektPrMnd() {
        return frilansInntektPrMnd;
    }

    public void setFrilansInntektPrMnd(BigDecimal frilansInntektPrMnd) {
        this.frilansInntektPrMnd = frilansInntektPrMnd;
    }

    public void setFrilansMottarYtelse(Boolean frilansMottarYtelse) {
        this.frilansMottarYtelse = frilansMottarYtelse;
    }

    public List<ArbeidstakerUtenInntektsmeldingAndelDto> getArbeidstakerAndelerUtenIM() {
        return arbeidstakerAndelerUtenIM;
    }

    public void leggTilArbeidstakerAndelUtenInntektsmelding(ArbeidstakerUtenInntektsmeldingAndelDto arbeidstakerAndelUtenInnteksmelding) {
        this.arbeidstakerAndelerUtenIM.add(arbeidstakerAndelUtenInnteksmelding);
    }
}
