package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class SvangerskapspengerGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private int dekningsgrad = 100;
    private Integer grunnbeløpMilitærHarKravPå;


    public SvangerskapspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbetalingsgrad) {
        super(tilretteleggingMedUtbetalingsgrad);
    }

    @Override
    public int getDekningsgrad(BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        // egentlig ikke relevant, eller alt kan sees på som alltid 100% dekning
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
