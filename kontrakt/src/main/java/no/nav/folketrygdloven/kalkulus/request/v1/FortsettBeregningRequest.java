package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class FortsettBeregningRequest {

    @JsonProperty(value = "referanse", required = true)
    @Valid
    @NotNull
    private UUID koblingReferanse;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes;

    @JsonProperty(value = "stegType", required = true)
    @NotNull
    @Valid
    private StegType stegType;

    public FortsettBeregningRequest() {
        // default ctor
    }

    public FortsettBeregningRequest(@Valid @NotNull UUID koblingReferanse,
                                    @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                    @NotNull @Valid StegType stegType) {
        this.koblingReferanse = koblingReferanse;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.stegType = stegType;
    }

    public UUID getKoblingReferanse() {
        return koblingReferanse;
    }

    public YtelseTyperKalkulusStøtterKontrakt getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public StegType getStegType() {
        return stegType;
    }
}
