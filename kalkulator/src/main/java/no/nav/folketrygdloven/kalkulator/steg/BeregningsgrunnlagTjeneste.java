package no.nav.folketrygdloven.kalkulator.steg;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat.Builder;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.ForeslåBesteberegning;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AksjonspunktUtlederFastsettBeregningsaktiviteter;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.FastsettBeregningAktiviteter;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.OpprettBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt.AksjonspunktUtlederFordelBeregning;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VilkårTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.VurderRefusjonBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;

/**
 * Fasade tjeneste for å delegere alle kall fra steg
 */
@ApplicationScoped
public class BeregningsgrunnlagTjeneste {

    protected FastsettBeregningAktiviteter fastsettBeregningAktiviteter;
    protected OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste;
    private Instance<AksjonspunktUtlederFaktaOmBeregning> aksjonspunktUtledereFaktaOmBeregning;
    private Instance<AksjonspunktUtlederFastsettBeregningsaktiviteter> apUtlederFastsettAktiviteter;
    private Instance<FullføreBeregningsgrunnlag> fullføreBeregningsgrunnlag;
    private Instance<ForeslåBeregningsgrunnlag> foreslåBeregningsgrunnlag;
    private ForeslåBesteberegning foreslåBesteberegning = new ForeslåBesteberegning();
    private Instance<VurderBeregningsgrunnlagTjeneste> vurderBeregningsgrunnlagTjeneste;
    private FordelBeregningsgrunnlagTjeneste fordelBeregningsgrunnlagTjeneste;
    private VurderRefusjonBeregningsgrunnlag vurderRefusjonBeregningsgrunnlag;
    private Instance<VilkårTjeneste> vilkårTjeneste;


    public BeregningsgrunnlagTjeneste() {
        // CDI Proxy
    }

    @Inject
    public BeregningsgrunnlagTjeneste(@Any Instance<FullføreBeregningsgrunnlag> fullføreBeregningsgrunnlag,
                                      @Any Instance<AksjonspunktUtlederFaktaOmBeregning> aksjonspunktUtledereFaktaOmBeregning,
                                      @Any Instance<AksjonspunktUtlederFastsettBeregningsaktiviteter> apUtlederFastsettAktiviteter,
                                      OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste,
                                      FordelBeregningsgrunnlagTjeneste fordelBeregningsgrunnlagTjeneste,
                                      VurderRefusjonBeregningsgrunnlag vurderRefusjonBeregningsgrunnlag,
                                      @Any Instance<ForeslåBeregningsgrunnlag> foreslåBeregningsgrunnlag,
                                      @Any Instance<VurderBeregningsgrunnlagTjeneste> vurderBeregningsgrunnlagTjeneste,
                                      FastsettBeregningAktiviteter fastsettBeregningAktiviteter,
                                      @Any Instance<VilkårTjeneste> vilkårTjeneste) {
        this.fullføreBeregningsgrunnlag = fullføreBeregningsgrunnlag;
        this.aksjonspunktUtledereFaktaOmBeregning = aksjonspunktUtledereFaktaOmBeregning;
        this.apUtlederFastsettAktiviteter = apUtlederFastsettAktiviteter;
        this.opprettBeregningsgrunnlagTjeneste = opprettBeregningsgrunnlagTjeneste;
        this.fordelBeregningsgrunnlagTjeneste = fordelBeregningsgrunnlagTjeneste;
        this.vurderRefusjonBeregningsgrunnlag = vurderRefusjonBeregningsgrunnlag;
        this.foreslåBeregningsgrunnlag = foreslåBeregningsgrunnlag;
        this.vurderBeregningsgrunnlagTjeneste = vurderBeregningsgrunnlagTjeneste;
        this.fastsettBeregningAktiviteter = fastsettBeregningAktiviteter;
        this.vilkårTjeneste = vilkårTjeneste;
    }

