package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel.Input;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingUtbgradTest {

    public static final LocalDate STP = LocalDate.now();
    public static final KoblingReferanse REFERANSE = new KoblingReferanseMock(STP);
    public static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("910909088");

    private MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingUtbgrad mapper = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingUtbgrad();


    @Test
    void skal_gi_refusjon_fra_start_for_arbeid_som_har_oppgitt_feil_startdato_i_inntektsmelding() {

        // Arrange
        String orgnr = "974749866";
        LocalDate stp = LocalDate.of(2019, 10, 16);
        AktivitetsAvtaleDtoBuilder ansettelsesPeriode = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMed(LocalDate.of(2013, 5, 27)))
                .medErAnsettelsesPeriode(true);
        Intervall periode= Intervall.fraOgMed(LocalDate.of(2019, 1, 1));
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleDtoBuilder = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(periode)
                .medSisteLønnsendringsdato(LocalDate.of(2018, 7, 1))
                .medErAnsettelsesPeriode(false);

        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(orgnr);
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .leggTilAktivitetsAvtale(aktivitetsAvtaleDtoBuilder)
                .leggTilAktivitetsAvtale(ansettelsesPeriode)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsgiver(virksomhet)
                .build();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                        .leggTilYrkesaktivitet(yrkesaktivitet));


        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder().medBeløp(BigDecimal.TEN).medRefusjon(BigDecimal.TEN).medArbeidsgiver(virksomhet).medStartDatoPermisjon(LocalDate.of(2019, 10, 22)).build();

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(registerBuilder)
                .medInntektsmeldinger(im)
                .build();

        UtbetalingsgradArbeidsforholdDto tilretteleggingArbeidsforhold = new UtbetalingsgradArbeidsforholdDto(virksomhet, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetaling = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, LocalDate.of(2019, 10, 21)), BigDecimal.valueOf(100));
        PeriodeMedUtbetalingsgradDto periodeMedUtbetaling2 = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(LocalDate.of(2019, 10, 22), LocalDate.of(2020, 1, 19)), BigDecimal.valueOf(40));

        UtbetalingsgradPrAktivitetDto tilrettelegging = new UtbetalingsgradPrAktivitetDto(tilretteleggingArbeidsforhold, List.of(periodeMedUtbetaling, periodeMedUtbetaling2));
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(List.of(tilrettelegging));


        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build();
        BeregningsgrunnlagPeriodeDto.Builder bgperiode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(stp, Intervall.TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(andel);
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(stp)
                .medGrunnbeløp(BigDecimal.valueOf(99000))
                .leggTilBeregningsgrunnlagPeriode(bgperiode)
                .build();
        BeregningsgrunnlagGrunnlagDtoBuilder bg = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(stp)
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMed(LocalDate.of(2013, 5, 27)))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .medArbeidsgiver(virksomhet)
                                .build())
                        .build())
                .medBeregningsgrunnlag(beregningsgrunnlagDto);


        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(REFERANSE, bg,
                BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag, svangerskapspengerGrunnlag);

        // Act
        PeriodeModell map = mapper.map(input, beregningsgrunnlagDto);

        // Assert
        assertThat(map.getArbeidsforholdOgInntektsmeldinger().size()).isEqualTo(1);
        ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding = map.getArbeidsforholdOgInntektsmeldinger().get(0);
        assertThat(arbeidsforholdOgInntektsmelding.getRefusjoner().size()).isEqualTo(2);
        Refusjonskrav refusjonskrav = arbeidsforholdOgInntektsmelding.getRefusjoner().get(0);
        assertThat(refusjonskrav.getPeriode().getFom()).isEqualTo(stp);

        Refusjonskrav opphør = arbeidsforholdOgInntektsmelding.getRefusjoner().get(1);
        assertThat(opphør.getMånedsbeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(opphør.getPeriode().getFom()).isEqualTo(LocalDate.of(2020, 1, 19).plusDays(1));
    }

    @Test
    void skal_returnere_skjæringstidspunkt_om_første_søkte_permisjonsdag_er_før_skjæringstidspunkt() {
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
                        .leggTilYrkesaktivitet(yrkesaktivitet));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(registerBuilder)
                .build();
        LocalDate førsteDagMedUtbetaling = STP.minusMonths(1);
        SvangerskapspengerGrunnlag svangerskapspengeGrunnlag = lagSVPGrunnlagMedTilrettelegging(førsteDagMedUtbetaling, BigDecimal.TEN);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(REFERANSE, lagBgGrunnlag(),
                BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag, svangerskapspengeGrunnlag);

        // Act
        var newInput = new Input(input, List.of());
        Optional<LocalDate> førsteSøktePermisjonsdag = mapper.utledStartdatoPermisjon(newInput , STP, yrkesaktivitet, ansettelsesperiode);

        // Assert
        assertThat(førsteSøktePermisjonsdag).isPresent();
        assertThat(førsteSøktePermisjonsdag.get()).isEqualTo(STP);
    }

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
        UtbetalingsgradArbeidsforholdDto tilretteleggingArbeidsforhold = new UtbetalingsgradArbeidsforholdDto(VIRKSOMHET, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetaling = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(førsteDagMedSøktYtelse, STP.plusMonths(3)), utbetalingsgrad);
        UtbetalingsgradPrAktivitetDto tilrettelegging = new UtbetalingsgradPrAktivitetDto(tilretteleggingArbeidsforhold, List.of(periodeMedUtbetaling));
        return new SvangerskapspengerGrunnlag(List.of(tilrettelegging));
    }

    private SvangerskapspengerGrunnlag lagSVPGrunnlagMedTilretteleggingIFlerePerioder(Map<Intervall, BigDecimal> periodeMedutbetalingsgrad) {
        UtbetalingsgradArbeidsforholdDto tilretteleggingArbeidsforhold = new UtbetalingsgradArbeidsforholdDto(VIRKSOMHET, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        List<PeriodeMedUtbetalingsgradDto> perioderMedUtbetaling = periodeMedutbetalingsgrad.entrySet().stream().map(e -> new PeriodeMedUtbetalingsgradDto(e.getKey(), e.getValue())).collect(Collectors.toList());
        UtbetalingsgradPrAktivitetDto tilrettelegging = new UtbetalingsgradPrAktivitetDto(tilretteleggingArbeidsforhold, perioderMedUtbetaling);
        return new SvangerskapspengerGrunnlag(List.of(tilrettelegging));
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
