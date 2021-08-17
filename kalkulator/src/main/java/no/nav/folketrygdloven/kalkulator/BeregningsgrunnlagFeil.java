package no.nav.folketrygdloven.kalkulator;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BeregningsgrunnlagFeil {

    public static KalkulatorException kanIkkeSerialisereRegelinput(JsonProcessingException e) {
        return new KalkulatorException("FT-370602", "Kunne ikke serialisere regelinput for beregningsgrunnlag.");
    }
}

