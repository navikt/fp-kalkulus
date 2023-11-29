package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper.mapGrunnlag;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
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
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagFRISINNTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.KalkulatorInterface;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AvklaringsbehovMedTilstandDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelSporingGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.KalkulusResultatKode;
import no.nav.folketrygdloven.kalkulus.kopiering.SpolFramoverTjeneste;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.VidereførOverstyring;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.forlengelse.ForlengelseTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelSporingTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelsporingRepository;

@ApplicationScoped
public class BeregningStegTjeneste {
    private static final boolean GRADERING_MOT_INNTEKT_ENABLED = KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false);
    private final KalkulatorInterface beregningsgrunnlagTjeneste = new BeregningsgrunnlagTjeneste();
    private final KalkulatorInterface beregningsgrunnlagFrisinnTjeneste = new BeregningsgrunnlagFRISINNTjeneste();
    private BeregningsgrunnlagRepository repository;
    private RegelsporingRepository regelsporingRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private ForlengelseTjeneste forlengelseTjeneste;
    private Instance<VidereførOverstyring> videreførOverstyring;
    private RegelSporingTjeneste regelSporingTjeneste;


    BeregningStegTjeneste() {
        // CDI
    }

    @Inject
    public BeregningStegTjeneste(BeregningsgrunnlagRepository repository,
                                 RegelsporingRepository regelsporingRepository,
                                 AvklaringsbehovTjeneste avklaringsbehovTjeneste,
                                 ForlengelseTjeneste forlengelseTjeneste,
                                 @Any Instance<VidereførOverstyring> videreførOverstyring,
                                 RegelSporingTjeneste regelSporingTjeneste) {
        this.repository = repository;
        this.regelsporingRepository = regelsporingRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
        this.forlengelseTjeneste = forlengelseTjeneste;
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
            case FORTS_FORS_BERGRUNN -> fortsettForeslåBeregningsgrunnlag((FortsettForeslåBeregningsgrunnlagInput) input);
            case VURDER_VILKAR_BERGRUNN -> vurderBeregningsgrunnlagsvilkår((VurderBeregningsgrunnlagvilkårInput) input);
            case VURDER_TILKOMMET_INNTEKT -> vurderTilkommetInntekt(input);
            case VURDER_REF_BERGRUNN -> vurderRefusjonForBeregningsgrunnlaget((VurderRefusjonBeregningsgrunnlagInput) input);
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
        var resultat = velgKalkulatorImplementasjon(input).fastsettBeregningsaktiviteter(input);
        lagreOgKopier(input, resultat);
        leggTilOverstyringHvisFinnes(BeregningSteg.FASTSETT_STP_BER, input.getFagsakYtelseType(), input.getKoblingId(), resultat);
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
        var beregningResultatAggregat = velgKalkulatorImplementasjon(input).kontrollerFaktaBeregningsgrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        leggTilOverstyringHvisFinnes(BeregningSteg.KOFAKBER, input.getFagsakYtelseType(), input.getKoblingId(), beregningResultatAggregat);
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
        var beregningResultatAggregat = velgKalkulatorImplementasjon(input).foreslåBeregningsgrunnlag(input);
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
        var beregningResultatAggregat = velgKalkulatorImplementasjon(input).fortsettForeslåBeregningsgrunnlag(input);
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
        if (input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag foreldrepengerGrunnlag) {
            if (foreldrepengerGrunnlag.isKvalifisererTilBesteberegning()) {
                var beregningResultatAggregat = velgKalkulatorImplementasjon(input).foreslåBesteberegning(input);
                repository.lagre(input.getKoblingId(), mapGrunnlag(beregningResultatAggregat.getBeregningsgrunnlagGrunnlag()), input.getStegTilstand());
                lagreRegelsporing(input.getKoblingId(), beregningResultatAggregat.getRegelSporingAggregat(), input.getStegTilstand());
                return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
            }
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
        var beregningResultatAggregat = velgKalkulatorImplementasjon(input).vurderBeregningsgrunnlagvilkår(input);
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
        var beregningResultatAggregat = velgKalkulatorImplementasjon(input).vurderTilkommetInntekt(input);
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
        var beregningResultatAggregat = velgKalkulatorImplementasjon(input).vurderRefusjonskravForBeregninggrunnlag(input);
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
        var beregningResultatAggregat = velgKalkulatorImplementasjon(input).fordelBeregningsgrunnlag(input);
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
        var beregningResultatAggregat = velgKalkulatorImplementasjon(input).fastsettBeregningsgrunnlag(input);
        lagreOgKopier(input, beregningResultatAggregat);
        regelsporingRepository.slettAlleInaktiveRegelsporinger(input.getKoblingId());
        return mapTilstandResponse(input.getKoblingReferanse(), beregningResultatAggregat);
    }

    private void lagreOgKopier(StegProsesseringInput input,
                               BeregningResultatAggregat resultat) {
        // Validering ved forlengelse og kopier bg utenfor forlengelseperioder
        var forlengetGrunnlag = forlengelseTjeneste.forlengEksisterendeBeregningsgrunnlag(input.getForlengelseperioder(), resultat.getBeregningsgrunnlagGrunnlag(), input.getOriginalGrunnlagFraSteg());

        // Lagring av grunnlag fra steg
        repository.lagre(input.getKoblingId(), mapGrunnlag(forlengetGrunnlag), input.getStegTilstand());
        lagreRegelsporing(input.getKoblingReferanse().getKoblingId(), resultat.getRegelSporingAggregat(), input.getStegTilstand());
        // Kopiering av data og spoling fram til neste tilstand
        SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(resultat.getBeregningAvklaringsbehovResultater(),
                        forlengetGrunnlag,
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
                .collect(Collectors.toList());
        if (resultat.getBeregningVilkårResultat() != null) {
            return new TilstandResponse(koblingReferanse.getKoblingUuid(),
                    avklaringsbehov,
                    avklaringsbehov.isEmpty() ? KalkulusResultatKode.BEREGNET : KalkulusResultatKode.BEREGNET_MED_AVKLARINGSBEHOV,
                    resultat.getBeregningVilkårResultat().getErVilkårOppfylt(),
                    resultat.getBeregningVilkårResultat().getErVilkårOppfylt() ? null : resultat.getBeregningVilkårResultat().getVilkårsavslagsårsak());
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
        var regelsporingGrunnlag = regelsporinger.getRegelsporingerGrunnlag();
        if (regelsporingGrunnlag != null) {
            regelsporingGrunnlag.forEach(sporing -> {
                validerRiktigTilstandForGrunnlagSporing(stegTilstand, sporing.getRegelType());
                RegelSporingGrunnlagEntitet.Builder sporingGrunnlagEntitet = RegelSporingGrunnlagEntitet.ny()
                        .medRegelEvaluering(sporing.getRegelEvaluering())
                        .medRegelInput(sporing.getRegelInput());
                regelsporingRepository.lagre(koblingId, sporingGrunnlagEntitet, sporing.getRegelType());
            });
        }
    }

    private void lagreRegelSporingPerioder(Long koblingId, RegelSporingAggregat
            regelsporinger, BeregningsgrunnlagTilstand stegTilstand) {
        if (regelsporinger.getRegelsporingPerioder() != null) {
            Map<BeregningsgrunnlagPeriodeRegelType, List<RegelSporingPeriode>> sporingPerioderPrType = regelsporinger.getRegelsporingPerioder()
                    .stream()
                    .collect(Collectors.groupingBy(RegelSporingPeriode::getRegelType));
            validerRiktigTilstandForPeriodeSporing(stegTilstand, sporingPerioderPrType);
            regelSporingTjeneste.lagre(koblingId, regelsporinger.getRegelsporingPerioder());
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
        List<AvklaringsbehovDefinisjon> avklaringsbehovSomLagresIKalkulus = beregningResultatAggregat.getBeregningAvklaringsbehovResultater().stream()
                .map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon)
                .filter(ap -> !ap.erVentepunkt())
                .collect(Collectors.toList());
        avklaringsbehovTjeneste.lagreAvklaringsresultater(input.getKoblingId(), avklaringsbehovSomLagresIKalkulus);
    }

    private void kontrollerIngenUløsteAvklaringsbehovFørSteg(BeregningSteg stegType, Long koblingId) {
        avklaringsbehovTjeneste.validerIngenAvklaringsbehovFørStegÅpne(stegType, koblingId);
    }

    private void validerIngenÅpneAvklaringsbehov(Long koblingId) {
        avklaringsbehovTjeneste.validerIngenÅpneAvklaringsbehovPåKobling(koblingId);
    }

    private void leggTilOverstyringHvisFinnes(BeregningSteg steg, FagsakYtelseType fagsakYtelseType, Long koblingId, BeregningResultatAggregat beregningResultatAggregat) {
        VidereførOverstyring.finnTjeneste(videreførOverstyring, fagsakYtelseType)
                .videreførOverstyringForSteg(koblingId, steg)
                .map(it -> BeregningAvklaringsbehovResultat.opprettFor(it.getDefinisjon()))
                .ifPresent(beregningResultatAggregat::leggTilAvklaringsbehov);
    }

    private KalkulatorInterface velgKalkulatorImplementasjon(StegProsesseringInput input) {
        return FagsakYtelseType.FRISINN.equals(input.getFagsakYtelseType()) ? beregningsgrunnlagFrisinnTjeneste : beregningsgrunnlagTjeneste;
    }
}
