package no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

/**
 * Ved nye inntektsforhold skal beregningsgrunnlaget graderes mot inntekt.
 * <p>
 * Utleder her om det er potensielle nye inntektsforhold.
 * <p>
 * Se https://navno.sharepoint.com/sites/fag-og-ytelser-arbeid-sykefravarsoppfolging-og-sykepenger/SitePages/%C2%A7-8-13-Graderte-sykepenger.aspx
 */
public class TilkommetInntektsforholdTjeneste {


    /**
     * Bestemmer hvilke statuser/arbeidsgivere som skal regnes som nytt
     * <p>
     * Dersom en inntekt/aktivitet regnes som nytt skal beregningsgrunnlaget graderes mot inntekt i denne perioden. Dette betyr at inntekt i tillegg til ytelsen kan føre til nedjustering av utbetalt ytelse.
     * <p>
     * Et inntektsforhold regnes som nytt dersom:
     * - Den fører til at bruker har en ekstra inntekt i tillegg til det hen ville ha hatt om hen ikke mottok ytelse
     * - Inntekten ikke erstatter inntekt i et arbeidsforhold som er avsluttet
     * - Det ikke er fullt fravær i arbeidsforholdet/aktiviteten (har opprettholdt noe arbeid og dermed sannsynligvis inntekt)
     * <p>
     * Vi antar bruker ville opprettholdt arbeid hos arbeidsgivere der bruker fortsatt er innregistrert i aareg, og at dette regner som en løpende aktivitet.
     * Dersom antall løpende aktiviteter øker, skal saksbehandler vurdere om de tilkomne aktivitetene skal føre til reduksjon i utbetaling.
     *
     * @param skjæringstidspunkt      skjæringstidspunkt
     * @param yrkesaktiviteter        yrkesaktiviteter
     * @param andeler                 Andeler
     * @param periode                 Beregningsgrunnlagperiode
     * @param utbetalingsgradGrunnlag Utbetalingsgradgrunnlag
     * @return Statuser/arbeidsgivere som skal regnes som tilkommet
     */
    public static Set<StatusOgArbeidsgiver> finnTilkomneInntektsforhold(LocalDate skjæringstidspunkt,
                                                                        Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                        Intervall periode,
                                                                        YtelsespesifiktGrunnlag utbetalingsgradGrunnlag) {


        var eksisterendeInntektsforhold = new LinkedHashSet<StatusOgArbeidsgiver>();
        leggTilIkkeArbeidstakerFraStart(andeler, eksisterendeInntektsforhold);
        return finnTilkomneAktiviteterFraYrkesaktiviteter(skjæringstidspunkt, yrkesaktiviteter, andeler, periode, utbetalingsgradGrunnlag, eksisterendeInntektsforhold);
    }

    private static Set<StatusOgArbeidsgiver> finnTilkomneAktiviteterFraYrkesaktiviteter(LocalDate skjæringstidspunkt,
                                                                                        Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                                        Intervall periode,
                                                                                        YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                                                        LinkedHashSet<StatusOgArbeidsgiver> eksisterendeInntektsforhold) {

        var utbetalinger = (UtbetalingsgradGrunnlag) utbetalingsgradGrunnlag;
        var harUtbetalingIPeriode = utbetalinger.getUtbetalingsgradPrAktivitet().stream()
                .flatMap(a -> a.getPeriodeMedUtbetalingsgrad().stream())
                .anyMatch(p -> p.getPeriode().overlapper(periode) && p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0);
        if (!harUtbetalingIPeriode) {
            return Collections.emptySet();
        }
        var aktiviteterVedStart = andeler.stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .map(a -> new StatusOgArbeidsgiver(a.getAktivitetStatus(), a.getArbeidsgiver().orElse(null)))
                .collect(Collectors.toSet());

        var antallGodkjenteInntektsforhold = aktiviteterVedStart.size();

        var sortertAktivitetListe = yrkesaktiviteter
                .stream()
                .filter(ya -> !mapTilAktivitetStatus(ya).equals(AktivitetStatus.UDEFINERT))
                .sorted(ikkeTilkomneFørst(andeler, skjæringstidspunkt)).collect(Collectors.toList());

        var tilkommetInntektsforhold = new LinkedHashSet<StatusOgArbeidsgiver>();

        for (var yrkesaktivitet : sortertAktivitetListe) {
            AktivitetStatus aktivitetStatus = mapTilAktivitetStatus(yrkesaktivitet);
            var utbetalingsgrad = finnUtbetalingsgrad(periode, utbetalingsgradGrunnlag, yrkesaktivitet);
            var statusOgArbeidsgiver = new StatusOgArbeidsgiver(aktivitetStatus, yrkesaktivitet.getArbeidsgiver());
            if (yrkesaktivitet.getArbeidsgiver() != null) {
                if (erAnsattIPeriode(yrkesaktiviteter, yrkesaktivitet, periode)) {
                    // Dersom vi har dekket opp "godkjente" inntektsforhold legges den til i lista av tilkomne
                    if (!harDekketOppEksisterendeInntektsforhold(antallGodkjenteInntektsforhold, eksisterendeInntektsforhold)) {
                        eksisterendeInntektsforhold.add(statusOgArbeidsgiver);
                    } else if (!eksisterendeInntektsforhold.contains(statusOgArbeidsgiver) && harIkkeFulltFravær(utbetalingsgrad)) {
                        tilkommetInntektsforhold.add(statusOgArbeidsgiver);
                    }
                }
            } else {
                if (!harDekketOppEksisterendeInntektsforhold(antallGodkjenteInntektsforhold, eksisterendeInntektsforhold)) {
                    eksisterendeInntektsforhold.add(statusOgArbeidsgiver);
                } else if (!eksisterendeInntektsforhold.contains(statusOgArbeidsgiver) && harIkkeFulltFravær(utbetalingsgrad)) {
                    tilkommetInntektsforhold.add(statusOgArbeidsgiver);
                }
            }
        }
        return tilkommetInntektsforhold;
    }

