package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.felles.jpa.AbstractIntervall.TIDENES_ENDE;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT_INN;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.KOFAKBER_UT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

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
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
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
    private static final Beløp GRUNNBELØP = Beløp.fra(90000L);
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
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BG));

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
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BG));

        // Act
        var spolFramGrunnlag = SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(avklaringsbehov, nyttGrunnlag,
            Optional.of(forrigeGrunnlagFraSteg),
            Optional.of(forrigeGrunnlagFraStegUt)
        );

        // Assert
        assertThat(spolFramGrunnlag).isEmpty();
    }

    @Test
    void skal_ikke_spole_hvis_forrige_steg_ut_grunnlag_har_andre_fakta_tilfeller() {
        // Arrange
        BeregningsgrunnlagTilstand tilstandFraSteg = OPPDATERT_MED_ANDELER;
        BeregningsgrunnlagTilstand tilstandFraStegUt = KOFAKBER_UT;
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt = lagGrunnlag(tilstandFraStegUt, SKJÆRINGSTIDSPUNKT.minusDays(1), FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT);
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.OVST_INNTEKT));

        // Act
        var spolFramGrunnlag = SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(avklaringsbehov, nyttGrunnlag,
            Optional.of(forrigeGrunnlagFraSteg),
            Optional.of(forrigeGrunnlagFraStegUt)
        );

        // Assert
        assertThat(spolFramGrunnlag).isPresent();
        BeregningsgrunnlagGrunnlagDto gr = spolFramGrunnlag.get();
        assertThat(gr.getBeregningsgrunnlagHvisFinnes().get().getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT.minusDays(1));
    }

    @Test
    void skal_spole_framover_uten_diff_i_grunnlag() {
        // Arrange
        BeregningsgrunnlagTilstand tilstandFraSteg = OPPDATERT_MED_REFUSJON_OG_GRADERING;
        BeregningsgrunnlagTilstand tilstandFraStegUt = FASTSATT_INN;
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg = lagGrunnlag(tilstandFraSteg, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt = lagGrunnlag(tilstandFraStegUt, SKJÆRINGSTIDSPUNKT.minusDays(1));
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BG));

        // Act
        var spolFramGrunnlag = SpolFramoverTjeneste.finnGrunnlagDetSkalSpolesTil(avklaringsbehov, nyttGrunnlag,
                Optional.of(forrigeGrunnlagFraSteg),
                Optional.of(forrigeGrunnlagFraStegUt)
        );

        // Assert
        assertThat(spolFramGrunnlag).isPresent();
        BeregningsgrunnlagGrunnlagDto gr = spolFramGrunnlag.get();
        assertThat(gr.getBeregningsgrunnlagHvisFinnes().get().getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT.minusDays(1));
    }

    private BeregningsgrunnlagGrunnlagDto lagGrunnlag(BeregningsgrunnlagTilstand tilstand, LocalDate skjæringstidspunkt, FaktaOmBeregningTilfelle... tilfeller) {
        var bg = lagBeregningsgrunnlag(skjæringstidspunkt, tilfeller);
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

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(LocalDate skjæringstidspunkt, FaktaOmBeregningTilfelle... tilfeller) {
        var tilfellerListe = List.of(tilfeller);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .leggTilFaktaOmBeregningTilfeller(tilfellerListe)
                .build();

        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT,TIDENES_ENDE);

        lagAndel(periode, null);

        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagPeriodeDto lagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fomDato, LocalDate tomDato) {
        return BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(fomDato, tomDato)
                .build(beregningsgrunnlag);
    }

    private void lagAndel(BeregningsgrunnlagPeriodeDto periode, Beløp fordeltPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ORG_NUMMER)))
                .medBeregnetPrÅr(Beløp.fra(100_000))
                .medFordeltPrÅr(fordeltPrÅr)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                        .medKilde(AndelKilde.PROSESS_START)
                                .build(periode);
    }
}
