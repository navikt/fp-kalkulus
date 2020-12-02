package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndelNøkkel;
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
                // Bruker vil få mindre andel av beregningsgrunnlaget, sjekk om noen andeler har fått økt refusjon
                List<RefusjonAndel> andelerMedØktRefusjon = finnAndelerMedØktRefusjon(refusjonsendring);
                if (!andelerMedØktRefusjon.isEmpty()) {
                    Intervall interval = Intervall.fraOgMedTilOgMed(segment.getFom(), segment.getTom());
                    andelerIPeriode.put(interval, andelerMedØktRefusjon);
                }
            }
        });
        return andelerIPeriode;
    }

    private static List<RefusjonAndel> finnAndelerMedØktRefusjon(RefusjonPeriodeEndring refusjonsendring) {
        Map<RefusjonAndelNøkkel, List<RefusjonAndel>> revurderingAndeler = refusjonsendring.getRevurderingAndelerMap();
        Map<RefusjonAndelNøkkel, List<RefusjonAndel>> originaleAndelMap = refusjonsendring.getOriginaleAndelerMap();
        List<RefusjonAndel> andelerMedØktRefusjon = new ArrayList<>();
        revurderingAndeler.forEach((nøkkel, andeler) -> andelerMedØktRefusjon.addAll(sjekkOmAndelerPåSammeNøkkelHarØktRefusjon(originaleAndelMap, nøkkel, andeler)));
        return andelerMedØktRefusjon;
    }

    private static List<RefusjonAndel> sjekkOmAndelerPåSammeNøkkelHarØktRefusjon(Map<RefusjonAndelNøkkel, List<RefusjonAndel>> originalAndelMap, RefusjonAndelNøkkel nøkkel, List<RefusjonAndel> revurderingAndeler) {
        List<RefusjonAndel> originaleAndelerPåNøkkel = originalAndelMap.getOrDefault(nøkkel, Collections.emptyList());
        // Tilkommet arbeidsgiver
        BigDecimal totalRefusjonRevurdering = totalRefusjon(revurderingAndeler);
        if (nøkkel.getAktivitetStatus().erArbeidstaker() && originaleAndelerPåNøkkel.isEmpty()) {
            if (totalRefusjonRevurdering.compareTo(BigDecimal.ZERO) > 0) {
                return revurderingAndeler;
            }
        }

        // Antar at vi ikke vil ha både generelle og spesifikke arbeidstakerandeler her
        validerReferanser(revurderingAndeler);
        BigDecimal totalRefusjonOriginal = totalRefusjon(originaleAndelerPåNøkkel);

        boolean refusjonINøkkelHarØkt = totalRefusjonRevurdering.compareTo(totalRefusjonOriginal) > 0;
        if (refusjonINøkkelHarØkt) {
            return finnAndelerPåSammeNøkkelMedØktRefusjon(revurderingAndeler, originaleAndelerPåNøkkel);

        }

        return Collections.emptyList();
    }

    private static List<RefusjonAndel> finnAndelerPåSammeNøkkelMedØktRefusjon(List<RefusjonAndel> alleRevurderingAndeler, List<RefusjonAndel> alleOriginaleAndeler) {
        List<RefusjonAndel> andelerMedØktRefusjon = new ArrayList<>();

        // Andeler med referanse som ikke finnes i originalt grunnlag må matches mot originale aggregatandeler
        List<RefusjonAndel> revurderingAndelerMedRefereanseSomIkkeKanMatches = andelerMedReferanseSomIkkeKanMatches(alleRevurderingAndeler, alleOriginaleAndeler);
        List<RefusjonAndel> originaleAggregatAndeler = alleOriginaleAndeler.stream()
                .filter(original -> !original.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .collect(Collectors.toList());
        if (harAndelerØktRefusjon(originaleAggregatAndeler, revurderingAndelerMedRefereanseSomIkkeKanMatches)) {
            andelerMedØktRefusjon.addAll(revurderingAndelerMedRefereanseSomIkkeKanMatches);
        }

        // Aggregatandeler i revurderingen må matches mot alle originale andeler
        List<RefusjonAndel> revurderingAggregatAndeler = alleRevurderingAndeler.stream()
                .filter(revurdering -> !revurdering.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .collect(Collectors.toList());
        if (harAndelerØktRefusjon(alleOriginaleAndeler, revurderingAggregatAndeler)) {
            andelerMedØktRefusjon.addAll(revurderingAggregatAndeler);
        }

        List<RefusjonAndel> spesifikkeRevurderingAndelerIBeggeGrunnlag = alleRevurderingAndeler.stream()
                .filter(andel -> gjelderSpesifikktArbeidsforholdOgHarMatchIOriginal(andel, alleOriginaleAndeler))
                .collect(Collectors.toList());
        spesifikkeRevurderingAndelerIBeggeGrunnlag.forEach(revurderingAndel -> {
            // Andel vi ikke har vurdert, finn referanse å vurdere den mot
            List<RefusjonAndel> matchendeOriginaleAndeler = alleOriginaleAndeler.stream()
                    .filter(original -> original.getArbeidsforholdRef().gjelderFor(revurderingAndel.getArbeidsforholdRef()))
                    .collect(Collectors.toList());
            if (harAndelerØktRefusjon(matchendeOriginaleAndeler, Collections.singletonList(revurderingAndel))) {
                andelerMedØktRefusjon.add(revurderingAndel);
            }
        });

        List<RefusjonAndel> kontrollerteAndeler = new ArrayList<>();
        Stream.of(revurderingAndelerMedRefereanseSomIkkeKanMatches, revurderingAggregatAndeler, spesifikkeRevurderingAndelerIBeggeGrunnlag).forEach(kontrollerteAndeler::addAll);
        validerAtAlleAndelerErKontrollert(kontrollerteAndeler, alleRevurderingAndeler);
        return andelerMedØktRefusjon;
    }

    private static void validerAtAlleAndelerErKontrollert(List<RefusjonAndel> kontrollerteAndeler, List<RefusjonAndel> alleRevurderingAndeler) {
        if (kontrollerteAndeler.size() != alleRevurderingAndeler.size() || !kontrollerteAndeler.containsAll(alleRevurderingAndeler)) {
            throw new IllegalStateException("Listen over kontrollerte andeler er har ikke samme størrelse som listen over revurderingsandeler, kontrollerte andeler: " +
                    "" + kontrollerteAndeler.toString() + " alle andeler: " + alleRevurderingAndeler.toString());
        }
    }

    private static boolean gjelderSpesifikktArbeidsforholdOgHarMatchIOriginal(RefusjonAndel revurderingAndel, List<RefusjonAndel> originaleAndelerPåNøkkel) {
        if (!revurderingAndel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
            return false;
        }
        return originaleAndelerPåNøkkel.stream()
                .anyMatch(andel -> andel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()
                && andel.getArbeidsforholdRef().gjelderFor(revurderingAndel.getArbeidsforholdRef()));
    }

    private static boolean harAndelerØktRefusjon(List<RefusjonAndel> listeÅSjekkeMot, List<RefusjonAndel> listeSomSkalSjekkes) {
        BigDecimal originalRefusjon = totalRefusjon(listeÅSjekkeMot);
        BigDecimal revurderingRefusjon = totalRefusjon(listeSomSkalSjekkes);
        return revurderingRefusjon.compareTo(originalRefusjon) > 0;
    }

    private static List<RefusjonAndel> andelerMedReferanseSomIkkeKanMatches(List<RefusjonAndel> revurderingAndeler, List<RefusjonAndel> originaleAndelerPåNøkkel) {
        return revurderingAndeler.stream()
                .filter(revurdering -> revurdering.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .filter(revurdering -> referanseManglerIOriginaltGrunnlag(revurdering.getArbeidsforholdRef(), originaleAndelerPåNøkkel))
                .collect(Collectors.toList());
    }

    private static boolean referanseManglerIOriginaltGrunnlag(InternArbeidsforholdRefDto arbeidsforholdRef, List<RefusjonAndel> originaleAndelerPåNøkkel) {
        return originaleAndelerPåNøkkel.stream()
                .noneMatch(andel -> Objects.equals(andel.getArbeidsforholdRef().getReferanse(), arbeidsforholdRef.getReferanse()));
    }

    private static void validerReferanser(List<RefusjonAndel> revurderingAndeler) {
        boolean finnesSpesifikkReferanse = revurderingAndeler.stream().anyMatch(andel -> andel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold());
        boolean finnesGenerellReferanse = revurderingAndeler.stream().anyMatch(andel -> !andel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold());
        if (finnesSpesifikkReferanse && finnesGenerellReferanse) {
            // Må løses i TFP-3933
            throw new IllegalStateException("Finnes andeler både med og uten referanse");
        }
    }

    private static BigDecimal totalRefusjon(List<RefusjonAndel> andeler) {
        return andeler.stream()
                .map(RefusjonAndel::getRefusjon)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static boolean erMindreAndelTilgjengeligForBruker(RefusjonPeriodeEndring refusjonsendring) {
        BigDecimal originalAndelTilBruker = refusjonsendring.getOriginalBrutto().subtract(refusjonsendring.getOriginalRefusjon()).max(BigDecimal.ZERO);
        BigDecimal revurderingAndelTilBruker = refusjonsendring.getRevurderingBrutto().subtract(refusjonsendring.getRevurderingRefusjon()).max(BigDecimal.ZERO);
        return revurderingAndelTilBruker.compareTo(originalAndelTilBruker) < 0;
    }

}
