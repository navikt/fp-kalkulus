package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.PeriodeSplitter;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.SplittPeriodeConfig;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.StandardPeriodeSplittCombinators;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;
import no.nav.k9.felles.konfigurasjon.env.Environment;

public class PeriodiserForAktivitetsgradTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(PeriodiserForAktivitetsgradTjeneste.class);

    public static BeregningsgrunnlagDto splittVedEndringIAktivitetsgrad(BeregningsgrunnlagDto beregningsgrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {

        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {

            for (UtbetalingsgradPrAktivitetDto ut : utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet()) {
                for (PeriodeMedUtbetalingsgradDto p : ut.getPeriodeMedUtbetalingsgrad()) {
                    if (Environment.current().isDev()) {
                        logger.info("Grunnlagsdata for [{}-{}] aktivitetsgrad {}", p.getPeriode().getFomDato(), p.getPeriode().getTomDato(), p.getAktivitetsgrad().orElse(null));
                    }
                }
            }

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
            if (Environment.current().isDev()) {
                logger.info("Beregningsgrunnlag før splitting pga tilkommet {}", prettyPrint(beregningsgrunnlag));
                logger.info("På tvers av arbeidsforhold-tidslinje for stp {}: {}", beregningsgrunnlag.getSkjæringstidspunkt(), prettyPrint(tidslinjePåTversAvArbeidsforhold));
            }
            beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(bg -> bg.getPeriode().getFomDato() + "," + bg.getPeriode().getTomDato()).reduce("", (a, b) -> a + ", " + b);
            BeregningsgrunnlagDto etterSplitt = getPeriodeSplitter(beregningsgrunnlag.getSkjæringstidspunkt()).splittPerioder(beregningsgrunnlag, tidslinjePåTversAvArbeidsforhold);
            if (Environment.current().isDev()) {
                logger.info("Beregningsgrunnlag etter splitting pga tilkommet {}", prettyPrint(etterSplitt));
            }
            return etterSplitt;
        }
        return beregningsgrunnlag;
    }

    private static String prettyPrint(LocalDateTimeline<BigDecimal> tidslinje) {
        return tidslinje.stream()
                .map(s -> "[" + s.getLocalDateInterval().getFomDato() + "," + s.getLocalDateInterval().getTomDato() + "]:" + s.getValue().toPlainString())
                .reduce("", (a, b) -> a + ", " + b);
    }

    private static String prettyPrint(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(bg -> "[" + bg.getPeriode().getFomDato() + "," + bg.getPeriode().getTomDato() + "]")
                .reduce("", (a, b) -> a + ", " + b);
    }


    private static PeriodeSplitter<BigDecimal> getPeriodeSplitter(LocalDate stp) {
        SplittPeriodeConfig<BigDecimal> splittPeriodeConfig = new SplittPeriodeConfig<>(
                StandardPeriodeSplittCombinators.splittPerioderOgSettÅrsakCombinator(ENDRING_I_AKTIVITETER_SØKT_FOR, (di1, lhs1, rhs1) -> di1.getFomDato().isAfter(stp) && lhs1 != null));
        splittPeriodeConfig.setLikhetsPredikatForCompress((e1, e2) -> e1.compareTo(e2) == 0);
        return new PeriodeSplitter<>(splittPeriodeConfig);
    }

}
