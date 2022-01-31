package no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLTest.GRUNNBELØP;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

class OmfordelingUtenRefusjonskravTjenesteTest {

    public static final Arbeidsgiver ARBEIDSGIVER_FRA_START = Arbeidsgiver.virksomhet("546776324");
    public static final Arbeidsgiver TILKOMMET_ARBEIDSGIVER = Arbeidsgiver.virksomhet("89437598345");
    public static final Arbeidsgiver TILKOMMET_ARBEIDSGIVER2 = Arbeidsgiver.virksomhet("897453573");
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    @Test
    void skal_omfordele_ved_opphør_og_ny_aktivitet_i_senere_periode() {
        var bgBuilder = lagBGBuilder();
        var arbeidFraStartTom = SKJÆRINGSTIDSPUNKT.plusDays(10);
        var tilkommetArbeidFom = SKJÆRINGSTIDSPUNKT.plusDays(15);
        var bgFraStart = BigDecimal.valueOf(400_000);
        bgBuilder.leggTilBeregningsgrunnlagPeriode(lagPeriodeMedKunAndelFraStart(SKJÆRINGSTIDSPUNKT, arbeidFraStartTom, bgFraStart));
        bgBuilder.leggTilBeregningsgrunnlagPeriode(lagPeriodeMedKunAndelFraStart(arbeidFraStartTom.plusDays(1), tilkommetArbeidFom.minusDays(1), bgFraStart));
        bgBuilder.leggTilBeregningsgrunnlagPeriode(lagPeriodeMedTilkommetAndel(tilkommetArbeidFom, tilkommetArbeidFom.plusDays(15), bgFraStart));
        PleiepengerSyktBarnGrunnlag ytelsespesifiktGrunnlag = lagUtbetalingsgradGrunnlag(arbeidFraStartTom, tilkommetArbeidFom);

        // Act
        var omfordelt = OmfordelingUtenRefusjonskravTjeneste.omfordel(bgBuilder.build(), ytelsespesifiktGrunnlag);

        // Assert
        var periode1 = omfordelt.getBeregningsgrunnlagPerioder().get(0);
        var fraStartAndel = periode1.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(fraStartAndel.getFordeltPrÅr()).isNull();
        assertThat(fraStartAndel.getBruttoPrÅr()).isEqualTo(bgFraStart);

        var periode2 = omfordelt.getBeregningsgrunnlagPerioder().get(0);
        var fraStartAndel2 = periode2.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(fraStartAndel2.getFordeltPrÅr()).isNull();
        assertThat(fraStartAndel2.getBruttoPrÅr()).isCloseTo(bgFraStart, Offset.offset(BigDecimal.valueOf(0.00000001)));


        var periode3 = omfordelt.getBeregningsgrunnlagPerioder().get(2);
        var fraStartAndelPeriode3 = periode3.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(fraStartAndelPeriode3.getArbeidsgiver().get()).isEqualTo(ARBEIDSGIVER_FRA_START);
        assertThat(fraStartAndelPeriode3.getFordeltPrÅr()).isEqualTo(BigDecimal.ZERO);
        var tilkommetAndel = periode3.getBeregningsgrunnlagPrStatusOgAndelList().get(1);
        assertThat(tilkommetAndel.getArbeidsgiver().get()).isEqualTo(TILKOMMET_ARBEIDSGIVER);
        assertThat(tilkommetAndel.getFordeltPrÅr()).isCloseTo(bgFraStart, Offset.offset(BigDecimal.valueOf(0.00000001)));
    }

