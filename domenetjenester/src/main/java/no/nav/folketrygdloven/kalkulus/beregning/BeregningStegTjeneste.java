package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper.mapBeregningsgrunnlagMedBesteberegning;
import static no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper.mapGrunnlag;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.VurderBeregningsgrunnlagvilkårInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.KalkulatorInterface;
import no.nav.folketrygdloven.kalkulator.steg.besteberegning.BesteberegningResultat;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AvklaringsbehovMedTilstandDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.KalkulusResultatKode;
import no.nav.folketrygdloven.kalkulus.kopiering.SpolFramoverTjeneste;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.VilkårResponse;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.VidereførOverstyringTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelSporingTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelsporingRepository;
import no.nav.foreldrepenger.konfig.Environment;

@ApplicationScoped
public class BeregningStegTjeneste {
    private static final boolean GRADERING_MOT_INNTEKT_ENABLED = Environment.current().getProperty("gradering.mot.inntekt", boolean.class, false);
    private final KalkulatorInterface beregningsgrunnlagTjeneste = new BeregningsgrunnlagTjeneste();
    private BeregningsgrunnlagRepository repository;
    private RegelsporingRepository regelsporingRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private VidereførOverstyringTjeneste videreførOverstyring;
    private RegelSporingTjeneste regelSporingTjeneste;


    BeregningStegTjeneste() {
        // CDI
    }

    @Inject
    public BeregningStegTjeneste(BeregningsgrunnlagRepository repository,
                                 RegelsporingRepository regelsporingRepository,
                                 AvklaringsbehovTjeneste avklaringsbehovTjeneste,
                                 VidereførOverstyringTjeneste videreførOverstyring,
                                 RegelSporingTjeneste regelSporingTjeneste) {
        this.repository = repository;
        this.regelsporingRepository = regelsporingRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
        this.videreførOverstyring = videreførOverstyring;
        this.regelSporingTjeneste = regelSporingTjeneste;
    }

    /**
     * Beregner for gitt steg
     *
     * @param stegType Stegtype - hvilket steg som beregnes (alle etter første steg er mulige)
     * @param input    Steginput
     * @return
     */
    public TilstandResponse beregnFor(BeregningSteg stegType, StegProsesseringInput input) {
        kontrollerIngenUløsteAvklaringsbehovFørSteg(stegType, input.getKoblingId());
        return switch (stegType) {
            case FASTSETT_STP_BER -> fastsettBeregningsaktiviteter((FastsettBeregningsaktiviteterInput) input);
            case KOFAKBER -> kontrollerFaktaBeregningsgrunnlag((FaktaOmBeregningInput) input);
            case FORS_BESTEBEREGNING -> foreslåBesteberegning((ForeslåBesteberegningInput) input);
            case FORS_BERGRUNN -> foreslåBeregningsgrunnlag((ForeslåBeregningsgrunnlagInput) input);
            case FORS_BERGRUNN_2 -> fortsettForeslåBeregningsgrunnlag((FortsettForeslåBeregningsgrunnlagInput) input);
            case VURDER_VILKAR_BERGRUNN -> vurderBeregningsgrunnlagsvilkår((VurderBeregningsgrunnlagvilkårInput) input);
            case VURDER_TILKOMMET_INNTEKT -> vurderTilkommetInntekt(input);
            case VURDER_REF_BERGRUNN ->
                    vurderRefusjonForBeregningsgrunnlaget((VurderRefusjonBeregningsgrunnlagInput) input);
            case FORDEL_BERGRUNN -> fordelBeregningsgrunnlag((FordelBeregningsgrunnlagInput) input);
            case FAST_BERGRUNN -> fastsettBeregningsgrunnlag(input);
        };
    }

    /**
     * FastsettBeregningsaktiviteter
     * Steg 1. FASTSETT_STP_BER
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link TilstandResponse}
     */
    public TilstandResponse fastsettBeregningsaktiviteter(FastsettBeregningsaktiviteterInput input) {
        validerIngenÅpneAvklaringsbehov(input.getKoblingId());
        var resultat = beregningsgrunnlagTjeneste.fastsettBeregningsaktiviteter(input);
        lagreOgKopier(input, resultat);
        leggTilOverstyringHvisFinnes(BeregningSteg.FASTSETT_STP_BER, input.getKoblingId(), resultat);
        lagreAvklaringsbehov(input, resultat);
        return mapTilstandResponse(input.getKoblingReferanse(), resultat);
    }

