package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.felles.frist.ArbeidsgiverRefusjonskravTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.frist.KravTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.frist.TreMånedersFristVurderer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
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
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.svp.MapRefusjonPerioderFraVLTilRegelSVP;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelGraderingUtbgradTest {

    public static final LocalDate STP = LocalDate.now();

    private final ArbeidsgiverRefusjonskravTjeneste arbeidsgiverRefusjonskravTjeneste = new ArbeidsgiverRefusjonskravTjeneste(
            new KravTjeneste(
                    new UnitTestLookupInstanceImpl<>(new TreMånedersFristVurderer())
            )
    );

    private final MapRefusjonPerioderFraVLTilRegelUtbgrad mapper = new MapRefusjonPerioderFraVLTilRegelSVP(arbeidsgiverRefusjonskravTjeneste);


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


        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(BigDecimal.TEN).medRefusjon(BigDecimal.TEN)
                .medArbeidsgiver(virksomhet)
                .medStartDatoPermisjon(LocalDate.of(2019, 10, 22)).build();

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(registerBuilder)
                .medInntektsmeldinger(im)
                .build();

        AktivitetDto tilretteleggingArbeidsforhold = new AktivitetDto(virksomhet, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        PeriodeMedUtbetalingsgradDto periodeMedUtbetaling = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(stp, LocalDate.of(2019, 10, 21)), BigDecimal.valueOf(100));
        PeriodeMedUtbetalingsgradDto periodeMedUtbetaling2 = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(LocalDate.of(2019, 10, 22), LocalDate.of(2020, 1, 19)), BigDecimal.valueOf(40));

        UtbetalingsgradPrAktivitetDto tilrettelegging = new UtbetalingsgradPrAktivitetDto(tilretteleggingArbeidsforhold,
                List.of(periodeMedUtbetaling, periodeMedUtbetaling2));
        SvangerskapspengerGrunnlag svangerskapspengerGrunnlag = new SvangerskapspengerGrunnlag(List.of(tilrettelegging));


        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build();
        BeregningsgrunnlagPeriodeDto.Builder bgperiode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(stp, TIDENES_ENDE)
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


        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(new KoblingReferanseMock(stp), bg,
                BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag, svangerskapspengerGrunnlag);

        // Act
        PeriodeModellRefusjon map = mapper.map(input, beregningsgrunnlagDto);

        // Assert
        assertThat(map.getArbeidsforholdOgInntektsmeldinger()).hasSize(1);
        ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding = map.getArbeidsforholdOgInntektsmeldinger().get(0);
        assertThat(arbeidsforholdOgInntektsmelding.getRefusjoner()).hasSize(2);
        Refusjonskrav refusjonskrav = arbeidsforholdOgInntektsmelding.getRefusjoner().get(0);
        assertThat(refusjonskrav.getPeriode().getFom()).isEqualTo(stp);

        Refusjonskrav opphør = arbeidsforholdOgInntektsmelding.getRefusjoner().get(1);
        assertThat(opphør.getMånedsbeløp()).isEqualTo(BigDecimal.ZERO);
        assertThat(opphør.getPeriode().getFom()).isEqualTo(LocalDate.of(2020, 1, 19).plusDays(1));
    }


}
