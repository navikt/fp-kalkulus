package no.nav.folketrygdloven.kalkulus.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.Response;


import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

class JsonMappingExceptionMapperTest {


    @Test
    void skal_mappe_InvalidTypeIdException() {
        JsonMappingExceptionMapper mapper = new JsonMappingExceptionMapper();
        @SuppressWarnings("resource")
        Response resultat = mapper.toResponse(new InvalidTypeIdException(null, "Ukjent type-kode", null, "23525"));
        FeilDto dto = (FeilDto) resultat.getEntity();
        assertThat(dto.getFeilmelding()).isEqualTo("FT-252294 JSON-mapping feil: Ukjent type-kode");
        assertThat(dto.getFeltFeil()).isNull();
    }
}
