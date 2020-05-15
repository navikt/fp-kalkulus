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

import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;


/**
 * Spesifikasjon for å hente aktivt beregningsgrunnlag.
 * Henter aktivt beregningsgrunnlag
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class HentBeregningsgrunnlagListeRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private List<HentBeregningsgrunnlagRequest> requestPrReferanse;

    @JsonProperty(value = "superReferanse", required = true)
    @Valid
    @NotNull
    private UUID superReferanse;

    protected HentBeregningsgrunnlagListeRequest() {
        // default ctor
    }

    public HentBeregningsgrunnlagListeRequest(@Valid @NotNull List<HentBeregningsgrunnlagRequest> requestPrReferanse,
                                              @Valid @NotNull UUID superReferanse) {
        this.requestPrReferanse = requestPrReferanse;
        this.superReferanse = superReferanse;
    }

    public List<HentBeregningsgrunnlagRequest> getRequestPrReferanse() {
        return requestPrReferanse;
    }

    public UUID getSuperReferanse() {
        return superReferanse;
    }
}
