package no.nav.folketrygdloven.kalkulus.beregning.input;

import static no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand.mapTilStegTilstand;
import static no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand.mapTilStegUtTilstand;
import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.FullføreBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@ApplicationScoped
public class StegProsessInputTjeneste {
    private static final Logger log = LoggerFactory.getLogger(StegProsessInputTjeneste.class);

    public static final MonthDay ENDRING_AV_GRUNNBELØP = MonthDay.of(5, 1);

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingRepository koblingRepository;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    public StegProsessInputTjeneste() {
        // CDI
    }

    @Inject
    public StegProsessInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository, KoblingRepository koblingRepository, KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
    }

    public FastsettBeregningsaktiviteterInput lagStartInput(KoblingEntitet koblingEntitet, KalkulatorInputDto input) {
        var beregningsgrunnlagInput = mapFraKalkulatorInputTilBeregningsgrunnlagInput(koblingEntitet, input, Optional.empty());
        var grunnlagFraSteg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(
                koblingEntitet,
                beregningsgrunnlagInput.getSkjæringstidspunktOpptjening(),
                BeregningsgrunnlagTilstand.OPPRETTET);
        var grunnlagFraStegUt = finnForrigeAvklarteGrunnlagForTilstand(grunnlagFraSteg, BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        var stegProsesseringInput = new StegProsesseringInput(beregningsgrunnlagInput, BeregningsgrunnlagTilstand.OPPRETTET)
                .medForrigeGrunnlagFraStegUt(grunnlagFraStegUt.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medForrigeGrunnlagFraSteg(grunnlagFraSteg.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medStegUtTilstand(BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        return new FastsettBeregningsaktiviteterInput(stegProsesseringInput).medGrunnbeløpsatser(finnSatser());
    }

    public Resultat<StegProsesseringInput> lagFortsettInput(List<Long> koblingId,
                                                            BeregningSteg stegType,
                                                            Map<UUID, List<UUID>> koblingRelasjon) {
        Objects.requireNonNull(koblingId, "koblingId");
        var koblingEntiteter = koblingRepository.hentKoblingerFor(koblingId);
        var inputRespons = kalkulatorInputTjeneste.hentForKoblinger(koblingId);
        if (inputRespons.getKode() == HentInputResponsKode.ETTERSPØR_NY_INPUT) {
            return new Resultat<>(inputRespons.getKode());
        }

        var grunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingId)
                .stream().collect(Collectors.toMap(BeregningsgrunnlagGrunnlagEntitet::getKoblingId, Function.identity()));
        List<Long> koblingUtenGrunnlag = koblingId.stream().filter(id -> grunnlagEntiteter.keySet().stream().noneMatch(k -> k.equals(id)))
                .collect(Collectors.toList());
        if (!koblingUtenGrunnlag.isEmpty()) {
            throw new IllegalStateException("Skal ha grunnlag i steg" + stegType.getKode() + ". Fant ikke grunnlag for " + koblingUtenGrunnlag);
        }

        Map<Long, StegProsesseringInput> koblingStegInputMap = koblingEntiteter.stream()
                .collect(Collectors.toMap(
                        KoblingEntitet::getId,
                        id -> mapStegInput(id, inputRespons.getResultatPrKobling().get(id.getId()), grunnlagEntiteter.get(id.getId()), stegType, finnOriginalKobling(id, koblingRelasjon))
                        ));
        return new Resultat<>(HentInputResponsKode.GYLDIG_INPUT, koblingStegInputMap);
    }

    private List<UUID> finnOriginalKobling(KoblingEntitet kobling, Map<UUID, List<UUID>> koblingRelasjon) {
        return koblingRelasjon.getOrDefault(kobling.getKoblingReferanse().getReferanse(), Collections.emptyList());
    }


    private StegProsesseringInput mapStegInput(KoblingEntitet kobling,
                                              KalkulatorInputDto input,
                                              BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet,
                                              BeregningSteg stegType,
                                              List<UUID> originaleKoblinger) {
        StegProsesseringInput stegProsesseringInput = lagStegProsesseringInput(kobling, input, grunnlagEntitet, stegType);
        if (stegType.equals(BeregningSteg.KOFAKBER)) {
            return new FaktaOmBeregningInput(stegProsesseringInput).medGrunnbeløpsatser(finnSatser());
        } else if (stegType.equals(BeregningSteg.FORS_BESTEBEREGNING)) {
            return lagInputForeslåBesteberegning(stegProsesseringInput);
        } else if (stegType.equals(BeregningSteg.FORS_BERGRUNN)) {
            return lagInputForeslå(stegProsesseringInput);
        } else if (stegType.equals(BeregningSteg.VURDER_REF_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning(), stegProsesseringInput.getSkjæringstidspunktOpptjening());
            return lagInputVurderRefusjon(stegProsesseringInput, førsteFastsatteGrunnlagEntitet, originaleKoblinger);
        } else if (stegType.equals(BeregningSteg.FORDEL_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning(), stegProsesseringInput.getSkjæringstidspunktOpptjening());
            return lagInputFordel(stegProsesseringInput, førsteFastsatteGrunnlagEntitet);
        } else if (stegType.equals(BeregningSteg.FAST_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning(), stegProsesseringInput.getSkjæringstidspunktOpptjening());
            return lagInputFullføre(stegProsesseringInput, førsteFastsatteGrunnlagEntitet);
        }
        return stegProsesseringInput;
    }

    private StegProsesseringInput lagInputVurderRefusjon(StegProsesseringInput stegProsesseringInput,
                                                         Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet,
                                                         List<UUID> originaleKoblinger) {
        var vurderVilkårOgRefusjonBeregningsgrunnlag = new VurderRefusjonBeregningsgrunnlagInput(stegProsesseringInput);
        Optional<BeregningsgrunnlagGrunnlagDto> originaltGrunnlag = finnOriginaltGrunnlag(originaleKoblinger);
        if (originaltGrunnlag.isPresent()) {
            vurderVilkårOgRefusjonBeregningsgrunnlag = vurderVilkårOgRefusjonBeregningsgrunnlag.medBeregningsgrunnlagGrunnlagFraForrigeBehandling(originaltGrunnlag.get());
        }
        if (førsteFastsatteGrunnlagEntitet.isPresent()) {
            vurderVilkårOgRefusjonBeregningsgrunnlag = førsteFastsatteGrunnlagEntitet.get().getBeregningsgrunnlag()
                    .map(BeregningsgrunnlagEntitet::getGrunnbeløp)
                    .map(Beløp::getVerdi)
                    .map(vurderVilkårOgRefusjonBeregningsgrunnlag::medUregulertGrunnbeløp)
                    .orElse(vurderVilkårOgRefusjonBeregningsgrunnlag);
        }
        return vurderVilkårOgRefusjonBeregningsgrunnlag;
    }

    private Optional<BeregningsgrunnlagGrunnlagDto> finnOriginaltGrunnlag(List<UUID> originaleKoblinger) {
        if (originaleKoblinger.isEmpty()) {
            return Optional.empty();
        }
        if (originaleKoblinger.size() > 1) {
            log.info("Fikk inn flere originale kobliner, støtter ikke mapping om til et enkelt grunnlag av disse, returnerer empty");
            return Optional.empty();
        }
        Long koblingId = koblingRepository.hentKoblingIdForKoblingReferanse(new KoblingReferanse(originaleKoblinger.get(0)));
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        return entitet.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag);

    }

    private StegProsesseringInput lagStegProsesseringInput(KoblingEntitet kobling, KalkulatorInputDto input, BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet, BeregningSteg stegType) {
        var beregningsgrunnlagInput = MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, Optional.of(grunnlagEntitet));
        var grunnlagFraSteg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(
                kobling,
                beregningsgrunnlagInput.getSkjæringstidspunktOpptjening(),
                mapTilStegTilstand(stegType));
        var grunnlagFraStegUt = finnForrigeAvklartGrunnlagHvisFinnes(grunnlagFraSteg, stegType);
        return new StegProsesseringInput(beregningsgrunnlagInput, mapTilStegTilstand(stegType))
                .medForrigeGrunnlagFraStegUt(grunnlagFraStegUt.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medForrigeGrunnlagFraSteg(grunnlagFraSteg.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medStegUtTilstand(mapTilStegUtTilstand(stegType).orElse(null));
    }

    private ForeslåBesteberegningInput lagInputForeslåBesteberegning(StegProsesseringInput stegProsesseringInput) {
        var input = new ForeslåBesteberegningInput(stegProsesseringInput);
        return input.medGrunnbeløpsatser(finnSatser());
    }

    private ForeslåBeregningsgrunnlagInput lagInputForeslå(StegProsesseringInput stegProsesseringInput) {
        var foreslåBeregningsgrunnlagInput = new ForeslåBeregningsgrunnlagInput(stegProsesseringInput);
        return foreslåBeregningsgrunnlagInput.medGrunnbeløpsatser(finnSatser());
    }

    private FordelBeregningsgrunnlagInput lagInputFordel(StegProsesseringInput stegProsesseringInput, Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet) {
        var fordelBeregningsgrunnlagInput = new FordelBeregningsgrunnlagInput(stegProsesseringInput);
        if (førsteFastsatteGrunnlagEntitet.isPresent()) {
            fordelBeregningsgrunnlagInput = førsteFastsatteGrunnlagEntitet.get().getBeregningsgrunnlag()
                    .map(BeregningsgrunnlagEntitet::getGrunnbeløp)
                    .map(Beløp::getVerdi)
                    .map(fordelBeregningsgrunnlagInput::medUregulertGrunnbeløp)
                    .orElse(fordelBeregningsgrunnlagInput);
        }
        return fordelBeregningsgrunnlagInput;
    }

    private FullføreBeregningsgrunnlagInput lagInputFullføre(StegProsesseringInput stegProsesseringInput, Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet) {
        var fullføreBeregningsgrunnlagInput = new FullføreBeregningsgrunnlagInput(stegProsesseringInput);
        if (førsteFastsatteGrunnlagEntitet.isPresent()) {
            fullføreBeregningsgrunnlagInput = førsteFastsatteGrunnlagEntitet.get().getBeregningsgrunnlag()
                    .map(BeregningsgrunnlagEntitet::getGrunnbeløp)
                    .map(Beløp::getVerdi)
                    .map(fullføreBeregningsgrunnlagInput::medUregulertGrunnbeløp)
                    .orElse(fullføreBeregningsgrunnlagInput);
        }
        return fullføreBeregningsgrunnlagInput;
    }


    private List<Grunnbeløp> finnSatser() {
        return new GrunnbeløpMapper(beregningsgrunnlagRepository.finnAlleSatser()).mapGrunnbeløpSatser();
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(KoblingEntitet koblingEntitet,
                                                                                                                             LocalDate skjæringstidspunktBeregning,
                                                                                                                             LocalDate skjæringstidspunktOpptjening) {
        if (MonthDay.from(skjæringstidspunktBeregning).isBefore(ENDRING_AV_GRUNNBELØP)) {
            return Optional.empty();
        }
        return beregningsgrunnlagRepository.hentSisteGrunnlagForSkjæringstidspunktOgTilstand(
                koblingEntitet.getSaksnummer(),
                koblingEntitet.getAktørId(),
                koblingEntitet.getYtelseTyperKalkulusStøtter(),
                skjæringstidspunktOpptjening, BeregningsgrunnlagTilstand.FASTSATT);
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnForrigeAvklartGrunnlagHvisFinnes(Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeGrunnlagFraSteg,
                                                                                             BeregningSteg stegType) {
        Optional<BeregningsgrunnlagTilstand> tilstandUt = mapTilStegUtTilstand(stegType);
        if (tilstandUt.isEmpty()) {
            return Optional.empty();
        }
        return finnForrigeAvklarteGrunnlagForTilstand(forrigeGrunnlagFraSteg, tilstandUt.get());
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnForrigeAvklarteGrunnlagForTilstand(Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeGrunnlagFraSteg, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        return forrigeGrunnlagFraSteg
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .flatMap(koblingId -> beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetOpprettetEtter(koblingId, forrigeGrunnlagFraSteg.get().getOpprettetTidspunkt(), beregningsgrunnlagTilstand));
    }

}
