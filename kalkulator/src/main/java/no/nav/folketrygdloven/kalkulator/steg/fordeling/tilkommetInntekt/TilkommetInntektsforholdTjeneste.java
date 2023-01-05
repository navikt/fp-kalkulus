package no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
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
        return finnTilkomneAktiviteter(skjæringstidspunkt, yrkesaktiviteter, andeler, periode, utbetalingsgradGrunnlag, eksisterendeInntektsforhold);
    }

    private static Set<StatusOgArbeidsgiver> finnTilkomneAktiviteter(LocalDate skjæringstidspunkt,
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

        var inntektsforholdListe = new ArrayList<>(finnInntektsforholdFraYrkesaktiviteter(skjæringstidspunkt, yrkesaktiviteter, andeler));
        finnOpplystNæringsforhold(periode, utbetalinger).ifPresent(inntektsforholdListe::add);

        var tilkommetInntektsforhold = new LinkedHashSet<StatusOgArbeidsgiver>();

        for (var inntektsforhold : inntektsforholdListe) {
            var utbetalingsgrad = finnUtbetalingsgrad(periode, utbetalingsgradGrunnlag, inntektsforhold);
            var statusOgArbeidsgiver = new StatusOgArbeidsgiver(inntektsforhold.aktivitetStatus, inntektsforhold.arbeidsgiver);
            if (statusOgArbeidsgiver.arbeidsgiver != null) {
                if (erAnsattIPeriode(yrkesaktiviteter, statusOgArbeidsgiver, periode)) {
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

    private static List<Inntektsforhold> finnInntektsforholdFraYrkesaktiviteter(LocalDate skjæringstidspunkt, Collection<YrkesaktivitetDto> yrkesaktiviteter, Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        return yrkesaktiviteter
                .stream()
                .filter(ya -> !mapTilAktivitetStatus(ya).equals(AktivitetStatus.UDEFINERT))
                .sorted(ikkeTilkomneFørst(andeler, skjæringstidspunkt))
                .map(ya -> new Inntektsforhold(mapTilAktivitetStatus(ya), ya.getArbeidsgiver(), ya.getArbeidsforholdRef()))
                .collect(Collectors.toList());
    }

    private static Optional<Inntektsforhold> finnOpplystNæringsforhold(Intervall periode, UtbetalingsgradGrunnlag utbetalinger) {
        var perioderMedNæring = finnPerioderMedNæring(periode, utbetalinger);
        if (perioderMedNæring.size() > 0) {
            return Optional.of(new Inntektsforhold(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, null));
        }
        return Optional.empty();
    }

    private static List<PeriodeMedUtbetalingsgradDto> finnPerioderMedNæring(Intervall periode, UtbetalingsgradGrunnlag utbetalinger) {
        return utbetalinger.getUtbetalingsgradPrAktivitet().stream()
                .filter(a -> a.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE))
                .flatMap(a -> a.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getPeriode().overlapper(periode))
                .toList();
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
                                                                                                     YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {

        if (!(ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag)) {
            return LocalDateTimeline.empty();
        }

        var aktiviteterVedStart = andeler.stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .map(a -> new StatusOgArbeidsgiver(a.getAktivitetStatus(), a.getArbeidsgiver().orElse(null)))
                .collect(Collectors.toSet());
        var eksisterendeInntektsforhold = new LinkedHashSet<>(aktiviteterVedStart);

        var fraStartTidslinje = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(skjæringstidspunkt, TIDENES_ENDE, Boolean.TRUE)));

        var aktivitetTidslinje = finnAktivitetTidslinje(yrkesaktiviteter, (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag, fraStartTidslinje);

        return aktivitetTidslinje
                .map(mapNyeInntektsforhold(skjæringstidspunkt, yrkesaktiviteter, andeler, ytelsespesifiktGrunnlag, eksisterendeInntektsforhold))
                .compress();
    }

    private static LocalDateTimeline<Boolean> finnAktivitetTidslinje(Collection<YrkesaktivitetDto> yrkesaktiviteter, UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag, LocalDateTimeline<Boolean> fraStartTidslinje) {
        var yrkesaktiviteterTidslinje = lagYrkesaktivitetTidslinje(yrkesaktiviteter);
        var næringstidslinje = lagNæringstidslinje(ytelsespesifiktGrunnlag);
        var aktivitetTidslinje = fraStartTidslinje.crossJoin(yrkesaktiviteterTidslinje, StandardCombinators::alwaysTrueForMatch)
                .crossJoin(næringstidslinje, StandardCombinators::alwaysTrueForMatch);
        return aktivitetTidslinje;
    }

    private static LocalDateTimeline<Boolean> lagYrkesaktivitetTidslinje(Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        var yrkesaktivitetSegmenter = lagSegmenterFraYrkesaktiviteter(yrkesaktiviteter);
        return new LocalDateTimeline<>(yrkesaktivitetSegmenter, StandardCombinators::alwaysTrueForMatch);
    }

    private static LocalDateTimeline<Boolean> lagNæringstidslinje(UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag) {
        var næringssegmenter = ytelsespesifiktGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .filter(a -> a.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE))
                .flatMap(a -> a.getPeriodeMedUtbetalingsgrad().stream())
                .map(p -> new LocalDateSegment<>(p.getPeriode().getFomDato(), p.getPeriode().getTomDato(), Boolean.TRUE))
                .toList();
        return new LocalDateTimeline<>(næringssegmenter, StandardCombinators::alwaysTrueForMatch);
    }

    private static List<LocalDateSegment<Boolean>> lagSegmenterFraYrkesaktiviteter(Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return yrkesaktiviteter.stream().flatMap(ya -> ya.getAlleAnsettelsesperioder().stream().map(
                ap -> new LocalDateSegment<>(ap.getPeriode().getFomDato(), ap.getPeriode().getTomDato(), Boolean.TRUE)
        )).collect(Collectors.toList());
    }

    private static Function<LocalDateSegment<Boolean>, List<LocalDateSegment<Set<StatusOgArbeidsgiver>>>> mapNyeInntektsforhold(LocalDate skjæringstidspunkt,
                                                                                                                                Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                                                                Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                                                                                YtelsespesifiktGrunnlag utbetalingsgradGrunnlag, LinkedHashSet<StatusOgArbeidsgiver> eksisterendeInntektsforhold) {
        return segment -> {
            var tilkomneStatuser = finnTilkomneAktiviteter(skjæringstidspunkt,
                    yrkesaktiviteter, andeler,
                    Intervall.fraOgMedTilOgMed(segment.getFom(), segment.getTom()),
                    utbetalingsgradGrunnlag, eksisterendeInntektsforhold);

            return List.of(new LocalDateSegment<>(segment.getFom(), segment.getTom(), tilkomneStatuser));
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
                                                  Inntektsforhold inntektsforhold) {
        if (inntektsforhold.aktivitetStatus.erArbeidstaker()) {
            return UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(
                    inntektsforhold.arbeidsgiver,
                    inntektsforhold.arbeidsforholdRef,
                    periode, utbetalingsgradGrunnlag, true);
        } else {
            return UtbetalingsgradTjeneste.finnUtbetalingsgradForStatus(
                    inntektsforhold.aktivitetStatus,
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

    private static boolean erAnsattIPeriode(Collection<YrkesaktivitetDto> yrkesaktiviteter, StatusOgArbeidsgiver aktivitet, Intervall periode) {
        var ansettelsesPerioderHosArbeidsgiver = finnAnsattperiodeHosSammeArbeidsgiver(yrkesaktiviteter, aktivitet);
        return ansettelsesPerioderHosArbeidsgiver.stream().anyMatch(ap -> ap.getPeriode().overlapper(periode));
    }

    private static List<AktivitetsAvtaleDto> finnAnsattperiodeHosSammeArbeidsgiver(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                   StatusOgArbeidsgiver aktivitet) {
        if (aktivitet.arbeidsgiver == null) {
            return Collections.emptyList();
        }
        return yrkesaktiviteter.stream()
                .filter(it -> it.getArbeidsgiver().equals(aktivitet.arbeidsgiver))
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

    private record Inntektsforhold(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver,
                                   InternArbeidsforholdRefDto arbeidsforholdRef) {
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
