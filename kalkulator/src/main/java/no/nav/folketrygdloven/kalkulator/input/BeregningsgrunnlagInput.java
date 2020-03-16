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
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto.OpptjeningPeriodeDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class BeregningsgrunnlagInput {

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

    private Map<BeregningsgrunnlagTilstand, BeregningsgrunnlagGrunnlagDto> tilstandHistorikk = new HashMap<>();

    /**
     * Grunnbeløpsatser
     */
    private List<Grunnbeløp> grunnbeløpsatser = new ArrayList<>();

    public Map<BeregningsgrunnlagTilstand, BeregningsgrunnlagGrunnlagDto> getTilstandHistorikk() {
        return tilstandHistorikk;
    }

    public void setTilstandHistorikk(Map<BeregningsgrunnlagTilstand, BeregningsgrunnlagGrunnlagDto> tilstandHistorikk) {
        this.tilstandHistorikk = tilstandHistorikk;
    }

    public void leggTilBeregningsgrunnlagIHistorikk(BeregningsgrunnlagGrunnlagDto grunnlag, BeregningsgrunnlagTilstand tilstand) {
        this.tilstandHistorikk.put(tilstand, grunnlag);
    }

    public Optional<BeregningsgrunnlagDto> hentForrigeBeregningsgrunnlag(BeregningsgrunnlagTilstand tilstand) {
        return Optional.ofNullable(tilstandHistorikk.get(tilstand)).flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
    }

    public Optional<BeregningsgrunnlagGrunnlagDto> hentForrigeBeregningsgrunnlagGrunnlag(BeregningsgrunnlagTilstand tilstand) {
        return Optional.ofNullable(tilstandHistorikk.get(tilstand));
    }

    /** IAY grunnlag benyttet av beregningsgrunnlag. Merk kan bli modifisert av innhenting av inntekter for beregning, sammenligning. */
    private final InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    /** Aktiviteter til grunnlag for opptjening. */
    private final OpptjeningAktiviteterDto opptjeningAktiviteter;

    private final YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag;

    private Map<String, Boolean> toggles = new HashMap<>();

    private Map<String, Object> konfigverdier = new HashMap<>();

    public BeregningsgrunnlagInput(BehandlingReferanse behandlingReferanse,
                                   InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                   OpptjeningAktiviteterDto opptjeningAktiviteter,
                                   AktivitetGradering aktivitetGradering,
                                   List<RefusjonskravDatoDto> refusjonskravDatoer,
                                   YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        this.behandlingReferanse = Objects.requireNonNull(behandlingReferanse, "behandlingReferanse");
        this.iayGrunnlag = iayGrunnlag;
        this.opptjeningAktiviteter = opptjeningAktiviteter;
        this.aktivitetGradering = aktivitetGradering;
        this.refusjonskravDatoer = refusjonskravDatoer;
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
    }

    private BeregningsgrunnlagInput(BeregningsgrunnlagInput input) {
        this(input.getBehandlingReferanse(), input.getIayGrunnlag(), input.getOpptjeningAktiviteter(), input.getAktivitetGradering(), input.getRefusjonskravDatoer(), input.getYtelsespesifiktGrunnlag());
        this.beregningsgrunnlagGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        this.grunnbeløpsatser = input.getGrunnbeløpsatser();
        this.toggles = input.getToggles();
        this.konfigverdier = input.getKonfigverdier();
    }

    public Map<String, Boolean> getToggles() {
        return toggles;
    }

    public void leggTilToggle(String feature, Boolean isEnabled) {
        toggles.put(feature, isEnabled);
    }


    public void leggTilKonfigverdi(String konfig, Object verdi) {
        konfigverdier.put(konfig, verdi);
    }

    public Map<String, Object> getKonfigverdier() {
        return konfigverdier;
    }

    public Object getKonfigVerdi(String konfig) {
        return konfigverdier.get(konfig);
    }

    public boolean isEnabled(String feature, boolean defaultValue) {
        return toggles.getOrDefault(feature, defaultValue);
    }


    public void setToggles(Map<String, Boolean> toggles) {
        this.toggles = toggles;
    }


    public AktivitetGradering getAktivitetGradering() {
        return aktivitetGradering == null ? AktivitetGradering.INGEN_GRADERING : aktivitetGradering;
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

    public OpptjeningAktiviteterDto getOpptjeningAktiviteter() {
        return opptjeningAktiviteter;
    }

    public Collection<OpptjeningPeriodeDto> getOpptjeningAktiviteterForBeregning() {
        LocalDate skjæringstidspunktOpptjening = getSkjæringstidspunktOpptjening();
        if(skjæringstidspunktOpptjening == null) return Collections.emptyList();
        var aktivitetFilter = new OpptjeningsaktiviteterPerYtelse(getFagsakYtelseType());
        var relevanteAktiviteter = opptjeningAktiviteter.getOpptjeningPerioder()
            .stream()
            .filter(p -> {
                return p.getPeriode().getFom().isBefore(skjæringstidspunktOpptjening);
            })
            .filter(p -> aktivitetFilter.erRelevantAktivitet(p.getOpptjeningAktivitetType()))
            .collect(Collectors.toList());
        return relevanteAktiviteter;
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

    public List<Grunnbeløp> getGrunnbeløpsatser() {
        return grunnbeløpsatser;
    }

    /** Sjekk fagsakytelsetype før denne kalles. */
    @SuppressWarnings("unchecked")
    public <V extends YtelsespesifiktGrunnlag> V getYtelsespesifiktGrunnlag() {
        return (V) ytelsespesifiktGrunnlag;
    }

    public BeregningsgrunnlagInput medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new BeregningsgrunnlagInput(this);
        newInput.beregningsgrunnlagGrunnlag = grunnlag;
        newInput = grunnlag.getBeregningsgrunnlag()
            .map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
            .map(newInput::medSkjæringstidspunktForBeregning)
            .orElse(newInput);
        return newInput;
    }

    public BeregningsgrunnlagInput medBeregningsgrunnlagGrunnlagFraForrigeBehandling(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new BeregningsgrunnlagInput(this);
        newInput.beregningsgrunnlagGrunnlagFraForrigeBehandling = grunnlag;
        newInput = grunnlag.getBeregningsgrunnlag()
            .map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
            .map(newInput::medSkjæringstidspunktForBeregning)
            .orElse(newInput);
        return newInput;
    }

    public BeregningsgrunnlagInput medGrunnbeløpsatser(List<Grunnbeløp> grunnbeløpsatser) {
        var newInput = new BeregningsgrunnlagInput(this);
        newInput.grunnbeløpsatser = grunnbeløpsatser;
        return newInput;
    }

    /** Overstyrer behandlingreferanse, eks for å få ny skjæringstidspunkt fra beregningsgrunnlag fra tidligere. */
    public BeregningsgrunnlagInput medBehandlingReferanse(BehandlingReferanse ref) {
        var newInput = new BeregningsgrunnlagInput(this);
        newInput.behandlingReferanse = Objects.requireNonNull(ref, "behandlingReferanse");
        return newInput;
    }

    private BeregningsgrunnlagInput medSkjæringstidspunktForBeregning(LocalDate skjæringstidspunkt) {
        var newInput = new BeregningsgrunnlagInput(this);
        var nyttSkjæringstidspunkt = Skjæringstidspunkt.builder(this.behandlingReferanse.getSkjæringstidspunkt()).medSkjæringstidspunktBeregning(skjæringstidspunkt).build();
        newInput.behandlingReferanse = this.behandlingReferanse.medSkjæringstidspunkt(nyttSkjæringstidspunkt);
        return newInput;
    }

    @Override
    public String toString() {
        return "BeregningsgrunnlagInput{" +
            "aktivitetGradering=" + aktivitetGradering +
            ", behandlingReferanse=" + behandlingReferanse +
            ", beregningsgrunnlagGrunnlag=" + beregningsgrunnlagGrunnlag +
            ", beregningsgrunnlagGrunnlagFraForrigeBehandling=" + beregningsgrunnlagGrunnlagFraForrigeBehandling +
            ", refusjonskravDatoer=" + refusjonskravDatoer +
            ", tilstandHistorikk=" + tilstandHistorikk +
            ", grunnbeløpsatser=" + grunnbeløpsatser +
            ", iayGrunnlag=" + iayGrunnlag +
            ", opptjeningAktiviteter=" + opptjeningAktiviteter +
            ", ytelsespesifiktGrunnlag=" + ytelsespesifiktGrunnlag +
            '}';
    }
}
