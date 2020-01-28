package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdatererTest {

    private static final List<FaktaOmBeregningTilfelle> FAKTA_OM_BEREGNING_TILFELLER = Collections
        .singletonList(FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET);
    public static final String ORGNR = "8934232423";
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);

    private VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer vurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer;
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPrStatusOgAndelDto snAndel;
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @Before
    public void setup() {
        vurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer = new VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer();
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilFaktaOmBeregningTilfeller(FAKTA_OM_BEREGNING_TILFELLER)
            .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(beregningsgrunnlag);
        snAndel = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAndelsnr(1L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(periode1);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    @Test
    public void skal_sette_ny_i_arbeidslivet() {
        // Arrange
        VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto nyIArbeidslivetDto = new VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto(true);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        dto.setVurderNyIArbeidslivet(nyIArbeidslivetDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        vurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);

        // Assert
        assertThat(snAndel.getNyIArbeidslivet()).isTrue();
    }

    @Test
    public void skal_sette_ny_i_arbeidslivet_til_false() {
        // Arrange
        VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto nyIArbeidslivetDto = new VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto(false);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        dto.setVurderNyIArbeidslivet(nyIArbeidslivetDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        vurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);

        // Assert
        assertThat(snAndel.getNyIArbeidslivet()).isFalse();
    }

}
