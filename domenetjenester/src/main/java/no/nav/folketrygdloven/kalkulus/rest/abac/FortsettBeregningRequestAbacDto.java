package no.nav.folketrygdloven.kalkulus.rest.abac;


import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.FortsettBeregningRequest;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * Json bean med Abac.
 */
@Deprecated(forRemoval=true, since="1.0")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FortsettBeregningRequestAbacDto extends FortsettBeregningRequest implements AbacDto {


    @JsonCreator
    public FortsettBeregningRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                           @JsonProperty(value = "stegType", required = true) @NotNull @Valid StegType stegType,
                                           @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
        super(eksternReferanse, ytelseSomSkalBeregnes, stegType);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        final var abacDataAttributter = AbacDataAttributter.opprett();
        abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getEksternReferanse());
        return abacDataAttributter;
    }
}
