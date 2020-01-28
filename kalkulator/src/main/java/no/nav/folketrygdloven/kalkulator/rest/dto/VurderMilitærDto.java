package no.nav.folketrygdloven.kalkulator.rest.dto;

public class VurderMilitærDto {

    private Boolean harMilitaer;

    public VurderMilitærDto(Boolean harMilitaer) {
        this.harMilitaer = harMilitaer;
    }

    public Boolean getHarMilitaer() {
        return harMilitaer;
    }
}
