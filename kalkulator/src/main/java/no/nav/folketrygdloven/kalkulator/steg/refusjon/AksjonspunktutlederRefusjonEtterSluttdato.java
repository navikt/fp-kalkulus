package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriode;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.vedtak.konfig.Tid;

public final class AksjonspunktutlederRefusjonEtterSluttdato {
    private static final Logger log = LoggerFactory.getLogger(AksjonspunktutlederRefusjonEtterSluttdato.class);

    private AksjonspunktutlederRefusjonEtterSluttdato() {
        // Skjuler default
    }

    public static boolean harRefusjonEtterSisteDatoIArbeidsforhold(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                   UUID koblingUuid,
                                                                   Optional<LocalDate> sisteSøkteUttaksdag,
                                                                   BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        // Trenger ikke sjekke om søker ikke er i jobb eller om det ikke finnes uttak
        if (yrkesaktiviteter.isEmpty() || sisteSøkteUttaksdag.isEmpty()) {
            return false;
        }
        LocalDateTimeline<RefusjonPeriode> allePerioder = RefusjonTidslinjeTjeneste.lagTidslinje(periodisertMedRefusjonOgGradering);
        LocalDateTimeline<RefusjonPeriode> perioderMedPotensieltUttak = allePerioder.intersection(finnPeriodeFremTilSisteUttak(sisteSøkteUttaksdag.get()));

        List<RefusjonAndel> andelerSomMåSjekkes = perioderMedPotensieltUttak.toSegments()
                .stream()
                .flatMap(segment -> segment.getValue().getAndeler()
                        .stream()
                        .filter(andel -> andel.getRefusjon().compareTo(BigDecimal.ZERO) > 0
                                && !erAnsattPåDato(andel, segment.getLocalDateInterval(), yrkesaktiviteter)))
                .collect(Collectors.toList());

        andelerSomMåSjekkes.forEach(andel -> log.info("FT-718273: behandlingUUID {} : Arbeidsgiver {} ", koblingUuid, andel.getArbeidsgiver().toString()));
        return !andelerSomMåSjekkes.isEmpty();
    }

    private static LocalDateTimeline<RefusjonPeriode> finnPeriodeFremTilSisteUttak(LocalDate sisteUttaksdato) {
        return new LocalDateTimeline<>(
                Tid.TIDENES_BEGYNNELSE,
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
            // Om vi ikke finner en ansettelsesperiode har vi ikke grunn til å opprette aksjonspunkt. Kan skje f.eks ved manuell oppretting
            return true;
        }

        // Er søker ansatt i arbeidsforholdet på siste dag med søkt refusjon?
        return alleAnsettelsesperioder.stream().anyMatch(aa -> aa.getPeriode().inkluderer(intervall.getTomDato()));
    }

}
