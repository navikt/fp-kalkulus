package no.nav.folketrygdloven.kalkulator;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.vedtak.exception.TekniskException;

public class BeregningsgrunnlagFeil {

    public static TekniskException kanIkkeSerialisereRegelinput(JsonProcessingException e) {
        return new TekniskException( "FT-370602", "Kunne ikke serialisere regelinput for beregningsgrunnlag.");
    }
}

