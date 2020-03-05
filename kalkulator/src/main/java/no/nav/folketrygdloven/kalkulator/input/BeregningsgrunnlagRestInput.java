package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class BeregningsgrunnlagRestInput {

    /** Aktiviteter for graderign av uttak. */
    private final AktivitetGradering aktivitetGradering;

    /** Data som referer behandlingen beregningsgrunnlag inngår i. */
    private BehandlingReferanse behandlingReferanse;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen i original behandling. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagFraForrigeBehandling;

    /** Datoer for innsending og oppstart av refusjon for alle arbeidsgivere og alle behandlinger på fagsaken */
    private List<RefusjonskravDatoDto> refusjonskravDatoer = new ArrayList<>();

    /** Grunnlag som skal brukes for preutfylling i fakta om beregning skjermbildet */
    private BeregningsgrunnlagGrunnlagDto faktaOmBeregningPreutfyllingsgrunnlag;

    private Map<BeregningsgrunnlagTilstand, BeregningsgrunnlagGrunnlagDto> tilstandHistorikk = new HashMap<>();

    public void setTilstandHistorikk(Map<BeregningsgrunnlagTilstand, BeregningsgrunnlagGrunnlagDto> tilstandHistorikk) {
        this.tilstandHistorikk = tilstandHistorikk;
    }

    public void leggTilBeregningsgrunnlagIHistorikk(BeregningsgrunnlagGrunnlagDto grunnlag, BeregningsgrunnlagTilstand tilstand) {
        this.tilstandHistorikk.put(tilstand, grunnlag);
    }


    /** IAY grunnlag benyttet av beregningsgrunnlag. Merk kan bli modifisert av innhenting av inntekter for beregning, sammenligning. */
    private final InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    private final YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag;

    public BeregningsgrunnlagRestInput(BeregningsgrunnlagInput input, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        this.behandlingReferanse = input.getBehandlingReferanse();
        InntektArbeidYtelseGrunnlagDtoBuilder oppdatere = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(input.getIayGrunnlag());
        arbeidsgiverOpplysninger.forEach(oppdatere::leggTilArbeidsgiverOpplysninger);
        this.iayGrunnlag = oppdatere.build();
        this.aktivitetGradering = input.getAktivitetGradering();
        this.refusjonskravDatoer = input.getRefusjonskravDatoer();
        this.ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        this.beregningsgrunnlagGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        this.beregningsgrunnlagGrunnlagFraForrigeBehandling = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().orElse(null);
    }

    public BeregningsgrunnlagRestInput(BehandlingReferanse behandlingReferanse,
                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                       AktivitetGradering aktivitetGradering,
                                       List<RefusjonskravDatoDto> refusjonskravDatoer,
                                       YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        this.behandlingReferanse = Objects.requireNonNull(behandlingReferanse, "behandlingReferanse");
        this.iayGrunnlag = iayGrunnlag;
        this.aktivitetGradering = aktivitetGradering;
        this.refusjonskravDatoer = refusjonskravDatoer;
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
    }

    public BeregningsgrunnlagRestInput(BehandlingReferanse behandlingReferanse,
                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                       AktivitetGradering aktivitetGradering,
                                       List<RefusjonskravDatoDto> refusjonskravDatoer,
                                       YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                       BeregningsgrunnlagGrunnlagDto faktaOmBeregningPreutfyllingsgrunnlag) {
        this.behandlingReferanse = Objects.requireNonNull(behandlingReferanse, "behandlingReferanse");
        this.iayGrunnlag = iayGrunnlag;
        this.aktivitetGradering = aktivitetGradering;
        this.refusjonskravDatoer = refusjonskravDatoer;
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
        this.faktaOmBeregningPreutfyllingsgrunnlag = faktaOmBeregningPreutfyllingsgrunnlag;
    }
    private BeregningsgrunnlagRestInput(BeregningsgrunnlagRestInput input) {
        this(input.getBehandlingReferanse(), input.getIayGrunnlag(), input.getAktivitetGradering(), input.getRefusjonskravDatoer(), input.getYtelsespesifiktGrunnlag());
        this.beregningsgrunnlagGrunnlag = input.getBeregningsgrunnlagGrunnlag();
    }

    public AktivitetGradering getAktivitetGradering() {
        return aktivitetGradering;
    }

    public AktørId getAktørId() {
        return behandlingReferanse.getAktørId();
    }

    public BehandlingReferanse getBehandlingReferanse() {
        return behandlingReferanse;
    }

    public BeregningsgrunnlagGrunnlagDto getBeregningsgrunnlagGrunnlag() {
        return beregningsgrunnlagGrunnlag;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlagGrunnlag == null ? null : beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElseThrow();
    }

    public Optional<BeregningsgrunnlagGrunnlagDto> getBeregningsgrunnlagGrunnlagFraForrigeBehandling() {
        return Optional.ofNullable(beregningsgrunnlagGrunnlagFraForrigeBehandling);
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return behandlingReferanse.getFagsakYtelseType();
    }

    public InntektArbeidYtelseGrunnlagDto getIayGrunnlag() {
        return iayGrunnlag;
    }

    public Collection<InntektsmeldingDto> getInntektsmeldinger() {
        LocalDate skjæringstidspunktOpptjening = getSkjæringstidspunktOpptjening();
        if(skjæringstidspunktOpptjening == null) return Collections.emptyList();
        return new InntektsmeldingFilter(iayGrunnlag).hentInntektsmeldingerBeregning(getBehandlingReferanse(), skjæringstidspunktOpptjening);
    }


    public Skjæringstidspunkt getSkjæringstidspunkt() {
        return behandlingReferanse.getSkjæringstidspunkt();
    }

    public LocalDate getSkjæringstidspunktForBeregning() {
        return behandlingReferanse.getSkjæringstidspunktBeregning();
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return getSkjæringstidspunkt().getSkjæringstidspunktOpptjening();
    }

    public List<RefusjonskravDatoDto> getRefusjonskravDatoer() {
        return refusjonskravDatoer;
    }

    public Optional<BeregningsgrunnlagGrunnlagDto> getFaktaOmBeregningPreutfyllingsgrunnlag() {
        return Optional.ofNullable(faktaOmBeregningPreutfyllingsgrunnlag);
    }

    /** Sjekk fagsakytelsetype før denne kalles. */
    @SuppressWarnings("unchecked")
    public <V extends YtelsespesifiktGrunnlag> V getYtelsespesifiktGrunnlag() {
        return (V) ytelsespesifiktGrunnlag;
    }

    public BeregningsgrunnlagRestInput medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new BeregningsgrunnlagRestInput(this);
        newInput.beregningsgrunnlagGrunnlag = grunnlag;
        newInput = grunnlag.getBeregningsgrunnlag()
            .map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
            .map(newInput::medSkjæringstidspunktForBeregning)
            .orElse(newInput);
        return newInput;
    }

    public BeregningsgrunnlagRestInput medBeregningsgrunnlagGrunnlagFraForrigeBehandling(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new BeregningsgrunnlagRestInput(this);
        newInput.beregningsgrunnlagGrunnlagFraForrigeBehandling = grunnlag;
        newInput = grunnlag.getBeregningsgrunnlag()
            .map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
            .map(newInput::medSkjæringstidspunktForBeregning)
            .orElse(newInput);
        return newInput;
    }

    private BeregningsgrunnlagRestInput medSkjæringstidspunktForBeregning(LocalDate skjæringstidspunkt) {
        var newInput = new BeregningsgrunnlagRestInput(this);
        var nyttSkjæringstidspunkt = Skjæringstidspunkt.builder(this.behandlingReferanse.getSkjæringstidspunkt()).medSkjæringstidspunktBeregning(skjæringstidspunkt).build();
        newInput.behandlingReferanse = this.behandlingReferanse.medSkjæringstidspunkt(nyttSkjæringstidspunkt);
        return newInput;
    }

}
