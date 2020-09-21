package no.nav.folketrygdloven.kalkulus.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulus.beregning.v1.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.beregning.v1.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;

class OmsorgspengeGrunnlagMapperTest {


    public static final LocalDate STP = LocalDate.of(2020, 4, 1);
    public static final String ARBEIDSGIVER_ORGNR = "174891324";

    @Test
    void skal_lage_omsorgspengegrunnlag_med_utbetalingsgrad_og_ta_hensyn_til_refusjonskravfrist() {
        // Arrange
        OmsorgspengerGrunnlag omsorgspengegrunnlag = lagOmsorgspengegrunnlag();
        KalkulatorInputDto input = new KalkulatorInputDto(new InntektArbeidYtelseGrunnlagDto(), new OpptjeningAktiviteterDto(List.of()), STP);
        leggTilRefdato(input);
        BeregningsgrunnlagGrunnlagEntitet bg = lagBG();

        // Act
        UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag = (UtbetalingsgradGrunnlag) OmsorgspengeGrunnlagMapper.mapOmsorgspengegrunnlag(input, Optional.of(bg), omsorgspengegrunnlag);

        // Assert
        var utbetalingsgradPrAktivitet = ytelsespesifiktGrunnlag.getUtbetalingsgradPrAktivitet();
        assertThat(utbetalingsgradPrAktivitet.size()).isEqualTo(1);
        var utbetalingsgradPrAktivitetDto = utbetalingsgradPrAktivitet.get(0);
        assertThat(utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().size()).isEqualTo(2);
        var perioder = utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad();

        assertThat(perioder.get(0).getPeriode().getFomDato()).isEqualTo(STP);
        assertThat(perioder.get(0).getPeriode().getTomDato()).isEqualTo(STP.plusMonths(9).minusDays(1));
        assertThat(perioder.get(0).getUtbetalingsgrad()).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(perioder.get(1).getPeriode().getFomDato()).isEqualTo(STP.plusMonths(9));
        assertThat(perioder.get(1).getPeriode().getTomDato()).isEqualTo(STP.plusMonths(12));
        assertThat(perioder.get(1).getUtbetalingsgrad()).isEqualByComparingTo(BigDecimal.valueOf(100));

    }

    @NotNull
    private OmsorgspengerGrunnlag lagOmsorgspengegrunnlag() {
        return new OmsorgspengerGrunnlag(List.of(
                    new UtbetalingsgradPrAktivitetDto(new UtbetalingsgradArbeidsforholdDto(
                            new Organisasjon(ARBEIDSGIVER_ORGNR),
                            null,
                            new UttakArbeidType("ORDINÆRT_ARBEID")
                    ),
                            List.of(new PeriodeMedUtbetalingsgradDto(new Periode(STP, STP.plusMonths(12)), BigDecimal.valueOf(100))))
            ));
    }

    private void leggTilRefdato(KalkulatorInputDto input) {
        RefusjonskravDatoDto refDato = new RefusjonskravDatoDto(
                new Organisasjon(ARBEIDSGIVER_ORGNR),
                STP,
                STP.plusMonths(12),
                true
        );
        input.medRefusjonskravDatoer(List.of(refDato));
    }

    private BeregningsgrunnlagGrunnlagEntitet lagBG() {
        return BeregningsgrunnlagGrunnlagBuilder.oppdatere(Optional.empty())
                    .medBeregningsgrunnlag(BeregningsgrunnlagEntitet.builder()
                            .medSkjæringstidspunkt(STP).build())
                    .medRegisterAktiviteter(BeregningAktivitetAggregatEntitet.builder()
                            .medSkjæringstidspunktOpptjening(STP)
                            .leggTilAktivitet(BeregningAktivitetEntitet.builder()
                                    .medPeriode(IntervallEntitet.fraOgMedTilOgMed(STP.minusMonths(10), STP.plusDays(2)))
                                    .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                    .medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR)).build())
                            .build()).build(1L, BeregningsgrunnlagTilstand.OPPRETTET);
    }
}
