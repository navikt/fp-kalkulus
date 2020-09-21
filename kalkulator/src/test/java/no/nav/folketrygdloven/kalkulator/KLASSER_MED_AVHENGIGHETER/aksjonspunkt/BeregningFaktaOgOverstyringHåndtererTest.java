package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import static no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.OverstyrBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller.FaktaOmBeregningTilfellerOppdaterer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall;


public class BeregningFaktaOgOverstyringHåndtererTest {

    private static final LocalDate STP = LocalDate.of(2019, 1, 1);

    @Inject
    private FaktaOmBeregningTilfellerOppdaterer faktaOmBeregningTilfellerOppdaterer;
    private BeregningFaktaOgOverstyringHåndterer beregningFaktaOgOverstyringHåndterer;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(STP);

    @BeforeEach
    public void setup() {
        this.beregningFaktaOgOverstyringHåndterer = new BeregningFaktaOgOverstyringHåndterer(faktaOmBeregningTilfellerOppdaterer);
    }

    @Test
    public void skal_sette_inntekt_for_en_andel_i_en_periode() {
        // Arrange
        Long andelsnr = 1L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(andelsnr, List.of(Intervall.fraOgMedTilOgMed(STP, AbstractIntervall.TIDENES_ENDE)));
        int fastsattBeløp = 10000;
        OverstyrBeregningsgrunnlagDto overstyrDto = new OverstyrBeregningsgrunnlagDto(lagFastsattAndeler(andelsnr, fastsattBeløp), null);
        BeregningsgrunnlagInput input = lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = beregningFaktaOgOverstyringHåndterer.håndterMedOverstyring(input, overstyrDto);

        // Assert
        Optional<BeregningsgrunnlagDto> nyttBg = nyttGrunnlag.getBeregningsgrunnlag();
        AssertionsForClassTypes.assertThat(nyttBg).isPresent();
        assertThat(nyttBg.get().isOverstyrt()).isTrue();
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBg.get().getBeregningsgrunnlagPerioder();
        AssertionsForClassTypes.assertThat(perioder.size()).isEqualTo(1);
        BeregningsgrunnlagPeriodeDto p1 = perioder.get(0);
        assertThat(p1.getBeregningsgrunnlagPeriodeFom()).isEqualTo(STP);
        validerAndeler(fastsattBeløp, p1);
    }


    @Test
    public void skal_sette_inntekt_for_en_andel_i_to_perioder() {
        // Arrange
        Long andelsnr = 1L;
        LocalDate tilOgMed = STP.plusMonths(1).minusDays(1);
        List<Intervall> periodeList = List.of(Intervall.fraOgMedTilOgMed(STP, tilOgMed),
            Intervall.fraOgMedTilOgMed(tilOgMed.plusDays(1), AbstractIntervall.TIDENES_ENDE));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(andelsnr,
            periodeList);
        int fastsattBeløp1 = 10000;
        OverstyrBeregningsgrunnlagDto overstyrDto = new OverstyrBeregningsgrunnlagDto(lagFastsattAndeler(andelsnr, fastsattBeløp1), null);
        BeregningsgrunnlagInput input = lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = beregningFaktaOgOverstyringHåndterer.håndterMedOverstyring(input, overstyrDto);

        // Assert
        Optional<BeregningsgrunnlagDto> nyttBg = nyttGrunnlag.getBeregningsgrunnlag();
        AssertionsForClassTypes.assertThat(nyttBg).isPresent();
        assertThat(nyttBg.get().isOverstyrt()).isTrue();
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBg.get().getBeregningsgrunnlagPerioder();
        AssertionsForClassTypes.assertThat(perioder.size()).isEqualTo(2);
        BeregningsgrunnlagPeriodeDto p1 = perioder.get(0);
        assertThat(p1.getBeregningsgrunnlagPeriodeFom()).isEqualTo(STP);
        validerAndeler(fastsattBeløp1, p1);
        BeregningsgrunnlagPeriodeDto p2 = perioder.get(1);
        assertThat(p2.getBeregningsgrunnlagPeriodeFom()).isEqualTo(tilOgMed.plusDays(1));
        validerAndeler(fastsattBeløp1, p2);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(Long andelsnr, List<Intervall> perioder) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(STP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();
        perioder.forEach(p -> {
            BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(p.getFomDato(), p.getTomDato())
                .build(beregningsgrunnlag);
            BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(andelsnr)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.fra(AktørId.dummy())))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).build(periode);
        });
        return beregningsgrunnlag;
    }

    private void validerAndeler(int fastsattBeløp, BeregningsgrunnlagPeriodeDto p1) {
        assertThat(p1.getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);
        assertThat(p1.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getBeregnetPrÅr().intValue()).isEqualTo(fastsattBeløp * 12);
        assertThat(p1.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getFastsattAvSaksbehandler()).isTrue();
        assertThat(p1.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    private List<FastsettBeregningsgrunnlagAndelDto> lagFastsattAndeler(Long andelsnr, int fastsattBeløp1) {
        RedigerbarAndelFaktaOmBeregningDto andelsInfo = new RedigerbarAndelFaktaOmBeregningDto(andelsnr, false, AktivitetStatus.ARBEIDSTAKER, false);
        FastsatteVerdierDto fastsatteVerdier1 = FastsatteVerdierDto.Builder.ny().medFastsattBeløpPrMnd(fastsattBeløp1).build();
        FastsettBeregningsgrunnlagAndelDto andelDto1 = new FastsettBeregningsgrunnlagAndelDto(andelsInfo, fastsatteVerdier1, Inntektskategori.ARBEIDSTAKER, null,null);
        return List.of(andelDto1);
    }

}
