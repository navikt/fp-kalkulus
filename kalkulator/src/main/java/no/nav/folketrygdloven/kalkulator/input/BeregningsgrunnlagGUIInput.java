package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdReferanseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class BeregningsgrunnlagGUIInput {

    /** Aktiviteter for graderign av uttak. */
    private final AktivitetGradering aktivitetGradering;

    /** Data som referer behandlingen beregningsgrunnlag inngår i. */
    private KoblingReferanse koblingReferanse;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag;

    /** Grunnlag fra fordelsteget. Brukes i visning av automatisk fordeling og utledning av andeler som skal redigeres */
    private BeregningsgrunnlagGrunnlagDto fordelBeregningsgrunnlagGrunnlag;

    /** Grunnlag fra vurderrefusjonsteget. Brukes i visning av aksjonspunkt og andeler som kan redigeres. */
    private BeregningsgrunnlagGrunnlagDto vurderRefusjonBeregningsgrunnlagGrunnlag;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen i original behandling. Settes på av modulen. */
    private BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagFraForrigeBehandling;

    /** Datoer for innsending og oppstart av refusjon for alle arbeidsgivere og alle behandlinger på fagsaken */
    private List<RefusjonskravDatoDto> refusjonskravDatoer;

    /** Grunnlag som skal brukes for preutfylling i fakta om beregning skjermbildet */
    private BeregningsgrunnlagGrunnlagDto faktaOmBeregningBeregningsgrunnlagGrunnlag;

    /** IAY grunnlag benyttet av beregningsgrunnlag. Merk kan bli modifisert av innhenting av inntekter for beregning, sammenligning. */
    private InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    private final YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag;

    public BeregningsgrunnlagGUIInput(KoblingReferanse koblingReferanse,
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

    private BeregningsgrunnlagGUIInput(BeregningsgrunnlagGUIInput input) {
        this(input.getKoblingReferanse(), input.getIayGrunnlag(), input.getAktivitetGradering(), input.getRefusjonskravDatoer(), input.getYtelsespesifiktGrunnlag());
        this.beregningsgrunnlagGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        this.fordelBeregningsgrunnlagGrunnlag = input.fordelBeregningsgrunnlagGrunnlag;
        this.faktaOmBeregningBeregningsgrunnlagGrunnlag = input.faktaOmBeregningBeregningsgrunnlagGrunnlag;
        this.beregningsgrunnlagGrunnlagFraForrigeBehandling = input.beregningsgrunnlagGrunnlagFraForrigeBehandling;
        this.vurderRefusjonBeregningsgrunnlagGrunnlag = input.vurderRefusjonBeregningsgrunnlagGrunnlag;

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
        return new InntektsmeldingFilter(iayGrunnlag).hentInntektsmeldingerBeregning(skjæringstidspunktOpptjening, getFagsakYtelseType());
    }

    public Optional<FaktaAggregatDto> getFaktaAggregat() {
        return getBeregningsgrunnlagGrunnlag().getFaktaAggregat();
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

    public Optional<BeregningsgrunnlagGrunnlagDto> getFaktaOmBeregningBeregningsgrunnlagGrunnlag() {
        return Optional.ofNullable(faktaOmBeregningBeregningsgrunnlagGrunnlag);
    }

    public Optional<BeregningsgrunnlagGrunnlagDto> getVurderRefusjonBeregningsgrunnlagGrunnlag() {
        return Optional.ofNullable(vurderRefusjonBeregningsgrunnlagGrunnlag);
    }

    /** Sjekk fagsakytelsetype før denne kalles. */
    @SuppressWarnings("unchecked")
    public <V extends YtelsespesifiktGrunnlag> V getYtelsespesifiktGrunnlag() {
        return (V) ytelsespesifiktGrunnlag;
    }

    /**
     * Oppdaterer iaygrunnlag med informasjon om arbeidsgiver og referanser for visning
     *
     * @param arbeidsgiverOpplysninger arbeidsgiveropplysninger (fra abakus og ereg)
     * @param referanser Referanser
     */
    public void oppdaterArbeidsgiverinformasjon(List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger, Set<ArbeidsforholdReferanseDto> referanser) {
        InntektArbeidYtelseGrunnlagDtoBuilder oppdatere = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(iayGrunnlag);
        arbeidsgiverOpplysninger.forEach(oppdatere::leggTilArbeidsgiverOpplysninger);
        ArbeidsforholdInformasjonDtoBuilder arbeidsforholdInformasjonDtoBuilder = ArbeidsforholdInformasjonDtoBuilder.builder(Optional.ofNullable(oppdatere.getInformasjon()));
        referanser.forEach(arbeidsforholdInformasjonDtoBuilder::leggTilNyReferanse);
        oppdatere.medInformasjon(arbeidsforholdInformasjonDtoBuilder.build());
        this.iayGrunnlag = oppdatere.build();
    }

    public BeregningsgrunnlagGUIInput medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new BeregningsgrunnlagGUIInput(this);
        newInput.beregningsgrunnlagGrunnlag = grunnlag;
        newInput = grunnlag.getBeregningsgrunnlag()
            .map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
            .map(newInput::medSkjæringstidspunktForBeregning)
            .orElse(newInput);
        return newInput;
    }

    public BeregningsgrunnlagGUIInput medBeregningsgrunnlagGrunnlagFraForrigeBehandling(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new BeregningsgrunnlagGUIInput(this);
        newInput.beregningsgrunnlagGrunnlagFraForrigeBehandling = grunnlag;
        return newInput;
    }

    // Brukes i FP-SAK
    public BeregningsgrunnlagGUIInput medBeregningsgrunnlagGrunnlagFraFaktaOmBeregning(BeregningsgrunnlagGrunnlagDto grunnlag) {
        if (!(grunnlag.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.KOFAKBER_UT)
                || grunnlag.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER))) {
            throw new IllegalArgumentException("Grunnlaget er ikke fra fakta om beregning.");
        }
        var newInput = new BeregningsgrunnlagGUIInput(this);
        newInput.faktaOmBeregningBeregningsgrunnlagGrunnlag = grunnlag;
        return newInput;
    }

    public BeregningsgrunnlagGUIInput medBeregningsgrunnlagGrunnlagFraFordel(BeregningsgrunnlagGrunnlagDto grunnlag) {
        if (!grunnlag.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING)) {
            throw new IllegalArgumentException("Grunnlaget er ikke fra fordel.");
        }
        var newInput = new BeregningsgrunnlagGUIInput(this);
        newInput.fordelBeregningsgrunnlagGrunnlag = grunnlag;
        return newInput;
    }

    // Brukes i FP-SAK
    public BeregningsgrunnlagGUIInput medBeregningsgrunnlagGrunnlagFraVurderRefusjon(BeregningsgrunnlagGrunnlagDto grunnlag) {
        if (!grunnlag.getBeregningsgrunnlagTilstand().equals(BeregningsgrunnlagTilstand.VURDERT_REFUSJON)) {
            throw new IllegalArgumentException("Grunnlaget er ikke fra vurderRefusjon.");
        }
        var newInput = new BeregningsgrunnlagGUIInput(this);
        newInput.vurderRefusjonBeregningsgrunnlagGrunnlag = grunnlag;
        return newInput;
    }

    private BeregningsgrunnlagGUIInput medSkjæringstidspunktForBeregning(LocalDate skjæringstidspunkt) {
        var newInput = new BeregningsgrunnlagGUIInput(this);
        var nyttSkjæringstidspunkt = Skjæringstidspunkt.builder(this.koblingReferanse.getSkjæringstidspunkt()).medSkjæringstidspunktBeregning(skjæringstidspunkt).build();
        newInput.koblingReferanse = this.koblingReferanse.medSkjæringstidspunkt(nyttSkjæringstidspunkt);
        return newInput;
    }

}
