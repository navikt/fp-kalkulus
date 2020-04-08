package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;
import java.time.Period;

public abstract class Konfigverdier {
    // Standardverdier
    private BigDecimal avviksgrenseProsent = BigDecimal.valueOf(25);
    private BigDecimal antallGØvreGrenseverdi = BigDecimal.valueOf(6);
    protected BigDecimal antallGForOppfyltVilkår = BigDecimal.valueOf(0.5);
    private Period meldekortPeriode = Period.parse("P30D");

    // Verdier som ikke skal endres
    private final BigDecimal ytelsesdagerIÅr = BigDecimal.valueOf(260);

    // Verdier som må settes for hver ytelse
    private BigDecimal antallGMilitærHarKravPå;


    public Konfigverdier(BigDecimal antallGMilitærHarKravPå) {
        this.antallGMilitærHarKravPå = antallGMilitærHarKravPå;
    }

    public BigDecimal getAntallGMilitærHarKravPå() {
        return antallGMilitærHarKravPå;
    }

    public BigDecimal getAvviksgrenseProsent() {
        return avviksgrenseProsent;
    }

    public BigDecimal getAntallGØvreGrenseverdi() {
        return antallGØvreGrenseverdi;
    }

    public BigDecimal getAntallGForOppfyltVilkår() {
        return antallGForOppfyltVilkår;
    }

    public Period getMeldekortPeriode() {
        return meldekortPeriode;
    }

    public BigDecimal getYtelsesdagerIÅr() {
        return ytelsesdagerIÅr;
    }
}
