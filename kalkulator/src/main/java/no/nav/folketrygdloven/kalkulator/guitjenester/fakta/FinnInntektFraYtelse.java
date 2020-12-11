package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.felles.BeregningUtils;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;


class FinnInntektFraYtelse {

    private static final BigDecimal VIRKEDAGER_I_1_ÅR = BigDecimal.valueOf(260);

    private FinnInntektFraYtelse() {
        // Skjul konstruktør
    }

    static Optional<BigDecimal> finnÅrbeløpFraMeldekort(KoblingReferanse ref, AktivitetStatus aktivitetStatus, InntektArbeidYtelseGrunnlagDto grunnlag) {
        LocalDate skjæringstidspunkt = ref.getSkjæringstidspunktBeregning();
        var ytelseFilter = new YtelseFilterDto(grunnlag.getAktørYtelseFraRegister()).før(skjæringstidspunkt);
        if (ytelseFilter.isEmpty()) {
            return Optional.empty();
        }

        Optional<YtelseDto> nyesteVedtak = BeregningUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(mapTilYtelseType(aktivitetStatus)));
        if (nyesteVedtak.isEmpty()) {
            return Optional.empty();
        }

        Optional<YtelseAnvistDto> nyesteMeldekort = BeregningUtils.sisteHeleMeldekortFørStp(ytelseFilter, nyesteVedtak.get(), skjæringstidspunkt, Set.of(mapTilYtelseType(aktivitetStatus)), ref.getFagsakYtelseType());
        return Optional.of(finnÅrsbeløp(nyesteVedtak.get(), nyesteMeldekort));
    }

    private static FagsakYtelseType mapTilYtelseType(AktivitetStatus aktivitetStatus) {
        if (AktivitetStatus.DAGPENGER.equals(aktivitetStatus)) {
            return FagsakYtelseType.DAGPENGER;
        }
        if (AktivitetStatus.ARBEIDSAVKLARINGSPENGER.equals(aktivitetStatus)) {
            return FagsakYtelseType.ARBEIDSAVKLARINGSPENGER;
        }
        return FagsakYtelseType.UDEFINERT;
    }

    private static BigDecimal finnÅrsbeløp(YtelseDto ytelse, Optional<YtelseAnvistDto> ytelseAnvist) {
        BigDecimal dagsats = ytelse.getVedtaksDagsats().map(Beløp::getVerdi)
            .orElse(ytelseAnvist.flatMap(YtelseAnvistDto::getDagsats).map(Beløp::getVerdi).orElse(BigDecimal.ZERO));
        BigDecimal utbetalingsgrad = ytelseAnvist.flatMap(YtelseAnvistDto::getUtbetalingsgradProsent).map(Stillingsprosent::getVerdi)
            .orElse(BeregningUtils.MAX_UTBETALING_PROSENT_AAP_DAG);
        BigDecimal utbetalingsFaktor = utbetalingsgrad.divide(BeregningUtils.MAX_UTBETALING_PROSENT_AAP_DAG, 10, RoundingMode.HALF_UP);
        return dagsats
            .multiply(utbetalingsFaktor)
            .multiply(VIRKEDAGER_I_1_ÅR);
    }

}
