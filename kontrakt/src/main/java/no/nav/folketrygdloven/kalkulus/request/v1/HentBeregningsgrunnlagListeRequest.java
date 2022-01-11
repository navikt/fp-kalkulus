package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
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

/**
 * Spesifikasjon for å hente aktivt beregningsgrunnlag.
 * Henter aktivt beregningsgrunnlag
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class HentBeregningsgrunnlagListeRequest implements KalkulusRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private List<HentBeregningsgrunnlagRequest> requestPrReferanse;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    @NotNull
    private UUID behandlingUuid;

    @JsonProperty(value = "inkluderRegelSporing", required = false)
    private boolean inkluderRegelSporing;
    
    //TODO: set saksnummer required + @NotNull når fpsak/k9-sak er oppdatert
    @JsonProperty(value = "saksnummer", required = false)
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    protected HentBeregningsgrunnlagListeRequest() {
        // default ctor
    }

    public HentBeregningsgrunnlagListeRequest(@Valid @NotNull List<HentBeregningsgrunnlagRequest> requestPrReferanse,
                                              @Valid @NotNull UUID behandlingUuid, 
                                              @Valid String saksnummer,
                                              boolean inkluderRegelSporing) {
        this.requestPrReferanse = requestPrReferanse;
        this.behandlingUuid = behandlingUuid;
        this.saksnummer = saksnummer;
        this.inkluderRegelSporing = inkluderRegelSporing;
    }

    public List<HentBeregningsgrunnlagRequest> getRequestPrReferanse() {
        return requestPrReferanse;
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }
    
    public boolean getInkluderRegelSporing() {
        return inkluderRegelSporing;
    }
    
    @Override
    public String getSaksnummer() {
        return saksnummer;
    }
}
