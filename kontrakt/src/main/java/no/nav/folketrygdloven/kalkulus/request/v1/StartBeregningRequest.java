package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GrunnbeløpDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;


/**
 * Spesifikasjon for å starte en beregning.
 * Oppretter starter en ny beregning
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class StartBeregningRequest {

    @JsonProperty(value = "referanse", required = true)
    @Valid
    @NotNull
    private UUID koblingReferanse;

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtter ytelseSomSkalBeregnes;

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

    public StartBeregningRequest(@Valid @NotNull UUID koblingReferanse,
                                 @NotNull @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'") @Valid String saksnummer,
                                 @NotNull @Valid PersonIdent aktør, @NotNull @Valid YtelseTyperKalkulusStøtter ytelseSomSkalBeregnes,
                                 @NotEmpty @Valid List<GrunnbeløpDto> grunnbeløpsatser,
                                 @NotNull @Valid InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                 @NotNull @Valid OpptjeningAktiviteterDto opptjeningAktiviteter) {

        this.koblingReferanse = koblingReferanse;
        this.saksnummer = saksnummer;
        this.aktør = aktør;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.grunnbeløpsatser = grunnbeløpsatser;
        this.iayGrunnlag = iayGrunnlag;
        this.opptjeningAktiviteter = opptjeningAktiviteter;
    }
}
