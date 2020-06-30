package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;

/**
 * Spesifikasjon for å hente beregningsgrunnlagGrunnlag for gitt referanse.
 * Henter beregningsgrunnlagGrunnlag for referanse
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class HentBeregningsgrunnlagGrunnlagForReferanseRequest extends HentBeregningsgrunnlagRequest {

    @JsonProperty(value = "grunnlagReferanse", required = true)
    @Valid
    @NotNull
    private UUID grunnlagReferanse;

    protected HentBeregningsgrunnlagGrunnlagForReferanseRequest() {
        // default ctor
    }

    @JsonCreator
    public HentBeregningsgrunnlagGrunnlagForReferanseRequest(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                                             @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                                             @JsonProperty(value = "grunnlagReferanse", required = true) @NotNull @Valid UUID grunnlagReferanse,
                                                             @JsonProperty(value = "inkluderRegelSporing", required = false) Boolean inkluderRegelSporing) {
        super(eksternReferanse, ytelseSomSkalBeregnes, inkluderRegelSporing);
        this.grunnlagReferanse = grunnlagReferanse;
    }

    public UUID getGrunnlagReferanse() {
        return grunnlagReferanse;
    }
}
