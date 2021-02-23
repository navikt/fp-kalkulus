package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;

public abstract class Konfigverdier {
    // Standardverdier
    public static final int FRIST_MÅNEDER_ETTER_REFUSJON = 3;
    private final BigDecimal avviksgrenseProsent = BigDecimal.valueOf(25);
    private final BigDecimal antallGØvreGrenseverdi = BigDecimal.valueOf(6);
    private final Period meldekortPeriode = Period.parse("P30D");

    protected BigDecimal antallGForOppfyltVilkår = BigDecimal.valueOf(0.5);
    protected BigDecimal antallGForOppfyltVilkårInaktiv = BigDecimal.valueOf(1);

    // Verdier som ikke skal endres
    private final BigDecimal ytelsesdagerIÅr = BigDecimal.valueOf(260);

    // Verdier som må settes for hver ytelse
    private final BigDecimal antallGMilitærHarKravPå;


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

    public BigDecimal getAntallGForOppfyltVilkårInaktiv() {
        return antallGForOppfyltVilkårInaktiv;
    }

    public Period getMeldekortPeriode() {
        return meldekortPeriode;
    }

    public BigDecimal getYtelsesdagerIÅr() {
        return ytelsesdagerIÅr;
    }

    public int getFristMånederEtterRefusjon(LocalDate datoForInnsendtRefKrav) {
        return FRIST_MÅNEDER_ETTER_REFUSJON;
    }

    public Hjemmel getHjemmelForRefusjonfrist(LocalDate datoForInnsendtRefKrav) {
        return Hjemmel.F_22_13_6;
    }
}
