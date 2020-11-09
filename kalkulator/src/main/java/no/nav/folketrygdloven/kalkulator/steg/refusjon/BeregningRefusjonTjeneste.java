package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriode;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriodeEndring;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.vedtak.konfig.Tid;

/**
 * Tjeneste for å vurdere refusjonskrav for beregning
 * I noen tilfeller, f.eks ved innsending av refusjonskrav etter allerede utbetalt periode, skal ikke alltid
 * dette refusjonskravet gjelde fra den perioden arbeidsgiver ber om det. Da kan saksbehandler måtte fastsette startdato
 * for refusjon selv.
 */
public final class BeregningRefusjonTjeneste {

    private BeregningRefusjonTjeneste() {
        // SKjuler default
    }

    /**
     * @param andelerMedØktRefusjonIUtbetaltPeriode - map mellom en periode og alle andeler i perioden som har økt refusjonskrav
     * @param originaltBeregningsgrunnlag - det orginale beregningsgrunnlaget
     * @return - sjekker andelerMedØktRefusjonIUtbetaltPeriode mot originaltBeregningsgrunnlag for å se
     * hvilke andeler med økt refusjon i ubtetalt periode som har hatt tidligerer utbetalt refusjon
     */
    public static Map<Intervall, List<RefusjonAndel>> finnAndelerMedØktRefusjonMedTidligereRefusjonIkkeTidligereVurdert(Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjonIUtbetaltPeriode,
                                                                                                                        BeregningsgrunnlagDto originaltBeregningsgrunnlag,
                                                                                                                        List<BeregningRefusjonOverstyringDto> orginaleOverstyringer) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndelerIOrginaltGrunnlag = originaltBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjonOgTidligereUtbetaltRefusjon = new HashMap<>();
        andelerMedØktRefusjonIUtbetaltPeriode.entrySet().forEach(entry -> {
            List<RefusjonAndel> andelerMedØktRefusjonOgTidligereRefusjon = finnAndelerMedTidligereRefusjon(entry, alleAndelerIOrginaltGrunnlag);
            if (!andelerMedØktRefusjonOgTidligereRefusjon.isEmpty()) {
                andelerMedØktRefusjonOgTidligereUtbetaltRefusjon.put(entry.getKey(), andelerMedØktRefusjonOgTidligereRefusjon);
            }
        });

        // Hvis det er vurdert før må man kunne vurdere igjen
        List<Arbeidsgiver> arbeidsgivereOrginaltVurdert = hentArbeidsgivereDetTidligereErVurdertRefusjonFor(orginaleOverstyringer);
        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjonOgTidligereRefusjonIkkeTidligereVurdert = new HashMap<>();
        andelerMedØktRefusjonOgTidligereUtbetaltRefusjon.forEach((key, value) -> {
            List<RefusjonAndel> andelerIkkeTidligereVurdert = value.stream()
                    .filter(andel -> !arbeidsgivereOrginaltVurdert.contains(andel.getArbeidsgiver()))
                    .collect(Collectors.toList());
            if (!andelerIkkeTidligereVurdert.isEmpty()) {
                andelerMedØktRefusjonOgTidligereRefusjonIkkeTidligereVurdert.put(key, andelerIkkeTidligereVurdert);
            }
        });


