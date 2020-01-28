package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

public class VurderMilitærDto {

    private Boolean harMilitaer;

    public VurderMilitærDto(Boolean harMilitaer) {
        this.harMilitaer = harMilitaer;
    }


    public Boolean getHarMilitaer() {
        return harMilitaer;
    }

    public void setHarMilitaer(Boolean harMilitaer) {
        this.harMilitaer = harMilitaer;
    }
}
