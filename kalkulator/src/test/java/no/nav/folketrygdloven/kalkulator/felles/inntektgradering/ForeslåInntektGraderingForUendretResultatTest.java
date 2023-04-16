package no.nav.folketrygdloven.kalkulator.felles.inntektgradering;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

class ForeslåInntektGraderingForUendretResultatTest {

    @Test
    void skal_foreslå_inntekt_med_reduksjon() {

        var periodeBuilder = BeregningsgrunnlagPeriodeDto.ny();
        var tilkommet_arbeidsgiver = Arbeidsgiver.virksomhet("123455667");
        periodeBuilder.leggTilTilkommetInntekt(
                new TilkommetInntektDto(AktivitetStatus.ARBEIDSTAKER, tilkommet_arbeidsgiver,
                        InternArbeidsforholdRefDto.nullRef(),
                        null, null, null)
        );

        periodeBuilder.medBeregningsgrunnlagPeriode(LocalDate.now(), LocalDate.now());

        var ikke_yrkesaktiv_arbeidsgiver = Arbeidsgiver.virksomhet("389472321");
        var utbetalingsgrad_tilkommet = 50;
        var ny_utbetalingsgrad_ikke_yrkesaktiv = 100;
        var ytelsespesifiktGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(
                new UtbetalingsgradPrAktivitetDto(new AktivitetDto(
                        ikke_yrkesaktiv_arbeidsgiver, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.IKKE_YRKESAKTIV),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now()), BigDecimal.valueOf(ny_utbetalingsgrad_ikke_yrkesaktiv)))),
                new UtbetalingsgradPrAktivitetDto(new AktivitetDto(
                        tilkommet_arbeidsgiver, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now()), BigDecimal.valueOf(utbetalingsgrad_tilkommet)))))
        );
        var dataIkkeYrkesaktiv = new ForeslåInntektGraderingForUendretResultat.GraderingsdataPrAndel(
                false, false, BigDecimal.valueOf(240_000),
                UttakArbeidType.IKKE_YRKESAKTIV, BigDecimal.valueOf(ny_utbetalingsgrad_ikke_yrkesaktiv), BigDecimal.valueOf(utbetalingsgrad_tilkommet)
        );
        var dataTilkommet = new ForeslåInntektGraderingForUendretResultat.GraderingsdataPrAndel(
                true, false, null, null, null, null
        );
        var graderingsdataPrAndel = List.of(
                dataTilkommet,
                dataIkkeYrkesaktiv
        );

        // Act
        var tilkommetInntektDtos = ForeslåInntektGraderingForUendretResultat.foreslåFraGraderingsdata(
                periodeBuilder.build(),
                ytelsespesifiktGrunnlag,
                graderingsdataPrAndel
        );

        // Assert
        assertThat(tilkommetInntektDtos.size()).isEqualTo(1);
        assertThat(tilkommetInntektDtos.get(0).skalRedusereUtbetaling()).isTrue();
        assertThat(tilkommetInntektDtos.get(0).getTilkommetInntektPrÅr().compareTo(BigDecimal.valueOf(120_000))).isEqualTo(0);
        assertThat(tilkommetInntektDtos.get(0).getBruttoInntektPrÅr().compareTo(BigDecimal.valueOf(240_000))).isEqualTo(0);
    }
}
