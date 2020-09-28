package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriode;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriodeEndring;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

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

    public static boolean måVurdereRefusjonskravForBeregning(BeregningsgrunnlagDto revurderingBeregningsgrunnlag, BeregningsgrunnlagDto originaltBeregningsgrunnlag) {
        return !finnPerioderMedAndelerMedØktRefusjon(revurderingBeregningsgrunnlag, originaltBeregningsgrunnlag).isEmpty();
    }

    public static Map<Intervall, List<RefusjonAndel>> finnPerioderMedAndelerMedØktRefusjon(BeregningsgrunnlagDto revurderingBeregningsgrunnlag, BeregningsgrunnlagDto originaltBeregningsgrunnlag) {
        LocalDateTimeline<RefusjonPeriode> originalTidslinje = RefusjonTidslinjeTjeneste.lagTidslinje(originaltBeregningsgrunnlag);
        LocalDateTimeline<RefusjonPeriode> revurderingTidslinje = RefusjonTidslinjeTjeneste.lagTidslinje(revurderingBeregningsgrunnlag);
        LocalDateTimeline<RefusjonPeriodeEndring> endringTidslinje = RefusjonTidslinjeTjeneste.kombinerTidslinjer(originalTidslinje, revurderingTidslinje);

        return vurderPerioder(endringTidslinje);
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
            Optional<RefusjonAndel> originalAndel = finnOriginalAndel(andel, originaleAndeler);
            if (andelHarMindreTilgjengeligForBruker(andel, originalAndel)) {
                andelerMedØktRefusjon.add(andel);
            }
        });
        return andelerMedØktRefusjon;
    }

    private static boolean andelHarMindreTilgjengeligForBruker(RefusjonAndel revurderingAndel, Optional<RefusjonAndel> originalAndelOpt) {
        if (originalAndelOpt.isEmpty()) {
            // Det har tilkommet en revurderingAndel, sant dersom det er refusjon på den
            return revurderingAndel.getRefusjon().compareTo(BigDecimal.ZERO) > 0;
        }

        RefusjonAndel originalAndel = originalAndelOpt.get();
        BigDecimal originalBrukersAndel = originalAndel.getBrutto().subtract(originalAndel.getRefusjon()).max(BigDecimal.ZERO);
        BigDecimal revurderingBrukersAndel = revurderingAndel.getBrutto().subtract(revurderingAndel.getRefusjon()).max(BigDecimal.ZERO);

        boolean refusjonHarØkt = revurderingAndel.getRefusjon().compareTo(originalAndel.getRefusjon()) > 0;
        boolean andelTilBrukerErMindre = revurderingBrukersAndel.compareTo(originalBrukersAndel) < 0;

        return refusjonHarØkt && andelTilBrukerErMindre;

    }

    private static Optional<RefusjonAndel> finnOriginalAndel(RefusjonAndel andel, List<RefusjonAndel> originaleAndeler) {
        List<RefusjonAndel> andeler = originaleAndeler.stream().filter(a -> a.matcher(andel)).collect(Collectors.toList());
        if (andeler.size() > 1) {
            throw new IllegalStateException("Fant flere mulige matcher for andel " + andel + " i listen " + originaleAndeler);
        }
        if (andeler.size() == 1) {
            return Optional.of(andeler.get(0));
        }
        return Optional.empty();
    }

    private static boolean erMindreAndelTilgjengeligForBruker(RefusjonPeriodeEndring refusjonsendring) {
        BigDecimal originalAndelTilBruker = refusjonsendring.getOriginalBrutto().subtract(refusjonsendring.getOriginalRefusjon()).max(BigDecimal.ZERO);
        BigDecimal revurderingAndelTilBruker = refusjonsendring.getRevurderingBrutto().subtract(refusjonsendring.getRevurderingRefusjon()).max(BigDecimal.ZERO);
        return revurderingAndelTilBruker.compareTo(originalAndelTilBruker) < 0;


    }

}
