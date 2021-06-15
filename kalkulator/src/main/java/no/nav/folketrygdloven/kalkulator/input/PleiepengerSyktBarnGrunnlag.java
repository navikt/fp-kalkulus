package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.MidlertidigInaktivType;

public class PleiepengerSyktBarnGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private final int dekningsgrad = 100;
    private final int dekningsgrad_inaktiv = 65;
    private Integer grunnbeløpMilitærHarKravPå;


    public PleiepengerSyktBarnGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
    }

    @Override
    public int getDekningsgrad(BeregningsgrunnlagDto bg) {
        return dekningsgrad;
    }

    public int getDekningsgradForMidlertidigInaktiv(BeregningsgrunnlagDto bg, OpptjeningAktiviteterDto dto) {
        if (erMidlertidigInaktiv(bg) && dto != null) {
            return MidlertidigInaktivType.A.equals(dto.getMidlertidigInaktivType()) ? dekningsgrad_inaktiv : dekningsgrad;
        }
        return dekningsgrad;
    }

    private boolean erMidlertidigInaktiv(BeregningsgrunnlagDto bg) {
        return bg.getAktivitetStatuser().stream().anyMatch(a -> AktivitetStatus.MIDLERTIDIG_INAKTIV.equals(a.getAktivitetStatus()));
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
