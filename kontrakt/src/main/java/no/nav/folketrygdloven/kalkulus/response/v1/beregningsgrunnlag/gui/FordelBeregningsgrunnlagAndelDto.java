package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FordelBeregningsgrunnlagAndelDto extends FaktaOmBeregningAndelDto {

    private static final int MÅNEDER_I_1_ÅR = 12;

    @Valid
    @JsonProperty(value = "fordelingForrigeBehandlingPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal fordelingForrigeBehandlingPrAar;

    @Valid
    @JsonProperty(value = "refusjonskravPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal refusjonskravPrAar = BigDecimal.ZERO;

    @Valid
    @JsonProperty(value = "fordeltPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal fordeltPrAar;

    @Valid
    @JsonProperty(value = "belopFraInntektsmeldingPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal belopFraInntektsmeldingPrAar;

    @Valid
    @JsonProperty(value = "refusjonskravFraInntektsmeldingPrAar")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal refusjonskravFraInntektsmeldingPrAar;

    @Valid
    @JsonProperty(value = "nyttArbeidsforhold")
    private boolean nyttArbeidsforhold;

    @Valid
    @JsonProperty(value = "arbeidsforholdType")
    private OpptjeningAktivitetType arbeidsforholdType;

    public FordelBeregningsgrunnlagAndelDto(FaktaOmBeregningAndelDto superDto) {
        super(superDto.getAndelsnr(), superDto.getArbeidsforhold(), superDto.getInntektskategori(),
            superDto.getAktivitetStatus(), superDto.getLagtTilAvSaksbehandler(), superDto.getFastsattAvSaksbehandler(), superDto.getAndelIArbeid());
    }

    public void setBelopFraInntektsmelding(BigDecimal belopFraInntektsmelding) {
        this.belopFraInntektsmeldingPrAar = belopFraInntektsmelding == null ?
            null : BigDecimal.valueOf(MÅNEDER_I_1_ÅR).multiply(belopFraInntektsmelding).setScale(0, RoundingMode.HALF_UP);
    }

    public void setFordelingForrigeBehandling(BigDecimal fordelingForrigeBehandling) {
        this.fordelingForrigeBehandlingPrAar = fordelingForrigeBehandling == null ?
            null : BigDecimal.valueOf(MÅNEDER_I_1_ÅR).multiply(fordelingForrigeBehandling).setScale(0, RoundingMode.HALF_UP);
    }

    public void setRefusjonskravPrAar(BigDecimal refusjonskravPrAar) {
        this.refusjonskravPrAar = refusjonskravPrAar == null ?
            null : refusjonskravPrAar.setScale(0, RoundingMode.HALF_UP);
    }


    public void setRefusjonskravFraInntektsmeldingPrÅr(BigDecimal refusjonskravFraInntektsmelding) {
        this.refusjonskravFraInntektsmeldingPrAar = refusjonskravFraInntektsmelding == null ?
            null : refusjonskravFraInntektsmelding.setScale(0, RoundingMode.HALF_UP);

    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public void setArbeidsforholdType(OpptjeningAktivitetType arbeidsforholdType) {
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public boolean isNyttArbeidsforhold() {
        return nyttArbeidsforhold;
    }

    public void setNyttArbeidsforhold(boolean nyttArbeidsforhold) {
        this.nyttArbeidsforhold = nyttArbeidsforhold;
    }

    public BigDecimal getFordelingForrigeBehandlingPrAar() {
        return fordelingForrigeBehandlingPrAar;
    }

    public BigDecimal getRefusjonskravPrAar() {
        return refusjonskravPrAar;
    }

    public BigDecimal getBelopFraInntektsmeldingPrAar() {
        return belopFraInntektsmeldingPrAar;
    }

    public BigDecimal getRefusjonskravFraInntektsmeldingPrAar() {
        return refusjonskravFraInntektsmeldingPrAar;
    }

    public BigDecimal getFordeltPrAar() {
        return fordeltPrAar;
    }

    public void setFordeltPrAar(BigDecimal fordeltPrAar) {
        this.fordeltPrAar = fordeltPrAar;
    }
}
