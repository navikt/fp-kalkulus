package no.nav.folketrygdloven.kalkulator.felles.ytelseovergang;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public class DirekteOvergangTjeneste {

    private static final List<FagsakYtelseType> YTELSER_FRA_KAP_8 = List.of(
            FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE,
            FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
            FagsakYtelseType.SYKEPENGER,
            FagsakYtelseType.FORELDREPENGER,
            FagsakYtelseType.OMSORGSPENGER,
            FagsakYtelseType.OPPLÆRINGSPENGER,
            FagsakYtelseType.SVANGERSKAPSPENGER
    );

    /**
     * Finner siste ytelseanvisning eller -anvisninger før skjæringstidspunktet for ytelser som beregnes fra folketrygdloven kapittel 8;
     * Sykepenger, Foreldrepenger, Svangerskapspenger, Pleiepenger, Omsorgspenger og Opplæringspenger
     *
     * @param iayGrunnlag        IAY-grunnlag
     * @param skjæringstidspunkt Skjæringstidspunkt for beregning
     * @return Liste med anvisninger
     */
    public static List<YtelseAnvistDto> finnAnvisningerForDirekteOvergangFraKap8(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunkt) {
        return finnSisteAnvisninger(getYtelseFilterKap8(iayGrunnlag, skjæringstidspunkt), skjæringstidspunkt);
    }

    private static YtelseFilterDto getYtelseFilterKap8(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunkt) {
        return new YtelseFilterDto(iayGrunnlag.getAktørYtelseFraRegister())
                .før(skjæringstidspunkt)
                .filter(y -> YTELSER_FRA_KAP_8.contains(y.getYtelseType()))
                .filter(y -> y.getPeriode().getTomDato().isAfter(skjæringstidspunkt.minusMonths(3).withDayOfMonth(1)));
    }

    private static List<YtelseAnvistDto> finnSisteAnvisninger(YtelseFilterDto filter, LocalDate skjæringstidspunkt) {
        var ytelser = filter.getAlleYtelser();
        var alleAnvisninger = ytelser.stream()
                .flatMap(y -> y.getYtelseAnvist().stream()
                        .filter(ya -> ya.getAnvistPeriode().getFomDato().isBefore(skjæringstidspunkt)))
                .toList();
        var sisteDagMedAnvisning = alleAnvisninger.stream().max(Comparator.comparing(YtelseAnvistDto::getAnvistTOM)).map(YtelseAnvistDto::getAnvistTOM);
        return sisteDagMedAnvisning.isEmpty() ? Collections.emptyList() : alleAnvisninger.stream()
                .filter(a -> a.getAnvistTOM().equals(sisteDagMedAnvisning.get())).toList();
    }


}
