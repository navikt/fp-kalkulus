package no.nav.folketrygdloven.kalkulus.beregning;

import java.util.Collection;
import java.util.Collections;
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

@ApplicationScoped
public class KalkulatorInputTjeneste {

    private static final ObjectWriter WRITER = JsonMapper.getMapper().writerWithDefaultPrettyPrinter();

    @Inject
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    @Inject
    private KoblingRepository koblingRepository;

    public KalkulatorInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository, KoblingRepository koblingRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingRepository = koblingRepository;
    }

    public KalkulatorInputTjeneste() {
        // CDI-runner
    }

    public BeregningsgrunnlagInput lagInput(Long koblingId) {
        KoblingEntitet koblingEntitet = koblingRepository.hentForKoblingId(koblingId);
        KalkulatorInputEntitet kalkulatorInputEntitet = beregningsgrunnlagRepository.hentKalkulatorInput(koblingId);
        return MapFraKalkulator.mapFraKalkulatorInputEntitetTilBeregningsgrunnlagInput(koblingEntitet, kalkulatorInputEntitet);
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
        throw new IllegalStateException("Utviklerfeil!!!, ikke kall meg når jeg ikke har beregningsgrunnlag");
    }

    public void lagreKalkulatorInput(Long id, KalkulatorInputDto kalkulatorInput) {
        String input = null;
        try {
            input = WRITER.writeValueAsString(kalkulatorInput);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (input != null) {
            beregningsgrunnlagRepository.lagre(new KalkulatorInputEntitet(id, input));
        } else {
            throw new IllegalStateException("Klarte ikke lagre ned input for koblingId " + id);
        }
    }
}
