package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderMilitærDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class VurderMilitærOppdatererTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    private static final Beløp GRUNNBELØP = new Beløp(BigDecimal.valueOf(85000));

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private VurderMilitærOppdaterer vurderMilitærOppdaterer;

    @BeforeEach
    public void setup() {
        vurderMilitærOppdaterer = new VurderMilitærOppdaterer();
    }

    @Test
    public void skal_legge_til_militærandel_om_vurdert_til_true_og_andel_ikke_finnes() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(Collections.singletonList(AktivitetStatus.ARBEIDSTAKER));
        VurderMilitærDto militærDto = new VurderMilitærDto(true);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE));
        dto.setVurderMilitaer(militærDto);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        vurderMilitærOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);

        // Assert
        BeregningsgrunnlagDto nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        Optional<BeregningsgrunnlagAktivitetStatusDto> militærStatus = nyttBg.getAktivitetStatuser().stream().filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).findFirst();
        boolean harMilitærandel = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .anyMatch(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus()));
        Assertions.assertThat(harMilitærandel).isTrue();
        Assertions.assertThat(militærStatus).isPresent();
        assertThat(militærStatus.get().getAktivitetStatus()).isEqualTo(AktivitetStatus.MILITÆR_ELLER_SIVIL);
        assertThat(militærStatus.get().getHjemmel()).isEqualTo(Hjemmel.F_14_7);
    }

    @Test
    public void skal_ikke_legge_til_militærandel_om_vurdert_til_true_og_andel_finnes_fra_før() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.MILITÆR_ELLER_SIVIL));
        VurderMilitærDto militærDto = new VurderMilitærDto(true);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE));
        dto.setVurderMilitaer(militærDto);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        vurderMilitærOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);

        // Assert
        BeregningsgrunnlagDto nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagAktivitetStatusDto> militærStatus = nyttBg.getAktivitetStatuser().stream().filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        List<BeregningsgrunnlagPrStatusOgAndelDto> militærAndeler = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        Assertions.assertThat(militærAndeler).hasSize(1);
        Assertions.assertThat(militærStatus).hasSize(1);
    }

    @Test
    public void skal_ikke_gjøre_noe_dersom_militær_er_false_men_det_ikke_ligger_militær_på_grunnlaget() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(Collections.singletonList(AktivitetStatus.ARBEIDSTAKER));
        VurderMilitærDto militærDto = new VurderMilitærDto(false);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE));
        dto.setVurderMilitaer(militærDto);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        vurderMilitærOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);

        // Assert
        BeregningsgrunnlagDto nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagAktivitetStatusDto> militærStatus = nyttBg.getAktivitetStatuser().stream().filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        List<BeregningsgrunnlagPrStatusOgAndelDto> militærAndeler = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        Assertions.assertThat(militærAndeler).hasSize(0);
        Assertions.assertThat(militærStatus).hasSize(0);
        assertThat(nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
    }

    @Test
    public void skal_fjerne_andel_dersom_militær_er_false_og_det_ligger_militær_på_grunnlaget() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.MILITÆR_ELLER_SIVIL));
        VurderMilitærDto militærDto = new VurderMilitærDto(false);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE));
        dto.setVurderMilitaer(militærDto);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        vurderMilitærOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);

        // Assert
        BeregningsgrunnlagDto nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagAktivitetStatusDto> militærStatus = nyttBg.getAktivitetStatuser().stream().filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        List<BeregningsgrunnlagPrStatusOgAndelDto> militærAndeler = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        Assertions.assertThat(militærAndeler).hasSize(0);
        Assertions.assertThat(militærStatus).hasSize(0);
        assertThat(nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(List<AktivitetStatus> statuser) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();

        BeregningsgrunnlagPeriodeDto periode = buildBeregningsgrunnlagPeriode(bg,
            SKJÆRINGSTIDSPUNKT, null);

        statuser.forEach(status -> {
            BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(status)
                .medHjemmel(Hjemmel.F_14_7).build(bg);
            buildBgPrStatusOgAndel(periode, status);
        });

        return bg;
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(aktivitetStatus)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID);
        if (aktivitetStatus.erArbeidstaker()) {
            builder.medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.fra(AktørId.dummy())));
        }
        builder
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

}
