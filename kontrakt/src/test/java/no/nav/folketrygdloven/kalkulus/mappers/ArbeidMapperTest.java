package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.READER_JSON;
import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.WRITER_JSON;
import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.validateResult;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsgiverOpplysningerDto;

public class ArbeidMapperTest {


    @Test
    void test_ArbeidsforholdOverstyringDto() throws JsonProcessingException {

        Organisasjon organisasjon = new Organisasjon("945748931");

        ArbeidsgiverOpplysningerDto dto = new ArbeidsgiverOpplysningerDto(organisasjon, "Veppsen");

        String json = WRITER_JSON.writeValueAsString(dto);
        System.out.println(json);

        ArbeidsgiverOpplysningerDto roundTripped = READER_JSON.forType(ArbeidsgiverOpplysningerDto.class).readValue(json);

        validateResult(roundTripped);
    }

    @Test
    void test_ArbeidsforholdOverstyringDto_person_med_dato() throws JsonProcessingException {

        AktørIdPersonident person = new AktørIdPersonident("9457489311234");

        ArbeidsgiverOpplysningerDto dto = new ArbeidsgiverOpplysningerDto(person, "Veppsen", LocalDate.now());

        String json = WRITER_JSON.writeValueAsString(dto);
        System.out.println(json);

        ArbeidsgiverOpplysningerDto roundTripped = READER_JSON.forType(ArbeidsgiverOpplysningerDto.class).readValue(json);

        validateResult(roundTripped);
    }
}
