package no.nav.folketrygdloven.kalkulus.request.v1.simulerTilkommetInntekt;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

/**
 * Spesifikasjon for å fortsette en beregning.
 * <p>
 * Må minimum angi en referanser kobling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class SimulerTilkommetInntektListeRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtterKontrakt ytelseType;

    @JsonProperty(value = "beregnForListe")
    @Size(min = 1)
    @Valid
    private List<SimulerTilkommetInntektForRequest> simulerForListe;


    protected SimulerTilkommetInntektListeRequest() {
    }

    @JsonCreator
    public SimulerTilkommetInntektListeRequest(@JsonProperty(value = "saksnummer", required = true) String saksnummer,
                                               @JsonProperty(value = "ytelseType", required = true) YtelseTyperKalkulusStøtterKontrakt ytelseType,
                                               @JsonProperty(value = "beregnForListe") List<SimulerTilkommetInntektForRequest> simulerForListe) {
        this.saksnummer = saksnummer;
        this.ytelseType = ytelseType;
        this.simulerForListe = simulerForListe;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public YtelseTyperKalkulusStøtterKontrakt getYtelseType() {
        return ytelseType;
    }

    public List<SimulerTilkommetInntektForRequest> getSimulerForListe() {
        return simulerForListe;
    }
}
