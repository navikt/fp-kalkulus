package no.nav.folketrygdloven.kalkulator.rest.fakta;

import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.EgenNæringDto;

public final class EgenNæringMapper {

    private EgenNæringMapper() {
        // Skjuler default
    }

    public static EgenNæringDto map(OppgittEgenNæringDto egenNæring) {
        EgenNæringDto dto = new EgenNæringDto();
        dto.setOrgnr(egenNæring.getOrgnr());
        dto.setVirksomhetType(egenNæring.getVirksomhetType());
        dto.setBegrunnelse(egenNæring.getBegrunnelse());
        dto.setEndringsdato(egenNæring.getEndringDato());
        dto.setErVarigEndret(egenNæring.getVarigEndring());
        dto.setErNyoppstartet(egenNæring.getNyoppstartet());
        dto.setErNyIArbeidslivet(egenNæring.getNyIArbeidslivet());
        dto.setRegnskapsførerNavn(egenNæring.getRegnskapsførerNavn());
        dto.setRegnskapsførerTlf(egenNæring.getRegnskapsførerTlf());
        dto.setOppgittInntekt(egenNæring.getBruttoInntekt());
        return dto;
    }

}
