package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

public class FastsettMånedsinntektFLDto {

    private Integer maanedsinntekt;

    public FastsettMånedsinntektFLDto(Integer maanedsInntekt) { // NOSONAR
        this.maanedsinntekt = maanedsInntekt;
    }

    public void setMaanedsinntekt(Integer maanedsinntekt) {
        this.maanedsinntekt = maanedsinntekt;
    }

    public Integer getMaanedsinntekt() {
        return maanedsinntekt;
    }
}
