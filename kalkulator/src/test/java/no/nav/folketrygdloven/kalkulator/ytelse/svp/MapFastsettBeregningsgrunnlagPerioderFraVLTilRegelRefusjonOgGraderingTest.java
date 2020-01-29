package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingMedUtbelingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    private BeregningsgrunnlagDto bg = lagBgMedEnPeriode();

    @Test
    public void skal_teste_endring_i_ytelse_svp() {
        // Skal hente gradering fra uttak fram til der oppgittfordeling starter
        String orgnr1 = "123";
        String orgnr2 = "321";
        LocalDate date = LocalDate.now();
        PeriodeMedUtbetalingsgradDto periode1 = lagPeriodeMedUtbetaling(date, BigDecimal.valueOf(100));
        PeriodeMedUtbetalingsgradDto periode2 = lagPeriodeMedUtbetaling(date.plusMonths(1), BigDecimal.valueOf(100));
        TilretteleggingMedUtbelingsgradDto tilrette1 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(orgnr1),
            periode1, periode2);

        PeriodeMedUtbetalingsgradDto periode3 = lagPeriodeMedUtbetaling(date, BigDecimal.valueOf(100));
        TilretteleggingMedUtbelingsgradDto tilrette2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(orgnr2),
            periode3);

        var tilrettelegginger = List.of(tilrette1, tilrette2);

        // Act
        var mapper = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingSvangerskapspenger();

        List<AndelGradering> andelGraderingList = mapper.mapTilrettelegginger(tilrettelegginger, bg);

        // Assert
        assertThat(andelGraderingList).hasSize(2);
        List<AndelGradering> andelArb1 = forArbeidsgiver(andelGraderingList, Arbeidsgiver.virksomhet(orgnr1), AktivitetStatus.ARBEIDSTAKER);
        List<AndelGradering> andelArb2 = forArbeidsgiver(andelGraderingList, Arbeidsgiver.virksomhet(orgnr2), AktivitetStatus.ARBEIDSTAKER);
        assertThat(andelArb1).hasSize(1);
        assertThat(andelArb2).hasSize(1);
        assertThat(andelArb1.get(0).getGraderinger()).hasSize(2);
        assertThat(andelArb2.get(0).getGraderinger()).hasSize(1);
        assertThat(andelArb1.get(0).getGraderinger()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFomDato()).isEqualTo(periode1.getPeriode().getFomDato());
            assertThat(gradering.getPeriode().getTomDato()).isEqualTo(periode1.getPeriode().getTomDato());
            assertThat(gradering.getArbeidstidProsent()).isEqualByComparingTo(periode1.getUtbetalingsgrad());
        });
        assertThat(andelArb1.get(0).getGraderinger()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFomDato()).isEqualTo(periode2.getPeriode().getFomDato());
            assertThat(gradering.getPeriode().getTomDato()).isEqualTo(periode2.getPeriode().getTomDato());
            assertThat(gradering.getArbeidstidProsent()).isEqualByComparingTo(periode2.getUtbetalingsgrad());
        });
        assertThat(andelArb2.get(0).getGraderinger()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFomDato()).isEqualTo(periode3.getPeriode().getFomDato());
            assertThat(gradering.getPeriode().getTomDato()).isEqualTo(periode3.getPeriode().getTomDato());
            assertThat(gradering.getArbeidstidProsent()).isEqualByComparingTo(periode3.getUtbetalingsgrad());
        });
    }

    @Test
    public void skal_ikke_lage_perioder_med_fom_før_skjæringstidspunkt() {
        // Skal hente gradering fra uttak fram til der oppgittfordeling starter
        String orgnr1 = "123";
        LocalDate date = LocalDate.now();
        PeriodeMedUtbetalingsgradDto periode1 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.minusDays(5), BigDecimal.valueOf(100));
        PeriodeMedUtbetalingsgradDto periode2 = lagPeriodeMedUtbetaling(date.plusDays(2), BigDecimal.valueOf(100));
        TilretteleggingMedUtbelingsgradDto tilrette1 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, Arbeidsgiver.virksomhet(orgnr1),
            periode1, periode2);

        var tilrettelegginger = List.of(tilrette1);

        // Act

        var mapper = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingSvangerskapspenger();

        List<AndelGradering> andelGraderingList = mapper.mapTilrettelegginger(tilrettelegginger, bg);

        // Assert
        assertThat(andelGraderingList).hasSize(1);
        List<AndelGradering> andelArb1 = forArbeidsgiver(andelGraderingList, Arbeidsgiver.virksomhet(orgnr1), AktivitetStatus.ARBEIDSTAKER);
        assertThat(andelArb1).hasSize(1);
        assertThat(andelArb1.get(0).getGraderinger()).hasSize(2);
        assertThat(andelArb1.get(0).getGraderinger()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT);
            assertThat(gradering.getPeriode().getTomDato()).isEqualTo(periode1.getPeriode().getTomDato());
            assertThat(gradering.getArbeidstidProsent()).isEqualByComparingTo(periode1.getUtbetalingsgrad());
        });
        assertThat(andelArb1.get(0).getGraderinger()).anySatisfy(gradering -> {
            assertThat(gradering.getPeriode().getFomDato()).isEqualTo(periode2.getPeriode().getFomDato());
            assertThat(gradering.getPeriode().getTomDato()).isEqualTo(periode2.getPeriode().getTomDato());
            assertThat(gradering.getArbeidstidProsent()).isEqualByComparingTo(periode2.getUtbetalingsgrad());
        });
    }

    private BeregningsgrunnlagDto lagBgMedEnPeriode() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();

        BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(bg);
        return bg;
    }

    private List<AndelGradering> forArbeidsgiver(List<AndelGradering> andelGraderingList, Arbeidsgiver arbeidsgiver, AktivitetStatus status) {
        return andelGraderingList.stream()
            .filter(ag -> Objects.equals(arbeidsgiver, ag.getArbeidsgiver()) && ag.getAktivitetStatus().equals(status))
            .collect(Collectors.toList());
    }

    private TilretteleggingMedUtbelingsgradDto lagTilretteleggingMedUtbelingsgrad(UttakArbeidType uttakArbeidType,
                                                                                  Arbeidsgiver arbeidsgiver,
                                                                                  PeriodeMedUtbetalingsgradDto... perioder) {
        var tilretteleggingArbeidsforhold = new TilretteleggingArbeidsforholdDto(arbeidsgiver, InternArbeidsforholdRefDto.nyRef(), uttakArbeidType);
        return new TilretteleggingMedUtbelingsgradDto(tilretteleggingArbeidsforhold, List.of(perioder));
    }

    private PeriodeMedUtbetalingsgradDto lagPeriodeMedUtbetaling(LocalDate skjæringstidspunkt, BigDecimal utbetalingsgrad) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(skjæringstidspunkt, skjæringstidspunkt.plusWeeks(1)), utbetalingsgrad);
    }

}
