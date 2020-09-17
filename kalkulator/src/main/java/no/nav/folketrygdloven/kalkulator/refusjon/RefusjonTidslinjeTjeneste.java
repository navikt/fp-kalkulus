package no.nav.folketrygdloven.kalkulator.refusjon;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.refusjon.modell.RefusjonPeriode;
import no.nav.folketrygdloven.kalkulator.refusjon.modell.RefusjonPeriodeEndring;
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
        Map<Arbeidsgiver, Set<BeregningsgrunnlagPrStatusOgAndelDto>> refusjonAndelIdListMap = lagAndelsmap(periode);
        List<RefusjonAndel> andeler = lagAndelsliste(refusjonAndelIdListMap);
        return new RefusjonPeriode(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), andeler);
    }

    private static List<RefusjonAndel> lagAndelsliste(Map<Arbeidsgiver, Set<BeregningsgrunnlagPrStatusOgAndelDto>> refusjonAndelIdListMap) {
        List<RefusjonAndel> andeler = new ArrayList<>();
        refusjonAndelIdListMap.forEach((key, bgAndeler) -> {
            BigDecimal samletBrutto = bgAndeler.stream()
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            BigDecimal samletRefusjon = bgAndeler.stream()
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            andeler.add(new RefusjonAndel(key, samletBrutto, samletRefusjon));
        });
        return andeler;
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
