package no.nav.folketrygdloven.kalkulus.beregning.input;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.KalkulatorInputEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
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

    public Map<Long, KalkulatorInputDto> hentOgLagreForSteg(Map<UUID, KalkulatorInputDto> inputPrReferanse, Set<Long> koblingIder, BeregningSteg stegType) {
        if (inputPrReferanse != null && !inputPrReferanse.isEmpty()) {
            // kalkulatorinput oppdateres
            return lagreKalkulatorInput(inputPrReferanse);
        }
        return hentOgOppdaterDersomUtdatert(inputPrReferanse, koblingIder);
    }

    public Map<Long, KalkulatorInputDto> hentOgLagre(Map<UUID, KalkulatorInputDto> inputPrReferanse, Set<Long> koblingIder) {
        return hentOgOppdaterDersomUtdatert(inputPrReferanse, koblingIder);
    }

    private Map<Long, KalkulatorInputDto> hentOgOppdaterDersomUtdatert(Map<UUID, KalkulatorInputDto> inputPrReferanse, Set<Long> koblingIder) {
        try {
            return hentForKoblinger(koblingIder);
        } catch (UgyldigInputException e) {
            if (!inputPrReferanse.isEmpty()) {
                // kalkulatorinput oppdateres
                return lagreKalkulatorInput(inputPrReferanse);
            } else {
                throw e;
            }
        }
    }

    public Map<Long, String> hentJsonInputForSak(Saksnummer saksnummer) throws UgyldigInputException {
        var koblinger = koblingTjeneste.hentKoblingerForSak(saksnummer).stream().map(KoblingEntitet::getId).toList();
        var kalkulatorInputEntitetListe = beregningsgrunnlagRepository.hentHvisEksistererKalkulatorInput(koblinger);
        Map<Long, String> inputMap = new HashMap<>();
        for (KalkulatorInputEntitet input : kalkulatorInputEntitetListe) {
            String json = input.getInput();
            inputMap.put(input.getKoblingId(), json);
        }
        return inputMap;
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
            throw new TekniskException("FT-KALKULUS-INPUT-1000002",
                    String.format("Kalkulus klarte ikke lese opp input for koblingId %s med følgende feilmelding %s",
                            koblingId, e.getMessage()));
        }

        var violations = VALIDATOR.validate(input);
        if (!violations.isEmpty()) {
            throw new UgyldigInputException("FT-KALKULUS-INPUT-1000004",
                    String.format("Ugyldig kalkulatorinput for kobling %s med følgende feilmeldinger: %s",
                            koblingId,
                            violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList())));
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

    public Map<Long, KalkulatorInputDto> lagreKalkulatorInput(Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse) {
        List<KoblingEntitet> koblinger = koblingTjeneste.hentKoblinger(kalkulatorInputPerKoblingReferanse.keySet()
                .stream()
                .map(KoblingReferanse::new)
                .collect(Collectors.toList()));
        Map<Long, KalkulatorInputDto> lagretMap = new HashMap<>();
        kalkulatorInputPerKoblingReferanse.forEach((ref, input) -> {
            Optional<KoblingEntitet> kobling = koblinger.stream()
                    .filter(k -> k.getKoblingReferanse().getReferanse().equals(ref)).findFirst();
            kobling.ifPresent(k -> {
                lagreKalkulatorInput(k.getId(), input);
                lagretMap.put(k.getId(), input);
            });
        });
        return lagretMap;
    }

}
