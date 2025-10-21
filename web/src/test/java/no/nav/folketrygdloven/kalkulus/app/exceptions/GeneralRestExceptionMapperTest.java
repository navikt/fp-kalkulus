package no.nav.folketrygdloven.kalkulus.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.Response;

import no.nav.vedtak.exception.TekniskException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.exception.ManglerTilgangException;

@SuppressWarnings("resource")
class GeneralRestExceptionMapperTest {

    private GeneralRestExceptionMapper generalRestExceptionMapper;

    @BeforeEach
    void setUp() {
        generalRestExceptionMapper = new GeneralRestExceptionMapper();
    }

    @Test
    void skalMappeManglerTilgangFeil() {
        var manglerTilgangFeil = manglerTilgangFeil();

        Response response = generalRestExceptionMapper.toResponse(manglerTilgangFeil);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getType()).isEqualTo(FeilType.MANGLER_TILGANG_FEIL);
        assertThat(feilDto.getFeilmelding()).isEqualTo("MANGLER_TILGANG_FEIL: ManglerTilgangFeilmeldingKode");
    }

    @Test
    void skalMappeVLException() {
        var vlException = tekniskFeil();

        Response response = generalRestExceptionMapper.toResponse(vlException);

        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains("TEK_FEIL");
        assertThat(feilDto.getFeilmelding()).contains("en teknisk feilmelding");
    }

    @Test
    void skalMappeGenerellFeil() {
        String feilmelding = "en helt generell feil";
        var generellFeil = new RuntimeException(feilmelding);

        Response response = generalRestExceptionMapper.toResponse(generellFeil);

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains(feilmelding);
    }

    private static TekniskException tekniskFeil() {
        return new TekniskException("TEK_FEIL", "en teknisk feilmelding");
    }

    private static ManglerTilgangException manglerTilgangFeil() {
        return new ManglerTilgangException("MANGLER_TILGANG_FEIL","ManglerTilgangFeilmeldingKode");
    }
}
