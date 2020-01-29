package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.rest.dto.EgenNæringDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.VirksomhetType;

public class EgenNæringMapperTest {

    @Test
    public void skal_mappe_fra_entitet_til_dto() {
        OppgittOpptjeningDtoBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny();

        egenNæringBuilder.medVirksomhetType(VirksomhetType.FISKE);
        egenNæringBuilder.medVirksomhet("923609016");
        egenNæringBuilder.medBegrunnelse("Dette e ren begrunnelse");
        egenNæringBuilder.medBruttoInntekt(BigDecimal.valueOf(123123123));
        egenNæringBuilder.medEndringDato(LocalDate.now().minusMonths(4));
        egenNæringBuilder.medVarigEndring(true);
        egenNæringBuilder.medNyoppstartet(false);

        OppgittEgenNæringDto egenNæring = egenNæringBuilder.build();

        EgenNæringDto dto = EgenNæringMapper.map(egenNæring);

        assertThat(dto).isNotNull();
        assertThat(dto.getBegrunnelse()).isEqualTo(egenNæring.getBegrunnelse());
        assertThat(dto.getEndringsdato()).isEqualTo(egenNæring.getEndringDato());
        assertThat(dto.getVirksomhetType()).isEqualTo(egenNæring.getVirksomhetType());
        assertThat(dto.getOppgittInntekt()).isEqualTo(egenNæring.getBruttoInntekt());
        assertThat(dto.getOrgnr()).isEqualTo(egenNæring.getOrgnr());
        assertThat(dto.isErVarigEndret()).isEqualTo(egenNæring.getVarigEndring());
        assertThat(dto.isErNyoppstartet()).isEqualTo(egenNæring.getNyoppstartet());
    }

}
