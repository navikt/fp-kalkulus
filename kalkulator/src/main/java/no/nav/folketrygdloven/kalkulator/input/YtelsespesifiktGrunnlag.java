package no.nav.folketrygdloven.kalkulator.input;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;

public interface YtelsespesifiktGrunnlag {

    int getDekningsgrad(BeregningsgrunnlagDto vlBeregningsgrunnlag, OpptjeningAktiviteterDto opptjeningAktiviteterDto);

    int getGrunnbeløpMilitærHarKravPå();

    void setGrunnbeløpMilitærHarKravPå(int antallGrunnbeløpMilitærHarKravPå);

}
