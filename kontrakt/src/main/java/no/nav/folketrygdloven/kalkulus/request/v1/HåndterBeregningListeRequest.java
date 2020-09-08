package no.nav.folketrygdloven.kalkulus.request.v1;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Spesifikasjon for å oppdatere grunnlaget med informasjon fra saksbehandler.
 *
 * Må minimum angi en referanser kobling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
public class HåndterBeregningListeRequest {

    @JsonProperty(value = "håndterBeregningListe")
    @NotNull
    @Valid
    private List<HåndterBeregningRequest> håndterBeregningListe;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    @NotNull
    private UUID behandlingUuid;

    public HåndterBeregningListeRequest(@NotNull @Valid List<HåndterBeregningRequest> håndterBeregningListe, @Valid @NotNull UUID behandlingUuid) {
        this.håndterBeregningListe = håndterBeregningListe;
        this.behandlingUuid = behandlingUuid;
    }

    public HåndterBeregningListeRequest() {
        // jackson
    }

    public List<HåndterBeregningRequest> getHåndterBeregningListe() {
        return håndterBeregningListe;
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }
}
