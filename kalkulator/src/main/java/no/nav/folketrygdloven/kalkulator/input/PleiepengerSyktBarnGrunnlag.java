package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class PleiepengerSyktBarnGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private int dekningsgrad = 100;
    private Integer grunnbeløpMilitærHarKravPå;


    public PleiepengerSyktBarnGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
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
