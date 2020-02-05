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

import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtter;


/**
 * Spesifikasjon for å starte en beregning.
 * Oppretter en ny koblingreferanse og starter beregning
 *
 * TODO(OJR) finne ut hva som minimum kreves for å starte en beregning
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class StartBeregningRequest {

    @JsonProperty(value = "referanse", required = true)
    @Valid
    @NotNull
    private UUID koblingReferanse;

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private YtelseTyperKalkulusStøtter ytelseSomSkalBeregnes;
}
