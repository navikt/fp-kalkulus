package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
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

    static Optional<BigDecimal> finnÅrbeløpFraMeldekortForAndel(KoblingReferanse ref,
                                                                BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                InntektArbeidYtelseGrunnlagDto grunnlag) {
        LocalDate skjæringstidspunkt = ref.getSkjæringstidspunktBeregning();
        var ytelseFilter = new YtelseFilterDto(grunnlag.getAktørYtelseFraRegister()).før(skjæringstidspunkt);
        if (ytelseFilter.isEmpty()) {
            return Optional.empty();
        }

        Optional<YtelseDto> nyesteVedtak = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(mapTilYtelseType(andel.getAktivitetStatus())));
        if (nyesteVedtak.isEmpty()) {
            return Optional.empty();
        }

        Optional<YtelseAnvistDto> nyesteMeldekort = MeldekortUtils.sisteHeleMeldekortFørStp(ytelseFilter, nyesteVedtak.get(),
                skjæringstidspunkt,
                Set.of(mapTilYtelseType(andel.getAktivitetStatus())),
                ref.getFagsakYtelseType());

        // Hvis søker kun har status DP / AAP tar ikke beregning hensyn til utbetalingsfaktor
        int antallUnikeStatuserIPeriode = andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getAktivitetStatus)
                .collect(Collectors.toSet())
                .size();

        if (antallUnikeStatuserIPeriode > 1) {
            return Optional.of(finnÅrsbeløpMedHensynTilUtbetalingsfaktor(nyesteVedtak.get(), nyesteMeldekort));
        } else {
            return Optional.of(finnÅrsbeløp(nyesteVedtak.get(), nyesteMeldekort));
        }

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

    private static BigDecimal finnÅrsbeløpMedHensynTilUtbetalingsfaktor(YtelseDto ytelse, Optional<YtelseAnvistDto> ytelseAnvist) {
        BigDecimal årsbeløpUtenHensynTilUtbetalingsfaktor = finnÅrsbeløp(ytelse, ytelseAnvist);
        BigDecimal utbetalingsgrad = ytelseAnvist.flatMap(YtelseAnvistDto::getUtbetalingsgradProsent).map(Stillingsprosent::getVerdi)
            .orElse(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG);
        BigDecimal utbetalingsFaktor = utbetalingsgrad.divide(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG, 10, RoundingMode.HALF_UP);
        return årsbeløpUtenHensynTilUtbetalingsfaktor
            .multiply(utbetalingsFaktor);
    }

    private static BigDecimal finnÅrsbeløp(YtelseDto ytelse, Optional<YtelseAnvistDto> ytelseAnvist) {
        BigDecimal dagsats = ytelse.getVedtaksDagsats().map(Beløp::getVerdi)
                .orElse(ytelseAnvist.flatMap(YtelseAnvistDto::getDagsats).map(Beløp::getVerdi).orElse(BigDecimal.ZERO));
        return dagsats
                .multiply(VIRKEDAGER_I_1_ÅR);
    }


}
