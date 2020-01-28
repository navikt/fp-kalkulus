package no.nav.folketrygdloven.kalkulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

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

import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.verdikjede.VerdikjedeTestHjelper;

public class ForeslåBeregningsgrunnlagSNTest {

    private static final double MÅNEDSINNTEKT1 = 12345d;

    private static final double BEREGNINGSGRUNNLAG = 148989.08d;

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private VerdikjedeTestHjelper verdikjedeTestHjelper = new VerdikjedeTestHjelper();


    @Test
    public void testBeregningsgrunnlagSelvstendigNæringsdrivende() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING,
            OpptjeningAktivitetType.NÆRING);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktiviteter)
            .medBeregningsgrunnlag(beregningsgrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = grunnlagDtoBuilder
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        verdikjedeTestHjelper.lagBehandlingForSN(BigDecimal.valueOf(12 * MÅNEDSINNTEKT1), 2014, behandlingReferanse, registerBuilder);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(registerBuilder)
            .medInntektsmeldinger(inntektsmeldinger).build();
        BehandlingReferanse ref = lagReferanseMedStp(behandlingReferanse);
        var input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(ref, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);

        // Act
        BeregningsgrunnlagRegelResultat resultat = ForeslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verifiserBGSN(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0));
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)));
        return beregningsgrunnlagBuilder.build();
    }

    private void verifiserPeriode(BeregningsgrunnlagPeriodeDto periode, LocalDate fom, LocalDate tom, int antallAndeler) {
        assertThat(periode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(fom);
        assertThat(periode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(tom);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(antallAndeler);
        assertThat(periode.getBruttoPrÅr().doubleValue()).isCloseTo(BEREGNINGSGRUNNLAG, within(0.01));
        assertThat(periode.getRedusertPrÅr()).isNull();
        assertThat(periode.getAvkortetPrÅr()).isNull();
    }

    private void verifiserBGSN(BeregningsgrunnlagPrStatusOgAndelDto bgpsa) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(bgpsa.getBeregningsperiodeFom()).isEqualTo(LocalDate.of(2014, Month.JANUARY, 1));
        assertThat(bgpsa.getBeregningsperiodeTom()).isEqualTo(LocalDate.of(2016, Month.DECEMBER, 31));
        assertThat(bgpsa.getBgAndelArbeidsforhold()).isEmpty();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.UDEFINERT);
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isCloseTo(BEREGNINGSGRUNNLAG, within(0.01));
        assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isCloseTo(BEREGNINGSGRUNNLAG, within(0.01));
        assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
        assertThat(bgpsa.getRedusertPrÅr()).isNull();
        assertThat(bgpsa.getAvkortetPrÅr()).isNull();
    }

    private static BehandlingReferanse lagReferanseMedStp(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medUtledetSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .build());
    }
}
