package no.nav.folketrygdloven.kalkulus.rest.tjenester.fakta;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;


/**
 Hjelpeklasse som utleder hvilket tall som best representerer årsinntekt for beregningsgrunnlaget da GUI idag kun støtter visning for et slikt tall selv om grunnlaget er periodisert
 */
public final class FinnÅrsinntektvisningstall {

    public static Optional<BigDecimal> finn(BeregningsgrunnlagDto beregningsgrunnlag) {

        if (beregningsgrunnlag.getBeregningsgrunnlagPerioder().isEmpty()) {
            return Optional.empty();
        }

        if (harStatusKunYtelse(beregningsgrunnlag)) {
            return Optional.ofNullable(førstePeriode(beregningsgrunnlag).getBruttoPrÅr());
        }

        if (erSelvstendigNæringsdrivende(beregningsgrunnlag)) {

            if  (harBesteberegningtilfelle(beregningsgrunnlag)) {
                return Optional.ofNullable(førstePeriode(beregningsgrunnlag).getBruttoPrÅr());
            }

            return finnBeregnetÅrsinntektVisningstallSelvstendigNæringsdrivende(beregningsgrunnlag);
        }

        return Optional.ofNullable(førstePeriode(beregningsgrunnlag).getBeregnetPrÅr());
    }

    private static Optional<BigDecimal> finnBeregnetÅrsinntektVisningstallSelvstendigNæringsdrivende(BeregningsgrunnlagDto beregningsgrunnlag) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> snAndelOpt = førstePeriode(beregningsgrunnlag).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
            .findFirst();

        if (snAndelOpt.isPresent()) {
            BeregningsgrunnlagPrStatusOgAndelDto snAndel = snAndelOpt.get();

            if (snAndel.getNyIArbeidslivet() == null || Boolean.FALSE.equals(snAndel.getNyIArbeidslivet())) {
                return Optional.ofNullable(snAndel.getPgiSnitt());
            }
        }
        return Optional.empty();
    }

    private static boolean harBesteberegningtilfelle(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE);
    }

    private static boolean erSelvstendigNæringsdrivende(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().stream()
            .anyMatch(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende());
    }

    private static BeregningsgrunnlagPeriodeDto førstePeriode(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
    }

    private static boolean harStatusKunYtelse(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().stream()
            .anyMatch(a -> AktivitetStatus.KUN_YTELSE.equals(a.getAktivitetStatus()));
    }
}
