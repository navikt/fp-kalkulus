package no.nav.folketrygdloven.kalkulator.output;

import static java.util.Collections.singletonList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

public class BeregningAksjonspunktResultat {

    private BeregningAksjonspunkt beregningAksjonspunktDefinisjon;
    private BeregningVenteårsak venteårsak;
    private LocalDateTime ventefrist;


    private BeregningAksjonspunktResultat(BeregningAksjonspunkt aksjonspunktDefinisjon) {
        this.beregningAksjonspunktDefinisjon = aksjonspunktDefinisjon;
    }

    private BeregningAksjonspunktResultat(BeregningAksjonspunkt aksjonspunktDefinisjon, BeregningVenteårsak venteårsak, LocalDateTime ventefrist) {
        this.beregningAksjonspunktDefinisjon = aksjonspunktDefinisjon;
        this.venteårsak = venteårsak;
        this.ventefrist = ventefrist;
    }

    /**
     * Factory-metode direkte basert på {@link BeregningAksjonspunkt}. Ingen callback for consumer.
     */
    public static BeregningAksjonspunktResultat opprettFor(BeregningAksjonspunkt aksjonspunktDefinisjon) {
        return new BeregningAksjonspunktResultat(aksjonspunktDefinisjon);
    }

    /**
     * Factory-metode direkte basert på {@link BeregningAksjonspunkt}, returnerer liste. Ingen callback for consumer.
     */
    public static List<BeregningAksjonspunktResultat> opprettListeFor(BeregningAksjonspunkt aksjonspunktDefinisjon) {
        return singletonList(new BeregningAksjonspunktResultat(aksjonspunktDefinisjon));
    }

    /**
     * Factory-metode som linker {@link BeregningAksjonspunkt} sammen med callback for consumer-operasjon.
     */
    public static BeregningAksjonspunktResultat opprettMedFristFor(BeregningAksjonspunkt aksjonspunktDefinisjon, BeregningVenteårsak venteårsak, LocalDateTime ventefrist) {
        return new BeregningAksjonspunktResultat(aksjonspunktDefinisjon, venteårsak, ventefrist);
    }

    public BeregningAksjonspunkt getBeregningAksjonspunktDefinisjon() {
        return beregningAksjonspunktDefinisjon;
    }

    public BeregningVenteårsak getVenteårsak() {
        return venteårsak;
    }

    public LocalDateTime getVentefrist() {
        return ventefrist;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + beregningAksjonspunktDefinisjon.getKode() 
            + ", venteårsak=" + getVenteårsak() + ", ventefrist=" + getVentefrist() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BeregningAksjonspunktResultat))
            return false;

        BeregningAksjonspunktResultat that = (BeregningAksjonspunktResultat) o;

        return beregningAksjonspunktDefinisjon.getKode().equals(that.beregningAksjonspunktDefinisjon.getKode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregningAksjonspunktDefinisjon.getKode());
    }

    public boolean harFrist() {
        return null != getVentefrist();
    }
}