    @Test
    void skal_omfordele_ved_opphør_og_ny_aktivitet_i_samme_periode() {
        var bgBuilder = lagBGBuilder();
        var arbeidFraStartTom = SKJÆRINGSTIDSPUNKT.plusDays(10);
        var bgFraStart = BigDecimal.valueOf(400_000);
        bgBuilder.leggTilBeregningsgrunnlagPeriode(lagPeriodeMedKunAndelFraStart(SKJÆRINGSTIDSPUNKT, arbeidFraStartTom, bgFraStart));
        bgBuilder.leggTilBeregningsgrunnlagPeriode(lagPeriodeMedTilkommetAndel(SKJÆRINGSTIDSPUNKT.plusDays(11), SKJÆRINGSTIDSPUNKT.plusDays(15), bgFraStart));
        PleiepengerSyktBarnGrunnlag ytelsespesifiktGrunnlag = lagUtbetalingsgradGrunnlag(arbeidFraStartTom, arbeidFraStartTom.plusDays(1));

        // Act
        var omfordelt = OmfordelingUtenRefusjonskravTjeneste.omfordel(bgBuilder.build(), ytelsespesifiktGrunnlag);

        // Assert
        var periode1 = omfordelt.getBeregningsgrunnlagPerioder().get(0);
        var fraStartAndel = periode1.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(fraStartAndel.getFordeltPrÅr()).isNull();
        assertThat(fraStartAndel.getBruttoPrÅr()).isEqualTo(bgFraStart);

        var periode2 = omfordelt.getBeregningsgrunnlagPerioder().get(1);
        var fraStartAndelPeriode2 = periode2.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(fraStartAndelPeriode2.getArbeidsgiver().get()).isEqualTo(ARBEIDSGIVER_FRA_START);
        assertThat(fraStartAndelPeriode2.getFordeltPrÅr()).isEqualTo(BigDecimal.ZERO);
        var tilkommetAndel = periode2.getBeregningsgrunnlagPrStatusOgAndelList().get(1);
        assertThat(tilkommetAndel.getArbeidsgiver().get()).isEqualTo(TILKOMMET_ARBEIDSGIVER);
        assertThat(tilkommetAndel.getFordeltPrÅr()).isCloseTo(bgFraStart, Offset.offset(BigDecimal.valueOf(0.00000001)));
    }

    @Test
    void skal_omfordele_ved_opphør_og_to_nye_aktiviteter() {
        var bgBuilder = lagBGBuilder();
        var arbeidFraStartTom = SKJÆRINGSTIDSPUNKT.plusDays(10);
        var tilkommetArbeidFom = SKJÆRINGSTIDSPUNKT.plusDays(11);
        var bgFraStart = BigDecimal.valueOf(400_000);
        bgBuilder.leggTilBeregningsgrunnlagPeriode(lagPeriodeMedKunAndelFraStart(SKJÆRINGSTIDSPUNKT, arbeidFraStartTom, bgFraStart));
        bgBuilder.leggTilBeregningsgrunnlagPeriode(lagPeriodeMedToTilkomneAndeler(tilkommetArbeidFom, SKJÆRINGSTIDSPUNKT.plusDays(15), bgFraStart));
        PleiepengerSyktBarnGrunnlag ytelsespesifiktGrunnlag = lagUtbetalingsgradGrunnlagForToTilkomnne(arbeidFraStartTom, tilkommetArbeidFom, BigDecimal.valueOf(100), BigDecimal.valueOf(100));

        // Act
        var omfordelt = OmfordelingUtenRefusjonskravTjeneste.omfordel(bgBuilder.build(), ytelsespesifiktGrunnlag);

        // Assert
        var periode1 = omfordelt.getBeregningsgrunnlagPerioder().get(0);
        var fraStartAndel = periode1.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(fraStartAndel.getFordeltPrÅr()).isNull();
        assertThat(fraStartAndel.getBruttoPrÅr()).isEqualTo(bgFraStart);

        var periode2 = omfordelt.getBeregningsgrunnlagPerioder().get(1);
        var fraStartAndelPeriode2 = periode2.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(fraStartAndelPeriode2.getArbeidsgiver().get()).isEqualTo(ARBEIDSGIVER_FRA_START);
        assertThat(fraStartAndelPeriode2.getFordeltPrÅr()).isEqualTo(BigDecimal.ZERO);
        var tilkommetAndel = periode2.getBeregningsgrunnlagPrStatusOgAndelList().get(1);
        assertThat(tilkommetAndel.getArbeidsgiver().get()).isEqualTo(TILKOMMET_ARBEIDSGIVER);
        assertThat(tilkommetAndel.getFordeltPrÅr()).isCloseTo(BigDecimal.valueOf(200_000), Offset.offset(BigDecimal.valueOf(0.00000001)));
        var tilkommetAndel2 = periode2.getBeregningsgrunnlagPrStatusOgAndelList().get(2);
        assertThat(tilkommetAndel2.getArbeidsgiver().get()).isEqualTo(TILKOMMET_ARBEIDSGIVER2);
        assertThat(tilkommetAndel2.getFordeltPrÅr()).isCloseTo(BigDecimal.valueOf(200_000), Offset.offset(BigDecimal.valueOf(0.00000001)));
    }

