package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BGAndelArbeidsforhold {

    @JsonProperty(value = "arbeidsgiver")
    @NotNull
    @Valid
    private Arbeidsgiver arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private String arbeidsforholdRef;

    @JsonProperty(value = "refusjonskravPrÅr")
    @Valid
    private BigDecimal refusjonskravPrÅr;

    @JsonProperty(value = "naturalytelseBortfaltPrÅr")
    @Valid
    private BigDecimal naturalytelseBortfaltPrÅr;

    @JsonProperty(value = "naturalytelseTilkommetPrÅr")
    @Valid
    private BigDecimal naturalytelseTilkommetPrÅr;

    @JsonProperty(value = "arbeidsperiodeFom")
    @NotNull
    @Valid
    private LocalDate arbeidsperiodeFom;

    @JsonProperty(value = "arbeidsperiodeTom")
    @NotNull
    @Valid
    private LocalDate arbeidsperiodeTom;

    public BGAndelArbeidsforhold(@NotNull @Valid Arbeidsgiver arbeidsgiver, @Valid String arbeidsforholdRef, @Valid BigDecimal refusjonskravPrÅr, @Valid BigDecimal naturalytelseBortfaltPrÅr, @Valid BigDecimal naturalytelseTilkommetPrÅr, @NotNull @Valid LocalDate arbeidsperiodeFom, @NotNull @Valid LocalDate arbeidsperiodeTom) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.refusjonskravPrÅr = refusjonskravPrÅr;
        this.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
        this.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
        this.arbeidsperiodeFom = arbeidsperiodeFom;
        this.arbeidsperiodeTom = arbeidsperiodeTom;
    }

    public String getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public BigDecimal getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
    }

    public Optional<BigDecimal> getNaturalytelseBortfaltPrÅr() {
        return Optional.ofNullable(naturalytelseBortfaltPrÅr);
    }

    public Optional<BigDecimal> getNaturalytelseTilkommetPrÅr() {
        return Optional.ofNullable(naturalytelseTilkommetPrÅr);
    }

    public LocalDate getArbeidsperiodeFom() {
        return arbeidsperiodeFom;
    }

    public Optional<LocalDate> getArbeidsperiodeTom() {
        return Optional.ofNullable(arbeidsperiodeTom);
    }

    public String getArbeidsforholdOrgnr() {
        return getArbeidsgiver().getArbeidsgiverOrgnr();
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

}
