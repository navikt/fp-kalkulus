package no.nav.foreldrepenger.kalkulus.kontrakt.request;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.HåndterBeregningDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.KalkulatorInputDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Saksnummer;

/**
 * Spesifikasjon for å løse avklaringsbehov for en behandling.
 */
public record EnkelHåndterBeregningRequestDto(
    @Valid @NotNull UUID behandlingUuid,
    @Valid @NotNull Saksnummer saksnummer,
    @Valid @NotNull KalkulatorInputDto kalkulatorInput,
    @Size(min = 1) List<@Valid HåndterBeregningDto> håndterBeregningDtoList,
    @Valid HåndterBeregningDto håndterBeregningDto) {

    /**
     * @deprecated Bruk konstruktøren som tar ett {@link HåndterBeregningDto}.
     */
    @Deprecated
    public EnkelHåndterBeregningRequestDto(UUID behandlingUuid,
                                           Saksnummer saksnummer,
                                           KalkulatorInputDto kalkulatorInput,
                                           List<HåndterBeregningDto> håndterBeregningDtoList) {
        this(behandlingUuid, saksnummer, kalkulatorInput, håndterBeregningDtoList, null);
    }

    public EnkelHåndterBeregningRequestDto(UUID behandlingUuid,
                                           Saksnummer saksnummer,
                                           KalkulatorInputDto kalkulatorInput,
                                           HåndterBeregningDto håndterBeregningDto) {
        this(behandlingUuid, saksnummer, kalkulatorInput, null, håndterBeregningDto);
    }

    /**
     * @deprecated Bruk {@link #håndterBeregningDto()} eller {@link #hentHåndterBeregningDto()}.
     */
    @Deprecated
    @Override
    public List<HåndterBeregningDto> håndterBeregningDtoList() {
        return håndterBeregningDtoList;
    }

    @JsonIgnore
    @AssertTrue(message = "Nøyaktig én av 'håndterBeregningDto' og 'håndterBeregningDtoList' må være angitt")
    public boolean isNøyaktigEnHåndteringAngitt() {
        return (håndterBeregningDto == null) != (håndterBeregningDtoList == null)
            && (håndterBeregningDtoList == null || !håndterBeregningDtoList.isEmpty());
    }

    @JsonIgnore
    public HåndterBeregningDto hentHåndterBeregningDto() {
        if (!isNøyaktigEnHåndteringAngitt()) {
            throw new IllegalStateException("Nøyaktig én håndtering må være angitt");
        }
        return håndterBeregningDto != null ? håndterBeregningDto : håndterBeregningDtoList.getFirst();
    }
}
