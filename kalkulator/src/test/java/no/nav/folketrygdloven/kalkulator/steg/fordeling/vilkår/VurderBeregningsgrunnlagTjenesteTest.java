package no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.verdikjede.VerdikjedeTestHjelper;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

@ExtendWith(MockitoExtension.class)
public class VurderBeregningsgrunnlagTjenesteTest {

    private static final double MÅNEDSINNTEKT1 = 12345d;
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;

    private VerdikjedeTestHjelper verdikjedeTestHjelper = new VerdikjedeTestHjelper();

    private Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();
    private final UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new ForeldrepengerGrunnlagMapper());
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);
    private VurderBeregningsgrunnlagTjeneste vurderBeregningsgrunnlagTjeneste = new VurderBeregningsgrunnlagTjeneste(mapBeregningsgrunnlagFraVLTilRegel);


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
        verdikjedeTestHjelper.lagBehandlingForSN(BigDecimal.valueOf(12 * MÅNEDSINNTEKT1), 2015, new KoblingReferanseMock(), registerBuilder);
        KoblingReferanse ref = lagReferanseMedSkjæringstidspunkt(koblingReferanse);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var iayGrunnlag = iayGrunnlagBuilder.medData(registerBuilder).medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(ref, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag);
        FordelBeregningsgrunnlagInput fordelBeregningsgrunnlagInput = new FordelBeregningsgrunnlagInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act
        BeregningsgrunnlagRegelResultat resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(fordelBeregningsgrunnlagInput, grunnlag);

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
        verdikjedeTestHjelper.lagBehandlingForSN(BigDecimal.valueOf(12 * MÅNEDSINNTEKT1), 2015, new KoblingReferanseMock(), registerBuilder);

        KoblingReferanse ref = lagReferanseMedSkjæringstidspunkt(koblingReferanse);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var iayGrunnlag = iayGrunnlagBuilder.medData(registerBuilder).medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(ref, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag);

        FordelBeregningsgrunnlagInput fordelBeregningsgrunnlagInput = new FordelBeregningsgrunnlagInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act
        BeregningsgrunnlagRegelResultat resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(fordelBeregningsgrunnlagInput, grunnlag);

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
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
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

    private static KoblingReferanse lagReferanseMedSkjæringstidspunkt(KoblingReferanse koblingReferanse) {
        return koblingReferanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .build());
    }

}
