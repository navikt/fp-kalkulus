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
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class BeregningsgrunnlagRestInput {

    /** Aktiviteter for graderign av uttak. */
    private final AktivitetGradering aktivitetGradering;

    /** Data som referer behandlingen beregningsgrunnlag inngår i. */
    private BehandlingReferanse behandlingReferanse;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagRestDto beregningsgrunnlagGrunnlag;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen i original behandling. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagRestDto beregningsgrunnlagGrunnlagFraForrigeBehandling;

    /** Datoer for innsending og oppstart av refusjon for alle arbeidsgivere og alle behandlinger på fagsaken */
    private List<RefusjonskravDatoDto> refusjonskravDatoer = new ArrayList<>();

    private Map<BeregningsgrunnlagTilstand, BeregningsgrunnlagGrunnlagRestDto> tilstandHistorikk = new HashMap<>();

    public Map<BeregningsgrunnlagTilstand, BeregningsgrunnlagGrunnlagRestDto> getTilstandHistorikk() {
        return tilstandHistorikk;
    }

    public void setTilstandHistorikk(Map<BeregningsgrunnlagTilstand, BeregningsgrunnlagGrunnlagRestDto> tilstandHistorikk) {
        this.tilstandHistorikk = tilstandHistorikk;
    }

    public void leggTilBeregningsgrunnlagIHistorikk(BeregningsgrunnlagGrunnlagRestDto grunnlag, BeregningsgrunnlagTilstand tilstand) {
        this.tilstandHistorikk.put(tilstand, grunnlag);
    }

    public Optional<BeregningsgrunnlagRestDto> hentForrigeBeregningsgrunnlag(BeregningsgrunnlagTilstand tilstand) {
        return Optional.ofNullable(tilstandHistorikk.get(tilstand)).flatMap(BeregningsgrunnlagGrunnlagRestDto::getBeregningsgrunnlag);
    }

    public Optional<BeregningsgrunnlagGrunnlagRestDto> hentForrigeBeregningsgrunnlagGrunnlag(BeregningsgrunnlagTilstand tilstand) {
        return Optional.ofNullable(tilstandHistorikk.get(tilstand));
    }

    /** IAY grunnlag benyttet av beregningsgrunnlag. Merk kan bli modifisert av innhenting av inntekter for beregning, sammenligning. */
    private final InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    private final YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag;

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

    public BeregningsgrunnlagGrunnlagRestDto getBeregningsgrunnlagGrunnlag() {
        return beregningsgrunnlagGrunnlag;
    }

    public BeregningsgrunnlagRestDto getBeregningsgrunnlag() {
        return beregningsgrunnlagGrunnlag == null ? null : beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElseThrow();
    }

    public Optional<BeregningsgrunnlagGrunnlagRestDto> getBeregningsgrunnlagGrunnlagFraForrigeBehandling() {
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


    /** Sjekk fagsakytelsetype før denne kalles. */
    @SuppressWarnings("unchecked")
    public <V extends YtelsespesifiktGrunnlag> V getYtelsespesifiktGrunnlag() {
        return (V) ytelsespesifiktGrunnlag;
    }

    public BeregningsgrunnlagRestInput medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagRestDto grunnlag) {
        var newInput = new BeregningsgrunnlagRestInput(this);
        newInput.beregningsgrunnlagGrunnlag = grunnlag;
        newInput = grunnlag.getBeregningsgrunnlag()
            .map(BeregningsgrunnlagRestDto::getSkjæringstidspunkt)
            .map(newInput::medSkjæringstidspunktForBeregning)
            .orElse(newInput);
        return newInput;
    }

    public BeregningsgrunnlagRestInput medBeregningsgrunnlagGrunnlagFraForrigeBehandling(BeregningsgrunnlagGrunnlagRestDto grunnlag) {
        var newInput = new BeregningsgrunnlagRestInput(this);
        newInput.beregningsgrunnlagGrunnlagFraForrigeBehandling = grunnlag;
        newInput = grunnlag.getBeregningsgrunnlag()
            .map(BeregningsgrunnlagRestDto::getSkjæringstidspunkt)
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
