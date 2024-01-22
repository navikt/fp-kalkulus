package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.felles.ytelseovergang.InfotrygdvedtakMedDagpengerTjeneste.finnDagsatsFraYtelsevedtak;
import static no.nav.folketrygdloven.kalkulator.felles.ytelseovergang.InfotrygdvedtakMedDagpengerTjeneste.harYtelsePåGrunnlagAvDagpenger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;


class FinnInntektFraYtelse {

    private static final BigDecimal VIRKEDAGER_I_1_ÅR = BigDecimal.valueOf(260);

    private FinnInntektFraYtelse() {
        // Skjul konstruktør
    }

    static Optional<BigDecimal> finnÅrbeløpFraMeldekortForAndel(KoblingReferanse ref,
                                                                BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                YtelseFilterDto ytelseFilter) {
        LocalDate skjæringstidspunkt = ref.getSkjæringstidspunktBeregning();
        if (ytelseFilter.isEmpty()) {
            return Optional.empty();
        }

        Optional<YtelseDto> nyesteVedtak = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(mapTilYtelseType(andel.getAktivitetStatus())));
        if (nyesteVedtak.isEmpty()) {
            return Optional.empty();
        }

        if (KonfigurasjonVerdi.get("MELDEKORT_DELVIS_PERIODE", false)) {
            var meldekort = MeldekortUtils.finnSisteHeleMeldekortFørStpMedJustertPeriode(ytelseFilter,
                    skjæringstidspunkt,
                    Set.of(mapTilYtelseType(andel.getAktivitetStatus()))
            );
            var utbetalingsfaktor = meldekort.map(MeldekortUtils.Meldekort::utbetalingsfaktor).orElse(BigDecimal.ONE);
            var dagsats = nyesteVedtak.get().getVedtaksDagsats().map(Beløp::getVerdi)
                    .orElse(meldekort.map(MeldekortUtils.Meldekort::dagsats).map(Beløp::getVerdi).orElse(BigDecimal.ZERO));
            return Optional.of(dagsats.multiply(utbetalingsfaktor).multiply(BigDecimal.valueOf(260)));
        } else {

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

    }

    static Optional<BigDecimal> finnÅrbeløpForDagpenger(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                        YtelseFilterDto ytelseFilter,
                                                        LocalDate skjæringstidspunkt) {

        Collection<YtelseDto> ytelser = ytelseFilter.getFiltrertYtelser();
        Boolean harSPAvDP = harYtelsePåGrunnlagAvDagpenger(ytelser, skjæringstidspunkt, YtelseType.SYKEPENGER);
        if (harSPAvDP && KonfigurasjonVerdi.get("BEREGNE_DAGPENGER_FRA_SYKEPENGER", false)) {
            return Optional.of(finnDagsatsFraYtelsevedtak(ytelser, skjæringstidspunkt, YtelseType.SYKEPENGER).multiply(VIRKEDAGER_I_1_ÅR));
        }
        Boolean harPSBAvDP = harYtelsePåGrunnlagAvDagpenger(ytelser, skjæringstidspunkt, YtelseType.PLEIEPENGER_SYKT_BARN);
        if (harPSBAvDP && KonfigurasjonVerdi.get("BEREGNE_DAGPENGER_FRA_PLEIEPENGER", false)) {
            return Optional.of(finnDagsatsFraYtelsevedtak(ytelser, skjæringstidspunkt, YtelseType.PLEIEPENGER_SYKT_BARN).multiply(VIRKEDAGER_I_1_ÅR));
        }
        return finnÅrbeløpFraMeldekortForAndel(ref, andel, ytelseFilter);
    }


    private static YtelseType mapTilYtelseType(AktivitetStatus aktivitetStatus) {
        if (AktivitetStatus.DAGPENGER.equals(aktivitetStatus)) {
            return YtelseType.DAGPENGER;
        }
        if (AktivitetStatus.ARBEIDSAVKLARINGSPENGER.equals(aktivitetStatus)) {
            return YtelseType.ARBEIDSAVKLARINGSPENGER;
        }
        return YtelseType.UDEFINERT;
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
