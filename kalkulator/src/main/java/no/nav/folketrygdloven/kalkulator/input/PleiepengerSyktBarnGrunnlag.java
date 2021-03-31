package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class PleiepengerSyktBarnGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private final int dekningsgrad = 100;
    private final int dekningsgrad_inaktiv = 65;
    private Integer grunnbeløpMilitærHarKravPå;


    public PleiepengerSyktBarnGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
    }

    @Override
    public int getDekningsgrad(BeregningsgrunnlagDto bg) {
        if (erMidlertidigInaktiv(bg)) {
            return erInaktivPåSkjæringstidspunktet(bg) ? dekningsgrad_inaktiv : dekningsgrad;
        }
        return dekningsgrad;
    }

    private boolean erInaktivPåSkjæringstidspunktet(BeregningsgrunnlagDto bg) {
        var periode = bg.getBeregningsgrunnlagPerioder().stream().filter(p -> p.getBeregningsgrunnlagPeriodeFom().equals(bg.getSkjæringstidspunkt()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Fant ingen periode med start på skjæringstidspunktet"));
        boolean harKunBrukersAndelPÅSkjæringstidspunktet = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().allMatch(andel -> AktivitetStatus.BRUKERS_ANDEL.equals(andel.getAktivitetStatus()));
        return !harKunBrukersAndelPÅSkjæringstidspunktet;
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
