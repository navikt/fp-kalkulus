package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class MapRefusjonskravFraVLTilRegel {
    private MapRefusjonskravFraVLTilRegel() {
        // skjul public constructor
    }

    static List<Refusjonskrav> periodiserRefusjonsbeløp(InntektsmeldingDto inntektsmelding, LocalDate startdatoPermisjon) {
        Map<LocalDate, Beløp> refusjoner = new TreeMap<>();
        Beløp refusjonBeløpPerMnd = Optional.ofNullable(inntektsmelding.getRefusjonBeløpPerMnd()).orElse(Beløp.ZERO);
        refusjoner.put(startdatoPermisjon, refusjonBeløpPerMnd);
        inntektsmelding.getEndringerRefusjon()
                .stream()
                .sorted(Comparator.comparing(RefusjonDto::getFom))
                .forEach(endring -> {
                    if (endring.getFom().isBefore(startdatoPermisjon)) {
                        refusjoner.put(startdatoPermisjon, endring.getRefusjonsbeløp());
                    } else {
                        refusjoner.put(endring.getFom(), endring.getRefusjonsbeløp());
                    }
                });
        if (inntektsmelding.getRefusjonOpphører() != null && !TIDENES_ENDE.equals(inntektsmelding.getRefusjonOpphører())) {
            refusjoner.put(inntektsmelding.getRefusjonOpphører().plusDays(1), Beløp.ZERO);
        }
        return lagForenkletRefusjonListe(refusjoner);
    }

    static List<Refusjonskrav> periodiserGradertRefusjonsbeløp(InntektsmeldingDto inntektsmelding,
                                                               List<PeriodeMedUtbetalingsgradDto> utbetalingsgrader) {
        Map<LocalDate, Beløp> refusjoner = new TreeMap<>();
        Beløp refusjonBeløpPerMnd = Optional.ofNullable(inntektsmelding.getRefusjonBeløpPerMnd()).orElse(Beløp.ZERO);
        Optional<PeriodeMedUtbetalingsgradDto> førsteUtbetalingsperiode = finnFørsteUtbetalingsgradPeriode(utbetalingsgrader);
        BigDecimal utbetalingsgradVedStart = førsteUtbetalingsperiode.map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                .map(g -> g.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN)).orElse(BigDecimal.ZERO);
        LocalDate startdatoPermisjon = førsteUtbetalingsperiode.map(PeriodeMedUtbetalingsgradDto::getPeriode).map(Intervall::getFomDato).orElse(TIDENES_ENDE);
        refusjoner.put(startdatoPermisjon, refusjonBeløpPerMnd.multipliser(utbetalingsgradVedStart));
        inntektsmelding.getEndringerRefusjon()
                .stream()
                .sorted(Comparator.comparing(RefusjonDto::getFom))
                .forEach(endring -> {
                    if (endring.getFom().isBefore(startdatoPermisjon)) {
                        refusjoner.put(startdatoPermisjon, endring.getRefusjonsbeløp().multipliser(utbetalingsgradVedStart));
                    } else {
                        BigDecimal utbetalingsgrad = finnUtbetalingsgradForDato(utbetalingsgrader, startdatoPermisjon);
                        refusjoner.put(endring.getFom(), endring.getRefusjonsbeløp().multipliser(utbetalingsgrad));
                    }
                });

        if (inntektsmelding.getRefusjonOpphører() != null && !TIDENES_ENDE.equals(inntektsmelding.getRefusjonOpphører())) {
            refusjoner.put(inntektsmelding.getRefusjonOpphører().plusDays(1), Beløp.ZERO);
        }
        return lagForenkletRefusjonListe(refusjoner);
    }

    private static BigDecimal finnUtbetalingsgradForDato(List<PeriodeMedUtbetalingsgradDto> utbetalingsgrader, LocalDate startdatoPermisjon) {
        return utbetalingsgrader.stream().filter(u -> u.getPeriode().inkluderer(startdatoPermisjon))
                .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                .map(b -> b.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN))
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private static Optional<PeriodeMedUtbetalingsgradDto> finnFørsteUtbetalingsgradPeriode(List<PeriodeMedUtbetalingsgradDto> utbetalingsgrader) {
        return utbetalingsgrader.stream()
                .min(Comparator.comparing(u -> u.getPeriode().getFomDato()));

    }

    private static List<Refusjonskrav> lagForenkletRefusjonListe(Map<LocalDate, Beløp> refusjoner) {
        List<Refusjonskrav> refusjonskravListe = new ArrayList<>();
        List<Map.Entry<LocalDate, Beløp>> entryList = new ArrayList<>(refusjoner.entrySet());
        ListIterator<Map.Entry<LocalDate, Beløp>> listIterator = entryList.listIterator();

        while (listIterator.hasNext()) {
            Map.Entry<LocalDate, Beløp> entry = listIterator.next();
            LocalDate fom = entry.getKey();
            LocalDate tom = utledTom(entryList, listIterator);
            BigDecimal refusjonPrMåned = entry.getValue().getVerdi();
            refusjonskravListe.add(new Refusjonskrav(refusjonPrMåned, fom, tom));
        }
        return refusjonskravListe;
    }

    private static LocalDate utledTom(List<Map.Entry<LocalDate, Beløp>> entryList, ListIterator<Map.Entry<LocalDate, Beløp>> listIterator) {
        Optional<LocalDate> nesteFomOpt = hentNesteFom(entryList, listIterator);
        return nesteFomOpt.map(nesteFom -> nesteFom.minusDays(1)).orElse(null);
    }

    private static Optional<LocalDate> hentNesteFom(List<Map.Entry<LocalDate, Beløp>> entryList,
                                                    ListIterator<Map.Entry<LocalDate, Beløp>> listIterator) {
        if (listIterator.hasNext()) {
            Map.Entry<LocalDate, Beløp> nesteEntry = entryList.get(listIterator.nextIndex());
            return Optional.of(nesteEntry.getKey());
        }
        return Optional.empty();
    }

    public static BigDecimal finnLavesteTotalRefusjonForBGPerioden(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                                   Collection<InntektsmeldingDto> inntektsmeldingerSomSkalBrukes,
                                                                   LocalDate stp,
                                                                   YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        Intervall relevantPeriode = vlBGPeriode.getPeriode();
        List<Refusjonskrav> refusjonskravs = new ArrayList<>();
        for (InntektsmeldingDto inntektsmeldingerSomSkalBruke : inntektsmeldingerSomSkalBrukes) {
            if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
                UtbetalingsgradGrunnlag utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag;
                var utbetalingsgrader = utbetalingsgradGrunnlag.finnUtbetalingsgraderForArbeid(inntektsmeldingerSomSkalBruke.getArbeidsgiver(), inntektsmeldingerSomSkalBruke.getArbeidsforholdRef());
                refusjonskravs.addAll(MapRefusjonskravFraVLTilRegel.periodiserGradertRefusjonsbeløp(inntektsmeldingerSomSkalBruke, utbetalingsgrader));
            } else {
                // Usikker på om vi trenger dette ettersom det kun brukes for omsorgspenger som alltid vil vere eit utbetalingsgradgrunnlag
                refusjonskravs.addAll(MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingerSomSkalBruke, stp));
            }
        }

        List<Refusjonskrav> relevanteRefusjonskrav = refusjonskravs.stream().filter(p -> p.getPeriode().overlapper(Periode.of(relevantPeriode.getFomDato(), relevantPeriode.getTomDato())))
                .collect(Collectors.toList());

        BigDecimal lavesteSummertIPerioden = relevanteRefusjonskrav.stream().map(Refusjonskrav::getPeriode)
                .map(periode -> relevanteRefusjonskrav.stream()
                        .filter(refusjonskrav -> refusjonskrav.getPeriode().overlapper(periode))
                        .map(Refusjonskrav::getMånedsbeløp)
                        .reduce(BigDecimal::add)
                        .orElse(BigDecimal.ZERO))
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        //ganger med 12 får å få pr år
        return lavesteSummertIPerioden.multiply(BigDecimal.valueOf(12));
    }

}
