package no.nav.folketrygdloven.kalkulator.felles.frist;

import static no.nav.folketrygdloven.kalkulator.felles.frist.StartRefusjonTjeneste.finnFørsteGyldigeDatoMedRefusjon;
import static no.nav.folketrygdloven.kalkulator.felles.frist.StartRefusjonTjeneste.finnFørsteMuligeDagRefusjon;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.konfig.Konfigverdier;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class HarYrkesaktivitetInnsendtRefusjonForSent {


    /** Finner tidslinje for refusjonskrav og vurdering av om det er mottatt innen fristen
     *
     * @param kravperioder Perioder for refusjonskrav
     * @param yrkesaktivitet Yrkesaktivitet
     * @param gjeldendeAktiviteter  Alle aktiviteter som er benyttet i beregning
     * @param skjæringstidspunktBeregning   Skjæringstidspunkte
     * @param overstyrtGodkjentRefusjonFom  Overstyrt fom-dato for når refusjonskravet er ansett som godkjent
     * @return Tidslinje for mottatt krav og utfall av fristvurdering
     */
    public static LocalDateTimeline<KravOgUtfall> lagTidslinjeForYrkesaktivitet(List<PerioderForKravDto> kravperioder,
                                                                                YrkesaktivitetDto yrkesaktivitet,
                                                                                BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                                                LocalDate skjæringstidspunktBeregning,
                                                                                Optional<LocalDate> overstyrtGodkjentRefusjonFom) {
        return kravperioder.stream()
                .map(krav -> finnFristvurdertTidslinje(krav, yrkesaktivitet, gjeldendeAktiviteter, skjæringstidspunktBeregning, overstyrtGodkjentRefusjonFom))
                .reduce(KombinerRefusjonskravFristTidslinje::kombinerOgKompress)
                .orElse(new LocalDateTimeline<>(Collections.emptyList()));
    }

    private static LocalDateTimeline<KravOgUtfall> finnFristvurdertTidslinje(PerioderForKravDto krav,
                                                                             YrkesaktivitetDto yrkesaktivitet,
                                                                             BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                                             LocalDate skjæringstidspunktBeregning,
                                                                             Optional<LocalDate> overstyrtGodkjentRefusjonFom) {
        var kravTidslinje = finnKravTidslinje(krav, yrkesaktivitet, gjeldendeAktiviteter, skjæringstidspunktBeregning);
        var godkjentTidslinje = finnFristTidslinje(krav, overstyrtGodkjentRefusjonFom);
        return kravTidslinje.combine(godkjentTidslinje, (intervall, lhs, rhs) -> {
            if (rhs == null) {
                return new LocalDateSegment<>(intervall, new KravOgUtfall(lhs.getValue(), Utfall.UNDERKJENT));
            }
            return new LocalDateSegment<>(intervall, new KravOgUtfall(lhs.getValue(), rhs.getValue()));
        }, LocalDateTimeline.JoinStyle.LEFT_JOIN);
    }

    private static LocalDateTimeline<BigDecimal> finnKravTidslinje(PerioderForKravDto krav,
                                                                   YrkesaktivitetDto yrkesaktivitet,
                                                                   BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                                   LocalDate skjæringstidspunktBeregning) {
        LocalDate førsteMuligeRefusjonsdato = finnFørsteMuligeDagRefusjon(gjeldendeAktiviteter, skjæringstidspunktBeregning, yrkesaktivitet);
        List<LocalDateSegment<BigDecimal>> kravSegmenter = krav.getPerioder().stream()
                .filter(p -> !p.periode().getTomDato().isBefore(førsteMuligeRefusjonsdato))
                .map(p -> new LocalDateSegment<>(
                        p.periode().inkluderer(førsteMuligeRefusjonsdato) ? førsteMuligeRefusjonsdato : p.periode().getFomDato(),
                        p.periode().getTomDato(),
                        p.beløp()))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(kravSegmenter);
    }

    private static LocalDateTimeline<Utfall> finnFristTidslinje(PerioderForKravDto krav, Optional<LocalDate> overstyrtRefusjonFom) {
        return new LocalDateTimeline<>(List.of(new LocalDateSegment<>(
                overstyrtRefusjonFom.orElse(finnFørsteGyldigeDatoMedRefusjon(krav.getInnsendingsdato())),
                TIDENES_ENDE, Utfall.GODKJENT)));
    }



}
