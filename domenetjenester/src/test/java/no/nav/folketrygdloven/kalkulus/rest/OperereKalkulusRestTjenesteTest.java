package no.nav.folketrygdloven.kalkulus.rest;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.beregning.v1.GrunnbeløpDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningPeriodeDto;
import no.nav.folketrygdloven.kalkulus.request.v1.StartBeregningRequest;


public class OperereKalkulusRestTjenesteTest {

    @Inject
    private OperereKalkulusRestTjeneste restTjeneste;


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

        KalkulatorInputDto kalkulatorInputDto = new KalkulatorInputDto(grunnbeløpDtos, inntektArbeidYtelseGrunnlagDto, opptjeningAktiviteterDto);

        StartBeregningRequest spesifikasjon = new StartBeregningRequest(randomUUID, saksnummer, dummy, YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_SYKT_BARN, kalkulatorInputDto);

//        //act
//        Response beregn = restTjeneste.beregn(spesifikasjon);
//
//
//        //assert
//        TilstandResponse tilstandResponse = (TilstandResponse) beregn.getEntity();
//        assertThat(tilstandResponse.getAksjonspunktMedTilstandDto()).isEmpty();
    }
}
