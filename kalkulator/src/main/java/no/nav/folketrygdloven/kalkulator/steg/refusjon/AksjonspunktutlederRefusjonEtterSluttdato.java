package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
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

public final class AksjonspunktutlederRefusjonEtterSluttdato {
    private static final Logger log = LoggerFactory.getLogger(AksjonspunktutlederRefusjonEtterSluttdato.class);

    private AksjonspunktutlederRefusjonEtterSluttdato() {
        // Skjuler default
    }

    public static boolean harRefusjonEtterSisteDatoIArbeidsforhold(Collection<YrkesaktivitetDto> yrkesaktiviteter, UUID koblingUuid, BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        if (yrkesaktiviteter.isEmpty()) {
            return false;
        }
        LocalDateTimeline<RefusjonPeriode> tidslinjeRefusjon = RefusjonTidslinjeTjeneste.lagTidslinje(periodisertMedRefusjonOgGradering);
        List<RefusjonAndel> andelerSomMåSjekkes = tidslinjeRefusjon.toSegments()
                .stream()
                .flatMap(segment -> segment.getValue().getAndeler()
                        .stream()
                        .filter(andel -> andel.getRefusjon().compareTo(BigDecimal.ZERO) > 0
                                && !erAnsattPåDato(andel, segment.getLocalDateInterval(), yrkesaktiviteter)))
                .collect(Collectors.toList());
        if (!andelerSomMåSjekkes.isEmpty()) {
            log.info("FT-718273: Behandling med UUID {} har andeler med refusjon etter sluttdato på arbeidsforholdet", koblingUuid);
        }
        andelerSomMåSjekkes.forEach(andel -> {
            log.info("behandlingUUID {} : Arbeidsgiver {} ", koblingUuid, andel.getArbeidsgiver().toString());
        });
        return !andelerSomMåSjekkes.isEmpty();
    }

    private static boolean erAnsattPåDato(RefusjonAndel andel, LocalDateInterval intervall, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        List<AktivitetsAvtaleDto> alleAnsettelsesperioder = yrkesaktiviteter.stream()
                .filter(ya -> ya.gjelderFor(andel.getArbeidsgiver(), andel.getArbeidsforholdRef()))
                .map(YrkesaktivitetDto::getAlleAktivitetsAvtaler).flatMap(Collection::stream)
                .filter(AktivitetsAvtaleDto::erAnsettelsesPeriode)
                .collect(Collectors.toList());

        // Er søker ansatt i arbeidsforholdet på siste dag med søkt refusjon?
        return alleAnsettelsesperioder.stream().anyMatch(aa -> aa.getPeriode().inkluderer(intervall.getTomDato()));
    }

}
