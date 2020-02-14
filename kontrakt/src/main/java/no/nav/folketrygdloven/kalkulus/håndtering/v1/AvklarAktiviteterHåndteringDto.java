package no.nav.folketrygdloven.kalkulus.håndtering.v1;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.AvklarteAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.AvklarAktiviteterDto;

public class AvklarAktiviteterHåndteringDto extends HåndterBeregningDto {

    private AvklarteAktiviteterDto avklarteAktiviteterDto;

    public AvklarAktiviteterHåndteringDto(AvklarteAktiviteterDto avklarteAktiviteterDto) {
        this.avklarteAktiviteterDto = avklarteAktiviteterDto;
    }

    public AvklarteAktiviteterDto getAvklarteAktiviteterDto() {
        return avklarteAktiviteterDto;
    }

}
