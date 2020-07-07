package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;

public abstract class Konfigverdier {
    // Standardverdier
    private BigDecimal avviksgrenseProsent = BigDecimal.valueOf(25);
    private BigDecimal antallGØvreGrenseverdi = BigDecimal.valueOf(6);
    private Period meldekortPeriode = Period.parse("P30D");

    protected BigDecimal antallGForOppfyltVilkår = BigDecimal.valueOf(0.5);
    protected int fristMånederEtterRefusjon = 3;

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

    public int getFristMånederEtterRefusjon(LocalDate datoForInnsendtRefKrav) {
        return fristMånederEtterRefusjon;
    }

    public Hjemmel getHjemmelForRefusjonfrist(LocalDate datoForInnsendtRefKrav) {
        return Hjemmel.F_22_13_6;
    }
}
