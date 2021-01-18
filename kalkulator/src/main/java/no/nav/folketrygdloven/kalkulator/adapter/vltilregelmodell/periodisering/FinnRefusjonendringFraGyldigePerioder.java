package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class FinnRefusjonendringFraGyldigePerioder {

    /**
     * Perioder med mulig utbetaling av refusjon.
     *
     * For Foreldrepenger er dette gitt av ansettelsesperioder for arbeidsforholdet/arbeidsforholdene.
     *
     * For K9/svangerskapspenger er dette gitt av perioder med utbetalingsgrad for arbeidsforholdet/arbeidsforholdene.
     *
     */
    private final List<Intervall> gyldigePerioder;
    private final List<Refusjonskrav> refusjonerFraInntektsmelding;
    private final LocalDate startdatoYtelse;

    FinnRefusjonendringFraGyldigePerioder(List<Intervall> gyldigePerioder,
                                          List<Refusjonskrav> refusjonerFraInntektsmelding,
                                          LocalDate startdatoYtelse) {
        this.gyldigePerioder = gyldigePerioder;
        this.refusjonerFraInntektsmelding = refusjonerFraInntektsmelding;
        this.startdatoYtelse = startdatoYtelse;
    }

    Map<LocalDate, Beløp> finnEndringerIRefusjon() {
        Map<LocalDate, Beløp> opphør = finnOpphørAvRefusjonVedOpphørtPeriode();
        Map<LocalDate, Beløp> startAvRefusjon = finnRestartAvRefusjonVedStartAvPeriode(opphør.keySet());
        opphør.forEach(startAvRefusjon::put);
        return startAvRefusjon;
    }

    private Map<LocalDate, Beløp> finnRestartAvRefusjonVedStartAvPeriode(Set<LocalDate> opphørDatoer) {
        return gyldigePerioder.stream().filter(this::erStartAvGyldigPeriode)
                .map(Intervall::getFomDato)
                .filter(d -> refusjonerFraInntektsmelding.stream().anyMatch(harOpphørtRefusjonSomOverlapperMedDato(d, opphørDatoer)))
                .distinct()
                .collect(Collectors.toMap(Function.identity(),
                        (d) -> refusjonerFraInntektsmelding.stream()
                                .filter(harOpphørtRefusjonSomOverlapperMedDato(d, opphørDatoer))
                                .findFirst().map(r -> new Beløp(r.getMånedsbeløp())).orElse(Beløp.ZERO)));
    }

    private Map<LocalDate, Beløp> finnOpphørAvRefusjonVedOpphørtPeriode() {
        return gyldigePerioder.stream().filter(this::erOpphørAvGyldigPeriode)
                .map(Intervall::getTomDato)
                .distinct()
                .collect(Collectors.toMap(d -> d.plusDays(1), (d) -> Beløp.ZERO));
    }

    private Predicate<Refusjonskrav> harOpphørtRefusjonSomOverlapperMedDato(LocalDate d, Set<LocalDate> opphørDatoer) {
        return r -> r.getPeriode().inneholder(d) && r.getMånedsbeløp().compareTo(BigDecimal.ZERO) > 0
                && opphørDatoer.stream().anyMatch(opphørsdato -> r.getPeriode().inneholder(opphørsdato) && opphørsdato.isBefore(d));
    }

    private boolean erOpphørAvGyldigPeriode(Intervall p1) {
        return !p1.getTomDato().equals(TIDENES_ENDE) && gyldigePerioder.stream()
                .noneMatch(p2 -> p2.getFomDato().minusDays(1).equals(p1.getTomDato()));
    }

    private boolean erStartAvGyldigPeriode(Intervall p1) {
        return !p1.getFomDato().isBefore(startdatoYtelse) && gyldigePerioder.stream()
                .noneMatch(p2 -> p2.getTomDato().plusDays(1).equals(p1.getFomDato()));
    }

}
