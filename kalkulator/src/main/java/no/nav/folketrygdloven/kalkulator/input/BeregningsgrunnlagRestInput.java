package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdReferanseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class BeregningsgrunnlagRestInput {

    /** Aktiviteter for graderign av uttak. */
    private final AktivitetGradering aktivitetGradering;

    /** Data som referer behandlingen beregningsgrunnlag inngår i. */
    private KoblingReferanse koblingReferanse;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag;

    /** Grunnlag fra fordelsteget. Brukes i visning av automatisk fordeling og utledning av andeler som skal redigeres */
    private BeregningsgrunnlagGrunnlagDto fordelBeregningsgrunnlagGrunnlag;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen i original behandling. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagFraForrigeBehandling;

    /** Datoer for innsending og oppstart av refusjon for alle arbeidsgivere og alle behandlinger på fagsaken */
    private List<RefusjonskravDatoDto> refusjonskravDatoer = new ArrayList<>();

    /** Grunnlag som skal brukes for preutfylling i fakta om beregning skjermbildet */
    private BeregningsgrunnlagGrunnlagDto faktaOmBeregningPreutfyllingsgrunnlag;

    /** IAY grunnlag benyttet av beregningsgrunnlag. Merk kan bli modifisert av innhenting av inntekter for beregning, sammenligning. */
    private final InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    private final YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag;

    public BeregningsgrunnlagRestInput(BeregningsgrunnlagInput input, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger, Set<ArbeidsforholdReferanseDto> referanser) {
        this.koblingReferanse = input.getKoblingReferanse();
        InntektArbeidYtelseGrunnlagDtoBuilder oppdatere = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(input.getIayGrunnlag());
        arbeidsgiverOpplysninger.forEach(oppdatere::leggTilArbeidsgiverOpplysninger);
        ArbeidsforholdInformasjonDtoBuilder arbeidsforholdInformasjonDtoBuilder = ArbeidsforholdInformasjonDtoBuilder.builder(Optional.ofNullable(oppdatere.getInformasjon()));
        referanser.forEach(arbeidsforholdInformasjonDtoBuilder::leggTilNyReferanse);
        oppdatere.medInformasjon(arbeidsforholdInformasjonDtoBuilder.build());
        this.iayGrunnlag = oppdatere.build();
        this.aktivitetGradering = input.getAktivitetGradering();
        this.refusjonskravDatoer = input.getRefusjonskravDatoer();
        this.ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        this.beregningsgrunnlagGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        this.fordelBeregningsgrunnlagGrunnlag = input.getTilstandHistorikk().get(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        this.beregningsgrunnlagGrunnlagFraForrigeBehandling = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().orElse(null);
    }

    public BeregningsgrunnlagRestInput(KoblingReferanse koblingReferanse,
                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                       AktivitetGradering aktivitetGradering,
                                       List<RefusjonskravDatoDto> refusjonskravDatoer,
                                       YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        this.koblingReferanse = Objects.requireNonNull(koblingReferanse, "behandlingReferanse");
        this.iayGrunnlag = iayGrunnlag;
        this.aktivitetGradering = aktivitetGradering;
        this.refusjonskravDatoer = refusjonskravDatoer;
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
    }

    public BeregningsgrunnlagRestInput(KoblingReferanse koblingReferanse,
                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                       AktivitetGradering aktivitetGradering,
                                       List<RefusjonskravDatoDto> refusjonskravDatoer,
                                       YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                       BeregningsgrunnlagGrunnlagDto faktaOmBeregningPreutfyllingsgrunnlag) {
        this.koblingReferanse = Objects.requireNonNull(koblingReferanse, "behandlingReferanse");
        this.iayGrunnlag = iayGrunnlag;
        this.aktivitetGradering = aktivitetGradering;
        this.refusjonskravDatoer = refusjonskravDatoer;
        this.ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlag;
        this.faktaOmBeregningPreutfyllingsgrunnlag = faktaOmBeregningPreutfyllingsgrunnlag;
    }
    private BeregningsgrunnlagRestInput(BeregningsgrunnlagRestInput input) {
        this(input.getKoblingReferanse(), input.getIayGrunnlag(), input.getAktivitetGradering(), input.getRefusjonskravDatoer(), input.getYtelsespesifiktGrunnlag());
        this.beregningsgrunnlagGrunnlag = input.getBeregningsgrunnlagGrunnlag();
    }

    public Optional<BeregningsgrunnlagDto> getFordelBeregningsgrunnlag() {
        if (fordelBeregningsgrunnlagGrunnlag == null) {
            return Optional.empty();
        }
        return fordelBeregningsgrunnlagGrunnlag.getBeregningsgrunnlag();
    }

    public AktivitetGradering getAktivitetGradering() {
        return aktivitetGradering == null ? AktivitetGradering.INGEN_GRADERING : aktivitetGradering;
    }

    public AktørId getAktørId() {
        return koblingReferanse.getAktørId();
    }

    public KoblingReferanse getKoblingReferanse() {
        return koblingReferanse;
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
        return koblingReferanse.getFagsakYtelseType();
    }

    public InntektArbeidYtelseGrunnlagDto getIayGrunnlag() {
        return iayGrunnlag;
    }

    public Collection<InntektsmeldingDto> getInntektsmeldinger() {
        LocalDate skjæringstidspunktOpptjening = getSkjæringstidspunktOpptjening();
        if(skjæringstidspunktOpptjening == null) return Collections.emptyList();
        return new InntektsmeldingFilter(iayGrunnlag).hentInntektsmeldingerBeregning(getKoblingReferanse(), skjæringstidspunktOpptjening);
    }


    public Skjæringstidspunkt getSkjæringstidspunkt() {
        return koblingReferanse.getSkjæringstidspunkt();
    }

    public LocalDate getSkjæringstidspunktForBeregning() {
        return koblingReferanse.getSkjæringstidspunktBeregning();
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
        return newInput;
    }

    public BeregningsgrunnlagRestInput medBeregningsgrunnlagGrunnlagFraFordel(BeregningsgrunnlagGrunnlagDto grunnlag) {
        if (!grunnlag.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING)) {
            throw new IllegalArgumentException("Grunnlaget er ikke fra fordel.");
        }
        var newInput = new BeregningsgrunnlagRestInput(this);
        newInput.fordelBeregningsgrunnlagGrunnlag = grunnlag;
        return newInput;
    }

    private BeregningsgrunnlagRestInput medSkjæringstidspunktForBeregning(LocalDate skjæringstidspunkt) {
        var newInput = new BeregningsgrunnlagRestInput(this);
        var nyttSkjæringstidspunkt = Skjæringstidspunkt.builder(this.koblingReferanse.getSkjæringstidspunkt()).medSkjæringstidspunktBeregning(skjæringstidspunkt).build();
        newInput.koblingReferanse = this.koblingReferanse.medSkjæringstidspunkt(nyttSkjæringstidspunkt);
        return newInput;
    }

}
