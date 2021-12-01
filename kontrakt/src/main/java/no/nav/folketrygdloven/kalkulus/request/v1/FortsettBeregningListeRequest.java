package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

/**
 * Spesifikasjon for å fortsette en beregning.
 *
 * Må minimum angi en referanser kobling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class FortsettBeregningListeRequest implements KalkulusRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "eksternReferanser", required = true)
    @Valid
    @NotNull
    @NotEmpty
    private Collection<UUID> eksternReferanser;

    /** Kalkulatorinput per ekstern kobling referanse. Brukes i tilfelle der input er utdatert */
    @JsonProperty(value = "kalkulatorInput")
    @Valid
    private Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes;

    @JsonProperty(value = "stegType", required = true)
    @NotNull
    @Valid
    private StegType stegType;


    protected FortsettBeregningListeRequest() {
    }


    public FortsettBeregningListeRequest(@JsonProperty(value = "saksnummer", required = true) @NotNull @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'") @Valid String saksnummer,
                                         @JsonProperty(value = "eksternReferanser", required = true) @Valid @NotNull List<UUID> eksternReferanser,
                                         @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                         @JsonProperty(value = "stegType", required = true) @NotNull @Valid StegType stegType) {
        this.eksternReferanser = new LinkedHashSet<>(Objects.requireNonNull(eksternReferanser, "eksterneReferanser"));
        this.ytelseSomSkalBeregnes = Objects.requireNonNull(ytelseSomSkalBeregnes, "ytelseSomSkalBeregnes");
        this.stegType = Objects.requireNonNull(stegType, "stegType");
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
    }

    @JsonCreator
    public FortsettBeregningListeRequest(@JsonProperty(value = "saksnummer", required = true) @NotNull @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'") @Valid String saksnummer,
                                         @JsonProperty(value = "eksternReferanser", required = true) @Valid @NotNull List<UUID> eksternReferanser,
                                         @JsonProperty(value = "kalkulatorInput") Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse,
                                         @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                         @JsonProperty(value = "stegType", required = true) @NotNull @Valid StegType stegType) {
        this.eksternReferanser = new LinkedHashSet<>(Objects.requireNonNull(eksternReferanser, "eksterneReferanser"));
        this.kalkulatorInputPerKoblingReferanse = kalkulatorInputPerKoblingReferanse;
        this.ytelseSomSkalBeregnes = Objects.requireNonNull(ytelseSomSkalBeregnes, "ytelseSomSkalBeregnes");
        this.stegType = Objects.requireNonNull(stegType, "stegType");
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
    }

    public List<UUID> getEksternReferanser() {
        return List.copyOf(new LinkedHashSet<>(eksternReferanser));
    }

    public YtelseTyperKalkulusStøtterKontrakt getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public StegType getStegType() {
        return stegType;
    }

    @Override
    public String getSaksnummer() {
        return saksnummer;
    }

    public Map<UUID, KalkulatorInputDto> getKalkulatorInputPerKoblingReferanse() {
        return kalkulatorInputPerKoblingReferanse;
    }

}