    @Test
    void skal_ikke_omfordele_ved_opphør_og_to_nye_aktiviteter_der_ikke_alle_har_full_utbetalingsgrad() {
        var bgBuilder = lagBGBuilder();
        var arbeidFraStartTom = SKJÆRINGSTIDSPUNKT.plusDays(10);
        var tilkommetArbeidFom = SKJÆRINGSTIDSPUNKT.plusDays(11);
        var bgFraStart = BigDecimal.valueOf(400_000);
        bgBuilder.leggTilBeregningsgrunnlagPeriode(lagPeriodeMedKunAndelFraStart(SKJÆRINGSTIDSPUNKT, arbeidFraStartTom, bgFraStart));
        bgBuilder.leggTilBeregningsgrunnlagPeriode(lagPeriodeMedToTilkomneAndeler(tilkommetArbeidFom, SKJÆRINGSTIDSPUNKT.plusDays(15), bgFraStart));
        PleiepengerSyktBarnGrunnlag ytelsespesifiktGrunnlag = lagUtbetalingsgradGrunnlagForToTilkomnne(arbeidFraStartTom,
                tilkommetArbeidFom, BigDecimal.valueOf(100), BigDecimal.valueOf(50));

        // Act
        var omfordelt = OmfordelingUtenRefusjonskravTjeneste.omfordel(bgBuilder.build(), ytelsespesifiktGrunnlag);

        // Assert
        var periode1 = omfordelt.getBeregningsgrunnlagPerioder().get(0);
        var fraStartAndel = periode1.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(fraStartAndel.getFordeltPrÅr()).isNull();
        assertThat(fraStartAndel.getBruttoPrÅr()).isEqualTo(bgFraStart);

        var periode2 = omfordelt.getBeregningsgrunnlagPerioder().get(1);
        var fraStartAndelPeriode2 = periode2.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(fraStartAndelPeriode2.getFordeltPrÅr()).isNull();
        assertThat(fraStartAndelPeriode2.getBruttoPrÅr()).isEqualTo(bgFraStart);
        var tilkommetAndel = periode2.getBeregningsgrunnlagPrStatusOgAndelList().get(1);
        assertThat(tilkommetAndel.getArbeidsgiver().get()).isEqualTo(TILKOMMET_ARBEIDSGIVER);
        assertThat(tilkommetAndel.getFordeltPrÅr()).isNull();
        var tilkommetAndel2 = periode2.getBeregningsgrunnlagPrStatusOgAndelList().get(2);
        assertThat(tilkommetAndel2.getArbeidsgiver().get()).isEqualTo(TILKOMMET_ARBEIDSGIVER2);
        assertThat(tilkommetAndel2.getFordeltPrÅr()).isNull();
    }