    /**
     * Utleder tilkommet inntekt tidslinje
     *
     * @param skjæringstidspunkt Skjæringstidspunkt
     * @param yrkesaktiviteter   Yrkesaktiviteter
     * @param andeler            Andeler fra start/første periode
     * @return Tidslinje som sier når det er tilkommet inntekt
     */
    public static LocalDateTimeline<Set<StatusOgArbeidsgiver>> finnTilkommetInntektsforholdTidslinje(LocalDate skjæringstidspunkt,
                                                                                                     Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                                     Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                                                     YtelsespesifiktGrunnlag utbetalingsgradGrunnlag) {

        var aktiviteterVedStart = andeler.stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .map(a -> new StatusOgArbeidsgiver(a.getAktivitetStatus(), a.getArbeidsgiver().orElse(null)))
                .collect(Collectors.toSet());
        var eksisterendeInntektsforhold = new LinkedHashSet<StatusOgArbeidsgiver>(aktiviteterVedStart);

        var fraStartTidslinje = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(skjæringstidspunkt, TIDENES_ENDE, aktiviteterVedStart)));

        var segmenter = yrkesaktiviteter.stream().flatMap(ya -> ya.getAlleAnsettelsesperioder().stream().map(
                ap -> new LocalDateSegment<>(ap.getPeriode().getFomDato(), ap.getPeriode().getTomDato(), Set.of(
                        new StatusOgArbeidsgiver(mapTilAktivitetStatus(ya), ya.getArbeidsgiver())
                ))
        )).collect(Collectors.toList());

        var yrkesaktiviteterTidslinje = new LocalDateTimeline<>(segmenter, StandardCombinators::union);
        var statusOgArbeidsgiverTimeline = fraStartTidslinje.crossJoin(yrkesaktiviteterTidslinje, StandardCombinators::union);
        var tilkomneAktiviteterTidslinje = statusOgArbeidsgiverTimeline
                .map(mapNyeInntektsforhold(skjæringstidspunkt, yrkesaktiviteter, andeler, utbetalingsgradGrunnlag, eksisterendeInntektsforhold));

        return tilkomneAktiviteterTidslinje;
    }

    private static Function<LocalDateSegment<Set<StatusOgArbeidsgiver>>, List<LocalDateSegment<Set<StatusOgArbeidsgiver>>>> mapNyeInntektsforhold(LocalDate skjæringstidspunkt, Collection<YrkesaktivitetDto> yrkesaktiviteter, Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler, YtelsespesifiktGrunnlag utbetalingsgradGrunnlag, LinkedHashSet<StatusOgArbeidsgiver> eksisterendeInntektsforhold) {
        return segment -> {
            var tilkomneStatuser = finnTilkomneAktiviteterFraYrkesaktiviteter(skjæringstidspunkt,
                    yrkesaktiviteter, andeler,
                    Intervall.fraOgMedTilOgMed(segment.getFom(), segment.getTom()),
                    utbetalingsgradGrunnlag, eksisterendeInntektsforhold);

            return List.of(new LocalDateSegment<>(segment.getFom(), segment.getTom(),
                    segment.getValue().stream().filter(tilkomneStatuser::contains).collect(Collectors.toSet())));
        };
    }


    private static void leggTilIkkeArbeidstakerFraStart(Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler, LinkedHashSet<StatusOgArbeidsgiver> eksisterendeInntektsforhold) {
        andeler.stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .filter(a -> !a.getAktivitetStatus().erArbeidstaker())
                .forEach(a -> eksisterendeInntektsforhold.add(new StatusOgArbeidsgiver(a.getAktivitetStatus(), null)));
    }

    private static BigDecimal finnUtbetalingsgrad(Intervall periode,
                                                  YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                  YrkesaktivitetDto yrkesaktivitet) {
        if (mapTilAktivitetStatus(yrkesaktivitet).erArbeidstaker()) {
            return UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(
                    yrkesaktivitet.getArbeidsgiver(),
                    yrkesaktivitet.getArbeidsforholdRef(),
                    periode, utbetalingsgradGrunnlag, true);
        } else {
            return UtbetalingsgradTjeneste.finnUtbetalingsgradForStatus(
                    mapTilAktivitetStatus(yrkesaktivitet),
                    periode,
                    utbetalingsgradGrunnlag);
        }
    }

    private static AktivitetStatus mapTilAktivitetStatus(YrkesaktivitetDto yrkesaktivitet) {
        return switch (yrkesaktivitet.getArbeidType()) {
            case FORENKLET_OPPGJØRSORDNING, MARITIMT_ARBEIDSFORHOLD, ORDINÆRT_ARBEIDSFORHOLD -> AktivitetStatus.ARBEIDSTAKER;
            case FRILANSER_OPPDRAGSTAKER_MED_MER -> AktivitetStatus.FRILANSER;
            case SELVSTENDIG_NÆRINGSDRIVENDE -> AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE;
            default -> AktivitetStatus.UDEFINERT;
        };
    }

    private static boolean harDekketOppEksisterendeInntektsforhold(int antallAktiviteterSomIkkeSkalRegnesSomTilkommet, HashSet<StatusOgArbeidsgiver> aktiviteterSomIkkeSkalRegnesSomTilkommet) {
        return antallAktiviteterSomIkkeSkalRegnesSomTilkommet == aktiviteterSomIkkeSkalRegnesSomTilkommet.size();
    }

    private static boolean harIkkeFulltFravær(BigDecimal utbetalingsgrad) {
        return utbetalingsgrad.compareTo(BigDecimal.valueOf(100)) < 0;
    }

    private static boolean erAnsattIPeriode(Collection<YrkesaktivitetDto> yrkesaktiviteter, YrkesaktivitetDto ya, Intervall periode) {
        var ansettelsesPerioderHosArbeidsgiver = finnAnsattperiodeHosSammeArbeidsgiver(yrkesaktiviteter, ya);
        return ansettelsesPerioderHosArbeidsgiver.stream().anyMatch(ap -> ap.getPeriode().overlapper(periode));
    }

    private static List<AktivitetsAvtaleDto> finnAnsattperiodeHosSammeArbeidsgiver(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                   YrkesaktivitetDto ya) {
        if (ya.getArbeidsgiver() == null) {
            return Collections.emptyList();
        }
        return yrkesaktiviteter.stream()
                .filter(it -> it.getArbeidsgiver().equals(ya.getArbeidsgiver()))
                .flatMap(it -> it.getAlleAnsettelsesperioder().stream())
                .toList();
    }

    private static Comparator<YrkesaktivitetDto> ikkeTilkomneFørst(Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                   LocalDate stp) {
        return (a1, a2) -> {
            if (harAndelFraStart(a1, andeler)) {
                return harAndelFraStart(a2, andeler) ? 0 : -1;
            }
            if (harAndelFraStart(a2, andeler)) {
                return 1;
            }
            var ansattperioder1 = a1.getAlleAnsettelsesperioder();
            var ansattperioder2 = a2.getAlleAnsettelsesperioder();
            var førsteAnsattdato1 = finnFørsteAnsattdatoEtterStp(stp, ansattperioder1);
            var førsteAnsattdato2 = finnFørsteAnsattdatoEtterStp(stp, ansattperioder2);
            return førsteAnsattdato1.compareTo(førsteAnsattdato2);
        };
    }

    private static boolean harAndelFraStart(YrkesaktivitetDto ya, Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        var matchendeAndeler1 = andeler.stream().filter(a -> Objects.equals(a.getArbeidsgiver().orElse(null), ya.getArbeidsgiver())
                        && a.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()).gjelderFor(ya.getArbeidsforholdRef()))
                .toList();
        return matchendeAndeler1.stream().map(BeregningsgrunnlagPrStatusOgAndelDto::getKilde)
                .anyMatch(AndelKilde.PROSESS_START::equals);
    }

    private static LocalDate finnFørsteAnsattdatoEtterStp(LocalDate stp, Collection<AktivitetsAvtaleDto> ansattperioder1) {
        return ansattperioder1.stream().map(AktivitetsAvtaleDto::getPeriode)
                .map(Intervall::getFomDato)
                .filter(fomDato -> fomDato.isAfter(stp))
                .min(Comparator.naturalOrder())
                .orElse(stp);
    }


    public record StatusOgArbeidsgiver(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StatusOgArbeidsgiver that = (StatusOgArbeidsgiver) o;
            return aktivitetStatus == that.aktivitetStatus && Objects.equals(arbeidsgiver, that.arbeidsgiver);
        }

        @Override
        public int hashCode() {
            return Objects.hash(aktivitetStatus, arbeidsgiver);
        }
    }

}
