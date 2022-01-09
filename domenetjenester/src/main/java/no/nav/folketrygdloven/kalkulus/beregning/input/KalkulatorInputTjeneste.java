package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mappers.JsonMapper;
import no.nav.folketrygdloven.kalkulus.rest.UgyldigInputException;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.k9.felles.exception.TekniskException;

@ApplicationScoped
public class KalkulatorInputTjeneste {

    private static final ObjectWriter WRITER = JsonMapper.getMapper().writerWithDefaultPrettyPrinter();
    private static final ObjectReader READER = JsonMapper.getMapper().reader();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingTjeneste koblingTjeneste;

    @Inject
    public KalkulatorInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
            KoblingTjeneste koblingTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingTjeneste = koblingTjeneste;
    }

    public KalkulatorInputTjeneste() {
        // CDI-runner
    }

    public Map<Long, KalkulatorInputDto> hentOgLagre(Map<UUID, KalkulatorInputDto> inputPrReferanse,
            Set<Long> koblingIder) {
        if (inputPrReferanse != null && !inputPrReferanse.isEmpty()) {
            // kalkulatorinput oppdateres
            lagreKalkulatorInput(inputPrReferanse);
        }
        return hentForKoblinger(koblingIder);
    }

    public Map<Long, KalkulatorInputDto> hentOgLagre(Map<UUID, KalkulatorInputDto> inputPrReferanse,
            Map<UUID, YtelsespesifiktGrunnlagDto> ytelsespesifiktGrunnlagPrKoblingReferanse,
            Set<Long> koblingIder) throws UgyldigInputException {
        if (inputPrReferanse != null && !inputPrReferanse.isEmpty()) {
            // kalkulatorinput oppdateres
            lagreKalkulatorInput(inputPrReferanse);
        } else if (ytelsespesifiktGrunnlagPrKoblingReferanse != null
                && !ytelsespesifiktGrunnlagPrKoblingReferanse.isEmpty()) {
            oppdaterYtelsesspesifiktGrunnlag(ytelsespesifiktGrunnlagPrKoblingReferanse);
        }
        return hentForKoblinger(koblingIder);
    }

    public Map<Long, KalkulatorInputDto> hentForKoblinger(Collection<Long> koblingId) throws UgyldigInputException {
        var kalkulatorInputEntitetListe = beregningsgrunnlagRepository.hentHvisEksistererKalkulatorInput(koblingId);
        List<Long> koblingUtenInput = koblingId.stream()
                .filter(id -> kalkulatorInputEntitetListe.stream().map(KalkulatorInputEntitet::getKoblingId)
                        .noneMatch(k -> k.equals(id)))
                .collect(Collectors.toList());
        if (!koblingUtenInput.isEmpty()) {
            throw new TekniskException("FT-KALKULUS-INPUT-1000000",
                    String.format("Kalkulus finner ikke kalkulator input for koblingId: %s", koblingUtenInput));
        }
        Map<Long, KalkulatorInputDto> inputMap = new HashMap<>();

        for (KalkulatorInputEntitet input : kalkulatorInputEntitetListe) {
            String json = input.getInput();

            var inputDto = konverterTilInput(json, input.getKoblingId());
            inputMap.put(input.getKoblingId(), inputDto);
        }
        return inputMap;
    }

    static KalkulatorInputDto konverterTilInput(String json, Long koblingId) {
        KalkulatorInputDto input;
        try {
            input = READER.forType(KalkulatorInputDto.class).readValue(json);
        } catch (JsonProcessingException e) {
            var encoded = Base64.getEncoder().encodeToString(json.getBytes(Charset.forName("UTF8")));
            System.out.println(encoded);
            throw new TekniskException("FT-KALKULUS-INPUT-1000002",
                    String.format("Kalkulus klarte ikke lese opp input for koblingId %s med følgende feilmelding %s",
                            koblingId, e.getMessage()));
        }

        var violations = VALIDATOR.validate(input);
        if (!violations.isEmpty()) {
            throw new UgyldigInputException("FT-KALKULUS-INPUT-1000004", "Ugyldig kalkulatorinput: "
                    + violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList()));
        }
        return input;

    }

    public boolean lagreKalkulatorInput(Long koblingId, KalkulatorInputDto kalkulatorInput) {
        String input;
        try {
            input = WRITER.writeValueAsString(kalkulatorInput);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TekniskException("FT-KALKULUS-INPUT-1000002",
                    String.format("Kalkulus klarte ikke lagre ned input for koblingId: %s med følgende feilmelding %s",
                            koblingId, e.getMessage()));
        }
        return beregningsgrunnlagRepository.lagreOgSjekkStatus(new KalkulatorInputEntitet(koblingId, input));
    }

    public void lagreKalkulatorInput(YtelseTyperKalkulusStøtterKontrakt ytelseTyperKalkulusStøtter,
            Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse) {
        List<KoblingEntitet> koblinger = koblingTjeneste.hentKoblinger(kalkulatorInputPerKoblingReferanse.keySet()
                .stream()
                .map(KoblingReferanse::new)
                .collect(Collectors.toList()), ytelseTyperKalkulusStøtter);
        kalkulatorInputPerKoblingReferanse.forEach((ref, input) -> {
            Optional<KoblingEntitet> kobling = koblinger.stream()
                    .filter(k -> k.getKoblingReferanse().getReferanse().equals(ref)).findFirst();
            kobling.ifPresent(k -> lagreKalkulatorInput(k.getId(), input));
        });
    }

    public void lagreKalkulatorInput(Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse) {
        List<KoblingEntitet> koblinger = koblingTjeneste.hentKoblinger(kalkulatorInputPerKoblingReferanse.keySet()
                .stream()
                .map(KoblingReferanse::new)
                .collect(Collectors.toList()));
        kalkulatorInputPerKoblingReferanse.forEach((ref, input) -> {
            Optional<KoblingEntitet> kobling = koblinger.stream()
                    .filter(k -> k.getKoblingReferanse().getReferanse().equals(ref)).findFirst();
            kobling.ifPresent(k -> lagreKalkulatorInput(k.getId(), input));
        });
    }

    public void oppdaterYtelsesspesifiktGrunnlag(
            Map<UUID, YtelsespesifiktGrunnlagDto> ytelsespesifiktGrunnlagPrKoblingReferanse) {
        List<KoblingEntitet> koblinger = koblingTjeneste
                .hentKoblinger(ytelsespesifiktGrunnlagPrKoblingReferanse.keySet()
                        .stream()
                        .map(KoblingReferanse::new)
                        .collect(Collectors.toList()));
        var resultatPrKobling = hentForKoblinger(
                koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toList()));
        resultatPrKobling.forEach((key, value) -> {
            var kobling = koblinger.stream().filter(k -> k.getId().equals(key)).findFirst().orElseThrow();
            var ytelsespesifiktGrunnlag = ytelsespesifiktGrunnlagPrKoblingReferanse
                    .get(kobling.getKoblingReferanse().getReferanse());
            var oppdatertInput = value.medYtelsespesifiktGrunnlag(ytelsespesifiktGrunnlag);
            lagreKalkulatorInput(key, oppdatertInput);
        });
    }

}
