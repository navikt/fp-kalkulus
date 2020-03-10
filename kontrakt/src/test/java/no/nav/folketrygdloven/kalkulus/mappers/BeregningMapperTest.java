package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.READER_JSON;
import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.WRITER_JSON;
import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.validateResult;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.folketrygdloven.kalkulus.beregning.v1.AksjonspunktMedTilstandDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

public class BeregningMapperTest {


    @Test
    void test_AksjonspunktMedTilstandDto() throws JsonProcessingException {
        AksjonspunktMedTilstandDto vent = new AksjonspunktMedTilstandDto(new BeregningAksjonspunkt("5058"), new BeregningVenteårsak("VENT"), LocalDateTime.now());

        String json = WRITER_JSON.writeValueAsString(vent);
        System.out.println(json);

        AksjonspunktMedTilstandDto roundTripped = READER_JSON.forType(AksjonspunktMedTilstandDto.class).readValue(json);

        validateResult(roundTripped);
    }
}
