package no.nav.folketrygdloven.kalkulus.rest;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.beregning.v1.GrunnbeløpDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningPeriodeDto;
import no.nav.folketrygdloven.kalkulus.request.v1.StartBeregningRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;

class OperereKalkulusRestTjenesteTest {


    private OperereKalkulusRestTjeneste restTjeneste = new OperereKalkulusRestTjeneste();



    @Test
    void skal_starte_beregning() {
        //arrange
        String saksnummer = "1234";
        UUID randomUUID = UUID.randomUUID();
        AktørIdPersonident dummy = AktørIdPersonident.dummy();
        Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
        Organisasjon organisasjon = new Organisasjon("945748931");

        List<GrunnbeløpDto> grunnbeløpDtos = List.of(new GrunnbeløpDto(periode, BigDecimal.valueOf(100000), BigDecimal.valueOf(100000)));
        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlagDto = new InntektArbeidYtelseGrunnlagDto();
        OpptjeningAktiviteterDto opptjeningAktiviteterDto = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, periode, organisasjon, new InternArbeidsforholdRefDto("Dummy"))));

        StartBeregningRequest spesifikasjon = new StartBeregningRequest(randomUUID, saksnummer, dummy, YtelseTyperKalkulusStøtter.PLEIEPENGER_SYKT_BARN, grunnbeløpDtos, inntektArbeidYtelseGrunnlagDto, opptjeningAktiviteterDto);

        //act
        Response beregn = restTjeneste.beregn(spesifikasjon);


        //assert
        TilstandResponse tilstandResponse = (TilstandResponse) beregn.getEntity();
        assertThat(tilstandResponse.getAksjonspunktMedTilstandDto()).isEmpty();
    }
}
