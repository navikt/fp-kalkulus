package no.nav.folketrygdloven.kalkulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.verdikjede.VerdikjedeTestHjelper;

public class VurderBeregningsgrunnlagTjenesteTest {

    private static final double MÅNEDSINNTEKT1 = 12345d;
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private VerdikjedeTestHjelper verdikjedeTestHjelper = new VerdikjedeTestHjelper();

    private Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);


    @Test
    public void testVilkårsvurderingArbeidstakerMedBGOverHalvG() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING,
            OpptjeningAktivitetType.ARBEID);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(400_000);
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktiviteter)
            .medBeregningsgrunnlag(beregningsgrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = grunnlagDtoBuilder
            .build(BeregningsgrunnlagTilstand.FORESLÅTT);
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        verdikjedeTestHjelper.lagBehandlingForSN(BigDecimal.valueOf(12 * MÅNEDSINNTEKT1), 2015, new BehandlingReferanseMock(), registerBuilder);
        BehandlingReferanse ref = lagReferanseMedSkjæringstidspunkt(behandlingReferanse);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var iayGrunnlag = iayGrunnlagBuilder.medData(registerBuilder).medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(ref, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag);


        // Act
        BeregningsgrunnlagRegelResultat resultat = VurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input, grunnlag);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getVilkårOppfylt()).isTrue();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getRegelInputVilkårvurdering()).isNotEmpty();
        assertThat(periode.getRegelEvalueringVilkårvurdering()).isNotEmpty();
    }

    @Test
    public void testVilkårsvurderingArbeidstakerMedBGUnderHalvG() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING,
            OpptjeningAktivitetType.ARBEID);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(40_000);
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktiviteter)
            .medBeregningsgrunnlag(beregningsgrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = grunnlagDtoBuilder
            .build(BeregningsgrunnlagTilstand.FORESLÅTT);
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        verdikjedeTestHjelper.lagBehandlingForSN(BigDecimal.valueOf(12 * MÅNEDSINNTEKT1), 2015, new BehandlingReferanseMock(), registerBuilder);

        BehandlingReferanse ref = lagReferanseMedSkjæringstidspunkt(behandlingReferanse);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var iayGrunnlag = iayGrunnlagBuilder.medData(registerBuilder).medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(ref, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag);

        // Act
        BeregningsgrunnlagRegelResultat resultat = VurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(input, grunnlag);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getVilkårOppfylt()).isFalse();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getRegelInputVilkårvurdering()).isNotEmpty();
        assertThat(periode.getRegelEvalueringVilkårvurdering()).isNotEmpty();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(int inntekt) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(BigDecimal.valueOf(600_000))
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .build(bg);
        SammenligningsgrunnlagDto.builder()
            .medSammenligningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medRapportertPrÅr(BigDecimal.ZERO)
            .medAvvikPromilleNy(BigDecimal.valueOf(0))
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12))
                .medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medArbeidsgiver(Arbeidsgiver.virksomhet("1234"))
                .medRefusjonskravPrÅr(BigDecimal.valueOf(inntekt)))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
            .medBeregnetPrÅr(BigDecimal.valueOf(inntekt))
            .build(periode);
        return bg;
    }

    private static BehandlingReferanse lagReferanseMedSkjæringstidspunkt(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medUtledetSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .build());
    }

}
