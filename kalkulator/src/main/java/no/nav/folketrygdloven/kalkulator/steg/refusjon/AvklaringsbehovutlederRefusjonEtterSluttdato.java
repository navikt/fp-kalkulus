package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriode;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

/**
 * På grunn av ulik rapporteringspraksis i aareg kan vi ikke bare se om søker har refusjon i et arbeidsforhold han/hun ikke lenger er aktiv i,
 * men vi må også se hvor langt unna denne datoen vi er på behandlingstidspunktet.
 * Eks: Vi beregner en sak 01.03.2021, og et av arbeidsforholdene det er innvilget refusjon i avsluttes 01.05.2021.
 * Det er innvilget refusjon etter denne datoen, som kan virke rart, men siden vi behandler søknaden så lenge før
 * denne sluttdatoen kan det hende den flyttes før vi kommer dit.
 * Ser derfor kun på perioder som ligger før behandlingstidspunktet.
 */
public final class AvklaringsbehovutlederRefusjonEtterSluttdato {
    private static final Logger log = LoggerFactory.getLogger(AvklaringsbehovutlederRefusjonEtterSluttdato.class);

    private AvklaringsbehovutlederRefusjonEtterSluttdato() {
        // Skjuler default
    }

    public static boolean harRefusjonEtterSisteDatoIArbeidsforhold(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                   UUID koblingUuid,
                                                                   Optional<LocalDate> sisteSøkteUttaksdag,
                                                                   Optional<LocalDate> behandlingstidspunkt,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering,
                                                                   YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        // Trenger ikke sjekke om søker ikke er i jobb eller om det ikke finnes uttak
        if (yrkesaktiviteter.isEmpty() || sisteSøkteUttaksdag.isEmpty() || behandlingstidspunkt.isEmpty()) {
            return false;
        }
        LocalDateTimeline<RefusjonPeriode> allePerioder = RefusjonTidslinjeTjeneste.lagTidslinje(periodisertMedRefusjonOgGradering, false, ytelsespesifiktGrunnlag);
        LocalDateTimeline<RefusjonPeriode> perioderMedPotensieltUttak = allePerioder.intersection(finnPeriodeFremTilSisteUttak(sisteSøkteUttaksdag.get()));

        List<RefusjonAndel> andelerSomMåSjekkes = perioderMedPotensieltUttak.toSegments()
                .stream()
                .filter(segment -> erTilbakeITidFraBehandlingstidspunktet(behandlingstidspunkt.get(), segment.getFom()))
                .flatMap(segment -> segment.getValue().getAndeler()
                        .stream()
                        .filter(AvklaringsbehovutlederRefusjonEtterSluttdato::erInnvilgetRefusjon)
                        .filter(andel -> !erAnsattPåDato(andel, segment.getLocalDateInterval(), yrkesaktiviteter)))
                .collect(Collectors.toList());

        andelerSomMåSjekkes.forEach(andel -> log.info("FT-718273: behandlingUUID {} : Arbeidsgiver {} ", koblingUuid, andel.getArbeidsgiver().toString()));
        return !andelerSomMåSjekkes.isEmpty();
    }

    private static boolean erTilbakeITidFraBehandlingstidspunktet(LocalDate behandlingstidspunkt, LocalDate segmentFom) {
        return segmentFom.isBefore(behandlingstidspunkt);
    }

    private static boolean erInnvilgetRefusjon(RefusjonAndel andel) {
        return andel.getRefusjon().compareTo(Beløp.ZERO) > 0;
    }

    private static LocalDateTimeline<RefusjonPeriode> finnPeriodeFremTilSisteUttak(LocalDate sisteUttaksdato) {
        return new LocalDateTimeline<>(
                TIDENES_BEGYNNELSE,
                sisteUttaksdato,
                null);
    }

    private static boolean erAnsattPåDato(RefusjonAndel andel, LocalDateInterval intervall, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        List<AktivitetsAvtaleDto> alleAnsettelsesperioder = yrkesaktiviteter.stream()
                .filter(ya -> ya.gjelderFor(andel.getArbeidsgiver(), andel.getArbeidsforholdRef()))
                .map(YrkesaktivitetDto::getAlleAktivitetsAvtaler).flatMap(Collection::stream)
                .filter(AktivitetsAvtaleDto::erAnsettelsesPeriode)
                .collect(Collectors.toList());

        if (alleAnsettelsesperioder.isEmpty()) {
            // Om vi ikke finner en ansettelsesperiode har vi ikke grunn til å opprette avklaringsbehov. Kan skje f.eks ved manuell oppretting
            return true;
        }

        // Er søker ansatt i arbeidsforholdet på siste dag med søkt refusjon?
        return alleAnsettelsesperioder.stream().anyMatch(aa -> aa.getPeriode().inkluderer(intervall.getTomDato()));
    }

}
