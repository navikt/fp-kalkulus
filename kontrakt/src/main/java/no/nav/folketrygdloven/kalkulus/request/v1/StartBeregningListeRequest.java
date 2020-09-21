package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

/**
 * Spesifikasjon for å starte en beregning.
 * Oppretter starter en ny beregning
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class StartBeregningListeRequest {

    private static final Comparator<KalkulatorInputDto> STP_COMP = Comparator.comparing(KalkulatorInputDto::getSkjæringstidspunkt,
        Comparator.nullsLast(Comparator.naturalOrder()));

    /** Kalkulatorinput per ekstern kobling referanse. */
    @JsonProperty(value = "kalkulatorInput", required = true)
    @Valid
    @NotNull
    @NotEmpty
    private Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse;

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes;

    protected StartBeregningListeRequest() {
    }

    @JsonCreator
    public StartBeregningListeRequest(@JsonProperty(value = "kalkulatorInput", required = true) @Valid @NotNull Map<UUID, KalkulatorInputDto> kalkulatorInput,
                                      @JsonProperty(value = "saksnummer", required = true) @NotNull @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'") @Valid String saksnummer,
                                      @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                      @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {

        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
        this.aktør = Objects.requireNonNull(aktør, "aktør");
        this.ytelseSomSkalBeregnes = Objects.requireNonNull(ytelseSomSkalBeregnes, "ytelseSomSkalBeregnes");
        this.kalkulatorInputPerKoblingReferanse = Map.copyOf(Objects.requireNonNull(kalkulatorInput, "kalkulatorInput"));
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public PersonIdent getAktør() {
        return aktør;
    }

    public YtelseTyperKalkulusStøtterKontrakt getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    /** Returnerer kalkulatorinput med koblingreferanse (sortert etter skjæringstidspunkt stigende rekkefølge. */
    public Map<UUID, KalkulatorInputDto> getKalkulatorInputPerKoblingReferanse() {
        var map = kalkulatorInputPerKoblingReferanse.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(STP_COMP))
            .collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return map;
    }

}
