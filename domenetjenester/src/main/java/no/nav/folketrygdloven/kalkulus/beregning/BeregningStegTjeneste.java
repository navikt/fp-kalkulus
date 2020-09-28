package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FASTSATT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FASTSATT_INN;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FORESLÅTT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FORESLÅTT_UT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.KOFAKBER_UT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.VURDERT_REFUSJON;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
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
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
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
    public TilstandResponse fastsettBeregningsaktiviteter(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat resultat = beregningsgrunnlagTjeneste.fastsettBeregningsaktiviteter(input);
        TilstandResponse tilstandResponse = mapTilstandResponse(input.getKoblingReferanse(), lagreOgKopier(input.getKoblingReferanse(), resultat));

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
    public TilstandResponse kontrollerFaktaBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.kontrollerFaktaBeregningsgrunnlag(input);
        Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeBekreftetGrunnlag = finnForrigeGrunnlagFraTilstand(input, KOFAKBER_UT);
        BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag = KalkulatorTilEntitetMapper.mapGrunnlag(input.getKoblingReferanse().getKoblingId(), beregningResultatAggregat.getBeregningsgrunnlagGrunnlag(), OPPDATERT_MED_ANDELER);
        BeregningsgrunnlagEntitet nyttBg = nyttGrunnlag.getBeregningsgrunnlag().orElseThrow(INGEN_BG_EXCEPTION_SUPPLIER);
        lagreOgKopier(input, beregningResultatAggregat, forrigeBekreftetGrunnlag, nyttGrunnlag, nyttBg);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat.getBeregningAksjonspunktResultater());
    }

    /**
     * ForeslåBeregningsgrunnlag
     * Steg 3. FORS_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse foreslåBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(input);
        Optional<BeregningsgrunnlagEntitet> forrigeBekreftetBeregningsgrunnlag = finnForrigeBgFraTilstand(input, FORESLÅTT_UT);
        BeregningsgrunnlagEntitet nyttBg = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningResultatAggregat.getBeregningsgrunnlag());
        lagreOgKopier(input, beregningResultatAggregat, forrigeBekreftetBeregningsgrunnlag, nyttBg, FORESLÅTT, FORESLÅTT_UT);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat.getBeregningAksjonspunktResultater());
    }

    /**
     * VurderRefusjonBeregningsgrunnlag
     * Steg 4. VURDER_REF_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse vurderRefusjonForBeregningsgrunnlaget(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.vurderRefusjonskravForBeregninggrunnlag(input);
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningResultatAggregat.getBeregningsgrunnlag());
        Long koblingId = input.getKoblingReferanse().getKoblingId();
        repository.lagre(koblingId, beregningsgrunnlagEntitet, VURDERT_REFUSJON);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat.getBeregningAksjonspunktResultater());
    }

    /**
     * FordelBeregningsgrunnlag
     * Steg 5. FORDEL_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse fordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.fordelBeregningsgrunnlag(input);
        Optional<BeregningsgrunnlagEntitet> forrigeBekreftetBeregningsgrunnlag = finnForrigeBgFraTilstand(input, FASTSATT_INN);
        BeregningsgrunnlagEntitet nyttBg = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningResultatAggregat.getBeregningsgrunnlag());
        lagreOgKopier(input, beregningResultatAggregat, forrigeBekreftetBeregningsgrunnlag, nyttBg, OPPDATERT_MED_REFUSJON_OG_GRADERING, FASTSATT_INN);
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
    public TilstandResponse fastsettBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.fastsettBeregningsgrunnlag(input);
        Long koblingId = input.getKoblingReferanse().getKoblingId();
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag = beregningResultatAggregat.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag();

        if (beregningsgrunnlag.isPresent()) {
            BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningsgrunnlag.get());
            repository.lagre(koblingId, beregningsgrunnlagEntitet, FASTSATT);
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

    private List<BeregningAksjonspunktResultat> lagreOgKopier(KoblingReferanse ref, BeregningResultatAggregat resultat) {
        BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag = KalkulatorTilEntitetMapper.mapGrunnlag(ref.getKoblingId(), resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.OPPRETTET);
        Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeBekreftetGrunnlag = repository.hentSisteBeregningsgrunnlagGrunnlagEntitet(
                ref.getKoblingId(),
                BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        List<BeregningAksjonspunktResultat> beregningAksjonspunktResultater = resultat.getBeregningAksjonspunktResultater();
        boolean kanKopiereGrunnlag = KopierBeregningsgrunnlag.kanKopiereFraForrigeBekreftetGrunnlag(
                beregningAksjonspunktResultater,
                nyttGrunnlag,
                repository.hentSisteBeregningsgrunnlagGrunnlagEntitet(ref.getKoblingId(), BeregningsgrunnlagTilstand.OPPRETTET),
                forrigeBekreftetGrunnlag
        );
        repository.lagre(ref.getKoblingId(), BeregningsgrunnlagGrunnlagBuilder.oppdatere(nyttGrunnlag), BeregningsgrunnlagTilstand.OPPRETTET);
        if (kanKopiereGrunnlag) {
            forrigeBekreftetGrunnlag.ifPresent(gr -> repository.lagre(ref.getKoblingId(), BeregningsgrunnlagGrunnlagBuilder.oppdatere(gr), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER));
        }
        lagreRegelsporing(ref.getKoblingId(), resultat.getRegelSporingAggregat());
        return beregningAksjonspunktResultater;
    }

    private void lagreOgKopier(BeregningsgrunnlagInput input,
                               BeregningResultatAggregat beregningResultatAggregat,
                               Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeBekreftetGrunnlag,
                               BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag,
                               BeregningsgrunnlagEntitet nyttBg) {
        KoblingReferanse ref = input.getKoblingReferanse();
        Long behandlingId = ref.getKoblingId();
        boolean kanKopiereFraBekreftet = KopierBeregningsgrunnlag.kanKopiereFraForrigeBekreftetGrunnlag(
                beregningResultatAggregat.getBeregningAksjonspunktResultater(),
                nyttBg,
                finnForrigeBgFraTilstand(input, OPPDATERT_MED_ANDELER),
                forrigeBekreftetGrunnlag.flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag));
        repository.lagre(behandlingId, nyttBg, OPPDATERT_MED_ANDELER);
        if (kanKopiereFraBekreftet) {
            Optional<BeregningsgrunnlagGrunnlagBuilder> bekreftetGrunnlagBuilder = forrigeBekreftetGrunnlag
                    .map(gr -> {
                                BeregningsgrunnlagGrunnlagBuilder b = BeregningsgrunnlagGrunnlagBuilder.oppdatere(nyttGrunnlag)
                                        .medBeregningsgrunnlag(gr.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag")));
                                gr.getRefusjonOverstyringer().ifPresent(b::medRefusjonOverstyring);
                                return b;
                            }
                    );
            bekreftetGrunnlagBuilder
                    .ifPresent(b -> repository.lagre(behandlingId, b, KOFAKBER_UT));
        }
        lagreRegelsporing(input.getKoblingReferanse().getKoblingId(), beregningResultatAggregat.getRegelSporingAggregat());
    }

    private void lagreOgKopier(BeregningsgrunnlagInput input,
                               BeregningResultatAggregat beregningResultatAggregat,
                               Optional<BeregningsgrunnlagEntitet> forrigeBekreftetBeregningsgrunnlag,
                               BeregningsgrunnlagEntitet nyttBg,
                               BeregningsgrunnlagTilstand tilstand, BeregningsgrunnlagTilstand bekreftetTilstand) {
        KoblingReferanse ref = input.getKoblingReferanse();
        Long behandlingId = ref.getKoblingId();
        boolean kanKopiereBekreftet = KopierBeregningsgrunnlag.kanKopiereFraForrigeBekreftetGrunnlag(
                beregningResultatAggregat.getBeregningAksjonspunktResultater(),
                nyttBg,
                finnForrigeBgFraTilstand(input, tilstand),
                forrigeBekreftetBeregningsgrunnlag
        );
        repository.lagre(behandlingId, nyttBg, tilstand);
        if (kanKopiereBekreftet) {
            forrigeBekreftetBeregningsgrunnlag.ifPresent(bg -> repository.lagre(behandlingId, bg, bekreftetTilstand));
        }
        lagreRegelsporing(input.getKoblingReferanse().getKoblingId(), beregningResultatAggregat.getRegelSporingAggregat());
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnForrigeGrunnlagFraTilstand(BeregningsgrunnlagInput input, BeregningsgrunnlagTilstand tilstandFraSteg) {
        KoblingReferanse referanse = input.getKoblingReferanse();
        return repository
                .hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(referanse.getKoblingId(), referanse.getOriginalKoblingId(), tilstandFraSteg);
    }

    private Optional<BeregningsgrunnlagEntitet> finnForrigeBgFraTilstand(BeregningsgrunnlagInput input, BeregningsgrunnlagTilstand tilstandFraSteg) {
        KoblingReferanse koblingReferanse = input.getKoblingReferanse();
        return repository
                .hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(koblingReferanse.getKoblingId(), koblingReferanse.getOriginalKoblingId(), tilstandFraSteg)
                .flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
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

    public TilstandResponse beregnFor(StegType stegType, BeregningsgrunnlagInput input, Long koblingId) {
        rullTilbakeTjeneste.rullTilbakeTilTilstandFørVedBehov(koblingId, MapStegTilTilstand.map(stegType));
        if (stegType.equals(StegType.KOFAKBER)) {
            return kontrollerFaktaBeregningsgrunnlag(input);
        } else if (stegType.equals(StegType.FORS_BERGRUNN)) {
            return foreslåBeregningsgrunnlag(input);
        } else if (stegType.equals(StegType.VURDER_REF_BERGRUNN)) {
            return vurderRefusjonForBeregningsgrunnlaget(input);
        } else if (stegType.equals(StegType.FORDEL_BERGRUNN)) {
            return fordelBeregningsgrunnlag(input);
        } else if (stegType.equals(StegType.FAST_BERGRUNN)) {
            return fastsettBeregningsgrunnlag(input);
        }
        throw new IllegalStateException("Kan ikke beregne for " + stegType.getKode());
    }
}
