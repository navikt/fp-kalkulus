package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class FrisinnGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private final int dekningsgrad = 80;

    private Integer grunnbeløpMilitærHarKravPå = 2;
    private final List<FrisinnPeriode> frisinnPerioder;

    public FrisinnGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, List<FrisinnPeriode> frisinnPerioder) {
        super(utbetalingsgradPrAktivitet);
        this.frisinnPerioder = frisinnPerioder;
    }


    @Override
    public int getDekningsgrad() {
        return dekningsgrad;
    }

    @Override
    public int getGrunnbeløpMilitærHarKravPå() {
        return grunnbeløpMilitærHarKravPå;
    }

    @Override
    public void setGrunnbeløpMilitærHarKravPå(int grunnbeløpMilitærHarKravPå) {
        this.grunnbeløpMilitærHarKravPå = grunnbeløpMilitærHarKravPå;
    }

    public boolean getSøkerYtelseForFrilans() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerFrilans);
    }

    public boolean getSøkerYtelseForFrilans(LocalDate dato) {
        return frisinnPerioder.stream().anyMatch(p -> p.getSøkerFrilans() && p.getPeriode().inkluderer(dato));
    }

    public boolean getSøkerYtelseForNæring() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerNæring);
    }

    public boolean getSøkerYtelseForNæring(LocalDate dato) {
        return frisinnPerioder.stream().anyMatch(p -> p.getSøkerNæring() && p.getPeriode().inkluderer(dato));
    }

    public List<FrisinnPeriode> getFrisinnPerioder() {
        return frisinnPerioder;
    }
}
