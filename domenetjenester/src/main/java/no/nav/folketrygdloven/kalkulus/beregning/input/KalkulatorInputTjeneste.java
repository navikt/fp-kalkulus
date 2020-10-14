package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.time.MonthDay;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.mappers.JsonMapper;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class KalkulatorInputTjeneste {

    private static final ObjectWriter WRITER = JsonMapper.getMapper().writerWithDefaultPrettyPrinter();
    private static final ObjectReader READER = JsonMapper.getMapper().reader();

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    @Inject
    public KalkulatorInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    public KalkulatorInputTjeneste() {
        // CDI-runner
    }

    public Optional<KalkulatorInputDto> hentForKobling(Long koblingId) {
        Optional<KalkulatorInputEntitet> kalkulatorInputEntitet = beregningsgrunnlagRepository.hentHvisEksitererKalkulatorInput(koblingId);
        return kalkulatorInputEntitet.map(inputEntitet -> {
            String json = inputEntitet.getInput();
            KalkulatorInputDto input = null;
            try {
                input = READER.forType(KalkulatorInputDto.class).readValue(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return input;
        });
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
