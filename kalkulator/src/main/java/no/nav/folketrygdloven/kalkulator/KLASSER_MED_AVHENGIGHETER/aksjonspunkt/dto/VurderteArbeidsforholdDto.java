package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

public class VurderteArbeidsforholdDto  {

    private Long andelsnr;
    private boolean tidsbegrensetArbeidsforhold;
    private Boolean opprinneligVerdi;

    public VurderteArbeidsforholdDto(Long andelsnr,
                                     boolean tidsbegrensetArbeidsforhold,
                                     Boolean opprinneligVerdi) {
        this.andelsnr = andelsnr;
        this.tidsbegrensetArbeidsforhold = tidsbegrensetArbeidsforhold;
        this.opprinneligVerdi = opprinneligVerdi;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public boolean isTidsbegrensetArbeidsforhold() {
        return tidsbegrensetArbeidsforhold;
    }

    public Boolean isOpprinneligVerdi() {
        return opprinneligVerdi;
    }
}
