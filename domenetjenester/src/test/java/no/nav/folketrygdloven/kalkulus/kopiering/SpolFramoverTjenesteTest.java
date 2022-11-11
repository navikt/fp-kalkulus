package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT_INN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class SpolFramoverTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 4);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000L);
    private static final String ORG_NUMMER = "974652269";

    @Test
    void skal_ikke_spole_framover_uten_avklaringsbehov() {
        // Arrange
        BeregningsgrunnlagTilstand tilstandFraSteg = OPPDATERT_MED_REFUSJON_OG_GRADERING;
        BeregningsgrunnlagTilstand tilstandFraStegUt = FASTSATT_INN;
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt = lagGrunnlag(tilstandFraStegUt, SKJÆRINGSTIDSPUNKT);

        // Act
        var spolFramGrunnlag = SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(List.of(), nyttGrunnlag,
                Optional.of(forrigeGrunnlagFraSteg),
                Optional.of(forrigeGrunnlagFraStegUt)
        );

        // Assert
        assertThat(spolFramGrunnlag).isEmpty();
    }

    @Test
    void skal_ikkje_spole_framover_utan_grunnlag() {
        // Arrange
        BeregningsgrunnlagTilstand tilstandFraSteg = OPPDATERT_MED_REFUSJON_OG_GRADERING;
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BEREGNINGSGRUNNLAG));

        // Act
        var spolFramGrunnlag = SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(avklaringsbehov, nyttGrunnlag,
                Optional.of(forrigeGrunnlagFraSteg),
                Optional.empty()
        );

        // Assert
        assertThat(spolFramGrunnlag).isEmpty();
    }

    @Test
    void skal_ikkje_spole_framover_med_diff_i_grunnlag() {
        // Arrange
        BeregningsgrunnlagTilstand tilstandFraSteg = OPPDATERT_MED_REFUSJON_OG_GRADERING;
        BeregningsgrunnlagTilstand tilstandFraStegUt = FASTSATT_INN;
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT.minusDays(1));
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt = lagGrunnlag(tilstandFraStegUt, SKJÆRINGSTIDSPUNKT.minusDays(1));
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BEREGNINGSGRUNNLAG));

        // Act
        var spolFramGrunnlag = SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(avklaringsbehov, nyttGrunnlag,
                Optional.of(forrigeGrunnlagFraSteg),
                Optional.of(forrigeGrunnlagFraStegUt)
        );

        // Assert
        assertThat(spolFramGrunnlag).isEmpty();
    }

    @Test
    void skal_spole_framover_uten_diff_i_grunnlag() {
        // Arrange
        BeregningsgrunnlagTilstand tilstandFraSteg = OPPDATERT_MED_REFUSJON_OG_GRADERING;
        BeregningsgrunnlagTilstand tilstandFraStegUt = FASTSATT_INN;
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt = lagGrunnlag(tilstandFraStegUt, SKJÆRINGSTIDSPUNKT.minusDays(1));
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BEREGNINGSGRUNNLAG));

        // Act
        var spolFramGrunnlag = SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(avklaringsbehov, nyttGrunnlag,
                Optional.of(forrigeGrunnlagFraSteg),
                Optional.of(forrigeGrunnlagFraStegUt)
        );

        // Assert
        assertThat(spolFramGrunnlag).isPresent();
        BeregningsgrunnlagGrunnlagDto gr = spolFramGrunnlag.get();
        assertThat(gr.getBeregningsgrunnlag().get().getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT.minusDays(1));
    }

    @Test
    void skal_kopiere_like_perioder_ved_forlengelse() {
        // Arrange
        BeregningsgrunnlagTilstand tilstandFraSteg = OPPDATERT_MED_REFUSJON_OG_GRADERING;
        BeregningsgrunnlagTilstand tilstandFraStegUt = FASTSATT_INN;
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = lagGrunnlagUtenPerioder(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg = lagGrunnlagUtenPerioder(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt = lagGrunnlagUtenPerioder(tilstandFraStegUt, SKJÆRINGSTIDSPUNKT);

        // Legger til første periode i alle grunnlag
        BigDecimal fordeltFørstePeriode = BigDecimal.valueOf(100_000);
        lagAndel(lagPeriode(nyttGrunnlag.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(2)), null);
        lagAndel(lagPeriode(forrigeGrunnlagFraSteg.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(2)), null);
        lagAndel(lagPeriode(forrigeGrunnlagFraStegUt.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(2)), fordeltFørstePeriode);

        // Legger til andre periode i alle grunnlag
        BigDecimal fordeltAndrePeriode = BigDecimal.valueOf(200_000);
        lagAndel(lagPeriode(nyttGrunnlag.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT.plusDays(3), SKJÆRINGSTIDSPUNKT.plusDays(5)), null);
        lagAndel(lagPeriode(forrigeGrunnlagFraSteg.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT.plusDays(3), SKJÆRINGSTIDSPUNKT.plusDays(5)), null);
        lagAndel(lagPeriode(forrigeGrunnlagFraStegUt.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT.plusDays(3), SKJÆRINGSTIDSPUNKT.plusDays(5)), fordeltAndrePeriode);

        // Tredje periode er ulik
        lagAndel(lagPeriode(nyttGrunnlag.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT.plusDays(6), SKJÆRINGSTIDSPUNKT.plusDays(7)), null);
        lagAndel(lagPeriode(forrigeGrunnlagFraSteg.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT.plusDays(6), TIDENES_ENDE), null);
        lagAndel(lagPeriode(forrigeGrunnlagFraStegUt.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT.plusDays(6), TIDENES_ENDE), null);

        // Fjerde periode eksisterer kun i siste grunnlag
        lagAndel(lagPeriode(nyttGrunnlag.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT.plusDays(8), TIDENES_ENDE), null);

        List<BeregningAvklaringsbehovResultat> avklaringsbehov = List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BEREGNINGSGRUNNLAG));

        // Act
        var spolFramGrunnlag = SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(avklaringsbehov, nyttGrunnlag,
                Optional.of(forrigeGrunnlagFraSteg),
                Optional.of(forrigeGrunnlagFraStegUt)
        );

        // Assert
        assertThat(spolFramGrunnlag).isPresent();
        BeregningsgrunnlagGrunnlagDto gr = spolFramGrunnlag.get();
        BeregningsgrunnlagDto bg = gr.getBeregningsgrunnlag().get();
        assertThat(bg.getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(4);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getPeriode().getFomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getPeriode().getTomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusDays(2));
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0).getFordeltPrÅr()).isEqualTo(fordeltFørstePeriode);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getPeriode().getFomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusDays(3));
        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getPeriode().getTomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusDays(5));
        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().get(0).getFordeltPrÅr()).isEqualTo(fordeltAndrePeriode);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(2).getPeriode().getFomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusDays(6));
        assertThat(bg.getBeregningsgrunnlagPerioder().get(2).getPeriode().getTomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusDays(7));
        assertThat(bg.getBeregningsgrunnlagPerioder().get(2).getBeregningsgrunnlagPrStatusOgAndelList().get(0).getFordeltPrÅr()).isNull();

        assertThat(bg.getBeregningsgrunnlagPerioder().get(3).getPeriode().getFomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusDays(8));
        assertThat(bg.getBeregningsgrunnlagPerioder().get(3).getPeriode().getTomDato()).isEqualTo(TIDENES_ENDE);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(3).getBeregningsgrunnlagPrStatusOgAndelList().get(0).getFordeltPrÅr()).isNull();
    }



    @Test
    void skal_kopiere_kun_forlenget_periode() {
        // Arrange
        BeregningsgrunnlagTilstand tilstandFraSteg = FASTSATT;
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = lagGrunnlagUtenPerioder(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg = lagGrunnlagUtenPerioder(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);

        BigDecimal fordeltFørstePeriode = BigDecimal.valueOf(100_000);
        lagAndel(lagPeriode(forrigeGrunnlagFraSteg.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(5)), fordeltFørstePeriode);

        BigDecimal fordeltAndrePeriode = BigDecimal.valueOf(200_000);
        lagAndel(lagPeriode(nyttGrunnlag.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(2)), BigDecimal.valueOf(50_000));
        lagAndel(lagPeriode(nyttGrunnlag.getBeregningsgrunnlag().get(), SKJÆRINGSTIDSPUNKT.plusDays(3), SKJÆRINGSTIDSPUNKT.plusDays(5)), fordeltAndrePeriode);

        // Act
        var gr = SpolFramoverTjeneste.kopierPerioderFraForrigeGrunnlag(nyttGrunnlag,
                forrigeGrunnlagFraSteg,
                Set.of(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(2)))
        );

        // Assert
        BeregningsgrunnlagDto bg = gr.getBeregningsgrunnlag().get();
        assertThat(bg.getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getPeriode().getFomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getPeriode().getTomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusDays(2));
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0).getFordeltPrÅr()).isEqualTo(fordeltFørstePeriode);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getPeriode().getFomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusDays(3));
        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getPeriode().getTomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT.plusDays(5));
        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().get(0).getFordeltPrÅr()).isEqualTo(fordeltAndrePeriode);
    }




    private BeregningsgrunnlagGrunnlagDto lagGrunnlag(BeregningsgrunnlagTilstand tilstand, LocalDate skjæringstidspunkt) {
        var bg = lagBeregningsgrunnlag(skjæringstidspunkt);
        return BeregningsgrunnlagGrunnlagDtoBuilder.nytt()
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMed(skjæringstidspunkt.minusMonths(12)))
                                .medArbeidsgiver(Arbeidsgiver.virksomhet(ORG_NUMMER))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .build())
                        .medSkjæringstidspunktOpptjening(skjæringstidspunkt).build())
                        .medBeregningsgrunnlag(bg)
                                .build(tilstand);
    }

    private BeregningsgrunnlagGrunnlagDto lagGrunnlagUtenPerioder(BeregningsgrunnlagTilstand tilstand, LocalDate skjæringstidspunkt) {
        var bg = lagBeregningsgrunnlagUtenPerioder();
        return BeregningsgrunnlagGrunnlagDtoBuilder.nytt()
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMed(skjæringstidspunkt.minusMonths(12)))
                                .medArbeidsgiver(Arbeidsgiver.virksomhet(ORG_NUMMER))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .build())
                        .medSkjæringstidspunktOpptjening(skjæringstidspunkt).build())
                .medBeregningsgrunnlag(bg)
                .build(tilstand);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(LocalDate skjæringstidspunkt) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .build();

        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT,TIDENES_ENDE);

        lagAndel(periode, null);

        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagUtenPerioder() {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();
        return beregningsgrunnlag;
    }


    private BeregningsgrunnlagPeriodeDto lagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fomDato, LocalDate tomDato) {
        return BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(fomDato, tomDato)
                .build(beregningsgrunnlag);
    }

    private void lagAndel(BeregningsgrunnlagPeriodeDto periode, BigDecimal fordeltPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ORG_NUMMER)))
                .medBeregnetPrÅr(BigDecimal.valueOf(100_000))
                .medFordeltPrÅr(fordeltPrÅr)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                        .medKilde(AndelKilde.PROSESS_START)
                                .build(periode);
    }
}