    /**
     * KontrollerFaktaBeregningsgrunnlag
     * Steg 2. KOFAKBER
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAvklaringsbehovResultat}
     */
    private TilstandResponse kontrollerFaktaBeregningsgrunnlag(FaktaOmBeregningInput input) {
        var beregningResultatAggregat = beregningsgrunnlagTjeneste.kontrollerFaktaBeregningsgrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        leggTilOverstyringHvisFinnes(BeregningSteg.KOFAKBER, input.getKoblingId(), beregningResultatAggregat);
        lagreAvklaringsbehov(input, beregningResultatAggregat);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
    }

    /**
     * ForeslåBeregningsgrunnlag
     * Steg 3. FORS_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAvklaringsbehovResultat}
     */
    private TilstandResponse foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input) {
        var beregningResultatAggregat = beregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        lagreAvklaringsbehov(input, beregningResultatAggregat);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
    }

    /**
     * ForeslåBeregningsgrunnlag
     * Steg 4. FORS_BERGRUNN_2
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAvklaringsbehovResultat}
     */
    private TilstandResponse fortsettForeslåBeregningsgrunnlag(FortsettForeslåBeregningsgrunnlagInput input) {
        var beregningResultatAggregat = beregningsgrunnlagTjeneste.fortsettForeslåBeregningsgrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        lagreAvklaringsbehov(input, beregningResultatAggregat);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
    }

    /**
     * ForeslåBesteberegning
     * Steg 4.5. FORS_BESTEBEREGNING
     * <p>
     * Dette steget vil aldri bli brukt av noe annet enn foreldrepenger, men legges inn her for å kunne testes via verdikjedetest
     *
     * @param input {@link ForeslåBeregningsgrunnlagInput}
     * @return {@link BeregningAvklaringsbehovResultat}
     */
    private TilstandResponse foreslåBesteberegning(ForeslåBesteberegningInput input) {
        if (input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag foreldrepengerGrunnlag && foreldrepengerGrunnlag.isKvalifisererTilBesteberegning()) {
                var beregningResultatAggregat = (BesteberegningResultat) beregningsgrunnlagTjeneste.foreslåBesteberegning(input);
                var grunnlagBuilder = mapGrunnlag(beregningResultatAggregat.getBeregningsgrunnlagGrunnlag());

                beregningResultatAggregat.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes().ifPresent(beregningsgrunnlagDto ->
                    grunnlagBuilder.medBeregningsgrunnlag(mapBeregningsgrunnlagMedBesteberegning(beregningsgrunnlagDto, beregningResultatAggregat.getBesteberegningVurderingGrunnlag())));

                repository.lagre(input.getKoblingId(), grunnlagBuilder, input.getStegTilstand());
                lagreRegelsporing(input.getKoblingId(), beregningResultatAggregat.getRegelSporingAggregat(), input.getStegTilstand());
                return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
        }
        return new TilstandResponse(input.getKoblingReferanse().getKoblingUuid(), KalkulusResultatKode.BEREGNET, List.of());
    }

    /**
     * VurderRefusjonBeregningsgrunnlag
     * Steg 5. VURDER_VILKAR_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAvklaringsbehovResultat}
     */
    private TilstandResponse vurderBeregningsgrunnlagsvilkår(VurderBeregningsgrunnlagvilkårInput input) {
        var beregningResultatAggregat = beregningsgrunnlagTjeneste.vurderBeregningsgrunnlagvilkår(input);
        lagreOgKopier(input, beregningResultatAggregat);
        if (beregningResultatAggregat.getBeregningVilkårResultat() == null) {
            throw new IllegalStateException("Hadde ikke vilkårsresultat for input med ref " + input.getKoblingReferanse());
        }
        lagreAvklaringsbehov(input, beregningResultatAggregat);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
    }

    /**
     * Vurder tilkommet inntekt (ikke påkrevd steg)
     * Steg 6. VURDER_TILKOMMET_INNTEKT
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAvklaringsbehovResultat}
     */
    private TilstandResponse vurderTilkommetInntekt(StegProsesseringInput input) {
        if (!GRADERING_MOT_INNTEKT_ENABLED) {
            return new TilstandResponse(input.getKoblingReferanse().getKoblingUuid(), KalkulusResultatKode.BEREGNET);
        }
        var beregningResultatAggregat = beregningsgrunnlagTjeneste.vurderTilkommetInntekt(input);
        lagreOgKopier(input, beregningResultatAggregat);
        lagreAvklaringsbehov(input, beregningResultatAggregat);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
    }


    /**
     * VurderRefusjonBeregningsgrunnlag
     * Steg 7. VURDER_REF_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAvklaringsbehovResultat}
     */
    private TilstandResponse vurderRefusjonForBeregningsgrunnlaget(VurderRefusjonBeregningsgrunnlagInput input) {
        var beregningResultatAggregat = beregningsgrunnlagTjeneste.vurderRefusjonskravForBeregninggrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        lagreAvklaringsbehov(input, beregningResultatAggregat);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);

    }

    /**
     * FordelBeregningsgrunnlag
     * Steg 8. FORDEL_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAvklaringsbehovResultat}
     */
    private TilstandResponse fordelBeregningsgrunnlag(FordelBeregningsgrunnlagInput input) {
        var beregningResultatAggregat = beregningsgrunnlagTjeneste.fordelBeregningsgrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        lagreAvklaringsbehov(input, beregningResultatAggregat);
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
    }

    /**
     * FastsettBeregningsgrunnlagSteg
     * Steg 9. FAST_BERGRUNN
     *
     * @param input {@link BeregningsgrunnlagInput}
     */
    public TilstandResponse fastsettBeregningsgrunnlag(StegProsesseringInput input) {
        var beregningResultatAggregat = beregningsgrunnlagTjeneste.fastsettBeregningsgrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        regelsporingRepository.slettAlleInaktiveRegelsporinger(input.getKoblingId());
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
    }

    private void lagreOgKopier(StegProsesseringInput input,
                               BeregningResultatAggregat resultat) {
        // Lagring av grunnlag fra steg
        repository.lagre(input.getKoblingId(), mapGrunnlag(resultat.getBeregningsgrunnlagGrunnlag()), input.getStegTilstand());
        lagreRegelsporing(input.getKoblingReferanse().getKoblingId(), resultat.getRegelSporingAggregat(), input.getStegTilstand());
        // Kopiering av data og spoling fram til neste tilstand
        SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(resultat.getBeregningAvklaringsbehovResultater(),
                resultat.getBeregningsgrunnlagGrunnlag(),
                        input.getForrigeGrunnlagFraSteg(),
                        input.getForrigeGrunnlagFraStegUt())
                .map(KalkulatorTilEntitetMapper::mapGrunnlag)
                .ifPresent(gr -> repository.lagre(input.getKoblingId(), gr, input.getStegUtTilstand()));
    }


    private TilstandResponse mapTilstandResponse(KoblingReferanse koblingReferanse, BeregningResultatAggregat
            resultat) {
        var avklaringsbehov = resultat.getBeregningAvklaringsbehovResultater().stream()
                .map(res -> new AvklaringsbehovMedTilstandDto(
                        res.getBeregningAvklaringsbehovDefinisjon(),
                        res.getVenteårsak(),
                        res.getVentefrist()))
                .toList();
        if (resultat.getBeregningVilkårResultat() != null) {
            var regelsporingVilkårsVurdering = resultat.getRegelSporingAggregat().map(RegelSporingAggregat::regelsporingPerioder).orElse(List.of()).stream()
                .filter(rs -> rs.regelType().equals(BeregningsgrunnlagPeriodeRegelType.VILKÅR_VURDERING))
                .findFirst()
                .orElseThrow();
            var avslagsårsak = resultat.getBeregningVilkårResultat().getErVilkårOppfylt() ? null : resultat.getBeregningVilkårResultat().getVilkårsavslagsårsak();
            var vilkårRespons = new VilkårResponse(resultat.getBeregningVilkårResultat().getErVilkårOppfylt(),
                regelsporingVilkårsVurdering.regelEvaluering(), regelsporingVilkårsVurdering.regelInput(),
                regelsporingVilkårsVurdering.regelVersjon(), avslagsårsak);
            return new TilstandResponse(koblingReferanse.getKoblingUuid(),
                    avklaringsbehov,
                    avklaringsbehov.isEmpty() ? KalkulusResultatKode.BEREGNET : KalkulusResultatKode.BEREGNET_MED_AVKLARINGSBEHOV,
                    resultat.getBeregningVilkårResultat().getErVilkårOppfylt(),
                    resultat.getBeregningVilkårResultat().getErVilkårOppfylt() ? null : resultat.getBeregningVilkårResultat().getVilkårsavslagsårsak(), vilkårRespons);
        } else {
            return new TilstandResponse(koblingReferanse.getKoblingUuid(), avklaringsbehov.isEmpty() ? KalkulusResultatKode.BEREGNET : KalkulusResultatKode.BEREGNET_MED_AVKLARINGSBEHOV, avklaringsbehov);
        }
    }

    private void lagreRegelsporing(Long
                                           koblingId, Optional<RegelSporingAggregat> regelsporinger, BeregningsgrunnlagTilstand stegTilstand) {
        if (regelsporinger.isPresent()) {
            lagreRegelSporingPerioder(koblingId, regelsporinger.get(), stegTilstand);
            lagreRegelSporingGrunnlag(koblingId, regelsporinger.get(), stegTilstand);
        }
    }

    private void lagreRegelSporingGrunnlag(Long koblingId, RegelSporingAggregat
            regelsporinger, BeregningsgrunnlagTilstand stegTilstand) {
        var regelsporingGrunnlag = regelsporinger.regelsporingerGrunnlag();
        if (regelsporingGrunnlag != null) {
            regelsporingGrunnlag.forEach(sporing -> {
                validerRiktigTilstandForGrunnlagSporing(stegTilstand, sporing.regelType());
                RegelSporingGrunnlagEntitet.Builder sporingGrunnlagEntitet = RegelSporingGrunnlagEntitet.ny()
                        .medRegelEvaluering(sporing.regelEvaluering())
                        .medRegelInput(sporing.regelInput())
                        .medRegelVersjon(sporing.regelVersjon());
                regelsporingRepository.lagre(koblingId, sporingGrunnlagEntitet, sporing.regelType());
            });
        }
    }

    private void lagreRegelSporingPerioder(Long koblingId, RegelSporingAggregat
            regelsporinger, BeregningsgrunnlagTilstand stegTilstand) {
        if (regelsporinger.regelsporingPerioder() != null) {
            Map<BeregningsgrunnlagPeriodeRegelType, List<RegelSporingPeriode>> sporingPerioderPrType = regelsporinger.regelsporingPerioder()
                    .stream()
                    .collect(Collectors.groupingBy(RegelSporingPeriode::regelType));
            validerRiktigTilstandForPeriodeSporing(stegTilstand, sporingPerioderPrType);
            regelSporingTjeneste.lagre(koblingId, regelsporinger.regelsporingPerioder());
        }
    }

    private void validerRiktigTilstandForPeriodeSporing(BeregningsgrunnlagTilstand
                                                                stegTilstand, Map<BeregningsgrunnlagPeriodeRegelType, List<RegelSporingPeriode>> sporingPerioderPrType) {
        Optional<BeregningsgrunnlagPeriodeRegelType> typeMedFeilTilstand = sporingPerioderPrType.keySet().stream().filter(type -> !erPeriodeRegelLagretIGyldigTilstand(stegTilstand, type)).findFirst();

        if (typeMedFeilTilstand.isPresent()) {
            throw new IllegalStateException("Kan ikke lagre regelsporing for " + typeMedFeilTilstand.get().getKode() + " i tilstand " + stegTilstand.getKode());
        }
    }

    private void validerRiktigTilstandForGrunnlagSporing(BeregningsgrunnlagTilstand
                                                                 stegTilstand, BeregningsgrunnlagRegelType regelType) {
        if (!erGrunnlagRegelLagretIGyldigTilstand(stegTilstand, regelType)) {
            throw new IllegalStateException("Kan ikke lagre regelsporing for " + regelType.getKode() + " i tilstand " + stegTilstand.getKode());
        }
    }

    private boolean erPeriodeRegelLagretIGyldigTilstand(BeregningsgrunnlagTilstand
                                                                stegTilstand, BeregningsgrunnlagPeriodeRegelType regelType) {
        return regelType.getLagretTilstand().equals(stegTilstand);
    }


    private boolean erGrunnlagRegelLagretIGyldigTilstand(BeregningsgrunnlagTilstand
                                                                 stegTilstand, BeregningsgrunnlagRegelType regelType) {
        return regelType.getLagretTilstand().equals(stegTilstand);
    }

    private void lagreAvklaringsbehov(StegProsesseringInput input, BeregningResultatAggregat
            beregningResultatAggregat) {
        // Lagrer ikke ventepunkter i kalkulus da det ikke finnes en mekanisme i k9-sak som samspiller med dette
        var avklaringsbehovSomLagresIKalkulus = beregningResultatAggregat.getBeregningAvklaringsbehovResultater().stream()
                .map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon)
                .filter(ap -> !ap.erVentepunkt())
                .toList();
        avklaringsbehovTjeneste.lagreAvklaringsresultater(input.getKoblingId(), avklaringsbehovSomLagresIKalkulus);
    }

    private void kontrollerIngenUløsteAvklaringsbehovFørSteg(BeregningSteg stegType, Long koblingId) {
        avklaringsbehovTjeneste.validerIngenAvklaringsbehovFørStegÅpne(stegType, koblingId);
    }

    private void validerIngenÅpneAvklaringsbehov(Long koblingId) {
        avklaringsbehovTjeneste.validerIngenÅpneAvklaringsbehovPåKobling(koblingId);
    }

    private void leggTilOverstyringHvisFinnes(BeregningSteg steg, Long koblingId, BeregningResultatAggregat beregningResultatAggregat) {
        videreførOverstyring.videreførOverstyringForSteg(koblingId, steg)
                .map(it -> BeregningAvklaringsbehovResultat.opprettFor(it.getDefinisjon()))
                .ifPresent(beregningResultatAggregat::leggTilAvklaringsbehov);
    }
}
