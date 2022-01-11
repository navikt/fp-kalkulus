package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.Sammenligningsgrunnlag;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagDto {

    @JsonProperty(value = "skjæringstidspunkt")
    @NotNull
    @Valid
    private LocalDate skjæringstidspunkt;

    @JsonProperty(value = "aktivitetStatuser")
    @NotNull
    @Size(min = 1, max = 20)
    @Valid
    private List<AktivitetStatus> aktivitetStatuser;

    @JsonProperty(value = "beregningsgrunnlagPerioder")
    @NotNull
    @Size(min = 1, max = 100)
    @Valid
    private List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder;

    @JsonProperty(value = "sammenligningsgrunnlag")
    @Valid
    private Sammenligningsgrunnlag sammenligningsgrunnlag;

    @JsonProperty(value = "grunnbeløp")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal grunnbeløp;

    public BeregningsgrunnlagDto() {
    }

    public BeregningsgrunnlagDto(LocalDate skjæringstidspunkt,
                                 List<AktivitetStatus> aktivitetStatuser,
                                 List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder,
                                 Sammenligningsgrunnlag sammenligningsgrunnlag, BigDecimal grunnbeløp) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.aktivitetStatuser = aktivitetStatuser;
        this.beregningsgrunnlagPerioder = beregningsgrunnlagPerioder;
        this.sammenligningsgrunnlag = sammenligningsgrunnlag;
        this.grunnbeløp = grunnbeløp;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<AktivitetStatus> getAktivitetStatuser() {
        return Collections.unmodifiableList(aktivitetStatuser);
    }

    public List<BeregningsgrunnlagPeriodeDto> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder
                .stream()
                .sorted(Comparator.comparing(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom)).toList();
    }

    public Sammenligningsgrunnlag getSammenligningsgrunnlag() {
        return sammenligningsgrunnlag;
    }

    public BigDecimal getGrunnbeløp() {
        return grunnbeløp;
    }
}
