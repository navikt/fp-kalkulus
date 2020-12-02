package no.nav.folketrygdloven.kalkulus.beregning;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingGrunnlag;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AksjonspunktMedTilstandDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingPeriodeEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Vilkårsavslagsårsak;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelsporingRepository;

@ApplicationScoped
public class BeregningStegTjeneste {
    private static final String UTVIKLER_FEIL_SKAL_HA_BEREGNINGSGRUNNLAG_HER = "Utvikler-feil: skal ha beregningsgrunnlag her";
    private static final Supplier<IllegalStateException> INGEN_BG_EXCEPTION_SUPPLIER = () -> new IllegalStateException(UTVIKLER_FEIL_SKAL_HA_BEREGNINGSGRUNNLAG_HER);

    private BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste;
    private BeregningsgrunnlagRepository repository;
    private RegelsporingRepository regelsporingRepository;
    private RullTilbakeTjeneste rullTilbakeTjeneste;

    BeregningStegTjeneste() {
        // CDI
    }

    @Inject
    public BeregningStegTjeneste(BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste, BeregningsgrunnlagRepository repository, RegelsporingRepository regelsporingRepository, RullTilbakeTjeneste rullTilbakeTjeneste) {
        this.beregningsgrunnlagTjeneste = beregningsgrunnlagTjeneste;
        this.repository = repository;
        this.regelsporingRepository = regelsporingRepository;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
    }

    /**
     * FastsettBeregningsaktiviteter
     * Steg 1. FASTSETT_STP_BER
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link TilstandResponse}
     */
    public TilstandResponse fastsettBeregningsaktiviteter(FastsettBeregningsaktiviteterInput input) {
        BeregningResultatAggregat resultat = beregningsgrunnlagTjeneste.fastsettBeregningsaktiviteter(input);
        TilstandResponse tilstandResponse = mapTilstandResponse(input.getKoblingReferanse(), lagreOgKopierFastsettBeregningsaktiviteter(input, resultat));

        if (resultat.getBeregningVilkårResultat() != null) {
            tilstandResponse.medVilkårResultat(resultat.getBeregningVilkårResultat().getErVilkårOppfylt());
            tilstandResponse.medVilkårsavslagsårsak(new Vilkårsavslagsårsak(resultat.getBeregningVilkårResultat().getVilkårsavslagsårsak().getKode()));
        }
        return tilstandResponse;
    }

