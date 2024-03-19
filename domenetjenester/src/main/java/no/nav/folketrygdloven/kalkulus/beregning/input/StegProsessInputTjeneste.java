package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@ApplicationScoped
public class StegProsessInputTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingRepository koblingRepository;
    private StegInputMapper stegInputMapper;

    public StegProsessInputTjeneste() {
        // CDI
    }

    @Inject
    public StegProsessInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                    KoblingRepository koblingRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
        this.stegInputMapper = new StegInputMapper(beregningsgrunnlagRepository);
    }

    public Map<Long, StegProsesseringInput> lagBeregningsgrunnlagInput(Set<Long> koblingId,
                                                                       Map<Long, KalkulatorInputDto> inputPrKobling,
                                                                       BeregningSteg stegType,
                                                                       List<KoblingRelasjon> koblingRelasjoner) {
        var koblingEntiteter = koblingRepository.hentKoblingerFor(koblingId);
        var grunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingId)
                .stream().collect(Collectors.toMap(BeregningsgrunnlagGrunnlagEntitet::getKoblingId, Function.identity()));

        Map<Long, StegProsesseringInput> koblingStegInputMap = koblingEntiteter.stream()
                .collect(Collectors.toMap(
                        KoblingEntitet::getId,
                        kobling -> stegInputMapper.mapStegInput(kobling,
                                inputPrKobling.get(kobling.getId()),
                                Optional.ofNullable(grunnlagEntiteter.get(kobling.getId())),
                                stegType,
                                finnOriginalKobling(kobling, koblingRelasjoner))
                        ));
        return koblingStegInputMap;
    }

    private List<Long> finnOriginalKobling(KoblingEntitet kobling, List<KoblingRelasjon> koblingRelasjoner) {
        return koblingRelasjoner.stream()
                .filter(r -> r.getKoblingId().equals(kobling.getId()))
                .map(KoblingRelasjon::getOriginalKoblingId)
                .collect(Collectors.toList());
    }

}
