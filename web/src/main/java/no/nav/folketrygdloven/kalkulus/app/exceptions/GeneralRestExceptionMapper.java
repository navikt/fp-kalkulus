package no.nav.folketrygdloven.kalkulus.app.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.folketrygdloven.kalkulator.KalkulatorException;
import no.nav.folketrygdloven.kalkulus.felles.verktøy.TomtResultatException;
import no.nav.k9.felles.exception.ManglerTilgangException;
import no.nav.k9.felles.log.mdc.MDCOperations;
import no.nav.k9.felles.log.util.LoggerUtils;

@Provider
public class GeneralRestExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);

    private static Response handleTomtResultatFeil(String feilmelding) {
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new FeilDto(FeilType.TOMT_RESULTAT_FEIL, feilmelding))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static Response serverError(String feilmelding) {
        return Response.serverError()
                .entity(new FeilDto(FeilType.GENERELL_FEIL, feilmelding))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static Response ikkeTilgang(String feilmelding) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new FeilDto(FeilType.MANGLER_TILGANG_FEIL, feilmelding))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static String getExceptionFullFeilmelding(Throwable feil) {
        var callId = MDCOperations.getCallId();
        var feilbeskrivelse = getExceptionMelding(feil);
        return String.format("Det oppstod en serverfeil: %s. Meld til support med referanse-id: %s", feilbeskrivelse, callId);
    }

    private static void loggTilApplikasjonslogg(Throwable feil) {
        var melding = "Fikk uventet feil: " + getExceptionMelding(feil);
        LOGGER.warn(melding, feil);
    }

    private static String getExceptionMelding(Throwable feil) {
        return getTextForField(feil.getMessage());
    }

    private static String getTextForField(String input) {
        return input != null ? LoggerUtils.removeLineBreaks(input) : "";
    }

    @Override
    public Response toResponse(Throwable feil) {
        try {
            if (feil instanceof TomtResultatException) {
                return handleTomtResultatFeil(getExceptionMelding(feil));
            }
            if (feil instanceof ManglerTilgangException) {
                return ikkeTilgang(getExceptionMelding(feil));
            }
            loggTilApplikasjonslogg(feil);
            return serverError(getExceptionFullFeilmelding(feil));
        } finally {
            MDC.remove("prosess"); //$NON-NLS-1$
        }
    }

}
