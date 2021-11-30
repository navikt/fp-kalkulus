package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.MidlertidigInaktivType;

public class PleiepengerNærståendeGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private Integer grunnbeløpMilitærHarKravPå;
    protected final int dekningsgrad = 100;
    protected final int dekningsgrad_inaktiv = 65;

    public PleiepengerNærståendeGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
    }

    @Override
    public int getDekningsgrad(BeregningsgrunnlagDto bg, OpptjeningAktiviteterDto opptjeningAktiviteterDto) {
        if (erMidlertidigInaktivTypeA(bg, opptjeningAktiviteterDto)) {
            return dekningsgrad_inaktiv;
        }
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
