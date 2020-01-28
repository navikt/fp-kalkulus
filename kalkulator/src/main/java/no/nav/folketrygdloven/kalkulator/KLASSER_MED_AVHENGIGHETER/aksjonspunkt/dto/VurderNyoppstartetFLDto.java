package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

public class VurderNyoppstartetFLDto {

    private Boolean erNyoppstartetFL;

    public VurderNyoppstartetFLDto(Boolean erNyoppstartetFL) { // NOSONAR
        this.erNyoppstartetFL = erNyoppstartetFL;
    }

    public void setErNyoppstartetFL(Boolean erNyoppstartetFL) {
        this.erNyoppstartetFL = erNyoppstartetFL;
    }

    public Boolean erErNyoppstartetFL() {
        return erNyoppstartetFL;
    }
}
