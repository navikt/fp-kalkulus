package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper.mapGrunnlag;

import java.time.MonthDay;
import java.util.Comparator;
import java.util.List;
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
import no.nav.folketrygdloven.kalkulus.felles.jpa.BaseEntitet;
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
    public static final MonthDay ENDRING_AV_GRUNNBELØP = MonthDay.of(5, 1);

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
        Optional<BeregningsgrunnlagGrunnlagEntitet> førsteFastsatteGrunnlagEntitet = finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløp(koblingEntitet);
        Optional<KalkulatorInputEntitet> inputEntitetOptional = beregningsgrunnlagRepository.hentHvisEksitererKalkulatorInput(koblingId);
        return inputEntitetOptional.map(kalkulatorInputEntitet ->
                MapFraKalkulator.mapFraKalkulatorInputEntitetTilBeregningsgrunnlagInput(
                        koblingEntitet,
                        kalkulatorInputEntitet,
                        beregningsgrunnlagGrunnlagEntitet,
                        førsteFastsatteGrunnlagEntitet,
                        beregningsgrunnlagRepository.finnAlleSatser()));
    }

    public BeregningsgrunnlagInput lagInput(Long koblingId, Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet) {
        return lagInputHvisFinnes(koblingId, beregningsgrunnlagGrunnlagEntitet)
                .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKalkulatorInput(koblingId).toException());
    }

    public Optional<BeregningsgrunnlagInput> lagInputMedBeregningsgrunnlagHvisFinnes(final Long koblingId) {
        boolean medSporingslogg = true;
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        Optional<BeregningsgrunnlagInput> inputOpt = lagInputHvisFinnes(koblingId, beregningsgrunnlagGrunnlagEntitet);
        return inputOpt.map(input -> {
            Optional<BeregningsgrunnlagGrunnlagDto> mappedGrunnlag = beregningsgrunnlagGrunnlagEntitet.map(grunnlagEntitet -> mapGrunnlag(grunnlagEntitet, input.getInntektsmeldinger(), medSporingslogg));
            leggTilTilstandhistorikk(input, medSporingslogg);
            return mappedGrunnlag.map(input::medBeregningsgrunnlagGrunnlag).orElse(input);
        });
    }
    
    public BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(Long koblingId) {
        return lagInputMedBeregningsgrunnlag(koblingId, true);
    }

    protected BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(Long koblingId, boolean medSporingslogg) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        BeregningsgrunnlagInput input = lagInput(koblingId, beregningsgrunnlagGrunnlagEntitet);
        BeregningsgrunnlagGrunnlagDto mappedGrunnlag = beregningsgrunnlagGrunnlagEntitet.map(grunnlagEntitet -> mapGrunnlag(grunnlagEntitet, input.getInntektsmeldinger(), medSporingslogg))
                .orElseThrow(() -> FeilFactory.create(KalkulatorInputFeil.class).kalkulusHarIkkeBeregningsgrunnlag(koblingId).toException());
        leggTilTilstandhistorikk(input, medSporingslogg);
        return input.medBeregningsgrunnlagGrunnlag(mappedGrunnlag);
    }
    
    public BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagUtenSporingslogg(Long koblingId) {
        return lagInputMedBeregningsgrunnlag(koblingId, false);
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

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnFørsteFastsatteGrunnlagEtterEndringAvGrunnbeløp(KoblingEntitet koblingEntitet) {
        List<KoblingEntitet> koblinger = koblingRepository.hentAlleKoblingReferanserFor(koblingEntitet.getAktørId(), koblingEntitet.getSaksnummer(), koblingEntitet.getYtelseTyperKalkulusStøtter());
        return koblinger.stream()
                .map(kobling -> beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(kobling.getId(), BeregningsgrunnlagTilstand.FASTSATT))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(gr -> MonthDay.from(gr.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag"))
                .getSkjæringstidspunkt()).isAfter(ENDRING_AV_GRUNNBELØP))
                .min(Comparator.comparing(BaseEntitet::getOpprettetTidspunkt));
    }

    private void leggTilTilstandhistorikk(final BeregningsgrunnlagInput input, final boolean medSporingslogg) {
        for (var tilstand : BeregningsgrunnlagTilstand.values()) {
            Optional<BeregningsgrunnlagGrunnlagEntitet> sisteBg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(input.getBehandlingReferanse().getBehandlingId(),
                    input.getBehandlingReferanse().getOriginalBehandlingId(), tilstand);
            sisteBg.ifPresent(gr -> input.leggTilBeregningsgrunnlagIHistorikk(mapGrunnlag(gr, input.getInntektsmeldinger(), medSporingslogg),
                    BeregningsgrunnlagTilstand.fraKode(tilstand.getKode())));
        }
    }
}
