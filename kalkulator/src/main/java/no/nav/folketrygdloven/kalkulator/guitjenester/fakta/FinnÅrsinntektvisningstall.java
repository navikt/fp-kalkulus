package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


/**
 Hjelpeklasse som utleder hvilket tall som best representerer årsinntekt for beregningsgrunnlaget da GUI idag kun støtter visning for et slikt tall selv om grunnlaget er periodisert
 */
public final class FinnÅrsinntektvisningstall {

    public static Optional<BigDecimal> finn(BeregningsgrunnlagDto beregningsgrunnlag, Optional<FaktaAktørDto> faktaAktør) {

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

            return finnBeregnetÅrsinntektVisningstallSelvstendigNæringsdrivende(beregningsgrunnlag, faktaAktør);
        }

        return Optional.ofNullable(førstePeriode(beregningsgrunnlag).getBeregnetPrÅr());
    }

    private static Optional<BigDecimal> finnBeregnetÅrsinntektVisningstallSelvstendigNæringsdrivende(BeregningsgrunnlagDto beregningsgrunnlag, Optional<FaktaAktørDto> faktaAktør) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> snAndelOpt = førstePeriode(beregningsgrunnlag).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
            .findFirst();

        if (snAndelOpt.isPresent()) {
            BeregningsgrunnlagPrStatusOgAndelDto snAndel = snAndelOpt.get();

            if (Boolean.FALSE.equals(erNyIArbeidslivet(faktaAktør))) {
                return Optional.ofNullable(snAndel.getPgiSnitt());
            }
        }
        return Optional.empty();
    }

    private static Boolean erNyIArbeidslivet(Optional<FaktaAktørDto> faktaAktør) {
        return faktaAktør.map(FaktaAktørDto::getErNyIArbeidslivetSN).orElse(false);
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
