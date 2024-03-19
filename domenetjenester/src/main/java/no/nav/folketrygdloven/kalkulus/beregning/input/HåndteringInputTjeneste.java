package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@ApplicationScoped
public class HåndteringInputTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingRepository koblingRepository;

    public HåndteringInputTjeneste() {
        // CDI
    }

    @Inject
    public HåndteringInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                   KoblingRepository koblingRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
    }

    public Map<Long, BeregningsgrunnlagInput> lagBeregningsgrunnlagInput(Set<Long> koblingId,
                                                                         Map<Long, KalkulatorInputDto> inputPrKobling,
                                                                         BeregningsgrunnlagTilstand tilstand) {
        Objects.requireNonNull(koblingId, "koblingId");
        var koblingEntiteter = koblingRepository.hentKoblingerFor(koblingId);
        var grunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingId)
                .stream().collect(Collectors.toMap(BeregningsgrunnlagGrunnlagEntitet::getKoblingId, Function.identity()));
        validerKoblingMotGrunnlag(koblingId, tilstand, grunnlagEntiteter);
        var grunnlagFraForrigeOppdatering = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingId, tilstand, null)
                .stream().collect(Collectors.toMap(BeregningsgrunnlagGrunnlagEntitet::getKoblingId, Function.identity()));
        return koblingEntiteter.stream()
                .collect(Collectors.toMap(KoblingEntitet::getId, kobling ->
                        lagHåndteringBeregningsgrunnlagInput(kobling,
                                inputPrKobling.get(kobling.getId()),
                                grunnlagEntiteter.get(kobling.getId()),
                                tilstand,
                                Optional.ofNullable(grunnlagFraForrigeOppdatering.get(kobling.getId())))
                ));
    }

    private void validerKoblingMotGrunnlag(Collection<Long> koblingId, BeregningsgrunnlagTilstand tilstand, Map<Long, BeregningsgrunnlagGrunnlagEntitet> grunnlagEntiteter) {
        List<Long> koblingUtenGrunnlag = koblingId.stream().filter(id -> grunnlagEntiteter.keySet().stream().noneMatch(k -> k.equals(id)))
                .collect(Collectors.toList());
        if (!koblingUtenGrunnlag.isEmpty()) {
            throw new IllegalStateException("Skal ha grunnlag for tilstand" + tilstand.getKode() + ". Fant ikke grunnlag for " + koblingUtenGrunnlag);
        }
    }

    private HåndterBeregningsgrunnlagInput lagHåndteringBeregningsgrunnlagInput(KoblingEntitet kobling,
                                                                                KalkulatorInputDto input,
                                                                                BeregningsgrunnlagGrunnlagEntitet aktivGrunnlagEntitet,
                                                                                BeregningsgrunnlagTilstand tilstand,
                                                                                Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagFraHåndteringTilstand) {
        BeregningsgrunnlagInput beregningsgrunnlagInput = MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, Optional.of(aktivGrunnlagEntitet));
        return new HåndterBeregningsgrunnlagInput(beregningsgrunnlagInput, tilstand)
                .medForrigeGrunnlagFraHåndtering(grunnlagFraHåndteringTilstand.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null));
    }


}
