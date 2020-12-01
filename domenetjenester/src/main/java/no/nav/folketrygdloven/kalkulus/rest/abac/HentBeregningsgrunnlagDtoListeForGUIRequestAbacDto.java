package no.nav.folketrygdloven.kalkulus.rest.abac;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoForGUIRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoListeForGUIRequest;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * Json bean med Abac.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto extends HentBeregningsgrunnlagDtoListeForGUIRequest implements no.nav.vedtak.sikkerhet.abac.AbacDto {


    public HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto() {
        // For Json deserialisering
    }

    public HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto(@Valid @NotNull List<HentBeregningsgrunnlagDtoForGUIRequest> requestPrReferanse,
                                                              @Valid Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse,
                                                              @Valid @NotNull UUID behandlingUuid) {
        super(requestPrReferanse, kalkulatorInputPerKoblingReferanse, behandlingUuid);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        final var abacDataAttributter = AbacDataAttributter.opprett();
        abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
        return abacDataAttributter;
    }
}
