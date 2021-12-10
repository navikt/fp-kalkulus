package no.nav.folketrygdloven.kalkulator.steg;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat.Builder;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningResultat;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.ForeslåBesteberegning;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AvklaringsbehovUtlederFastsettBeregningsaktiviteter;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ForeslåSkjæringstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.OpprettBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.AvklaringsbehovUtlederFordelBeregning;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VilkårTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.AvklaringsbehovUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.VurderRefusjonBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * Fasadetjeneste for å delegere alle kall fra steg
 */
@ApplicationScoped
public class BeregningsgrunnlagTjeneste implements KalkulatorInterface {

    private ForeslåSkjæringstidspunktTjeneste foreslåSkjæringstidspunktTjeneste;
    protected OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste;
    private Instance<AvklaringsbehovUtlederFaktaOmBeregning> avklaringsbehovUtledereFaktaOmBeregning;
    private Instance<AvklaringsbehovUtlederFastsettBeregningsaktiviteter> apUtlederFastsettAktiviteter;
    private Instance<FullføreBeregningsgrunnlag> fullføreBeregningsgrunnlag;
    private Instance<ForeslåBeregningsgrunnlag> foreslåBeregningsgrunnlag;
    private final ForeslåBesteberegning foreslåBesteberegning = new ForeslåBesteberegning();
    private Instance<VurderBeregningsgrunnlagTjeneste> vurderBeregningsgrunnlagTjeneste;
    private Instance<FordelBeregningsgrunnlagTjeneste> fordelBeregningsgrunnlagTjeneste;
    private VurderRefusjonBeregningsgrunnlag vurderRefusjonBeregningsgrunnlag;
    private Instance<VilkårTjeneste> vilkårTjeneste;


    public BeregningsgrunnlagTjeneste() {
        // CDI Proxy
    }

    @Inject
    public BeregningsgrunnlagTjeneste(ForeslåSkjæringstidspunktTjeneste foreslåSkjæringstidspunktTjeneste,
                                      @Any Instance<FullføreBeregningsgrunnlag> fullføreBeregningsgrunnlag,
                                      @Any Instance<AvklaringsbehovUtlederFaktaOmBeregning> avklaringsbehovUtledereFaktaOmBeregning,
                                      @Any Instance<AvklaringsbehovUtlederFastsettBeregningsaktiviteter> apUtlederFastsettAktiviteter,
                                      OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste,
                                      @Any Instance<FordelBeregningsgrunnlagTjeneste> fordelBeregningsgrunnlagTjeneste,
                                      VurderRefusjonBeregningsgrunnlag vurderRefusjonBeregningsgrunnlag,
                                      @Any Instance<ForeslåBeregningsgrunnlag> foreslåBeregningsgrunnlag,
                                      @Any Instance<VurderBeregningsgrunnlagTjeneste> vurderBeregningsgrunnlagTjeneste,
                                      @Any Instance<VilkårTjeneste> vilkårTjeneste) {
        this.foreslåSkjæringstidspunktTjeneste = foreslåSkjæringstidspunktTjeneste;
        this.fullføreBeregningsgrunnlag = fullføreBeregningsgrunnlag;
        this.avklaringsbehovUtledereFaktaOmBeregning = avklaringsbehovUtledereFaktaOmBeregning;
        this.apUtlederFastsettAktiviteter = apUtlederFastsettAktiviteter;
        this.opprettBeregningsgrunnlagTjeneste = opprettBeregningsgrunnlagTjeneste;
        this.fordelBeregningsgrunnlagTjeneste = fordelBeregningsgrunnlagTjeneste;
        this.vurderRefusjonBeregningsgrunnlag = vurderRefusjonBeregningsgrunnlag;
        this.foreslåBeregningsgrunnlag = foreslåBeregningsgrunnlag;
        this.vurderBeregningsgrunnlagTjeneste = vurderBeregningsgrunnlagTjeneste;
        this.vilkårTjeneste = vilkårTjeneste;
    }

