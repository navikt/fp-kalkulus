package no.nav.folketrygdloven.kalkulus.beregning;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
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
    public KalkulatorInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository, KoblingRepository koblingRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
    }

    public KalkulatorInputTjeneste() {
        // CDI-runner
    }

    public BeregningsgrunnlagInput lagInput(Long koblingId) {
        Objects.requireNonNull(koblingId, "Denne kan ikke være null");

        KoblingEntitet koblingEntitet = koblingRepository.hentForKoblingId(koblingId);
        Optional<KalkulatorInputEntitet> inputEntitetOptional = beregningsgrunnlagRepository.hentHvisEksitererKalkulatorInput(koblingId);
        if (inputEntitetOptional.isPresent()) {
            KalkulatorInputEntitet kalkulatorInputEntitet = inputEntitetOptional.get();
            return MapFraKalkulator.mapFraKalkulatorInputEntitetTilBeregningsgrunnlagInput(koblingEntitet, kalkulatorInputEntitet);
        }
        throw FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKalkulatorInput(koblingId).toException();
    }

    public BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(Long koblingId) {
        BeregningsgrunnlagInput input = lagInput(koblingId);
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        if (beregningsgrunnlagGrunnlagEntitet.isPresent()) {
            Optional<InntektsmeldingAggregatDto> innOpt = input.getIayGrunnlag().getInntektsmeldinger();

            Collection<InntektsmeldingDto> inntektsmeldingDtos = Collections.emptyList();
            if (innOpt.isPresent()) {
                InntektsmeldingAggregatDto inntektsmeldingAggregatDto = innOpt.get();
                inntektsmeldingDtos = inntektsmeldingAggregatDto.getAlleInntektsmeldinger();
            }
            BeregningsgrunnlagGrunnlagEntitet grunnlagEntitet = beregningsgrunnlagGrunnlagEntitet.get();
            return input.medBeregningsgrunnlagGrunnlag(BehandlingslagerTilKalkulusMapper.mapGrunnlag(grunnlagEntitet, inntektsmeldingDtos));
        }
        throw FeilFactory.create(KalkulatorInputFeil.class).kalkulusHarIkkeBeregningsgrunnlag(koblingId).toException();
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
