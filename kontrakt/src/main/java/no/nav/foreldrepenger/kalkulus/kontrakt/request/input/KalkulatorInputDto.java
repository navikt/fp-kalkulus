package no.nav.foreldrepenger.kalkulus.kontrakt.request.input;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.opptjening.OpptjeningAktiviteterDto;

/**
 * NB: Denne blir lagret som json struktur i db for senere sammenligning. Endringer her kan derfor påvirke hvordan det blir lagret, og hva
 * som oppfattes som endret input senere.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class KalkulatorInputDto {

    /**
     * Aggregat for inntekt, arbeid og ytelser
     */
    @JsonProperty(value = "iayGrunnlag", required = true)
    @NotNull
    @Valid
    private InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    /**
     * Aktiviteter som skal brukes i utledning av aktivitetstatus på skjæringstidspunktet
     */
    @JsonProperty(value = "opptjeningAktiviteter", required = true)
    @NotNull
    @Valid
    private OpptjeningAktiviteterDto opptjeningAktiviteter;

    /**
     * Ytelsesspesifikt grunnlag
     */
    @JsonProperty(value = "ytelsespesifiktGrunnlag")
    @Valid
    private YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag;

    /**
     * Skjæringstidspunkt for ytelse
     */
    @JsonProperty(value = "skjæringstidspunkt")
    @Valid
    @NotNull
    private LocalDate skjæringstidspunkt;

    protected KalkulatorInputDto() {
        // default ctor
    }

    public KalkulatorInputDto(@NotNull @Valid InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                              @NotNull @Valid OpptjeningAktiviteterDto opptjeningAktiviteter,
                              @NotNull @Valid LocalDate skjæringstidspunkt,
                              @NotNull @Valid YtelsespesifiktGrunnlagDto ytelsespesifiktGrunnlag) {
        this.iayGrunnlag = iayGrunnlag;
        this.opptjeningAktiviteter = opptjeningAktiviteter;
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
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

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }
}
