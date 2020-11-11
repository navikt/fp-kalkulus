package no.nav.folketrygdloven.kalkulus.beregning.input;

import static no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand.mapTilStegTilstand;
import static no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand.mapTilStegUtTilstand;
import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FullføreBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@ApplicationScoped
public class StegProsessInputTjeneste {

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
        BeregningsgrunnlagInput beregningsgrunnlagInput = mapFraKalkulatorInputTilBeregningsgrunnlagInput(koblingEntitet, input, Optional.empty());
        // Vurder om vi skal begynne å ta inn koblingId for originalbehandling ved revurdering
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagFraStegUt = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(koblingEntitet.getId(), Optional.empty(), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagFraSteg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(koblingEntitet.getId(), Optional.empty(), BeregningsgrunnlagTilstand.OPPRETTET);
        StegProsesseringInput stegProsesseringInput = new StegProsesseringInput(beregningsgrunnlagInput, BeregningsgrunnlagTilstand.OPPRETTET)
                .medForrigeGrunnlagFraStegUt(grunnlagFraStegUt.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medForrigeGrunnlagFraSteg(grunnlagFraSteg.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medStegUtTilstand(BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        return new FastsettBeregningsaktiviteterInput(stegProsesseringInput).medGrunnbeløpsatser(finnSatser());
    }

    public Resultat<StegProsesseringInput> lagFortsettInput(List<Long> koblingId, StegType stegType) {
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
                        id -> mapStegInput(id, inputRespons.getResultatPrKobling().get(id.getId()), grunnlagEntiteter.get(id.getId()), stegType)
                        ));
        return new Resultat<>(HentInputResponsKode.GYLDIG_INPUT, koblingStegInputMap);
    }


    public StegProsesseringInput mapStegInput(KoblingEntitet kobling,
                                              KalkulatorInputDto input,
                                              BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet,
                                              StegType stegType) {
        StegProsesseringInput stegProsesseringInput = lagStegProsesseringInput(kobling, input, grunnlagEntitet, stegType);
        if (stegType.equals(StegType.KOFAKBER)) {
            return new FaktaOmBeregningInput(stegProsesseringInput).medGrunnbeløpsatser(finnSatser());
        } else if (stegType.equals(StegType.FORS_BERGRUNN)) {
            return lagInputForeslå(stegProsesseringInput);
        } else if (stegType.equals(StegType.VURDER_REF_BERGRUNN)) {
            return stegProsesseringInput;
        } else if (stegType.equals(StegType.FORDEL_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning(), stegProsesseringInput.getSkjæringstidspunktOpptjening());
            return lagInputFordel(stegProsesseringInput, førsteFastsatteGrunnlagEntitet);
        } else if (stegType.equals(StegType.FAST_BERGRUNN)) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløpForVilkårsperiode(kobling, stegProsesseringInput.getSkjæringstidspunktForBeregning(), stegProsesseringInput.getSkjæringstidspunktOpptjening());
            return lagInputFullføre(stegProsesseringInput, førsteFastsatteGrunnlagEntitet);
        }
        return stegProsesseringInput;
    }

    private StegProsesseringInput lagStegProsesseringInput(KoblingEntitet kobling, KalkulatorInputDto input, BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet, StegType stegType) {
        BeregningsgrunnlagInput beregningsgrunnlagInput = MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, Optional.of(grunnlagEntitet));
        // Vurder om vi skal begynne å ta inn koblingId for originalbehandling ved revurdering
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagFraStegUt = mapTilStegUtTilstand(stegType)
                .flatMap(tilstand -> beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(kobling.getId(), Optional.empty(), tilstand));
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagFraSteg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(kobling.getId(), Optional.empty(), mapTilStegTilstand(stegType));
        return new StegProsesseringInput(beregningsgrunnlagInput, mapTilStegTilstand(stegType))
                .medForrigeGrunnlagFraStegUt(grunnlagFraStegUt.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medForrigeGrunnlagFraSteg(grunnlagFraSteg.map(BehandlingslagerTilKalkulusMapper::mapGrunnlag).orElse(null))
                .medStegUtTilstand(mapTilStegUtTilstand(stegType).orElse(null));
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
        return beregningsgrunnlagRepository.hentSisteFastsatteGrunnlagForSkjæringstidspunkt(
                koblingEntitet.getSaksnummer(),
                koblingEntitet.getAktørId(),
                koblingEntitet.getYtelseTyperKalkulusStøtter(),
                skjæringstidspunktOpptjening);
    }

}
