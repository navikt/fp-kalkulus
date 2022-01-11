package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
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
public class BeregningsgrunnlagListeRequest implements KalkulusRequest {

    @JsonProperty(value = "beregningsgrunnlagRequest", required = true)
    @Valid
    @NotNull
    private List<BeregningsgrunnlagRequest> beregningsgrunnlagRequest;

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    protected BeregningsgrunnlagListeRequest() {
    }

    @JsonCreator
    public BeregningsgrunnlagListeRequest(@JsonProperty(value = "saksnummer", required = true) @NotNull @Pattern(regexp = "^[A-Za-z0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'") @Valid String saksnummer,
                                          @JsonProperty(value = "beregningsgrunnlagRequest", required = true) @Valid @NotNull List<BeregningsgrunnlagRequest> requestPrReferanse) {
        this.beregningsgrunnlagRequest = Objects.requireNonNull(requestPrReferanse, "requestPrReferanse");
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
    }

    public List<BeregningsgrunnlagRequest> getRequestPrReferanse() {
        return List.copyOf(beregningsgrunnlagRequest);
    }

    @Override
    public String getSaksnummer() {
        return saksnummer;
    }
}
