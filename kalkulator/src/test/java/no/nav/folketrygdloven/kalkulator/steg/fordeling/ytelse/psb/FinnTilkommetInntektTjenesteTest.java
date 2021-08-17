package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
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
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class FinnTilkommetInntektTjenesteTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusMonths(1).withDayOfMonth(10);
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("31564237482");
    public static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet("09347858734");
    private final FinnTilkommetInntektTjeneste tjeneste = new FinnTilkommetInntektTjeneste();


    @Test
    void skal_ikke_finne_en_aktivitet_uten_tilkommet_inntekt() {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayGrunnlag(Optional.empty(), Optional.empty());
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(List.of(ARBEIDSGIVER));

        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = lagUtbetalingsgradperiode(SKJÆRINGSTIDSPUNKT.plusMonths(1));
        List<AktivitetDto> aktivitetDtos = tjeneste.finnAktiviteterMedTilkommetInntekt(beregningsgrunnlag, iayGrunnlag, utbetalingsgradPrAktivitet);

        assertThat(aktivitetDtos).isEmpty();
    }

    @Test
    void skal_finne_en_aktivitet_for_tilkommet_inntekt() {
        YrkesaktivitetDtoBuilder tilkommetYrkesaktivitet = lagYrkesaktivitet(ARBEIDSGIVER2, SKJÆRINGSTIDSPUNKT.plusMonths(1));
        InntektDtoBuilder tilkommetInntekt = lagInntektDto(ARBEIDSGIVER2,
                List.of(lagInntektspost(
                        SKJÆRINGSTIDSPUNKT.plusMonths(1).withDayOfMonth(1),
                        SKJÆRINGSTIDSPUNKT.plusMonths(2).withDayOfMonth(1).minusDays(1),
                        30000))
        );
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayGrunnlag(Optional.of(tilkommetYrkesaktivitet), Optional.of(tilkommetInntekt));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(List.of(ARBEIDSGIVER));
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = lagUtbetalingsgradperiode(SKJÆRINGSTIDSPUNKT.plusMonths(2).withDayOfMonth(1).minusDays(1));

        List<AktivitetDto> aktivitetDtos = tjeneste.finnAktiviteterMedTilkommetInntekt(beregningsgrunnlag, iayGrunnlag, utbetalingsgradPrAktivitet);

        assertThat(aktivitetDtos).hasSize(1);
        assertThat(aktivitetDtos.get(0).getInntekter()).hasSize(1);
        assertThat(aktivitetDtos.get(0).getYrkesaktivitetDto().getArbeidsgiver()).isEqualTo(ARBEIDSGIVER2);
    }


    @Test
    void skal_ikke_finne_en_aktivitet_for_tilkommet_inntekt_utenfor_søkt_periode() {
        YrkesaktivitetDtoBuilder tilkommetYrkesaktivitet = lagYrkesaktivitet(ARBEIDSGIVER2, SKJÆRINGSTIDSPUNKT.plusMonths(1));
        InntektDtoBuilder tilkommetInntekt = lagInntektDto(ARBEIDSGIVER2,
                List.of(lagInntektspost(
                        SKJÆRINGSTIDSPUNKT.plusMonths(1).withDayOfMonth(1),
                        SKJÆRINGSTIDSPUNKT.plusMonths(2).withDayOfMonth(1).minusDays(1),
                        30000))
        );
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayGrunnlag(Optional.of(tilkommetYrkesaktivitet), Optional.of(tilkommetInntekt));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(List.of(ARBEIDSGIVER));
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = lagUtbetalingsgradperiode(SKJÆRINGSTIDSPUNKT.plusMonths(1).withDayOfMonth(1).minusDays(1));

        List<AktivitetDto> aktivitetDtos = tjeneste.finnAktiviteterMedTilkommetInntekt(beregningsgrunnlag, iayGrunnlag, utbetalingsgradPrAktivitet);

        assertThat(aktivitetDtos).isEmpty();
    }


    @Test
    void skal_finne_en_aktivitet_for_tilkommet_inntekt_på_skjæringstidspunktet() {
        YrkesaktivitetDtoBuilder tilkommetYrkesaktivitet = lagYrkesaktivitet(ARBEIDSGIVER2, SKJÆRINGSTIDSPUNKT.plusMonths(1));
        InntektDtoBuilder tilkommetInntekt = lagInntektDto(ARBEIDSGIVER2,
                List.of(lagInntektspost(
                        SKJÆRINGSTIDSPUNKT.withDayOfMonth(1),
                        SKJÆRINGSTIDSPUNKT.plusMonths(1).withDayOfMonth(1).minusDays(1),
                        30000))
        );
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayGrunnlag(Optional.of(tilkommetYrkesaktivitet), Optional.of(tilkommetInntekt));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(List.of(ARBEIDSGIVER));
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = lagUtbetalingsgradperiode(SKJÆRINGSTIDSPUNKT.plusMonths(2).withDayOfMonth(1).minusDays(1));

        List<AktivitetDto> aktivitetDtos = tjeneste.finnAktiviteterMedTilkommetInntekt(beregningsgrunnlag, iayGrunnlag, utbetalingsgradPrAktivitet);

        assertThat(aktivitetDtos).hasSize(1);
        assertThat(aktivitetDtos.get(0).getInntekter()).hasSize(1);
        assertThat(aktivitetDtos.get(0).getYrkesaktivitetDto().getArbeidsgiver()).isEqualTo(ARBEIDSGIVER2);
    }

    @Test
    void skal_ikke_finne_en_aktivitet_for_tilkommet_inntekt_som_har_andel_i_beregningsgrunnlaget_fra_før() {
        YrkesaktivitetDtoBuilder tilkommetYrkesaktivitet = lagYrkesaktivitet(ARBEIDSGIVER2, SKJÆRINGSTIDSPUNKT.plusMonths(1));
        InntektDtoBuilder tilkommetInntekt = lagInntektDto(ARBEIDSGIVER2,
                List.of(lagInntektspost(
                        SKJÆRINGSTIDSPUNKT.withDayOfMonth(1),
                        SKJÆRINGSTIDSPUNKT.plusMonths(1).withDayOfMonth(1).minusDays(1),
                        30000))
        );
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayGrunnlag(Optional.of(tilkommetYrkesaktivitet), Optional.of(tilkommetInntekt));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(List.of(ARBEIDSGIVER, ARBEIDSGIVER2));
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = lagUtbetalingsgradperiode(SKJÆRINGSTIDSPUNKT.plusMonths(2).withDayOfMonth(1).minusDays(1));

        List<AktivitetDto> aktivitetDtos = tjeneste.finnAktiviteterMedTilkommetInntekt(beregningsgrunnlag, iayGrunnlag, utbetalingsgradPrAktivitet);

        assertThat(aktivitetDtos).isEmpty();
    }

    private List<UtbetalingsgradPrAktivitetDto> lagUtbetalingsgradperiode(LocalDate sisteSøkteDato) {
        UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforhold = new UtbetalingsgradArbeidsforholdDto(ARBEIDSGIVER, InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        PeriodeMedUtbetalingsgradDto søktPeriode = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, sisteSøkteDato), BigDecimal.TEN);
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = List.of(new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforhold, List.of(søktPeriode)));
        return utbetalingsgradPrAktivitet;
    }

    private InntektArbeidYtelseGrunnlagDto lagIayGrunnlag(Optional<YrkesaktivitetDtoBuilder> tilkommetAktivitet, Optional<InntektDtoBuilder> tilkommetInntekt) {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                        .leggTilAktørArbeid(lagAktørArbeid(tilkommetAktivitet))
                        .leggTilAktørInntekt(lagAktørInntekt(tilkommetInntekt)))
                        .build();
        return iayGrunnlag;
    }

    private InntektDtoBuilder lagInntektDto(Arbeidsgiver arbeidsgiver, List<InntektspostDtoBuilder> inntektsposter) {
        InntektDtoBuilder inntektDtoBuilder = InntektDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING);
        inntektsposter.forEach(inntektDtoBuilder::leggTilInntektspost);
        return inntektDtoBuilder;
    }

    private YrkesaktivitetDtoBuilder lagYrkesaktivitet(Arbeidsgiver arbeidsgiver, LocalDate localDate) {
        return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                        .medErAnsettelsesPeriode(true)
                        .medPeriode(Intervall.fraOgMed(localDate)));
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(List<Arbeidsgiver> arbeidsgivere) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE)
                .build(beregningsgrunnlag);

        arbeidsgivere.forEach(a -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT.withDayOfMonth(1).minusDays(1))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(a))
                .medBeregnetPrÅr(BigDecimal.valueOf(400_000))
                .build(periode));
        return beregningsgrunnlag;
    }

    private InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder lagAktørArbeid(Optional<YrkesaktivitetDtoBuilder> tilkommetAktivitet) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .leggTilYrkesaktivitet(lagYrkesaktivitet(ARBEIDSGIVER, SKJÆRINGSTIDSPUNKT)
                );
        tilkommetAktivitet.ifPresent(aktørArbeidBuilder::leggTilYrkesaktivitet);
        return aktørArbeidBuilder;
    }

    private InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder lagAktørInntekt(Optional<InntektDtoBuilder> tilkommetInntekt) {
        var inntektsposter = List.of(lagInntektspost(
                SKJÆRINGSTIDSPUNKT.minusMonths(3).withDayOfMonth(1),
                SKJÆRINGSTIDSPUNKT.minusMonths(2).withDayOfMonth(1).minusDays(1),
                30000),
                lagInntektspost(
                        SKJÆRINGSTIDSPUNKT.minusMonths(2).withDayOfMonth(1),
                        SKJÆRINGSTIDSPUNKT.minusMonths(1).withDayOfMonth(1).minusDays(1),
                        30000),
                lagInntektspost(
                        SKJÆRINGSTIDSPUNKT.minusMonths(1).withDayOfMonth(1),
                        SKJÆRINGSTIDSPUNKT.withDayOfMonth(1).minusDays(1),
                        30000),
                lagInntektspost(
                        SKJÆRINGSTIDSPUNKT.withDayOfMonth(1),
                        SKJÆRINGSTIDSPUNKT.plusMonths(1).withDayOfMonth(1).minusDays(1),
                        30000),
                lagInntektspost(
                        SKJÆRINGSTIDSPUNKT.plusMonths(1).withDayOfMonth(1),
                        SKJÆRINGSTIDSPUNKT.plusMonths(2).withDayOfMonth(1).minusDays(1),
                        30000));
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty())
                .leggTilInntekt(lagInntektDto(ARBEIDSGIVER, inntektsposter));
        tilkommetInntekt.ifPresent(aktørInntekt::leggTilInntekt);
        return aktørInntekt;
    }

    private InntektspostDtoBuilder lagInntektspost(LocalDate fraOgMed, LocalDate tilOgMed, int inntekt) {
        return InntektspostDtoBuilder.ny()
                .medBeløp(BigDecimal.valueOf(inntekt))
                .medPeriode(fraOgMed, tilOgMed)
                .medInntektspostType(InntektspostType.LØNN);
    }

}
