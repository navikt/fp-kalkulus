package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.PeriodeSplitter;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.SplittPeriodeConfig;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.StandardPeriodeSplittCombinators;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class PeriodiserForAktivitetsgradTjeneste {

    public static BeregningsgrunnlagDto splittVedEndringIAktivitetsgrad(BeregningsgrunnlagDto beregningsgrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {

        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {

            var tidslinjePåTversAvArbeidsforhold = utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                    .map(a -> {
                        var segmenterForAktivitet = a.getPeriodeMedUtbetalingsgrad().stream()
                                .filter(p -> p.getAktivitetsgrad().isPresent())
                                .map(p -> new LocalDateSegment<>(p.getPeriode().getFomDato(), p.getPeriode().getTomDato(), p.getAktivitetsgrad().get()))
                                .toList();
                        var tidslinjeForAktivitet = new LocalDateTimeline<>(segmenterForAktivitet, StandardCombinators::rightOnly);
                        tidslinjeForAktivitet.compress((e1, e2) -> e1.compareTo(e2) == 0, StandardCombinators::leftOnly);
                        return tidslinjeForAktivitet;
                    })
                    .reduce((t1, t2) -> t1.crossJoin(t2, StandardCombinators::coalesceLeftHandSide))
                    .orElse(LocalDateTimeline.empty());
            return getPeriodeSplitter(beregningsgrunnlag.getSkjæringstidspunkt()).splittPerioder(beregningsgrunnlag, tidslinjePåTversAvArbeidsforhold);
        }
        return beregningsgrunnlag;
    }

    private static PeriodeSplitter<BigDecimal> getPeriodeSplitter(LocalDate stp) {
        SplittPeriodeConfig<BigDecimal> splittPeriodeConfig = new SplittPeriodeConfig<>(
                StandardPeriodeSplittCombinators.splittPerioderOgSettÅrsakCombinator(ENDRING_I_AKTIVITETER_SØKT_FOR, (di1, lhs1, rhs1) -> di1.getFomDato().isAfter(stp) && lhs1 != null));
        splittPeriodeConfig.setLikhetsPredikatForCompress((e1, e2) -> e1.compareTo(e2) == 0);
        return new PeriodeSplitter<>(splittPeriodeConfig);
    }

}
