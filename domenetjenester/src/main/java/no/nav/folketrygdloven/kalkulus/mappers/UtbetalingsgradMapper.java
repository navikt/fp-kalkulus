package no.nav.folketrygdloven.kalkulus.mappers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.beregning.v1.UtbetalingsgradPrAktivitetDto;

class UtbetalingsgradMapper {

    static List<no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto> mapUtbetalingsgrad(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        return samleArbeidsforhold(utbetalingsgradPrAktivitet).stream().map(e ->
                        new no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto(
                                UtbetalingsgradMapper.mapArbeidsforhold(e.getKey()),
                                UtbetalingsgradMapper.mapPerioderMedUtbetalingsgrad(e.getValue())))
                .collect(Collectors.toList());
    }

    private static List<Map.Entry<no.nav.folketrygdloven.kalkulus.beregning.v1.UtbetalingsgradArbeidsforholdDto, List<no.nav.folketrygdloven.kalkulus.beregning.v1.PeriodeMedUtbetalingsgradDto>>> samleArbeidsforhold(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        return utbetalingsgradPrAktivitet.stream()
                .collect(Collectors.toMap(
                        UtbetalingsgradPrAktivitetDto::getUtbetalingsgradArbeidsforholdDto,
                        UtbetalingsgradPrAktivitetDto::getPeriodeMedUtbetalingsgrad,
                        (e1, e2) -> {
                            e1.addAll(e2);
                            return e1;
                        },
                        LinkedHashMap::new))
                .entrySet()
                .stream()
                .collect(Collectors.toList());
    }

    private static no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto mapUtbetalingsgradPrAktivitet(UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto) {
        return new no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto(mapArbeidsforhold(utbetalingsgradPrAktivitetDto.getUtbetalingsgradArbeidsforholdDto()), mapPerioderMedUtbetalingsgrad(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad()));
    }

    private static List<PeriodeMedUtbetalingsgradDto> mapPerioderMedUtbetalingsgrad(List<no.nav.folketrygdloven.kalkulus.beregning.v1.PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad) {
        return periodeMedUtbetalingsgrad.stream().map(UtbetalingsgradMapper::mapPeriodeMedUtbetalingsgrad).collect(Collectors.toList());
    }

    private static PeriodeMedUtbetalingsgradDto mapPeriodeMedUtbetalingsgrad(no.nav.folketrygdloven.kalkulus.beregning.v1.PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(periodeMedUtbetalingsgradDto.getPeriode().getFom(), periodeMedUtbetalingsgradDto.getPeriode().getTom()), periodeMedUtbetalingsgradDto.getUtbetalingsgrad());
    }

    public static UtbetalingsgradArbeidsforholdDto mapArbeidsforhold(no.nav.folketrygdloven.kalkulus.beregning.v1.UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto) {
        return new UtbetalingsgradArbeidsforholdDto(MapFraKalkulator.mapArbeidsgiver(utbetalingsgradArbeidsforholdDto.getArbeidsgiver()), mapReferanse(utbetalingsgradArbeidsforholdDto), mapUttakArbeidType(utbetalingsgradArbeidsforholdDto.getUttakArbeidType()));
    }

    private static UttakArbeidType mapUttakArbeidType(no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType uttakArbeidType) {
        return UttakArbeidType.fraKode(uttakArbeidType.getKode());
    }

    private static InternArbeidsforholdRefDto mapReferanse(no.nav.folketrygdloven.kalkulus.beregning.v1.UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto) {
        return utbetalingsgradArbeidsforholdDto.getInternArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : InternArbeidsforholdRefDto.ref(utbetalingsgradArbeidsforholdDto.getInternArbeidsforholdRef().getAbakusReferanse());
    }


}
