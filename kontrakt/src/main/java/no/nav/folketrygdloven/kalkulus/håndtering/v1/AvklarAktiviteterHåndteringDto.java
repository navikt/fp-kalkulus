package no.nav.folketrygdloven.kalkulus.håndtering.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AvklarAktiviteterHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("avklarteAktiviteterDto")
    @Valid
    @NotNull
    private AvklarteAktiviteterDto avklarteAktiviteterDto;

    public AvklarAktiviteterHåndteringDto(AvklarteAktiviteterDto avklarteAktiviteterDto) {
        this.avklarteAktiviteterDto = avklarteAktiviteterDto;
    }

    public AvklarteAktiviteterDto getAvklarteAktiviteterDto() {
        return avklarteAktiviteterDto;
    }

}