        return andelerMedØktRefusjonOgTidligereRefusjonIkkeTidligereVurdert;
    }

    private static List<Arbeidsgiver> hentArbeidsgivereDetTidligereErVurdertRefusjonFor(List<BeregningRefusjonOverstyringDto> orginaleOverstyringer) {
        return orginaleOverstyringer.stream()
                .filter(ro -> !ro.getRefusjonPerioder().isEmpty())
                .map(BeregningRefusjonOverstyringDto::getArbeidsgiver)
                .collect(Collectors.toList());
    }


    private static List<RefusjonAndel> finnAndelerMedTidligereRefusjon(Map.Entry<Intervall, List<RefusjonAndel>> entry, List<BeregningsgrunnlagPrStatusOgAndelDto> alleOrginaleAndeler) {
        return entry.getValue().stream()
                .filter(andelMedØktRefusjon -> andelFinnesIOrginaltGrunnlagMedRefusjon(andelMedØktRefusjon, alleOrginaleAndeler))
                .collect(Collectors.toList());
    }

    private static boolean andelFinnesIOrginaltGrunnlagMedRefusjon(RefusjonAndel andelMedØktRefusjon, List<BeregningsgrunnlagPrStatusOgAndelDto> alleOrginaleAndeler) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> matchendeAndeler = finnMatchendeAndelIOrginalperiode(andelMedØktRefusjon, alleOrginaleAndeler);
        return matchendeAndeler.stream().anyMatch(BeregningRefusjonTjeneste::harRefusjon);
    }

    private static boolean harRefusjon(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        BigDecimal refusjonskrav = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(BigDecimal.ZERO);
        return refusjonskrav.compareTo(BigDecimal.ZERO) > 0;
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> finnMatchendeAndelIOrginalperiode(RefusjonAndel andelMedØktRefusjon, List<BeregningsgrunnlagPrStatusOgAndelDto> orginaleAndeler) {
        return orginaleAndeler.stream()
                .filter(bgAndel -> bgAndel.getAktivitetStatus().erArbeidstaker() && bgAndel.getBgAndelArbeidsforhold().isPresent())
                .filter(bgAndel -> andelMedØktRefusjon.getArbeidsgiver().equals(bgAndel.getArbeidsgiver().orElse(null)))
                .filter(bgAndel -> bgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).orElse(InternArbeidsforholdRefDto.nullRef()).gjelderFor(andelMedØktRefusjon.getArbeidsforholdRef()))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param revurderingBeregningsgrunnlag - nytt beregningsgrunnlag
     * @param originaltBeregningsgrunnlag - beregningsgrunnlag fra forrige behandling
     * @param alleredeUtbetaltTOM - datoen ytelse er utbetalt til, det er kun relevant å se på perioder frem til denne datoen
     * @return - Ser på revurderingBeregningsgrunnlag og sjekker hvilke andeler i hvilke perioder
     * frem til alleredeUtbetaltTOM som har hatt økt refusjon i forhold til originaltBeregningsgrunnlag
     */
    public static Map<Intervall, List<RefusjonAndel>> finnUtbetaltePerioderMedAndelerMedØktRefusjon(BeregningsgrunnlagDto revurderingBeregningsgrunnlag,
                                                                                                    BeregningsgrunnlagDto originaltBeregningsgrunnlag,
                                                                                                    LocalDate alleredeUtbetaltTOM) {
        if (alleredeUtbetaltTOM.isBefore(revurderingBeregningsgrunnlag.getSkjæringstidspunkt())) {
            return Collections.emptyMap();
        }
        LocalDateTimeline<RefusjonPeriode> alleredeUtbetaltPeriode = finnAlleredeUtbetaltPeriode(alleredeUtbetaltTOM);
        LocalDateTimeline<RefusjonPeriode> originalUtbetaltTidslinje = RefusjonTidslinjeTjeneste.lagTidslinje(originaltBeregningsgrunnlag).intersection(alleredeUtbetaltPeriode);
        LocalDateTimeline<RefusjonPeriode> revurderingTidslinje = RefusjonTidslinjeTjeneste.lagTidslinje(revurderingBeregningsgrunnlag);
        LocalDateTimeline<RefusjonPeriodeEndring> endringTidslinje = RefusjonTidslinjeTjeneste.kombinerTidslinjer(originalUtbetaltTidslinje, revurderingTidslinje);

        return vurderPerioder(endringTidslinje);
    }

    private static LocalDateTimeline<RefusjonPeriode> finnAlleredeUtbetaltPeriode(LocalDate alleredeUtbetaltTOM) {
        return new LocalDateTimeline<>(
                Tid.TIDENES_BEGYNNELSE,
                alleredeUtbetaltTOM,
                null);
    }

    private static Map<Intervall, List<RefusjonAndel>> vurderPerioder(LocalDateTimeline<RefusjonPeriodeEndring> endringTidslinje) {
        Map<Intervall, List<RefusjonAndel>> andelerIPeriode = new HashMap<>();
        endringTidslinje.toSegments().forEach(segment -> {
            RefusjonPeriodeEndring refusjonsendring = segment.getValue();
            if (erMindreAndelTilgjengeligForBruker(refusjonsendring)) {
                // Bruker vil få mindre andel av beregningsgrunnlaget, vi må lage liste med hvilker andeler som nå får en større andel
                List<RefusjonAndel> andelerMedØktRefusjon = finnAndelerMedMindreTilgjengeligForBruker(refusjonsendring);
                Intervall interval = Intervall.fraOgMedTilOgMed(segment.getFom(), segment.getTom());
                if (!andelerMedØktRefusjon.isEmpty()) {
                    andelerIPeriode.put(interval, andelerMedØktRefusjon);
                }
            }
        });
        return andelerIPeriode;
    }

    private static List<RefusjonAndel> finnAndelerMedMindreTilgjengeligForBruker(RefusjonPeriodeEndring refusjonsendring) {
        List<RefusjonAndel> revurderingAndeler = refusjonsendring.getRevurderingAndeler();
        List<RefusjonAndel> originaleAndeler = refusjonsendring.getOriginaleAndeler();
        List<RefusjonAndel> andelerMedØktRefusjon = new ArrayList<>();
        revurderingAndeler.forEach(andel -> {
            List<RefusjonAndel> matchedeAndeler = finnOrginaleAndeler(andel, originaleAndeler);
            if (andelHarMindreTilgjengeligForBruker(andel, matchedeAndeler)) {
                andelerMedØktRefusjon.add(andel);
            }
        });
        return andelerMedØktRefusjon;
    }

    private static boolean andelHarMindreTilgjengeligForBruker(RefusjonAndel revurderingAndel, List<RefusjonAndel> matchedeAndelerOrginal) {
        if (matchedeAndelerOrginal.isEmpty()) {
            // Det har tilkommet en revurderingAndel, sant dersom det er refusjon på den
            return revurderingAndel.getRefusjon().compareTo(BigDecimal.ZERO) > 0;
        }
        BigDecimal orginalBrutto = matchedeAndelerOrginal.stream()
                .map(RefusjonAndel::getBrutto)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        BigDecimal orginalRefusjon = matchedeAndelerOrginal.stream()
                .map(RefusjonAndel::getRefusjon)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        BigDecimal originalBrukersAndel = orginalBrutto.subtract(orginalRefusjon).max(BigDecimal.ZERO);
        BigDecimal revurderingBrukersAndel = revurderingAndel.getBrutto().subtract(revurderingAndel.getRefusjon()).max(BigDecimal.ZERO);

        boolean refusjonHarØkt = revurderingAndel.getRefusjon().compareTo(orginalRefusjon) > 0;
        boolean andelTilBrukerErMindre = revurderingBrukersAndel.compareTo(originalBrukersAndel) < 0;

        return refusjonHarØkt && andelTilBrukerErMindre;

    }

    private static List<RefusjonAndel> finnOrginaleAndeler(RefusjonAndel andel, List<RefusjonAndel> originaleAndeler) {
        // Hvis flere andeler er blitt slått sammen siden orginalbehandling må vi returnere en liste
        return originaleAndeler.stream().filter(a -> a.matcher(andel)).collect(Collectors.toList());
    }

    private static boolean erMindreAndelTilgjengeligForBruker(RefusjonPeriodeEndring refusjonsendring) {
        BigDecimal originalAndelTilBruker = refusjonsendring.getOriginalBrutto().subtract(refusjonsendring.getOriginalRefusjon()).max(BigDecimal.ZERO);
        BigDecimal revurderingAndelTilBruker = refusjonsendring.getRevurderingBrutto().subtract(refusjonsendring.getRevurderingRefusjon()).max(BigDecimal.ZERO);
        return revurderingAndelTilBruker.compareTo(originalAndelTilBruker) < 0;


    }

}
