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

    public static LocalDateTimeline<RefusjonPeriode> lagTidslinje(BeregningsgrunnlagDto beregningsgrunnlag) {
        List<LocalDateSegment<RefusjonPeriode>> periodeSegmenter = beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .stream()
                .map(periode -> new LocalDateSegment<>(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), lagRefusjonsperiode(periode)))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(periodeSegmenter).compress();
    }

    private static RefusjonPeriode lagRefusjonsperiode(BeregningsgrunnlagPeriodeDto periode) {
        List<RefusjonAndel> andeler = lagAndelsliste(periode.getBeregningsgrunnlagPrStatusOgAndelList());
        return new RefusjonPeriode(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), andeler);
    }

    private static List<RefusjonAndel> lagAndelsliste(List<BeregningsgrunnlagPrStatusOgAndelDto> bgAndeler) {
        return bgAndeler.stream()
                .filter(andel -> andel.getAktivitetStatus().erArbeidstaker() && andel.getArbeidsgiver().isPresent())
                .map(a -> new RefusjonAndel(a.getArbeidsgiver().get(), a.getArbeidsforholdRef().orElse(null), getBrutto(a), getRefusjonskravPrÅr(a)))
                .collect(Collectors.toList());
    }

    private static BigDecimal getBrutto(BeregningsgrunnlagPrStatusOgAndelDto a) {
        return a.getBruttoPrÅr() == null ? BigDecimal.ZERO : a.getBruttoPrÅr();
    }

    private static BigDecimal getRefusjonskravPrÅr(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(BigDecimal.ZERO);
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
