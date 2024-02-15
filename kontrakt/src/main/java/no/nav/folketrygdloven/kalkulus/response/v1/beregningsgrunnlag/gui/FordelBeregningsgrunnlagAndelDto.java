package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FordelBeregningsgrunnlagAndelDto extends FaktaOmBeregningAndelDto {

    private static final int MÅNEDER_I_1_ÅR = 12;

    @Valid
    @JsonProperty(value = "fordelingForrigeBehandlingPrAar")
    private Beløp fordelingForrigeBehandlingPrAar;

    @Valid
    @JsonProperty(value = "refusjonskravPrAar")
    private Beløp refusjonskravPrAar = Beløp.ZERO;

    @Valid
    @JsonProperty(value = "fordeltPrAar")
    private Beløp fordeltPrAar;

    @Valid
    @JsonProperty(value = "belopFraInntektsmeldingPrAar")
    private Beløp belopFraInntektsmeldingPrAar;

    @Valid
    @JsonProperty(value = "refusjonskravFraInntektsmeldingPrAar")
    private Beløp refusjonskravFraInntektsmeldingPrAar;

    @Valid
    @JsonProperty(value = "nyttArbeidsforhold")
    private boolean nyttArbeidsforhold;

    @Valid
    @JsonProperty(value = "arbeidsforholdType")
    @NotNull
    private OpptjeningAktivitetType arbeidsforholdType;

    public FordelBeregningsgrunnlagAndelDto() {
        super();
        // For deserialisering av json
    }

    public FordelBeregningsgrunnlagAndelDto(FaktaOmBeregningAndelDto superDto) {
        super(superDto.getAndelsnr(), superDto.getArbeidsforhold(), superDto.getInntektskategori(),
            superDto.getAktivitetStatus(), superDto.getLagtTilAvSaksbehandler(), superDto.getFastsattAvSaksbehandler(), superDto.getAndelIArbeid(), superDto.getKilde());
    }

    public void setBelopFraInntektsmelding(Beløp belopFraInntektsmelding) {
        this.belopFraInntektsmeldingPrAar = belopFraInntektsmelding == null || belopFraInntektsmelding.verdi() == null ? null :
                Beløp.fra(BigDecimal.valueOf(MÅNEDER_I_1_ÅR).multiply(belopFraInntektsmelding.verdi()).setScale(0, RoundingMode.HALF_UP));
    }

    public void setFordelingForrigeBehandling(Beløp fordelingForrigeBehandling) {
        this.fordelingForrigeBehandlingPrAar = fordelingForrigeBehandling == null || fordelingForrigeBehandling.verdi() == null ? null :
                Beløp.fra(BigDecimal.valueOf(MÅNEDER_I_1_ÅR).multiply(fordelingForrigeBehandling.verdi()).setScale(0, RoundingMode.HALF_UP));
    }

    public void setRefusjonskravPrAar(Beløp refusjonskravPrAar) {
        this.refusjonskravPrAar = refusjonskravPrAar == null || refusjonskravPrAar.verdi() == null ?
                null : Beløp.fra(refusjonskravPrAar.verdi().setScale(0, RoundingMode.HALF_UP));
    }


    public void setRefusjonskravFraInntektsmeldingPrÅr(Beløp refusjonskravFraInntektsmelding) {
        this.refusjonskravFraInntektsmeldingPrAar = refusjonskravFraInntektsmelding == null || refusjonskravFraInntektsmelding.verdi() == null?
            null : Beløp.fra(refusjonskravFraInntektsmelding.verdi().setScale(0, RoundingMode.HALF_UP));

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

    public Beløp getFordelingForrigeBehandlingPrAar() {
        return fordelingForrigeBehandlingPrAar;
    }

    public Beløp getRefusjonskravPrAar() {
        return refusjonskravPrAar;
    }

    public Beløp getBelopFraInntektsmeldingPrAar() {
        return belopFraInntektsmeldingPrAar;
    }

    public Beløp getRefusjonskravFraInntektsmeldingPrAar() {
        return refusjonskravFraInntektsmeldingPrAar;
    }

    public Beløp getFordeltPrAar() {
        return fordeltPrAar;
    }

    public void setFordeltPrAar(Beløp fordeltPrAar) {
        this.fordeltPrAar = fordeltPrAar;
    }
}
