package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class OmsorgspengerGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private final int dekningsgrad = 100;
    private Integer grunnbeløpMilitærHarKravPå;

    public OmsorgspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        super(utbetalingsgradPrAktivitet);
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


}
