package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.forlengelse.ForlengelseperiodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.forlengelse.ForlengelseperioderEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.forlengelse.ForlengelseRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@ApplicationScoped
public class StegProsessInputTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private ForlengelseRepository forlengelseRepository;
    private KoblingRepository koblingRepository;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private StegInputMapper stegInputMapper;

    public StegProsessInputTjeneste() {
        // CDI
    }

    @Inject
    public StegProsessInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository, ForlengelseRepository forlengelseRepository, KoblingRepository koblingRepository, KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.forlengelseRepository = forlengelseRepository;
        this.koblingRepository = koblingRepository;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.stegInputMapper = new StegInputMapper(beregningsgrunnlagRepository);
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
        var forlengelsePerioderPrKobling = forlengelseRepository.hentAktivePerioderForKoblingId(koblingId)
                .stream().collect(Collectors.toMap(ForlengelseperioderEntitet::getKoblingId, Function.identity()));

        Map<Long, StegProsesseringInput> koblingStegInputMap = koblingEntiteter.stream()
                .collect(Collectors.toMap(
                        KoblingEntitet::getId,
                        kobling -> stegInputMapper.mapStegInput(kobling,
                                inputPrKobling.get(kobling.getId()),
                                Optional.ofNullable(grunnlagEntiteter.get(kobling.getId())),
                                stegType,
                                finnOriginalKobling(kobling, koblingRelasjoner),
                                finnForlengelseperioder(forlengelsePerioderPrKobling, kobling))
                        ));
        return koblingStegInputMap;
    }

    private List<IntervallEntitet> finnForlengelseperioder(Map<Long, ForlengelseperioderEntitet> forlengelsePerioderPrKobling, KoblingEntitet kobling) {
        var forlengelseperioderEntitet = forlengelsePerioderPrKobling.get(kobling.getId());
        return forlengelseperioderEntitet != null ?
                forlengelseperioderEntitet.getForlengelseperioder().stream().map(ForlengelseperiodeEntitet::getPeriode).toList() : Collections.emptyList();
    }

    private List<Long> finnOriginalKobling(KoblingEntitet kobling, List<KoblingRelasjon> koblingRelasjoner) {
        return koblingRelasjoner.stream()
                .filter(r -> r.getKoblingId().equals(kobling.getId()))
                .map(KoblingRelasjon::getOriginalKoblingId)
                .collect(Collectors.toList());
    }

}
