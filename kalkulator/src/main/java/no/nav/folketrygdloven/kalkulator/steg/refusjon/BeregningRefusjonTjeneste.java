package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
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
