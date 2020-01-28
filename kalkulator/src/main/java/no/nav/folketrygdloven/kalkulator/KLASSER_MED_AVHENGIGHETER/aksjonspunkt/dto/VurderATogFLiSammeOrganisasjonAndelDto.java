package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

public class VurderATogFLiSammeOrganisasjonAndelDto {

    private Long andelsnr;

    private Integer arbeidsinntekt;

    public VurderATogFLiSammeOrganisasjonAndelDto(Long andelsnr, Integer arbeidsinntekt) { // NOSONAR
        this.andelsnr = andelsnr;
        this.arbeidsinntekt = arbeidsinntekt;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Integer getArbeidsinntekt() {
        return arbeidsinntekt;
    }
}
