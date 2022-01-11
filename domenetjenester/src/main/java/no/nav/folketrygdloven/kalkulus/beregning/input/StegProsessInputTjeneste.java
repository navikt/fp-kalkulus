package no.nav.folketrygdloven.kalkulus.beregning.input;

import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@ApplicationScoped
public class StegProsessInputTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingRepository koblingRepository;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private StegInputMapper stegInputMapper;

    public StegProsessInputTjeneste() {
        // CDI
    }

    @Inject
    public StegProsessInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository, KoblingRepository koblingRepository, KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.stegInputMapper = new StegInputMapper(beregningsgrunnlagRepository);
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

    @Deprecated // Bruk kalkulatorInputTjeneste og lagBeregningsgrunnlagInput separat (sjå OperereKalkulusOrkestrerer)
    public Map<Long, StegProsesseringInput> lagFortsettInput(Set<Long> koblingId,
                                                             BeregningSteg stegType) {
        Objects.requireNonNull(koblingId, "koblingId");
        var inputRespons = kalkulatorInputTjeneste.hentForKoblinger(koblingId);
        return lagBeregningsgrunnlagInput(koblingId, inputRespons, stegType, List.of());
    }

    public Map<Long, StegProsesseringInput> lagBeregningsgrunnlagInput(Set<Long> koblingId,
                                                                       Map<Long, KalkulatorInputDto> inputPrKobling,
                                                                       BeregningSteg stegType,
                                                                       List<KoblingRelasjon> koblingRelasjoner) {
        var koblingEntiteter = koblingRepository.hentKoblingerFor(koblingId);
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
                        id -> stegInputMapper.mapStegInput(id, inputPrKobling.get(id.getId()), grunnlagEntiteter.get(id.getId()), stegType, finnOriginalKobling(id, koblingRelasjoner))
                        ));
        return koblingStegInputMap;
    }

    private List<Long> finnOriginalKobling(KoblingEntitet kobling, List<KoblingRelasjon> koblingRelasjoner) {
        return koblingRelasjoner.stream()
                .filter(r -> r.getKoblingId().equals(kobling.getId()))
                .map(KoblingRelasjon::getOriginalKoblingId)
                .collect(Collectors.toList());
    }


    private List<Grunnbeløp> finnSatser() {
        return new GrunnbeløpMapper(beregningsgrunnlagRepository.finnAlleSatser()).mapGrunnbeløpSatser();
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnForrigeAvklarteGrunnlagForTilstand(Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeGrunnlagFraSteg, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        return forrigeGrunnlagFraSteg
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .flatMap(koblingId -> beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetOpprettetEtter(koblingId, forrigeGrunnlagFraSteg.get().getOpprettetTidspunkt(), beregningsgrunnlagTilstand));
    }

}
