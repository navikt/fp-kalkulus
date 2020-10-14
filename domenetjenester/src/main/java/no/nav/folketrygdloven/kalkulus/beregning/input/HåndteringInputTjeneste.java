package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.beregning.MapHåndteringskodeTilTilstand;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class HåndteringInputTjeneste {

    public static final boolean MED_SPORINGSLOGG = true;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingRepository koblingRepository;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    public HåndteringInputTjeneste() {
        // CDI
    }

    @Inject
    public HåndteringInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                   KoblingRepository koblingRepository,
                                   KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
    }

    public HåndterBeregningsgrunnlagInput lagInput(Long koblingId, HåndteringKode kode) {
        Objects.requireNonNull(koblingId, "koblingId");
        KoblingEntitet koblingEntitet = koblingRepository.hentForKoblingId(koblingId);
        Optional<KalkulatorInputDto> inputEntitetOptional = kalkulatorInputTjeneste.hentForKobling(koblingId);
        BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId)
                .orElseThrow(() -> new IllegalStateException("Skal ha grunnlag ved oppdatering"));
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagFraForrigeOppdatering = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(
                koblingEntitet.getId(),
                Optional.empty(),
                MapHåndteringskodeTilTilstand.map(kode));
        return inputEntitetOptional.map(input ->
                lagHåndteringBeregningsgrunnlagInput(koblingEntitet, input, grunnlagEntitet, kode, grunnlagFraForrigeOppdatering))
                .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKalkulatorInput(koblingId).toException());

    }

    private HåndterBeregningsgrunnlagInput lagHåndteringBeregningsgrunnlagInput(KoblingEntitet kobling,
                                                                                KalkulatorInputDto input,
                                                                                BeregningsgrunnlagGrunnlagEntitet aktivGrunnlagEntitet,
                                                                                HåndteringKode håndteringKode,
                                                                                Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagFraHåndteringTilstand) {
        BeregningsgrunnlagInput beregningsgrunnlagInput = MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput(kobling, input, Optional.of(aktivGrunnlagEntitet));
        return new HåndterBeregningsgrunnlagInput(beregningsgrunnlagInput, MapHåndteringskodeTilTilstand.map(håndteringKode))
                .medForrigeGrunnlagFraHåndtering(grunnlagFraHåndteringTilstand.map(beregningsgrunnlagFraFagsystem -> BehandlingslagerTilKalkulusMapper.mapGrunnlag(beregningsgrunnlagFraFagsystem, MED_SPORINGSLOGG)).orElse(null));
    }


}
