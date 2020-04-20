package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
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

    public static BigDecimal finnHøyestRefusjonskravForBGPerioden(BeregningsgrunnlagPeriodeDto vlBGPeriode, Optional<InntektsmeldingAggregatDto> inntektsmeldinger, LocalDate stp) {
        Intervall relevantPeriode = vlBGPeriode.getPeriode();
        List<Refusjonskrav> refusjonskravs = new ArrayList<>();

        if (inntektsmeldinger.isPresent()) {
            InntektsmeldingAggregatDto inntektsmeldingAggregatDto = inntektsmeldinger.get();
            List<InntektsmeldingDto> inntektsmeldingerSomSkalBrukes = inntektsmeldingAggregatDto.getInntektsmeldingerSomSkalBrukes();
            for (InntektsmeldingDto inntektsmeldingerSomSkalBruke : inntektsmeldingerSomSkalBrukes) {
                refusjonskravs.addAll(MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingerSomSkalBruke, stp));
            }
        }

        BigDecimal høyesteIPerioden = refusjonskravs.stream()
                .filter(ref -> relevantPeriode.overlapper(Intervall.fraOgMedTilOgMed(ref.getPeriode().getFom(), ref.getPeriode().getTom())))
                .max(Comparator.comparing(Refusjonskrav::getMånedsbeløp))
                .map(Refusjonskrav::getMånedsbeløp).orElse(BigDecimal.ZERO);

        //ganger med 12 får å få pr år
        return høyesteIPerioden.multiply(BigDecimal.valueOf(12));
    }
}