    private BeregningsgrunnlagPeriodeDto.Builder lagPeriodeMedTilkommetAndel(LocalDate fom, LocalDate tom, BigDecimal bgFraStart) {
        BeregningsgrunnlagPeriodeDto.Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.builder();
        periodeBuilder.medBeregningsgrunnlagPeriode(fom, tom);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder1 = lagArbeidFraStart(bgFraStart);
        periodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder1);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilde2 = lagTilkommetArbeid(TILKOMMET_ARBEIDSGIVER);
        periodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilde2);
        return periodeBuilder;
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagPeriodeMedToTilkomneAndeler(LocalDate fom, LocalDate tom, BigDecimal bgFraStart) {
        BeregningsgrunnlagPeriodeDto.Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.builder();
        periodeBuilder.medBeregningsgrunnlagPeriode(fom, tom);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder1 = lagArbeidFraStart(bgFraStart);
        periodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder1);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilde2 = lagTilkommetArbeid(TILKOMMET_ARBEIDSGIVER);
        periodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilde2);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilde3 = lagTilkommetArbeid(TILKOMMET_ARBEIDSGIVER2);
        periodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilde3);
        return periodeBuilder;
    }


    private PleiepengerSyktBarnGrunnlag lagUtbetalingsgradGrunnlag(LocalDate arbeidFraStartTom, LocalDate tilkommetArbeidFom) {
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(
                new UtbetalingsgradPrAktivitetDto(
                        new UtbetalingsgradArbeidsforholdDto(ARBEIDSGIVER_FRA_START, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, arbeidFraStartTom), BigDecimal.valueOf(50)))),
                new UtbetalingsgradPrAktivitetDto(
                        new UtbetalingsgradArbeidsforholdDto(TILKOMMET_ARBEIDSGIVER, InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(tilkommetArbeidFom, SKJÆRINGSTIDSPUNKT.plusDays(15)), BigDecimal.valueOf(100))))
        ));
        return ytelsespesifiktGrunnlag;
    }


    private PleiepengerSyktBarnGrunnlag lagUtbetalingsgradGrunnlagForToTilkomnne(LocalDate arbeidFraStartTom, LocalDate tilkommetArbeidFom, BigDecimal tilkommetUtbetalingsgrad1, BigDecimal tilkommetUtbetalingsgrad2) {
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(
                new UtbetalingsgradPrAktivitetDto(
                        new UtbetalingsgradArbeidsforholdDto(ARBEIDSGIVER_FRA_START, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, arbeidFraStartTom), BigDecimal.valueOf(50)))),
                new UtbetalingsgradPrAktivitetDto(
                        new UtbetalingsgradArbeidsforholdDto(TILKOMMET_ARBEIDSGIVER2, InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(tilkommetArbeidFom, SKJÆRINGSTIDSPUNKT.plusDays(15)), tilkommetUtbetalingsgrad2))),
                new UtbetalingsgradPrAktivitetDto(
                        new UtbetalingsgradArbeidsforholdDto(TILKOMMET_ARBEIDSGIVER, InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(tilkommetArbeidFom, SKJÆRINGSTIDSPUNKT.plusDays(15)), tilkommetUtbetalingsgrad1)))
        ));
        return ytelsespesifiktGrunnlag;
    }


    private BeregningsgrunnlagPeriodeDto.Builder lagPeriodeMedKunAndelFraStart(LocalDate fom, LocalDate tom, BigDecimal bgFraStart) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.builder();
        builder.medBeregningsgrunnlagPeriode(fom, tom);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagArbeidFraStart(bgFraStart);
        builder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder);
        return builder;
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagArbeidFraStart(BigDecimal bg) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medKilde(AndelKilde.PROSESS_START)
                .medBeregnetPrÅr(bg)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(ARBEIDSGIVER_FRA_START)
                        .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1)));
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagTilkommetArbeid(Arbeidsgiver tilkommetArbeidsgiver) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medKilde(AndelKilde.PROSESS_PERIODISERING)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(tilkommetArbeidsgiver)
                        .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.plusDays(1)));
    }


    private BeregningsgrunnlagDto.Builder lagBGBuilder() {
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7));
        return beregningsgrunnlagBuilder;
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagBeregningsgrunnlagPerioderBuilder(LocalDate fom, LocalDate tom,
                                                                                      Map<String, BigDecimal> orgnrs) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.builder();
        for (String orgnr : orgnrs.keySet()) {
            Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
            BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                    .medBeregnetPrÅr(orgnrs.get(orgnr))
                    .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                            .medArbeidsgiver(arbeidsgiver)
                            .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1)));
            builder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder);
        }
        return builder
                .medBeregningsgrunnlagPeriode(fom, tom);
    }


}
