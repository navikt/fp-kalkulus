package no.nav.folketrygdloven.kalkulus.domene.mappers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.input.svangerskapspenger.UtbetalingsgradPrAktivitetDto;

class UtbetalingsgradMapper {

    static List<no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto> mapUtbetalingsgrad(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        return samleArbeidsforhold(utbetalingsgradPrAktivitet).stream().map(e ->
                        new no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto(
                                UtbetalingsgradMapper.mapArbeidsforhold(e.getKey()),
                                UtbetalingsgradMapper.mapPerioderMedUtbetalingsgrad(e.getValue())))
                .toList();
    }

    private static List<Map.Entry<no.nav.foreldrepenger.kalkulus.kontrakt.request.input.svangerskapspenger.AktivitetDto, List<no.nav.foreldrepenger.kalkulus.kontrakt.request.input.svangerskapspenger.PeriodeMedUtbetalingsgradDto>>> samleArbeidsforhold(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        return new ArrayList<>(utbetalingsgradPrAktivitet.stream()
                .collect(Collectors.toMap(
                        UtbetalingsgradPrAktivitetDto::getUtbetalingsgradArbeidsforholdDto,
                        UtbetalingsgradPrAktivitetDto::getPeriodeMedUtbetalingsgrad,
                        (e1, e2) -> {
                            e1.addAll(e2.stream().filter(p -> !e1.contains(p)).toList());
                            return e1;
                        },
                        LinkedHashMap::new))
                .entrySet());
    }
    private static List<PeriodeMedUtbetalingsgradDto> mapPerioderMedUtbetalingsgrad(List<no.nav.foreldrepenger.kalkulus.kontrakt.request.input.svangerskapspenger.PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad) {
        return periodeMedUtbetalingsgrad.stream().map(UtbetalingsgradMapper::mapPeriodeMedUtbetalingsgrad).toList();
    }

    private static PeriodeMedUtbetalingsgradDto mapPeriodeMedUtbetalingsgrad(no.nav.foreldrepenger.kalkulus.kontrakt.request.input.svangerskapspenger.PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto) {
        var aktivitetsgradDomene = periodeMedUtbetalingsgradDto.getAktivitetsgrad() == null ? null : Aktivitetsgrad.fra(periodeMedUtbetalingsgradDto.getAktivitetsgrad().verdi());
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(periodeMedUtbetalingsgradDto.getPeriode().getFom(), periodeMedUtbetalingsgradDto.getPeriode().getTom()),
            utbetalingsgradFraDto(periodeMedUtbetalingsgradDto.getUtbetalingsgrad()), aktivitetsgradDomene);
    }

    public static Utbetalingsgrad utbetalingsgradFraDto(no.nav.foreldrepenger.kalkulus.kontrakt.typer.Utbetalingsgrad utbetalingsgrad) {
        return utbetalingsgrad != null && utbetalingsgrad.verdi() != null ? new Utbetalingsgrad(utbetalingsgrad.verdi()) : null;
    }

    public static AktivitetDto mapArbeidsforhold(no.nav.foreldrepenger.kalkulus.kontrakt.request.input.svangerskapspenger.AktivitetDto aktivitetDto) {
        return new AktivitetDto(MapFraKalkulator.mapArbeidsgiver(aktivitetDto.getArbeidsgiver()), mapReferanse(aktivitetDto), aktivitetDto.getUttakArbeidType());
    }

    private static InternArbeidsforholdRefDto mapReferanse(no.nav.foreldrepenger.kalkulus.kontrakt.request.input.svangerskapspenger.AktivitetDto aktivitetDto) {
        return aktivitetDto.getInternArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : InternArbeidsforholdRefDto.ref(aktivitetDto.getInternArbeidsforholdRef().getAbakusReferanse());
    }
}
