package no.nav.folketrygdloven.kalkulator.endringsresultat;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

public class ErEndringIBeregningTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.now();

    @Test
    public void skal_gi_ingen_endring_i_beregningsgrunnlag_ved_lik_dagsats_på_periodenoivå() {
        // Arrange
        List<Periode> bgPeriode = Collections.singletonList(new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        BeregningsgrunnlagDto originalGrunnlag = byggBeregningsgrunnlagForBehandling(false, true, bgPeriode);
        BeregningsgrunnlagDto revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(false, true, bgPeriode);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isFalse();
    }

    @Test
    public void skal_gi_endring_når_vi_mangler_beregningsgrunnlag_på_en_av_behandlingene() {
        // Arrange
        List<Periode> bgPeriode = Collections.singletonList(new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        BeregningsgrunnlagDto revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(false, true, bgPeriode);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.empty());

        // Assert
        assertThat(endring).isTrue();
    }

    @Test
    public void skal_gi_ingen_endring_når_vi_mangler_begge_beregningsgrunnlag() {
        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.empty(), Optional.empty());

        // Assert
        assertThat(endring).isFalse();
    }

    @Test
    public void skal_gi_ingen_endring_når_vi_har_like_mange_perioder_med_med_forskjellige_fom_og_tom() {
        // Arrange
        List<Periode> bgPerioderNyttGrunnlag = List.of(
                new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(35)),
                new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(36), null));
        List<Periode> bgPerioderOriginaltGrunnlag = List.of(
                new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(40)),
                new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(41), null));
        BeregningsgrunnlagDto originalGrunnlag = byggBeregningsgrunnlagForBehandling(false, true, bgPerioderOriginaltGrunnlag);
        BeregningsgrunnlagDto revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(false, true, bgPerioderNyttGrunnlag);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isFalse();
    }

    @Test
    public void skal_gi_ingen_endring_når_vi_har_like_mange_perioder_med_forskjellig_startdato() {
        // Arrange
        List<Periode> bgPerioderNyttGrunnlag = List.of(
                new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(35)),
                new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(36), null));
        List<Periode> bgPerioderOriginaltGrunnlag = List.of(
                new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(40)),
                new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(41), null));
        BeregningsgrunnlagDto originalGrunnlag = byggBeregningsgrunnlagForBehandling(false, true, bgPerioderOriginaltGrunnlag);
        BeregningsgrunnlagDto revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(false, true, bgPerioderNyttGrunnlag);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isFalse();
    }

    @Test
    public void skal_gi_endring_i_beregningsgrunnlag_ved_ulik_dagsats_på_periodenoivå() {
        // Arrange
        List<Periode> bgPeriode = Collections.singletonList(new Periode(SKJÆRINGSTIDSPUNKT_BEREGNING, null));
        BeregningsgrunnlagDto originalGrunnlag = byggBeregningsgrunnlagForBehandling(false, true, bgPeriode);
        BeregningsgrunnlagDto revurderingGrunnlag = byggBeregningsgrunnlagForBehandling(true, true, bgPeriode);

        // Act
        boolean endring = ErEndringIBeregning.vurder(Optional.of(revurderingGrunnlag), Optional.of(originalGrunnlag));

        // Assert
        assertThat(endring).isTrue();
    }


    private BeregningsgrunnlagDto byggBeregningsgrunnlagForBehandling(boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker, List<Periode> perioder) {
        return byggBeregningsgrunnlagForBehandling(medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker, perioder, new LagEnAndelTjeneste());
    }

    private BeregningsgrunnlagDto byggBeregningsgrunnlagForBehandling(boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker, List<Periode> perioder, LagAndelTjeneste lagAndelTjeneste) {
        BeregningsgrunnlagDto beregningsgrunnlag = LagBeregningsgrunnlagTjeneste.lagBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_BEREGNING, medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker, perioder, lagAndelTjeneste);
        return beregningsgrunnlag;
    }
}
