package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

public class VurderEtterlønnSluttpakkeDto {

    private Boolean erEtterlønnSluttpakke;

    public VurderEtterlønnSluttpakkeDto(Boolean erEtterlønnSluttpakke) { // NOSONAR
        this.erEtterlønnSluttpakke = erEtterlønnSluttpakke;
    }

    public Boolean erEtterlønnSluttpakke() {
        return erEtterlønnSluttpakke;
    }

    public void setErEtterlønnSluttpakke(Boolean erEtterlønnSluttpakke) {
        this.erEtterlønnSluttpakke = erEtterlønnSluttpakke;
    }
}
