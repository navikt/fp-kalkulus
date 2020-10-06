package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
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
        List<RefusjonAndel> refusjonandeler = bgAndeler.stream()
                .filter(andel -> andel.getAktivitetStatus().erArbeidstaker() && andel.getArbeidsgiver().isPresent())
                .map(a -> new RefusjonAndel(a.getArbeidsgiver().get(), a.getArbeidsforholdRef().orElse(null), getBrutto(a), getRefusjonskravPrÅr(a)))
                .collect(Collectors.toList());
        return refusjonandeler;
    }

    private static BigDecimal getBrutto(BeregningsgrunnlagPrStatusOgAndelDto a) {
        return a.getBruttoPrÅr() == null ? BigDecimal.ZERO : a.getBruttoPrÅr();
    }

    private static BigDecimal getRefusjonskravPrÅr(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(BigDecimal.ZERO);
    }

    private static Map<Arbeidsgiver, Set<BeregningsgrunnlagPrStatusOgAndelDto>> lagAndelsmap(BeregningsgrunnlagPeriodeDto periode) {
        Map<Arbeidsgiver, Set<BeregningsgrunnlagPrStatusOgAndelDto>> resultatMap = new HashMap<>();
        periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> a.getArbeidsgiver().isPresent())
                .forEach(andel ->  {
                    Set<BeregningsgrunnlagPrStatusOgAndelDto> andelSet = resultatMap.getOrDefault(andel.getArbeidsgiver().get(), new HashSet<>());
                    andelSet.add(andel);
                    resultatMap.put(andel.getArbeidsgiver().get(), andelSet);
                });
        return resultatMap;
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