    public BeregningResultatAggregat fastsettBeregningsaktiviteter(FastsettBeregningsaktiviteterInput input) {
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = fastsettBeregningAktiviteter.fastsettAktiviteter(input);
        if (beregningAktivitetAggregat.getBeregningAktiviteter().isEmpty()) {
            // Avslår vilkår
            return BeregningResultatAggregat.Builder.fra(input)
                    .medAvslåttVilkårIHelePerioden(Vilkårsavslagsårsak.FOR_LAVT_BG)
                    .build();
        }
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = opprettBeregningsgrunnlagTjeneste.fastsettSkjæringstidspunktOgStatuser(input, beregningAktivitetAggregat, input.getIayGrunnlag());
        Optional<BeregningAktivitetOverstyringerDto> overstyrt = hentTidligereOverstyringer(input);
        BeregningsgrunnlagGrunnlagDtoBuilder builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medOverstyring(overstyrt.orElse(null));
        builder.medBeregningsgrunnlag(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag());

        var beregningsgrunnlagGrunnlag = builder.build(input.getStegTilstand());
        boolean erOverstyrt = overstyrt.isPresent();
        BeregningsgrunnlagInput inputOppdatertMedBg = input.medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);
        var aksjonspunkter = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), apUtlederFastsettAktiviteter).utledAksjonspunkter(
                beregningsgrunnlagRegelResultat,
                beregningAktivitetAggregat,
                inputOppdatertMedBg,
                erOverstyrt,
                input.getFagsakYtelseType());
        BeregningResultatAggregat.Builder resultatBuilder = BeregningResultatAggregat.Builder.fra(inputOppdatertMedBg).medAksjonspunkter(aksjonspunkter);
        resultatBuilder.medBeregningsgrunnlag(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag(), input.getStegTilstand());
        return resultatBuilder.build();
    }

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

    public BeregningResultatAggregat fordelBeregningsgrunnlag(FordelBeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat vilkårVurderingResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), vurderBeregningsgrunnlagTjeneste)
                .vurderBeregningsgrunnlag(input, input.getBeregningsgrunnlagGrunnlag());
        BeregningsgrunnlagDto vurdertBeregningsgrunnlag = vilkårVurderingResultat.getBeregningsgrunnlag();
        BeregningVilkårResultat vilkårResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), vilkårTjeneste)
                .lagVilkårResultatFordel(input, vilkårVurderingResultat.getVilkårsresultat());
        if (!vilkårResultat.getErVilkårOppfylt()) {
            // Om vilkåret ikke er oppfylt kan vi returnere uten å kjøre fordeling
            return BeregningResultatAggregat.Builder.fra(input)
                    .medAksjonspunkter(vilkårVurderingResultat.getAksjonspunkter())
                    .medBeregningsgrunnlag(vurdertBeregningsgrunnlag, input.getStegTilstand())
                    .medVilkårResultat(vilkårResultat)
                    .medRegelSporingAggregat(vilkårVurderingResultat.getRegelsporinger().orElse(null))
                    .build();
        } else {
            var fordelResultat = fordelBeregningsgrunnlagTjeneste.fordelBeregningsgrunnlag(input, vurdertBeregningsgrunnlag);
            BeregningsgrunnlagGrunnlagDto nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                    .medBeregningsgrunnlag(fordelResultat.getBeregningsgrunnlag())
                    .build(input.getStegTilstand());
            List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederFordelBeregning.utledAksjonspunkterFor(
                    input.getKoblingReferanse(),
                    nyttGrunnlag,
                    input.getAktivitetGradering(),
                    input.getInntektsmeldinger());
            return Builder.fra(input)
                    .medAksjonspunkter(aksjonspunkter)
                    .medBeregningsgrunnlag(fordelResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                    .medVilkårResultat(vilkårResultat)
                    .medRegelSporingAggregat(new RegelSporingAggregat(
                            fordelResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingerGrunnlag).orElse(Collections.emptyList()),
                            Stream.concat(fordelResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingPerioder).stream().flatMap(Collection::stream),
                                    vilkårVurderingResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingPerioder).stream().flatMap(Collection::stream))
                                    .collect(Collectors.toList())))
                    .build();
        }
    }

    // TODO TSF-1315 rename denne metoden til #fordelBeregningsgrunnlag og slett den eksisterende #fordelBeregningsgrunnlag metoden
    public BeregningResultatAggregat fordelBeregningsgrunnlagUtenPeriodisering(StegProsesseringInput input) {
        var fordelResultat = fordelBeregningsgrunnlagTjeneste.omfordelBeregningsgrunnlag(input);
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                .medBeregningsgrunnlag(fordelResultat.getBeregningsgrunnlag())
                .build(input.getStegTilstand());
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederFordelBeregning.utledAksjonspunkterFor(
                input.getKoblingReferanse(),
                nyttGrunnlag,
                input.getAktivitetGradering(),
                input.getInntektsmeldinger());
        return Builder.fra(input)
                .medAksjonspunkter(aksjonspunkter)
                .medBeregningsgrunnlag(fordelResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(new RegelSporingAggregat(
                        fordelResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingerGrunnlag).orElse(Collections.emptyList()),
                        fordelResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingPerioder).stream().flatMap(Collection::stream)
                                .collect(Collectors.toList())))
                .build();
    }

    public BeregningResultatAggregat vurderRefusjonskravForBeregninggrunnlag(StegProsesseringInput input) {
        BeregningsgrunnlagRegelResultat vilkårVurderingResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), vurderBeregningsgrunnlagTjeneste)
                .vurderBeregningsgrunnlag(input, input.getBeregningsgrunnlagGrunnlag());
        BeregningsgrunnlagDto vurdertBeregningsgrunnlag = vilkårVurderingResultat.getBeregningsgrunnlag();
        BeregningVilkårResultat vilkårResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), vilkårTjeneste)
                .lagVilkårResultatFordel(input, vilkårVurderingResultat.getVilkårsresultat());
        if (!vilkårResultat.getErVilkårOppfylt()) {
            // Om vilkåret ikke er oppfylt kan vi returnere uten å kjøre fordeling
            return BeregningResultatAggregat.Builder.fra(input)
                    .medAksjonspunkter(vilkårVurderingResultat.getAksjonspunkter())
                    .medBeregningsgrunnlag(vurdertBeregningsgrunnlag, input.getStegTilstand())
                    .medVilkårResultat(vilkårResultat)
                    .medRegelSporingAggregat(vilkårVurderingResultat.getRegelsporinger().orElse(null))
                    .build();
        } else {
            BeregningsgrunnlagRegelResultat vurderRefusjonResultat = vurderRefusjonBeregningsgrunnlag.vurderRefusjon(input, vurdertBeregningsgrunnlag);
            return Builder.fra(input)
                    .medAksjonspunkter(vurderRefusjonResultat.getAksjonspunkter())
                    .medBeregningsgrunnlag(vurderRefusjonResultat.getBeregningsgrunnlag(), input.getStegTilstand())
                    .medVilkårResultat(vilkårResultat)
                    .medRegelSporingAggregat(new RegelSporingAggregat(
                            vurderRefusjonResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingerGrunnlag).orElse(Collections.emptyList()),
                            Stream.concat(vurderRefusjonResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingPerioder).stream().flatMap(Collection::stream),
                                    vilkårVurderingResultat.getRegelsporinger().map(RegelSporingAggregat::getRegelsporingPerioder).stream().flatMap(Collection::stream))
                                    .collect(Collectors.toList())))
                    .build();
        }
    }

    /** Foreslår besteberegning
     *
     *
     * @param input Input til foreslå besteberegning
     * @return resultat av foreslått besteberegning
     */
    public BeregningResultatAggregat foreslåBesteberegning(ForeslåBesteberegningInput input) {
        BeregningsgrunnlagRegelResultat resultat = foreslåBesteberegning.foreslåBesteberegning(input);
        return BeregningResultatAggregat.Builder.fra(input)
                .medAksjonspunkter(resultat.getAksjonspunkter())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }


    public BeregningResultatAggregat foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat resultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), foreslåBeregningsgrunnlag)
                .foreslåBeregningsgrunnlag(input);
        return BeregningResultatAggregat.Builder.fra(input)
                .medAksjonspunkter(resultat.getAksjonspunkter())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), input.getStegTilstand())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    public BeregningResultatAggregat kontrollerFaktaBeregningsgrunnlag(FaktaOmBeregningInput input) {
        var resultat = opprettBeregningsgrunnlagTjeneste.opprettOgLagreBeregningsgrunnlag(input);

        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(input.getStegTilstand());
        var apUtleder = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), aksjonspunktUtledereFaktaOmBeregning);
        FaktaOmBeregningAksjonspunktResultat aksjonspunktresultat = apUtleder.utledAksjonspunkterFor(
                input,
                nyttGrunnlag,
                harOverstyrtBergningsgrunnlag(input));

        BeregningsgrunnlagDto grunnlagMedTilfeller = BeregningsgrunnlagDto.builder(beregningsgrunnlag)
                .leggTilFaktaOmBeregningTilfeller(aksjonspunktresultat.getFaktaOmBeregningTilfeller())
                .build();

        return BeregningResultatAggregat.Builder.fra(input)
                .medBeregningsgrunnlag(grunnlagMedTilfeller, input.getStegTilstand())
                .medAksjonspunkter(aksjonspunktresultat.getBeregningAksjonspunktResultatList())
                .medRegelSporingAggregat(resultat.getRegelsporinger().orElse(null))
                .build();
    }

    private boolean harOverstyrtBergningsgrunnlag(StegProsesseringInput input) {
        return input.getForrigeGrunnlagFraStegUt()
                .flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag)
                .stream()
                .anyMatch(BeregningsgrunnlagDto::isOverstyrt);
    }

    Optional<BeregningAktivitetOverstyringerDto> hentTidligereOverstyringer(FastsettBeregningsaktiviteterInput input) {
        Optional<BeregningsgrunnlagGrunnlagDto> overstyrtGrunnlag = input.getForrigeGrunnlagFraStegUt();
        return overstyrtGrunnlag
                .flatMap(BeregningsgrunnlagGrunnlagDto::getOverstyring);
    }

    private <T> T finnImplementasjonForYtelseType(FagsakYtelseType fagsakYtelseType, Instance<T> instanser) {
        return FagsakYtelseTypeRef.Lookup.find(instanser, fagsakYtelseType)
                .orElseThrow(() -> new IllegalStateException("Finner ikke implementasjon for ytelse " + fagsakYtelseType.getKode()));
    }

}
