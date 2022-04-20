package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriode;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriodeEndring;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class RefusjonTidslinjeTjeneste {

    public static LocalDateTimeline<RefusjonPeriode> lagTidslinje(BeregningsgrunnlagDto beregningsgrunnlag, boolean utbetalt) {
        List<LocalDateSegment<RefusjonPeriode>> periodeSegmenter = beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .stream()
                .map(periode -> new LocalDateSegment<>(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), lagRefusjonsperiode(periode, utbetalt)))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(periodeSegmenter).compress();
    }

    private static RefusjonPeriode lagRefusjonsperiode(BeregningsgrunnlagPeriodeDto periode, boolean utbetalt) {
        List<RefusjonAndel> andeler = lagAndelsliste(periode.getBeregningsgrunnlagPrStatusOgAndelList(), utbetalt);
        return new RefusjonPeriode(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), andeler);
    }

    private static List<RefusjonAndel> lagAndelsliste(List<BeregningsgrunnlagPrStatusOgAndelDto> bgAndeler, boolean utbetalt) {
        return bgAndeler.stream()
                .map(a -> new RefusjonAndel(a.getAktivitetStatus(), a.getArbeidsgiver().orElse(null), a.getArbeidsforholdRef().orElse(null), getBrutto(a, utbetalt), getRefusjonskravPrÅr(a, utbetalt)))
                .collect(Collectors.toList());
    }

    private static BigDecimal getBrutto(BeregningsgrunnlagPrStatusOgAndelDto a, boolean utbetalt) {
        // Tar hensyn til om det er søkt om ytelse i original behandling. Dersom det ikke er søkt vil dagsats vere 0.
        // Dersom man ikke ser bort fra disse vil nye søknader kunne tolkes som et tilbaketrekk fra bruker.
        // Forklaringen på dette er at brutto > 0 selv om utbetalingsgrad = 0, men refusjonskravPrÅr = 0 når utbetalingsgrad = 0.
        // Ref: https://jira.adeo.no/browse/TSF-2590
        if (utbetalt && (a.getDagsats() == null || a.getDagsats() == 0L)) {
            return BigDecimal.ZERO;
        }
        return a.getBruttoPrÅr() == null ? BigDecimal.ZERO : a.getBruttoPrÅr();
    }

    private static BigDecimal getRefusjonskravPrÅr(BeregningsgrunnlagPrStatusOgAndelDto andel, boolean utbetalt) {
        if (utbetalt) {
            return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(BigDecimal.ZERO);
        }
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getInnvilgetRefusjonskravPrÅr).orElse(BigDecimal.ZERO);
    }


    public static LocalDateTimeline<RefusjonPeriodeEndring> kombinerTidslinjer(LocalDateTimeline<RefusjonPeriode> originalePerioder, LocalDateTimeline<RefusjonPeriode> revurderingPerioder) {
        return originalePerioder.intersection(revurderingPerioder, (dateInterval, segment1, segment2) ->
        {
            RefusjonPeriode orignalPeriode = segment1.getValue();
            RefusjonPeriode revurderingPeriode = segment2.getValue();
            RefusjonPeriodeEndring refusjonPeriodeEndring = new RefusjonPeriodeEndring(orignalPeriode.getAndeler(), revurderingPeriode.getAndeler());
            return new LocalDateSegment<>(dateInterval, refusjonPeriodeEndring);
        });
    }

}
