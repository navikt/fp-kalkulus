package no.nav.folketrygdloven.kalkulus.felles.v1;


import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GrunnbeløpDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class KalkulatorInputDto {

    @JsonProperty(value = "aktivitetGradering")
    @Valid
    private AktivitetGraderingDto aktivitetGradering;

    @JsonProperty(value = "refusjonskravDatoer")
    @Valid
    private List<RefusjonskravDatoDto> refusjonskravDatoer;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotEmpty
    @Valid
    private List<GrunnbeløpDto> grunnbeløpsatser;

    @JsonProperty(value = "iayGrunnlag", required = true)
    @NotNull
    @Valid
    private InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    @JsonProperty(value = "opptjeningAktiviteter", required = true)
    @NotNull
    @Valid
    private OpptjeningAktiviteterDto opptjeningAktiviteter;

    @JsonProperty(value = "ytelsespesifiktGrunnlag")
    @Valid
    private YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag;

    @JsonProperty(value = "skjæringstidspunkt")
    @Valid
    @NotNull
    private LocalDate skjæringstidspunkt;


    protected KalkulatorInputDto() {
        // default ctor
    }

    public KalkulatorInputDto(@NotEmpty @Valid List<GrunnbeløpDto> grunnbeløpsatser,
                              @NotNull @Valid InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                              @NotNull @Valid OpptjeningAktiviteterDto opptjeningAktiviteter,
                              @NotNull @Valid LocalDate skjæringstidspunkt) {
        this.grunnbeløpsatser = grunnbeløpsatser;
        this.iayGrunnlag = iayGrunnlag;
        this.opptjeningAktiviteter = opptjeningAktiviteter;
        this.skjæringstidspunkt = skjæringstidspunkt;
    }

    public AktivitetGraderingDto getAktivitetGradering() {
        return aktivitetGradering;
    }

    public List<RefusjonskravDatoDto> getRefusjonskravDatoer() {
        return refusjonskravDatoer;
    }

    public List<GrunnbeløpDto> getGrunnbeløpsatser() {
        return grunnbeløpsatser;
    }

    public InntektArbeidYtelseGrunnlagDto getIayGrunnlag() {
        return iayGrunnlag;
    }

    public OpptjeningAktiviteterDto getOpptjeningAktiviteter() {
        return opptjeningAktiviteter;
    }

    public YtelsespesifiktGrunnlagDto getYtelsespesifiktGrunnlag() {
        return ytelsespesifiktGrunnlag;
    }

    public KalkulatorInputDto medRefusjonskravDatoer(List<RefusjonskravDatoDto> refusjonskravDatoer) {
        this.refusjonskravDatoer = refusjonskravDatoer;
        return this;
    }

    public KalkulatorInputDto medAktivitetGradering(AktivitetGraderingDto aktivitetGradering) {
        this.aktivitetGradering = aktivitetGradering;
        return this;
    }

    public KalkulatorInputDto medYtelsespesifiktGrunnlag(YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag) {
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
        return this;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }
}
