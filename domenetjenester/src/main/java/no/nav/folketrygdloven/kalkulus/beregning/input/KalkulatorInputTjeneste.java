package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.mappers.JsonMapper;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class KalkulatorInputTjeneste {

    private static final ObjectWriter WRITER = JsonMapper.getMapper().writerWithDefaultPrettyPrinter();
    private static final ObjectReader READER = JsonMapper.getMapper().reader();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    @Inject
    public KalkulatorInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    public KalkulatorInputTjeneste() {
        // CDI-runner
    }

    public Resultat<KalkulatorInputDto> hentForKoblinger(Collection<Long> koblingId) {
        var kalkulatorInputEntitetListe = beregningsgrunnlagRepository.hentHvisEksistererKalkulatorInput(koblingId);
        List<Long> koblingUtenInput = koblingId.stream().filter(id -> kalkulatorInputEntitetListe.stream().map(KalkulatorInputEntitet::getKoblingId).noneMatch(k -> k.equals(id)))
                .collect(Collectors.toList());
        if (!koblingUtenInput.isEmpty()) {
            throw FeilFactory.create(KalkulatorInputFeil.class).kalkulusFinnerIkkeKalkulatorInput(koblingUtenInput).toException();
        }
        Map<Long, KalkulatorInputDto> inputMap = new HashMap<>();

        for (KalkulatorInputEntitet input : kalkulatorInputEntitetListe) {
            String json = input.getInput();

            Optional<KalkulatorInputDto> inputDto = konverterTilInput(json, input.getKoblingId());
            if (inputDto.isEmpty()) {
                return new Resultat<>(HentInputResponsKode.ETTERSPØR_NY_INPUT);
            } else {
                inputMap.put(input.getKoblingId(), inputDto.get());
            }
        }
        return new Resultat<>(HentInputResponsKode.GYLDIG_INPUT, inputMap);
    }

    static Optional<KalkulatorInputDto> konverterTilInput(String json, Long koblingId) {
        KalkulatorInputDto input;
        try {
            input = READER.forType(KalkulatorInputDto.class).readValue(json);
        } catch (JsonProcessingException e) {
            throw FeilFactory.create(KalkulatorInputFeil.class).kalkulusKlarteIkkeLeseOppInput(koblingId, e.getMessage()).toException();
        }

        var violations = VALIDATOR.validate(input);
        if (!violations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(input);

    }

    public boolean lagreKalkulatorInput(Long koblingId, KalkulatorInputDto kalkulatorInput) {
        String input;
        try {
            input = WRITER.writeValueAsString(kalkulatorInput);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw FeilFactory.create(KalkulatorInputFeil.class).kalkulusKlarteIkkeLagreNedInput(koblingId, e.getMessage()).toException();
        }
        return beregningsgrunnlagRepository.lagreOgSjekkStatus(new KalkulatorInputEntitet(koblingId, input));
    }


}
