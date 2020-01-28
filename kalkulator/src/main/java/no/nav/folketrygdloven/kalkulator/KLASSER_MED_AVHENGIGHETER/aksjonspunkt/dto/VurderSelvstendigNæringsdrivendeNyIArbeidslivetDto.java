package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

public class VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto {
    private Boolean erNyIArbeidslivet;

    public VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto(Boolean erNyIArbeidslivet) { // NOSONAR
        this.erNyIArbeidslivet = erNyIArbeidslivet;
    }

    public void setErNyIArbeidslivet(Boolean erNyIArbeidslivet) {
        this.erNyIArbeidslivet = erNyIArbeidslivet;
    }

    public Boolean erNyIArbeidslivet() {
        return erNyIArbeidslivet;
    }
}