    /**
     * KontrollerFaktaBeregningsgrunnlag
     * Steg 2. KOFAKBER
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse kontrollerFaktaBeregningsgrunnlag(FaktaOmBeregningInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.kontrollerFaktaBeregningsgrunnlag(input);
        lagreOgKopierFaktaOmBeregning(input, beregningResultatAggregat);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat.getBeregningAksjonspunktResultater());
    }

    /**
     * ForeslåBesteberegning
     * Steg 2.5. FORS_BESTEBEREGNING
     *
     * Dette steget vil aldri bli brukt av noe annet enn foreldrepenger, men legges inn her for å kunne testes via verdikjedetest
     *
     * @param input {@link ForeslåBeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse foreslåBesteberegning(ForeslåBesteberegningInput input) {
        if (input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag) {
            ForeldrepengerGrunnlag foreldrepengerGrunnlag = input.getYtelsespesifiktGrunnlag();
            if (foreldrepengerGrunnlag.isKvalifisererTilBesteberegning()) {
                BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.foreslåBesteberegning(input);
                Long koblingId = input.getKoblingReferanse().getKoblingId();
                Optional<BeregningsgrunnlagDto> beregningsgrunnlag = beregningResultatAggregat.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag();
                if (beregningsgrunnlag.isPresent()) {
                    BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningsgrunnlag.get());
                    repository.lagre(koblingId, beregningsgrunnlagEntitet, input.getStegTilstand());
                    lagreRegelsporing(koblingId, beregningResultatAggregat.getRegelSporingAggregat());
                }
                return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat.getBeregningAksjonspunktResultater());
            }
        }
        return mapTilstandResponse(input.getKoblingReferanse(), List.of());
    }


    /**
     * ForeslåBeregningsgrunnlag
     * Steg 3. FORS_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat.getBeregningAksjonspunktResultater());
    }

    /**
     * VurderRefusjonBeregningsgrunnlag
     * Steg 4. VURDER_REF_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse vurderRefusjonForBeregningsgrunnlaget(StegProsesseringInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.vurderRefusjonskravForBeregninggrunnlag(input);
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningResultatAggregat.getBeregningsgrunnlag());
        Long koblingId = input.getKoblingReferanse().getKoblingId();
        repository.lagre(koblingId, beregningsgrunnlagEntitet, input.getStegTilstand());
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat.getBeregningAksjonspunktResultater());
    }

    /**
     * FordelBeregningsgrunnlag
     * Steg 5. FORDEL_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse fordelBeregningsgrunnlag(FordelBeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.fordelBeregningsgrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        BeregningVilkårResultat vilkårResultat = beregningResultatAggregat.getBeregningVilkårResultat();
        if (vilkårResultat == null) {
            throw new IllegalStateException("Hadde ikke vilkårsresultat for input med ref " + input.getKoblingReferanse());
        }
        TilstandResponse tilstandResponse = mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat.getBeregningAksjonspunktResultater());
        if (!vilkårResultat.getErVilkårOppfylt()) {
            tilstandResponse.medVilkårsavslagsårsak(new Vilkårsavslagsårsak(vilkårResultat.getVilkårsavslagsårsak().getKode()));
        }
        return tilstandResponse.medVilkårResultat(vilkårResultat.getErVilkårOppfylt());
    }

    /**
     * FastsettBeregningsgrunnlagSteg
     * Steg 6. FAST_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     */
    public TilstandResponse fastsettBeregningsgrunnlag(StegProsesseringInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.fastsettBeregningsgrunnlag(input);
        Long koblingId = input.getKoblingReferanse().getKoblingId();
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag = beregningResultatAggregat.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag();

        if (beregningsgrunnlag.isPresent()) {
            BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningsgrunnlag.get());
            repository.lagre(koblingId, beregningsgrunnlagEntitet, input.getStegTilstand());
            lagreRegelsporing(koblingId, beregningResultatAggregat.getRegelSporingAggregat());
        }
        TilstandResponse tilstandResponse = mapTilstandResponse(input.getKoblingReferanse(), List.of());

