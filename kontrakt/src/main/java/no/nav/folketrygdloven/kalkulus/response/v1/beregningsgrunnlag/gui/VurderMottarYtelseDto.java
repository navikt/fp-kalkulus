package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class VurderMottarYtelseDto {

    @JsonProperty(value = "erFrilans")
    private boolean erFrilans;

    @JsonProperty(value = "frilansMottarYtelse")
    private Boolean frilansMottarYtelse;

    @JsonProperty(value = "frilansInntektPrMnd")
    private BigDecimal frilansInntektPrMnd;

    @JsonProperty(value = "arbeidstakerAndelerUtenIM")
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
