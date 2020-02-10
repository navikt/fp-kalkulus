package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingMedUtbelingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingSvangerskapspengerTest {

    public static final LocalDate STP = LocalDate.now();
    public static final BehandlingReferanse REFERANSE = new BehandlingReferanseMock(STP);
    public static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("910909088");

    private MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingSvangerskapspenger mapper = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingSvangerskapspenger();

    @Test
    void skal_finne_første_dag_med_utbetaling_for_arbeid() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(STP.minusMonths(10), STP.plusMonths(10));
        Periode ansettelsesperiode = Periode.of(periode.getFomDato(), periode.getTomDato());
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(periode)
                .medErAnsettelsesPeriode(true);
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .leggTilAktivitetsAvtale(aktivitetsAvtaleDtoBuilder)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                        .medAktørId(REFERANSE.getAktørId())
                        .leggTilYrkesaktivitet(yrkesaktivitet));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(registerBuilder)
                .build();
        LocalDate førsteDagMedUtbetaling = STP.plusMonths(1);
        SvangerskapspengerGrunnlag svangerskapspengeGrunnlag = lagSVPGrunnlagMedTilrettelegging(førsteDagMedUtbetaling, BigDecimal.TEN);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(REFERANSE, lagBgGrunnlag(),
                BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag, svangerskapspengeGrunnlag);

        // Act
        Optional<LocalDate> førsteSøktePermisjonsdag = mapper.finnFørsteSøktePermisjonsdag(input, yrkesaktivitet, ansettelsesperiode);

        // Assert
        assertThat(førsteSøktePermisjonsdag).isPresent();
        assertThat(førsteSøktePermisjonsdag.get()).isEqualTo(førsteDagMedUtbetaling);
    }

    @Test
    void skal_returnere_empty_for_ingen_utbetaling() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(STP.minusMonths(10), STP.plusMonths(10));
        Periode ansettelsesperiode = Periode.of(periode.getFomDato(), periode.getTomDato());
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(periode)
                .medErAnsettelsesPeriode(true);
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .leggTilAktivitetsAvtale(aktivitetsAvtaleDtoBuilder)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                        .medAktørId(REFERANSE.getAktørId())
                        .leggTilYrkesaktivitet(yrkesaktivitet));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(registerBuilder)
                .build();
        LocalDate førsteDagMedUtbetaling = STP.plusMonths(1);
        SvangerskapspengerGrunnlag svangerskapspengeGrunnlag = lagSVPGrunnlagMedTilrettelegging(førsteDagMedUtbetaling, BigDecimal.ZERO);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(REFERANSE, lagBgGrunnlag(),
                BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag, svangerskapspengeGrunnlag);

        // Act
        Optional<LocalDate> førsteSøktePermisjonsdag = mapper.finnFørsteSøktePermisjonsdag(input, yrkesaktivitet, ansettelsesperiode);

        // Assert
        assertThat(førsteSøktePermisjonsdag).isNotPresent();
    }

    @Test
    void skal_returnere_første_dag_for_utbetaling_med_flere_perioder() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(STP.minusMonths(10), STP.plusMonths(10));
        Periode ansettelsesperiode = Periode.of(periode.getFomDato(), periode.getTomDato());
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(periode)
                .medErAnsettelsesPeriode(true);
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .leggTilAktivitetsAvtale(aktivitetsAvtaleDtoBuilder)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                        .medAktørId(REFERANSE.getAktørId())
                        .leggTilYrkesaktivitet(yrkesaktivitet));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(registerBuilder)
                .build();
        LocalDate førsteDagSøkt = STP.plusMonths(1);
        Map<Intervall, BigDecimal> periodeUtbetalingMap = new HashMap<>();
        periodeUtbetalingMap.put(Intervall.fraOgMedTilOgMed(førsteDagSøkt, førsteDagSøkt.plusMonths(1)), BigDecimal.ZERO);
        LocalDate førsteDagMedUtbetaling = førsteDagSøkt.plusMonths(1).plusDays(1);
        periodeUtbetalingMap.put(Intervall.fraOgMedTilOgMed(førsteDagMedUtbetaling, førsteDagSøkt.plusMonths(2)), BigDecimal.TEN);
        periodeUtbetalingMap.put(Intervall.fraOgMedTilOgMed(førsteDagMedUtbetaling.plusMonths(1).plusDays(1), førsteDagMedUtbetaling.plusMonths(8)), BigDecimal.valueOf(100));
        SvangerskapspengerGrunnlag svangerskapspengeGrunnlag = lagSVPGrunnlagMedTilretteleggingIFlerePerioder(periodeUtbetalingMap);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(REFERANSE, lagBgGrunnlag(),
                BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag, svangerskapspengeGrunnlag);

        // Act
        Optional<LocalDate> førsteSøktePermisjonsdag = mapper.finnFørsteSøktePermisjonsdag(input, yrkesaktivitet, ansettelsesperiode);

        // Assert
        assertThat(førsteSøktePermisjonsdag).isPresent();
        assertThat(førsteSøktePermisjonsdag.get()).isEqualTo(førsteDagMedUtbetaling);
    }


    private SvangerskapspengerGrunnlag lagSVPGrunnlagMedTilrettelegging(LocalDate førsteDagMedSøktYtelse, BigDecimal utbetalingsgrad) {
        TilretteleggingArbeidsforholdDto tilretteleggingArbeidsforhold = new TilretteleggingArbeidsforholdDto(VIRKSOMHET, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetaling = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(førsteDagMedSøktYtelse, STP.plusMonths(3)), utbetalingsgrad);
        TilretteleggingMedUtbelingsgradDto tilrettelegging = new TilretteleggingMedUtbelingsgradDto(tilretteleggingArbeidsforhold, List.of(periodeMedUtbetaling));
        return new SvangerskapspengerGrunnlag(List.of(tilrettelegging), List.of());
    }

    private SvangerskapspengerGrunnlag lagSVPGrunnlagMedTilretteleggingIFlerePerioder(Map<Intervall, BigDecimal> periodeMedutbetalingsgrad) {
        TilretteleggingArbeidsforholdDto tilretteleggingArbeidsforhold = new TilretteleggingArbeidsforholdDto(VIRKSOMHET, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        List<PeriodeMedUtbetalingsgradDto> perioderMedUtbetaling = periodeMedutbetalingsgrad.entrySet().stream().map(e -> new PeriodeMedUtbetalingsgradDto(e.getKey(), e.getValue())).collect(Collectors.toList());
        TilretteleggingMedUtbelingsgradDto tilrettelegging = new TilretteleggingMedUtbelingsgradDto(tilretteleggingArbeidsforhold, perioderMedUtbetaling);
        return new SvangerskapspengerGrunnlag(List.of(tilrettelegging), List.of());
    }

    private BeregningsgrunnlagGrunnlagDtoBuilder lagBgGrunnlag() {
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(VIRKSOMHET))
                .build();
        BeregningsgrunnlagPeriodeDto.Builder periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(STP, Intervall.TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(andel);
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(BeregningsgrunnlagDto.builder()
                        .medSkjæringstidspunkt(STP)
                        .leggTilBeregningsgrunnlagPeriode(periode)
                        .build());
    }
}
