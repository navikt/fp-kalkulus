package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper.mapGrunnlag;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.mappers.JsonMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class KalkulatorInputTjeneste {

    private static final ObjectWriter WRITER = JsonMapper.getMapper().writerWithDefaultPrettyPrinter();

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingRepository koblingRepository;

    @Inject
    public KalkulatorInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                   KoblingRepository koblingRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
    }

    public KalkulatorInputTjeneste() {
        // CDI-runner
    }

    public Optional<BeregningsgrunnlagInput> lagInputHvisFinnes(Long koblingId, Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        Objects.requireNonNull(koblingId, "koblingId");
        KoblingEntitet koblingEntitet = koblingRepository.hentForKoblingId(koblingId);
        Optional<KalkulatorInputEntitet> inputEntitetOptional = beregningsgrunnlagRepository.hentHvisEksitererKalkulatorInput(koblingId);
        return inputEntitetOptional.map(kalkulatorInputEntitet ->
                MapFraKalkulator.mapFraKalkulatorInputEntitetTilBeregningsgrunnlagInput(
                        koblingEntitet,
                        kalkulatorInputEntitet,
                        beregningsgrunnlagGrunnlagEntitet,
                        beregningsgrunnlagRepository.finnAlleSatser()));
    }

    public BeregningsgrunnlagInput lagInput(Long koblingId, Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        return lagInputHvisFinnes(koblingId, beregningsgrunnlagGrunnlagEntitet)
                .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKalkulatorInput(koblingId).toException());
    }

    public Optional<BeregningsgrunnlagInput> lagInputMedBeregningsgrunnlagHvisFinnes(Long koblingId) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        Optional<BeregningsgrunnlagInput> inputOpt = lagInputHvisFinnes(koblingId, beregningsgrunnlagGrunnlagEntitet);
        return inputOpt.map(input -> {
            Optional<BeregningsgrunnlagGrunnlagDto> mappedGrunnlag = beregningsgrunnlagGrunnlagEntitet.map(grunnlagEntitet -> mapGrunnlag(grunnlagEntitet, input.getInntektsmeldinger()));
            leggTilTilstandhistorikk(input);
            return mappedGrunnlag.map(input::medBeregningsgrunnlagGrunnlag).orElse(input);
        });
    }

    public BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(Long koblingId) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        BeregningsgrunnlagInput input = lagInput(koblingId, beregningsgrunnlagGrunnlagEntitet);
        BeregningsgrunnlagGrunnlagDto mappedGrunnlag = beregningsgrunnlagGrunnlagEntitet.map(grunnlagEntitet -> mapGrunnlag(grunnlagEntitet, input.getInntektsmeldinger()))
                .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusHarIkkeBeregningsgrunnlag(koblingId).toException());
        leggTilTilstandhistorikk(input);
        return input.medBeregningsgrunnlagGrunnlag(mappedGrunnlag);
    }

    private void leggTilTilstandhistorikk(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagTilstand[] tilstander = BeregningsgrunnlagTilstand.values();
        for (BeregningsgrunnlagTilstand tilstand : tilstander) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> sisteBg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(input.getBehandlingReferanse().getBehandlingId(),
                    input.getBehandlingReferanse().getOriginalBehandlingId(), tilstand);
            sisteBg.ifPresent(gr -> input.leggTilBeregningsgrunnlagIHistorikk(mapGrunnlag(gr, input.getInntektsmeldinger()),
                    BeregningsgrunnlagTilstand.fraKode(tilstand.getKode())));
        }
    }

    public boolean lagreKalkulatorInput(Long koblingId, KalkulatorInputDto kalkulatorInput) {
        String input = null;
        try {
            input = WRITER.writeValueAsString(kalkulatorInput);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (input != null) {
            return beregningsgrunnlagRepository.lagreOgSjekkStatus(new KalkulatorInputEntitet(koblingId, input));
        } else {
            throw FeilFactory.create(KalkulatorInputFeil.class).kalkulusKlarteIkkeLagreNedInput(koblingId).toException();
        }
    }
}
