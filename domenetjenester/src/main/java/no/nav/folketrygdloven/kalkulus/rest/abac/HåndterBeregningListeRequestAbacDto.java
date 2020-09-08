package no.nav.folketrygdloven.kalkulus.rest.abac;


import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningRequest;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * Json bean med Abac.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class HåndterBeregningListeRequestAbacDto extends HåndterBeregningListeRequest implements AbacDto {

    @JsonCreator
    public HåndterBeregningListeRequestAbacDto(@NotNull @Valid List<HåndterBeregningRequest> håndterBeregningListe, @Valid @NotNull UUID behandlingUuid) {
        super(håndterBeregningListe, behandlingUuid);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        final var abacDataAttributter = AbacDataAttributter.opprett();

        abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
        return abacDataAttributter;
    }
}
