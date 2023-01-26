package no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt;

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
import no.nav.fpsak.tidsserie.LocalDateInterval;
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
     * Utleder tidslinje over tilkommet inntektsforhold
     * <p>
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
     * @param skjæringstidspunkt      Skjæringstidspunkt for beregning
     * @param yrkesaktiviteter        Yrkesaktiviteter
     * @param andelerFraStart         Andeler i første periode
     * @param utbetalingsgradGrunnlag Ytelsesspesifikt grunnlag
     * @return Tidslinje for tilkommet aktivitet/inntektsforhold
     */
    public static LocalDateTimeline<Set<StatusOgArbeidsgiver>> finnTilkommetInntektsforholdTidslinje(LocalDate skjæringstidspunkt,
                                                                                                               Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                                               Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerFraStart,
                                                                                                               YtelsespesifiktGrunnlag utbetalingsgradGrunnlag) {
        var antallGodkjenteInntektsforhold = andelerFraStart.stream().filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START)).count();
        var yrkesaktivitetTidslinje = finnInntektsforholdFraYrkesaktiviteter(skjæringstidspunkt, yrkesaktiviteter);
        var næringTidslinje = finnInntektsforholdForStatusFraFravær((UtbetalingsgradGrunnlag) utbetalingsgradGrunnlag, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        var frilansTidslinje = finnInntektsforholdForStatusFraFravær((UtbetalingsgradGrunnlag) utbetalingsgradGrunnlag, AktivitetStatus.FRILANSER);
        var aktivitetTidslinje = yrkesaktivitetTidslinje.union(næringTidslinje, StandardCombinators::union)
                .union(frilansTidslinje, StandardCombinators::union).compress();
        var utbetalingTidslinje = finnUtbetalingTidslinje((UtbetalingsgradGrunnlag) utbetalingsgradGrunnlag);
        return aktivitetTidslinje
                .intersection(utbetalingTidslinje, StandardCombinators::leftOnly)
                .map(s -> mapTilkommetTidslinje(skjæringstidspunkt, andelerFraStart, yrkesaktiviteter, utbetalingsgradGrunnlag, antallGodkjenteInntektsforhold, s));

    }

    private static LocalDateTimeline<Boolean> finnUtbetalingTidslinje(UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        var utbetalingSegmenter = utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .flatMap(a -> a.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                .map(p -> new LocalDateSegment<>(p.getPeriode().getFomDato(), p.getPeriode().getTomDato(), Boolean.TRUE))
                .toList();
        return new LocalDateTimeline<>(utbetalingSegmenter, StandardCombinators::alwaysTrueForMatch);
    }

    private static List<LocalDateSegment<Set<StatusOgArbeidsgiver>>> mapTilkommetTidslinje(LocalDate skjæringstidspunkt,
                                                                                           Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                                           Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                           YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                                                           long antallGodkjenteInntektsforhold, LocalDateSegment<Set<Inntektsforhold>> s) {
        var periode = Intervall.fraOgMedTilOgMed(s.getFom(), s.getTom());
        return List.of(new LocalDateSegment<>(s.getFom(), s.getTom(),
                mapTilkomne(yrkesaktiviteter, andeler, utbetalingsgradGrunnlag, antallGodkjenteInntektsforhold, periode, s.getValue(), skjæringstidspunkt)));
    }

    private static Set<StatusOgArbeidsgiver> mapTilkomne(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                         Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler, YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                         long antallGodkjenteInntektsforhold,
                                                         Intervall periode,
                                                         Set<Inntektsforhold> inntektsforholdListe,
                                                         LocalDate skjæringstidspunkt) {
        var tilkommetInntektsforhold = new LinkedHashSet<StatusOgArbeidsgiver>();
        // Legger til inntektsforhold som ikke er AT til eksisterende inntektsforhold siden vi ikke kan definere en sluttdato for disse, de anses dermed som aktive i hele perioden
        var eksisterendeInntektsforhold = new LinkedHashSet<>(finnIkkeArbeidstakerStatuserFraStart(andeler));
        var sortertListe = inntektsforholdListe.stream()
                .sorted(TilkommetInntektsforholdTjeneste.sorterPåStartdato(andeler, yrkesaktiviteter, skjæringstidspunkt)).collect(Collectors.toCollection(LinkedHashSet::new));
        for (var inntektsforhold : sortertListe) {

            var utbetalingsgrad = finnUtbetalingsgrad(periode, utbetalingsgradGrunnlag, inntektsforhold);
            var statusOgArbeidsgiver = new StatusOgArbeidsgiver(inntektsforhold.aktivitetStatus(), inntektsforhold.arbeidsgiver());
            if (statusOgArbeidsgiver.arbeidsgiver() != null) {
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


    private static LocalDateTimeline<Set<Inntektsforhold>> finnInntektsforholdFraYrkesaktiviteter(LocalDate skjæringstidspunkt, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        var yrkesaktivitetSegmenter = yrkesaktiviteter
                .stream()
                .filter(ya -> !mapTilAktivitetStatus(ya).equals(AktivitetStatus.UDEFINERT))
                .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream()
                        .filter(p -> p.getPeriode().getTomDato().isAfter(skjæringstidspunkt.minusDays(1)))
                        .map(p -> new LocalDateSegment<>(
                                p.getPeriode().getFomDato(),
                                p.getPeriode().getTomDato(),
                                Set.of(mapTilInntektsforhold(ya))))
                )
                .collect(Collectors.toList());

        return new LocalDateTimeline<>(yrkesaktivitetSegmenter, StandardCombinators::union);
    }

    private static Inntektsforhold mapTilInntektsforhold(YrkesaktivitetDto ya) {
        var aktivitetStatus = mapTilAktivitetStatus(ya);
        if (aktivitetStatus.equals(AktivitetStatus.ARBEIDSTAKER)) {
            return new Inntektsforhold(aktivitetStatus, ya.getArbeidsgiver(), ya.getArbeidsforholdRef());
        } else {
            return new Inntektsforhold(aktivitetStatus, null, null);
        }
    }

    private static LocalDateTimeline<Set<Inntektsforhold>> finnInntektsforholdForStatusFraFravær(UtbetalingsgradGrunnlag utbetalinger, AktivitetStatus status) {
        var perioderMedStatus = finnPerioderMedStatus(utbetalinger, mapTilUttakArbeidType(status));
        var segmenter = perioderMedStatus.stream()
                .filter(p -> p.getUtbetalingsgrad().compareTo(BigDecimal.valueOf(100)) < 0)
                .map(p -> new LocalDateSegment<>(p.getPeriode().getFomDato(), p.getPeriode().getTomDato(), Set.of(new Inntektsforhold(status, null, null))))
                .toList();
        return new LocalDateTimeline<>(segmenter, StandardCombinators::union);
    }

    private static UttakArbeidType mapTilUttakArbeidType(AktivitetStatus status) {
        return switch (status) {
            case SELVSTENDIG_NÆRINGSDRIVENDE -> UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE;
            case FRILANSER -> UttakArbeidType.FRILANS;
            default -> throw new IllegalStateException("Støtter ikke tilkommet inntektsforhold fra fravær for status " + status);
        };
    }

    private static List<PeriodeMedUtbetalingsgradDto> finnPerioderMedStatus(UtbetalingsgradGrunnlag utbetalinger, UttakArbeidType uttakArbeidType) {
        return utbetalinger.getUtbetalingsgradPrAktivitet().stream()
                .filter(a -> a.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(uttakArbeidType))
                .flatMap(a -> a.getPeriodeMedUtbetalingsgrad().stream())
                .toList();
    }


    private static Set<StatusOgArbeidsgiver> finnIkkeArbeidstakerStatuserFraStart(Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        return andeler.stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .filter(a -> !a.getAktivitetStatus().erArbeidstaker())
                .map(a -> new StatusOgArbeidsgiver(a.getAktivitetStatus(), null))
                .collect(Collectors.toSet());
    }

    private static BigDecimal finnUtbetalingsgrad(Intervall periode,
                                                  YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                  Inntektsforhold inntektsforhold) {
        if (inntektsforhold.aktivitetStatus().erArbeidstaker()) {
            return UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(
                    inntektsforhold.arbeidsgiver(),
                    inntektsforhold.arbeidsforholdRef(),
                    periode, utbetalingsgradGrunnlag, true);
        } else {
            return UtbetalingsgradTjeneste.finnUtbetalingsgradForStatus(
                    inntektsforhold.aktivitetStatus(),
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

    private static boolean harDekketOppEksisterendeInntektsforhold(long antallAktiviteterSomIkkeSkalRegnesSomTilkommet, HashSet<StatusOgArbeidsgiver> aktiviteterSomIkkeSkalRegnesSomTilkommet) {
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
        if (aktivitet.arbeidsgiver() == null) {
            return Collections.emptyList();
        }
        return yrkesaktiviteter.stream()
                .filter(it -> it.getArbeidsgiver().equals(aktivitet.arbeidsgiver()))
                .flatMap(it -> it.getAlleAnsettelsesperioder().stream())
                .toList();
    }

    private static Comparator<Inntektsforhold> sorterPåStartdato(Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler, Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                 LocalDate stp) {
        return (i1, i2) -> {
            if (harAndelFraStart(i1, andeler)) {
                return harAndelFraStart(i2, andeler) ? 0 : -1;
            }
            if (harAndelFraStart(i2, andeler)) {
                return 1;
            }
            var ansattperioder1 = finnAnsattperioder(yrkesaktiviteter, i1);
            var ansattperioder2 = finnAnsattperioder(yrkesaktiviteter, i2);
            if (ansattperioder1.isEmpty()) {
                return ansattperioder2.isEmpty() ? 0 : 1;
            }
            if (ansattperioder2.isEmpty()) {
                return -1;
            }
            var førsteAnsattdato1 = finnFørsteAnsattdatoEtterStp(stp, ansattperioder1);
            var førsteAnsattdato2 = finnFørsteAnsattdatoEtterStp(stp, ansattperioder2);
            return førsteAnsattdato1.compareTo(førsteAnsattdato2);
        };
    }

    private static List<AktivitetsAvtaleDto> finnAnsattperioder(Collection<YrkesaktivitetDto> yrkesaktiviteter, Inntektsforhold a1) {
        return yrkesaktiviteter.stream().filter(ya -> ya.getArbeidsgiver().equals(a1.arbeidsgiver()) && ya.getArbeidsforholdRef().gjelderFor(a1.arbeidsforholdRef()))
                .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
                .toList();
    }

    private static boolean harAndelFraStart(Inntektsforhold inntektsforhold, Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        var matchendeAndeler1 = andeler.stream().filter(a -> Objects.equals(a.getArbeidsgiver().orElse(null), inntektsforhold.arbeidsgiver())
                        && a.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()).gjelderFor(inntektsforhold.arbeidsforholdRef()))
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Inntektsforhold that = (Inntektsforhold) o;
            return aktivitetStatus == that.aktivitetStatus && Objects.equals(arbeidsgiver, that.arbeidsgiver) && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(aktivitetStatus, arbeidsgiver, arbeidsforholdRef);
        }

        @Override
        public InternArbeidsforholdRefDto arbeidsforholdRef() {
            return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
        }
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
