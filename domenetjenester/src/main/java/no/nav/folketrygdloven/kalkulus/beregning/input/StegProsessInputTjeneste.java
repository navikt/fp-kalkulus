package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.Collections;
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

    public StegProsesseringInput lagBeregningsgrunnlagInput(Long koblingId,
                                                                       KalkulatorInputDto inputDto,
                                                                       BeregningSteg stegType,
                                                                       List<KoblingRelasjon> koblingRelasjoner) {
        var koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        var grunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        var stegInput = stegInputMapper.mapStegInput(koblingEntitet, inputDto, grunnlagEntitet, stegType, Collections.emptyList()); // TODO tfp-5742 kobling relasjon
        return stegInput;
    }

    private List<Long> finnOriginalKobling(KoblingEntitet kobling, List<KoblingRelasjon> koblingRelasjoner) {
        return koblingRelasjoner.stream()
                .filter(r -> r.getKoblingId().equals(kobling.getId()))
                .map(KoblingRelasjon::getOriginalKoblingId)
                .collect(Collectors.toList());
    }

}