    @Override
    public BeregningResultatAggregat fastsettBeregningsaktiviteter(FastsettBeregningsaktiviteterInput input) {
        var beregningsgrunnlagRegelResultat = foreslåSkjæringstidspunktTjeneste.foreslåSkjæringstidspunkt(input);
        var tidligereAktivitetOverstyring = hentTidligereOverstyringer(input);
        var avklaringsbehov = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), apUtlederFastsettAktiviteter)
                .utledAvklaringsbehov(beregningsgrunnlagRegelResultat, input, tidligereAktivitetOverstyring.isPresent());
        var vilkårResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), vilkårTjeneste)
                .lagVilkårResultatFastsettAktiviteter(input, beregningsgrunnlagRegelResultat.getVilkårsresultat());
        return BeregningResultatAggregat.Builder.fra(input)
                .medRegisterAktiviteter(beregningsgrunnlagRegelResultat.getRegisterAktiviteter())
                .medOverstyrteAktiviteter(vilkårResultat.isPresent() ? null : tidligereAktivitetOverstyring.orElse(null))
                .medBeregningsgrunnlag(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medVilkårResultat(vilkårResultat.orElse(null))
                .medAvklaringsbehov(avklaringsbehov)
                .build();
    }

    @Override
    public BeregningResultatAggregat fastsettBeregningsgrunnlag(StegProsesseringInput input) {
        FullføreBeregningsgrunnlag fullføre = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), fullføreBeregningsgrunnlag);
        var resultat = fullføre.fullføreBeregningsgrunnlag(input);
        Builder resultatBuilder = Builder.fra(input)
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand());
        var vilkårResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), vilkårTjeneste)
                .lagVilkårResultatFullføre(input, resultat.getBeregningsgrunnlag());
        resultatBuilder.medVilkårResultat(vilkårResultat.orElse(null));
        return resultatBuilder.build();
    }

    @Override
    public BeregningResultatAggregat fordelBeregningsgrunnlag(StegProsesseringInput input) {
        var fordelResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), fordelBeregningsgrunnlagTjeneste)
                .omfordelBeregningsgrunnlag(input);
        var nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                .medBeregningsgrunnlag(fordelResultat.getBeregningsgrunnlag())
                .build(input.getStegTilstand());
        var avklaringsbehov = AvklaringsbehovUtlederFordelBeregning.utledAvklaringsbehovFor(
                input.getKoblingReferanse(),
                nyttGrunnlag,
                input.getYtelsespesifiktGrunnlag(),
                input.getInntektsmeldinger());
        return Builder.fra(input)
                .medAvklaringsbehov(avklaringsbehov)
                .medBeregningsgrunnlag(fordelResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(new RegelSporingAggregat(
                        fordelResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingerGrunnlag).orElse(Collections.emptyList()),
                        fordelResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingPerioder).stream().flatMap(Collection::stream)
                                .collect(Collectors.toList())))
                .build();
    }

    /**
     * Vurderer beregningsgrunnlagsvilkåret
     *
     * @param input Input til vurdering av vilkåret
     * @return Resultat av vilkårsvurdering
     */
    @Override
    public BeregningResultatAggregat vurderBeregningsgrunnlagvilkår(StegProsesseringInput input) {
        BeregningsgrunnlagRegelResultat vilkårVurderingResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), vurderBeregningsgrunnlagTjeneste)
                .vurderBeregningsgrunnlag(input, input.getBeregningsgrunnlagGrunnlag());
        BeregningsgrunnlagDto vurdertBeregningsgrunnlag = vilkårVurderingResultat.getBeregningsgrunnlag();
        BeregningVilkårResultat vilkårResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), vilkårTjeneste)
                .lagVilkårResultatFordel(input, vilkårVurderingResultat.getVilkårsresultat());
        return BeregningResultatAggregat.Builder.fra(input)
                .medAvklaringsbehov(vilkårVurderingResultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(vurdertBeregningsgrunnlag, input.getStegTilstand())
                .medVilkårResultat(vilkårResultat)
                .medRegelSporingAggregat(vilkårVurderingResultat.getRegelsporinger().orElse(null))
                .build();
    }


    /**
     * Vurderer peridoder med refusjon
     *
     * @param input Input til steget
     * @return Resultat av vurdering av refusjonskrav
     */
    @Override
    public BeregningResultatAggregat vurderRefusjonskravForBeregninggrunnlag(VurderRefusjonBeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat vurderRefusjonResultat = vurderRefusjonBeregningsgrunnlag.vurderRefusjon(input);
        return Builder.fra(input)
                .medAvklaringsbehov(vurderRefusjonResultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(vurderRefusjonResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(vurderRefusjonResultat.getRegelsporinger().orElse(null))
                .build();
    }

    /**
     * Foreslår besteberegning
     *
     * @param input Input til foreslå besteberegning
     * @return resultat av foreslått besteberegning
     */
    @Override
    public BesteberegningResultat foreslåBesteberegning(ForeslåBesteberegningInput input) {
        if (!input.isEnabled("automatisk-besteberegning", false) || input.getBeregningsgrunnlag().isOverstyrt()) {
            // Skal ikkje gjere noko i steget om togglet av eller overstyrt inntekt
            return BesteberegningResultat.Builder.fra(input)
                    .medBeregningsgrunnlag(new BeregningsgrunnlagDto(input.getBeregningsgrunnlag()))
                    .build();
        }
        BesteberegningRegelResultat resultat = foreslåBesteberegning.foreslåBesteberegning(input);
        BeregningsgrunnlagVerifiserer.verifiserBesteberegnetBeregningsgrunnlag(resultat.getBeregningsgrunnlag());
        return BesteberegningResultat.Builder.fra(input)
                .medVurderingsgrunnlag(resultat.getBesteberegningVurderingGrunnlag())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    @Override
    public BeregningResultatAggregat foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat resultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), foreslåBeregningsgrunnlag)
                .foreslåBeregningsgrunnlag(input);
        return BeregningResultatAggregat.Builder.fra(input)
                .medAvklaringsbehov(resultat.getAvklaringsbehov())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    @Override
    public BeregningResultatAggregat kontrollerFaktaBeregningsgrunnlag(FaktaOmBeregningInput input) {
        var resultat = opprettBeregningsgrunnlagTjeneste.opprettOgLagreBeregningsgrunnlag(input);

        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(input.getStegTilstand());
        var apUtleder = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), avklaringsbehovUtledereFaktaOmBeregning);
        FaktaOmBeregningAvklaringsbehovResultat avklaringsbehovresultat = apUtleder.utledAvklaringsbehovFor(
                input,
                nyttGrunnlag,
                harOverstyrtBergningsgrunnlag(input));

        BeregningsgrunnlagDto grunnlagMedTilfeller = BeregningsgrunnlagDto.builder(beregningsgrunnlag)
                .leggTilFaktaOmBeregningTilfeller(avklaringsbehovresultat.getFaktaOmBeregningTilfeller())
                .build();

        return BeregningResultatAggregat.Builder.fra(input)
                .medBeregningsgrunnlag(grunnlagMedTilfeller, input.getStegTilstand())
                .medFaktaAggregat(resultat.getFaktaAggregatDto(), input.getStegTilstand())
                .medAvklaringsbehov(avklaringsbehovresultat.getBeregningAvklaringsbehovResultatList())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    private boolean harOverstyrtBergningsgrunnlag(StegProsesseringInput input) {
        return input.getForrigeGrunnlagFraStegUt()
                .flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag)
                .stream()
                .anyMatch(BeregningsgrunnlagDto::isOverstyrt);
    }

    private Optional<BeregningAktivitetOverstyringerDto> hentTidligereOverstyringer(FastsettBeregningsaktiviteterInput input) {
        Optional<BeregningsgrunnlagGrunnlagDto> overstyrtGrunnlag = input.getForrigeGrunnlagFraStegUt();
        return overstyrtGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getOverstyring);
    }

    private <T> T finnImplementasjonForYtelseType(FagsakYtelseType fagsakYtelseType, Instance<T> instanser) {
        return FagsakYtelseTypeRef.Lookup.find(instanser, fagsakYtelseType)
                .orElseThrow(() -> new IllegalStateException("Finner ikke implementasjon for ytelse " + fagsakYtelseType.getKode()));
    }

}
