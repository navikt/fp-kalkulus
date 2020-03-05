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

import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;


/**
 * Spesifikasjon for å hente aktivt beregningsgrunnlag.
 * Henter aktivt beregningsgrunnlag
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class ErEndringIBeregningRequest {

    @JsonProperty(value = "eksternReferanse1", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse1;

    @JsonProperty(value = "eksternReferanse2", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse2;


    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes;


    protected ErEndringIBeregningRequest() {
        // default ctor
    }

    public ErEndringIBeregningRequest(@Valid @NotNull UUID eksternReferanse1,
                                      @Valid @NotNull UUID eksternReferanse2,
                                      @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {

        this.eksternReferanse1 = eksternReferanse1;
        this.eksternReferanse2 = eksternReferanse2;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
    }

    public UUID getKoblingReferanse1() {
        return eksternReferanse1;
    }

    public UUID getKoblingReferanse2() {
        return eksternReferanse2;
    }


    public YtelseTyperKalkulusStøtterKontrakt getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

}
