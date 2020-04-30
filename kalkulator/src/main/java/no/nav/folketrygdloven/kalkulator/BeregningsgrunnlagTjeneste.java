package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FASTSATT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FORESLÅTT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.KOFAKBER_UT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.OPPRETTET;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat.Builder;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.refusjon.BeregningRefusjonAksjonspunktutleder;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
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
    private Instance<VurderBeregningsgrunnlagTjeneste> vurderBeregningsgrunnlagTjeneste;
    private FordelBeregningsgrunnlagTjeneste fordelBeregningsgrunnlagTjeneste;
    private BeregningRefusjonAksjonspunktutleder beregningRefusjonAksjonspunktutleder;

    public BeregningsgrunnlagTjeneste() {
        // CDI Proxy
    }

    @Inject
    public BeregningsgrunnlagTjeneste(@Any Instance<FullføreBeregningsgrunnlag> fullføreBeregningsgrunnlag,
                                      @Any Instance<AksjonspunktUtlederFaktaOmBeregning> aksjonspunktUtledereFaktaOmBeregning,
                                      @Any Instance<AksjonspunktUtlederFastsettBeregningsaktiviteter> apUtlederFastsettAktiviteter,
                                      OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste,
                                      FordelBeregningsgrunnlagTjeneste fordelBeregningsgrunnlagTjeneste,
                                      BeregningRefusjonAksjonspunktutleder beregningRefusjonAksjonspunktutleder,
                                      @Any Instance<ForeslåBeregningsgrunnlag> foreslåBeregningsgrunnlag,
                                      @Any Instance<VurderBeregningsgrunnlagTjeneste> vurderBeregningsgrunnlagTjeneste,
                                      FastsettBeregningAktiviteter fastsettBeregningAktiviteter) {
        this.fullføreBeregningsgrunnlag = fullføreBeregningsgrunnlag;
        this.aksjonspunktUtledereFaktaOmBeregning = aksjonspunktUtledereFaktaOmBeregning;
        this.apUtlederFastsettAktiviteter = apUtlederFastsettAktiviteter;
        this.opprettBeregningsgrunnlagTjeneste = opprettBeregningsgrunnlagTjeneste;
        this.fordelBeregningsgrunnlagTjeneste = fordelBeregningsgrunnlagTjeneste;
        this.beregningRefusjonAksjonspunktutleder = beregningRefusjonAksjonspunktutleder;
        this.foreslåBeregningsgrunnlag = foreslåBeregningsgrunnlag;
        this.vurderBeregningsgrunnlagTjeneste = vurderBeregningsgrunnlagTjeneste;
        this.fastsettBeregningAktiviteter = fastsettBeregningAktiviteter;
    }

    public BeregningResultatAggregat fastsettBeregningsaktiviteter(BeregningsgrunnlagInput input) {
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = fastsettBeregningAktiviteter.fastsettAktiviteter(input);
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = opprettBeregningsgrunnlagTjeneste.fastsettSkjæringstidspunktOgStatuser(input, beregningAktivitetAggregat, input.getIayGrunnlag());
        Optional<BeregningAktivitetOverstyringerDto> overstyrt = hentTidligereOverstyringer(input);
        BeregningsgrunnlagGrunnlagDtoBuilder builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medOverstyring(overstyrt.orElse(null));
        builder.medBeregningsgrunnlag(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag());

        var beregningsgrunnlagGrunnlag = builder.build(OPPRETTET);
        boolean erOverstyrt = overstyrt.isPresent();
        BeregningsgrunnlagInput inputOppdatertMedBg = input.medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);
        var aksjonspunkter = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), apUtlederFastsettAktiviteter).utledAksjonspunkter(
                beregningsgrunnlagRegelResultat,
                beregningAktivitetAggregat,
                inputOppdatertMedBg,
                erOverstyrt,
                input.getFagsakYtelseType());
        BeregningResultatAggregat.Builder resultatBuilder = BeregningResultatAggregat.Builder.fra(inputOppdatertMedBg)
                .medAksjonspunkter(aksjonspunkter);

        if (input.getFagsakYtelseType().equals(FagsakYtelseType.FRISINN)) {
            leggPåFrisinnData(input, beregningsgrunnlagRegelResultat, resultatBuilder);
        }

        resultatBuilder.medBeregningsgrunnlag(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag(), OPPRETTET);
        return resultatBuilder.build();
    }

    //FIXME(OJR) skill ut i egen tjeneste
    private void leggPåFrisinnData(BeregningsgrunnlagInput input, BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat, Builder resultatBuilder) {
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        if (beregningsgrunnlagRegelResultat.getAksjonspunkter().stream().anyMatch(bra -> bra.getBeregningAksjonspunktDefinisjon() == BeregningAksjonspunktDefinisjon.INGEN_AKTIVITETER)) {
            if (frisinnGrunnlag.getSøkerYtelseForFrilans() && !frisinnGrunnlag.getSøkerYtelseForNæring()) {
                resultatBuilder.medVilkårAvslått(Vilkårsavslagsårsak.SØKT_FL_INGEN_FL_INNTEKT);
            } else {
                resultatBuilder.medVilkårAvslått(Vilkårsavslagsårsak.FOR_LAVT_BG);
            }
        } else {
            if (frisinnGrunnlag.getSøkerYtelseForFrilans() && !frisinnGrunnlag.getSøkerYtelseForNæring()) {
                if (beregningsgrunnlagRegelResultat.getBeregningsgrunnlag() != null) {
                    if (beregningsgrunnlagRegelResultat.getBeregningsgrunnlag().getAktivitetStatuser().stream().noneMatch(as -> as.getAktivitetStatus().erFrilanser())) {
                        resultatBuilder.medVilkårAvslått(Vilkårsavslagsårsak.SØKT_FL_INGEN_FL_INNTEKT);
                    }
                }
            }
        }
    }

    public BeregningResultatAggregat fastsettBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        FullføreBeregningsgrunnlag fullføre = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), fullføreBeregningsgrunnlag);
        BeregningsgrunnlagDto fastsattBeregningsgrunnlag = fullføre.fullføreBeregningsgrunnlag(input);
        return BeregningResultatAggregat.Builder.fra(input)
                .medBeregningsgrunnlag(fastsattBeregningsgrunnlag, FASTSATT)
                .build();
    }

    public BeregningResultatAggregat fordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat vilkårVurderingResultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), vurderBeregningsgrunnlagTjeneste)
                .vurderBeregningsgrunnlag(input, input.getBeregningsgrunnlagGrunnlag());
        BeregningsgrunnlagDto vurdertBeregningsgrunnlag = vilkårVurderingResultat.getBeregningsgrunnlag();
        if (Boolean.FALSE.equals(vilkårVurderingResultat.getVilkårOppfylt())) {
            return BeregningResultatAggregat.Builder.fra(input)
                    .medAksjonspunkter(vilkårVurderingResultat.getAksjonspunkter())
                    .medBeregningsgrunnlag(vurdertBeregningsgrunnlag, OPPDATERT_MED_REFUSJON_OG_GRADERING)
                    .medVilkårResultat(vilkårVurderingResultat.getVilkårOppfylt())
                    .build();
        } else {
            var fordeltBeregningsgrunnlag = fordelBeregningsgrunnlagTjeneste.fordelBeregningsgrunnlag(input, vurdertBeregningsgrunnlag);
            BeregningsgrunnlagGrunnlagDto nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                    .medBeregningsgrunnlag(fordeltBeregningsgrunnlag)
                    .build(OPPDATERT_MED_REFUSJON_OG_GRADERING);
            List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederFordelBeregning.utledAksjonspunkterFor(
                    input.getBehandlingReferanse(),
                    nyttGrunnlag,
                    input.getAktivitetGradering(),
                    input.getInntektsmeldinger());
            return BeregningResultatAggregat.Builder.fra(input)
                    .medAksjonspunkter(aksjonspunkter)
                    .medVilkårResultat(vilkårVurderingResultat.getVilkårOppfylt())
                    .medBeregningsgrunnlag(fordeltBeregningsgrunnlag, OPPDATERT_MED_REFUSJON_OG_GRADERING)
                    .build();
        }
    }

    public BeregningResultatAggregat vurderRefusjonskravForBeregninggrunnlag(BeregningsgrunnlagInput input) {
        List<BeregningAksjonspunktResultat> beregningAksjonspunktResultats = beregningRefusjonAksjonspunktutleder.utledAksjonspunkter(input);
        return BeregningResultatAggregat.builder()
                .medAksjonspunkter(beregningAksjonspunktResultats)
                .build();

    }

    public BeregningResultatAggregat foreslåBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat resultat = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), foreslåBeregningsgrunnlag)
                .foreslåBeregningsgrunnlag(input);
        return BeregningResultatAggregat.Builder.fra(input)
                .medAksjonspunkter(resultat.getAksjonspunkter())
                .medBeregningsgrunnlag(resultat.getBeregningsgrunnlag(), FORESLÅTT)
                .build();
    }

    public BeregningResultatAggregat kontrollerFaktaBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagDto beregningsgrunnlag = opprettBeregningsgrunnlagTjeneste.opprettOgLagreBeregningsgrunnlag(input);

        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(OPPDATERT_MED_ANDELER);
        var apUtleder = finnImplementasjonForYtelseType(input.getFagsakYtelseType(), aksjonspunktUtledereFaktaOmBeregning);
        FaktaOmBeregningAksjonspunktResultat aksjonspunktresultat = apUtleder.utledAksjonspunkterFor(
                input,
                nyttGrunnlag,
                harOverstyrtBergningsgrunnlag(input));

        BeregningsgrunnlagDto grunnlagMedTilfeller = BeregningsgrunnlagDto.builder(beregningsgrunnlag)
                .leggTilFaktaOmBeregningTilfeller(aksjonspunktresultat.getFaktaOmBeregningTilfeller())
                .build();

        return BeregningResultatAggregat.Builder.fra(input)
                .medBeregningsgrunnlag(grunnlagMedTilfeller, OPPDATERT_MED_ANDELER)
                .medAksjonspunkter(aksjonspunktresultat.getBeregningAksjonspunktResultatList())
                .build();
    }

    private boolean harOverstyrtBergningsgrunnlag(BeregningsgrunnlagInput input) {
        return input.hentForrigeBeregningsgrunnlag(KOFAKBER_UT)
                .stream()
                .anyMatch(BeregningsgrunnlagDto::isOverstyrt);
    }

    Optional<BeregningAktivitetOverstyringerDto> hentTidligereOverstyringer(BeregningsgrunnlagInput input) {
        Optional<BeregningsgrunnlagGrunnlagDto> overstyrtGrunnlag = input.hentForrigeBeregningsgrunnlagGrunnlag(FASTSATT_BEREGNINGSAKTIVITETER);
        return overstyrtGrunnlag
                .flatMap(BeregningsgrunnlagGrunnlagDto::getOverstyring);
    }

    private <T> T finnImplementasjonForYtelseType(FagsakYtelseType fagsakYtelseType, Instance<T> instanser) {
        return FagsakYtelseTypeRef.Lookup.find(instanser, fagsakYtelseType)
                .orElseThrow(() -> new IllegalStateException("Finner ikke implementasjon for ytelse " + fagsakYtelseType.getKode()));
    }

}
