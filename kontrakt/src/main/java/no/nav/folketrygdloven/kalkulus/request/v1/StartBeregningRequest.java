package no.nav.folketrygdloven.kalkulus.request.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.UuidDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

/**
 * Spesifikasjon for å starte en beregning.
 * Oppretter starter en ny beregning
 * @deprecated Bruk {@link StartBeregningListeRequest}
 */
@Deprecated(forRemoval=true, since="1.0")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class StartBeregningRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UuidDto eksternReferanse;

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

    @JsonProperty(value = "kalkulatorInput", required = true)
    @NotNull
    @Valid
    private KalkulatorInputDto kalkulatorInput;

    protected StartBeregningRequest() {
        // default ctor
    }

    public StartBeregningRequest(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UuidDto eksternReferanse,
                                 @JsonProperty(value = "saksnummer", required = true) @NotNull @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'") @Valid String saksnummer,
                                 @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                 @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                 @JsonProperty(value = "kalkulatorInput", required = true) @NotNull @Valid KalkulatorInputDto kalkulatorInput) {

        this.eksternReferanse = eksternReferanse;
        this.saksnummer = saksnummer;
        this.aktør = aktør;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.kalkulatorInput = kalkulatorInput;
    }

    public UuidDto getEksternReferanse() {
        return eksternReferanse;
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

    public KalkulatorInputDto getKalkulatorInput() {
        return kalkulatorInput;
    }

}
