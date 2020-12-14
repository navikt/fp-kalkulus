package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
public class HentBeregningsgrunnlagRequest implements KalkulusRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes;

    @JsonProperty(value = "inkluderRegelSporing", required = false)
    private boolean inkluderRegelSporing;
    
    //TODO: set saksnummer required + @NotNull når fpsak/k9-sak er oppdatert
    @JsonProperty(value = "saksnummer", required = false)
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    protected HentBeregningsgrunnlagRequest() {
        // default ctor
    }

    public HentBeregningsgrunnlagRequest(@Valid @NotNull UUID eksternReferanse,
                                         @Valid String saksnummer,
                                         @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {

        this.eksternReferanse = eksternReferanse;
        this.saksnummer = saksnummer;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
    }

    public HentBeregningsgrunnlagRequest(@Valid @NotNull UUID eksternReferanse,
                                         @Valid String saksnummer,
                                         @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                         Boolean inkluderRegelSporing) {

        this.eksternReferanse = eksternReferanse;
        this.saksnummer = saksnummer;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.inkluderRegelSporing = inkluderRegelSporing==null?false:inkluderRegelSporing;
    }

    public UUID getKoblingReferanse() {
        return eksternReferanse;
    }

    public YtelseTyperKalkulusStøtterKontrakt getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public boolean getInkluderRegelSporing() {
        return inkluderRegelSporing;
    }
    
    public String getSaksnummer() {
        return saksnummer;
    }

}
