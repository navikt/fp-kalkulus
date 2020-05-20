package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Spesifikasjon for å hente beregningsgrunnlagDto for GUI.
 * Henter DTO-struktur som brukes av beregning i frontend
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class HentBeregningsgrunnlagDtoListeForGUIRequest {

    @JsonProperty(value = "requestPrReferanse", required = true)
    @Valid
    @NotNull
    private List<HentBeregningsgrunnlagDtoForGUIRequest> requestPrReferanse;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    @NotNull
    private UUID behandlingUuid;

    protected HentBeregningsgrunnlagDtoListeForGUIRequest() {
        // default ctor
    }

    public HentBeregningsgrunnlagDtoListeForGUIRequest(@Valid @NotNull List<HentBeregningsgrunnlagDtoForGUIRequest> requestPrReferanse,
                                                       @Valid @NotNull UUID behandlingUuid) {
        this.requestPrReferanse = requestPrReferanse;
        this.behandlingUuid = behandlingUuid;
    }

    public List<HentBeregningsgrunnlagDtoForGUIRequest> getRequestPrReferanse() {
        return requestPrReferanse;
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }
}
