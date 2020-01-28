package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto;

public class RefusjonskravPrArbeidsgiverVurderingDto {

    private String arbeidsgiverId;

    private boolean skalUtvideGyldighet;


    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public void setArbeidsgiverId(String arbeidsgiverId) {
        this.arbeidsgiverId = arbeidsgiverId;
    }

    public boolean isSkalUtvideGyldighet() {
        return skalUtvideGyldighet;
    }

    public void setSkalUtvideGyldighet(boolean skalUtvideGyldighet) {
        this.skalUtvideGyldighet = skalUtvideGyldighet;
    }
}
