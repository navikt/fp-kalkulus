package no.nav.folketrygdloven.kalkulus.rest.tjenester.fakta;

import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.EgenNæringDto;

public final class EgenNæringMapper {

    private EgenNæringMapper() {
        // Skjuler default
    }

    public static EgenNæringDto map(OppgittEgenNæringDto egenNæring) {
        EgenNæringDto dto = new EgenNæringDto();
        dto.setOrgnr(egenNæring.getOrgnr());
        dto.setVirksomhetType(new VirksomhetType(egenNæring.getVirksomhetType().getKode()));
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
