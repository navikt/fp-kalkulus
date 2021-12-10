package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk;


import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class MapUttakArbeidTypeTilAktivitetStatusV2 {

    public static AktivitetStatusV2 mapAktivitetStatus(UtbetalingsgradArbeidsforholdDto utbetalingsgradAktivitet, List<UtbetalingsgradPrAktivitetDto> allePerioder) {
        UttakArbeidType uttakArbeidType = utbetalingsgradAktivitet.getUttakArbeidType();
        if (UttakArbeidType.ORDINÆRT_ARBEID.equals(uttakArbeidType)) {
            return AktivitetStatusV2.AT;
        }
        if (UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(uttakArbeidType)) {
            return AktivitetStatusV2.SN;
        }
        if (UttakArbeidType.FRILANS.equals(uttakArbeidType)) {
            return AktivitetStatusV2.FL;
        }
        if (UttakArbeidType.MIDL_INAKTIV.equals(uttakArbeidType)) {
            return AktivitetStatusV2.IN;
        }
        if (UttakArbeidType.DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatusV2.DP;
        }
        if (UttakArbeidType.SYKEPENGER_AV_DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatusV2.SP_AV_DP;
        }
        if (UttakArbeidType.PLEIEPENGER_AV_DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatusV2.PSB_AV_DP;
        }
        if (UttakArbeidType.BRUKERS_ANDEL.equals(uttakArbeidType)) {
            return AktivitetStatusV2.BA;
        }
        if (UttakArbeidType.IKKE_YRKESAKTIV.equals(uttakArbeidType)) {
            if (utbetalingsgradAktivitet.getArbeidsgiver().isPresent()) {
                return AktivitetStatusV2.AT;
            }
            return finnSisteSøkteStatus(allePerioder);
        }
        if (UttakArbeidType.ANNET.equals(uttakArbeidType)) {
            throw new IllegalArgumentException("Kan ikke gradere " + UttakArbeidType.ANNET);
        }
        throw new IllegalArgumentException("Ukjent UttakArbeidType '" + utbetalingsgradAktivitet + "' kan ikke mappe til " + AktivitetStatus.class.getName());
    }

    private static AktivitetStatusV2 finnSisteSøkteStatus(List<UtbetalingsgradPrAktivitetDto> allePerioder) {
        var sisteSøkteAktivitet = finnSisteSøkteAktivitetSomYrkesaktiv(allePerioder);
        validerKunEnStatus(allePerioder, sisteSøkteAktivitet);
        return mapAktivitetStatus(sisteSøkteAktivitet.getUtbetalingsgradArbeidsforhold(), allePerioder);
    }

    private static void validerKunEnStatus(List<UtbetalingsgradPrAktivitetDto> allePerioder, UtbetalingsgradPrAktivitetDto sisteSøkteAktivitet) {
        List<UtbetalingsgradPrAktivitetDto> aktiviteterISisteSøktePeriode = finnAktiviteterMedSammePeriode(allePerioder, sisteSøkteAktivitet);
        if (aktiviteterISisteSøktePeriode.size() > 1) {
            List<UttakArbeidType> uttakarbeidTyper = aktiviteterISisteSøktePeriode.stream().map(UtbetalingsgradPrAktivitetDto::getUtbetalingsgradArbeidsforhold)
                    .map(UtbetalingsgradArbeidsforholdDto::getUttakArbeidType)
                    .collect(Collectors.toList());
            throw new UnsupportedOperationException("Støtter ikke overgang til ikke-yrkesaktiv for flere statuser samtidig: " + uttakarbeidTyper);
        }
    }

    private static List<UtbetalingsgradPrAktivitetDto> finnAktiviteterMedSammePeriode(List<UtbetalingsgradPrAktivitetDto> allePerioder, UtbetalingsgradPrAktivitetDto sisteSøkteAktivitet) {
        return allePerioder.stream()
                .filter(a -> finnSistePeriode(a).getTomDato().equals(finnSistePeriode(sisteSøkteAktivitet).getTomDato()))
                .filter(p -> p.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().isEmpty())
                .collect(Collectors.toList());
    }

    private static Intervall finnSistePeriode(UtbetalingsgradPrAktivitetDto a) {
        return a.getPeriodeMedUtbetalingsgrad().stream().max(PeriodeMedUtbetalingsgradDto::compareTo)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .orElseThrow(() -> new IllegalStateException("Antar at det er søkt om minst en periode"));
    }

    private static UtbetalingsgradPrAktivitetDto finnSisteSøkteAktivitetSomYrkesaktiv(List<UtbetalingsgradPrAktivitetDto> allePerioder) {
        return allePerioder.stream()
                .filter(a -> !a.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.IKKE_YRKESAKTIV))
                .filter(a -> a.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().isEmpty())
                .filter(a -> a.getPeriodeMedUtbetalingsgrad().stream().anyMatch(p -> p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0))
                .max(Comparator.comparing(perioder -> perioder.getPeriodeMedUtbetalingsgrad().stream()
                        .filter(p -> p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                        .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                        .max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalStateException("Antar at det er søkt om minst en periode"))))
                .orElseThrow(() -> new IllegalStateException("Fant ingen periode der søker var yrkesaktiv"));
    }


}