        BeregningVilkårResultat vilkårResultat = beregningResultatAggregat.getBeregningVilkårResultat();
        if (vilkårResultat != null && !vilkårResultat.getErVilkårOppfylt()) {
            tilstandResponse.medVilkårResultat(vilkårResultat.getErVilkårOppfylt());
            tilstandResponse.medVilkårsavslagsårsak(new Vilkårsavslagsårsak(vilkårResultat.getVilkårsavslagsårsak().getKode()));
        }
        return tilstandResponse;
    }

    private List<BeregningAksjonspunktResultat> lagreOgKopierFastsettBeregningsaktiviteter(StegProsesseringInput input,
                                                                                           BeregningResultatAggregat resultat) {
        Long koblingId = input.getKoblingReferanse().getKoblingId();
        Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeBekreftetGrunnlag = repository.hentSisteBeregningsgrunnlagGrunnlagEntitet(
                koblingId,
                input.getStegUtTilstand());
        List<BeregningAksjonspunktResultat> beregningAksjonspunktResultater = resultat.getBeregningAksjonspunktResultater();
        boolean kanKopiereGrunnlag = KopierBeregningsgrunnlag.kanKopiereFraForrigeBekreftetGrunnlag(
                beregningAksjonspunktResultater,
                resultat.getBeregningsgrunnlagGrunnlag(),
                input.getForrigeGrunnlagFraSteg(),
                input.getForrigeGrunnlagFraStegUt()
        );
        var beregningsgrunnlagGrunnlagBuilder = KalkulatorTilEntitetMapper.mapGrunnlag(resultat.getBeregningsgrunnlagGrunnlag());
        repository.lagre(koblingId, beregningsgrunnlagGrunnlagBuilder, input.getStegTilstand());
        if (kanKopiereGrunnlag) {
            forrigeBekreftetGrunnlag.ifPresent(gr -> repository.lagre(koblingId, BeregningsgrunnlagGrunnlagBuilder.oppdatere(gr), input.getStegUtTilstand()));
        }
        lagreRegelsporing(koblingId, resultat.getRegelSporingAggregat());
        return beregningAksjonspunktResultater;
    }

    private void lagreOgKopierFaktaOmBeregning(StegProsesseringInput input, BeregningResultatAggregat beregningResultatAggregat) {
        KoblingReferanse ref = input.getKoblingReferanse();
        Long behandlingId = ref.getKoblingId();
        boolean kanKopiereFraBekreftet = KopierBeregningsgrunnlag.kanKopiereFraForrigeBekreftetGrunnlag(
                beregningResultatAggregat.getBeregningAksjonspunktResultater(),
                beregningResultatAggregat.getBeregningsgrunnlagGrunnlag(),
                input.getForrigeGrunnlagFraSteg(),
                input.getForrigeGrunnlagFraStegUt()
        );

        Long koblingId = input.getKoblingReferanse().getKoblingId();
        var beregningsgrunnlagGrunnlagBuilder = KalkulatorTilEntitetMapper.mapGrunnlag(beregningResultatAggregat.getBeregningsgrunnlagGrunnlag());
        repository.lagre(behandlingId, beregningsgrunnlagGrunnlagBuilder, input.getStegTilstand());
        if (kanKopiereFraBekreftet) {
            input.getForrigeGrunnlagFraStegUt().map(gr -> {
                        BeregningsgrunnlagGrunnlagDtoBuilder b = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(beregningResultatAggregat.getBeregningsgrunnlagGrunnlag())
                                        .medBeregningsgrunnlag(gr.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag")));
                                gr.getRefusjonOverstyringer().ifPresent(b::medRefusjonOverstyring);
                                return b;
                            })
                    .map(builder -> builder.build(input.getStegUtTilstand()))
                    .map(KalkulatorTilEntitetMapper::mapGrunnlag)
                    .ifPresent(b -> repository.lagre(behandlingId, b, input.getStegUtTilstand()));
        }
        lagreRegelsporing(koblingId, beregningResultatAggregat.getRegelSporingAggregat());
    }

    private void lagreOgKopier(StegProsesseringInput input,
                               BeregningResultatAggregat beregningResultatAggregat) {
        KoblingReferanse ref = input.getKoblingReferanse();
        Long behandlingId = ref.getKoblingId();
        boolean kanKopiereBekreftet = KopierBeregningsgrunnlag.kanKopiereFraForrigeBekreftetGrunnlag(
                beregningResultatAggregat.getBeregningAksjonspunktResultater(),
                beregningResultatAggregat.getBeregningsgrunnlagGrunnlag(),
                input.getForrigeGrunnlagFraSteg(),
                input.getForrigeGrunnlagFraStegUt()
        );
        BeregningsgrunnlagEntitet nyttBg = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningResultatAggregat.getBeregningsgrunnlag());
        repository.lagre(behandlingId, nyttBg, input.getStegTilstand());
        if (kanKopiereBekreftet) {
            input.getForrigeGrunnlagFraStegUt()
                    .map(gr -> KalkulatorTilEntitetMapper.mapGrunnlag(gr))
                    .ifPresent(bg -> repository.lagre(behandlingId, bg, input.getStegUtTilstand()));
        }
        lagreRegelsporing(input.getKoblingReferanse().getKoblingId(), beregningResultatAggregat.getRegelSporingAggregat());
    }

    private TilstandResponse mapTilstandResponse(KoblingReferanse koblingReferanse, List<BeregningAksjonspunktResultat> resultat) {
        List<AksjonspunktMedTilstandDto> aksjonspunkter = resultat.stream()
                .map(res -> new AksjonspunktMedTilstandDto(
                        new BeregningAksjonspunkt(res.getBeregningAksjonspunktDefinisjon().getKode()),
                        res.getVenteårsak() != null ? new BeregningVenteårsak(res.getVenteårsak().getKode()) : null,
                        res.getVentefrist())).collect(Collectors.toList());
        return new TilstandResponse(koblingReferanse.getKoblingUuid(), aksjonspunkter);
    }

    private void lagreRegelsporing(Long koblingId, Optional<RegelSporingAggregat> regelsporinger) {
        if (regelsporinger.isPresent()) {
            lagreRegelSporingPerioder(koblingId, regelsporinger.get());
            lagreRegelSporingGrunnlag(koblingId, regelsporinger.get());
        }
    }

    private void lagreRegelSporingGrunnlag(Long koblingId, RegelSporingAggregat regelsporinger) {
        List<RegelSporingGrunnlag> regelsporingGrunnlag = regelsporinger.getRegelsporingerGrunnlag();
        if (regelsporingGrunnlag != null) {
            regelsporingGrunnlag.forEach(sporing -> {
                        RegelSporingGrunnlagEntitet.Builder sporingGrunnlagEntitet = RegelSporingGrunnlagEntitet.ny()
                                .medRegelEvaluering(sporing.getRegelEvaluering())
                                .medRegelInput(sporing.getRegelInput());
                        regelsporingRepository.lagre(koblingId, sporingGrunnlagEntitet, sporing.getRegelType());
                    }
            );
        }
    }

    private void lagreRegelSporingPerioder(Long koblingId, RegelSporingAggregat regelsporinger) {
        if (regelsporinger.getRegelsporingPerioder() != null) {
            Map<BeregningsgrunnlagPeriodeRegelType, List<RegelSporingPeriode>> sporingPerioderPrType = regelsporinger.getRegelsporingPerioder()
                    .stream()
                    .collect(Collectors.groupingBy(RegelSporingPeriode::getRegelType));
            Map<BeregningsgrunnlagPeriodeRegelType, List<RegelSporingPeriodeEntitet.Builder>> builderMap = sporingPerioderPrType.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, this::lagRegelSporingPeriodeBuilders));
            regelsporingRepository.lagre(koblingId, builderMap);
        }
    }

    private List<RegelSporingPeriodeEntitet.Builder> lagRegelSporingPeriodeBuilders(Map.Entry<BeregningsgrunnlagPeriodeRegelType, List<RegelSporingPeriode>> e) {
        return e.getValue().stream().map(sporing ->
                RegelSporingPeriodeEntitet.ny()
                        .medRegelEvaluering(sporing.getRegelEvaluering())
                        .medRegelInput(sporing.getRegelInput())
                        .medPeriode(IntervallEntitet.fraOgMedTilOgMed(sporing.getPeriode().getFomDato(), sporing.getPeriode().getTomDato())))
                .collect(Collectors.toList());
    }

    public TilstandResponse beregnFor(StegType stegType, StegProsesseringInput input, Long koblingId) {
        rullTilbakeTjeneste.rullTilbakeTilTilstandFørVedBehov(koblingId, input.getStegTilstand());
        if (stegType.equals(StegType.KOFAKBER)) {
            return kontrollerFaktaBeregningsgrunnlag((FaktaOmBeregningInput) input);
        } else if (stegType.equals(StegType.FORS_BESTEBEREGNING)) {
            return foreslåBesteberegning((ForeslåBesteberegningInput) input);
        } else if (stegType.equals(StegType.FORS_BERGRUNN)) {
            return foreslåBeregningsgrunnlag((ForeslåBeregningsgrunnlagInput) input);
        } else if (stegType.equals(StegType.VURDER_REF_BERGRUNN)) {
            return vurderRefusjonForBeregningsgrunnlaget(input);
        } else if (stegType.equals(StegType.FORDEL_BERGRUNN)) {
            return fordelBeregningsgrunnlag((FordelBeregningsgrunnlagInput) input);
        } else if (stegType.equals(StegType.FAST_BERGRUNN)) {
            return fastsettBeregningsgrunnlag(input);
        }
        throw new IllegalStateException("Kan ikke beregne for " + stegType.getKode());
    }
}
