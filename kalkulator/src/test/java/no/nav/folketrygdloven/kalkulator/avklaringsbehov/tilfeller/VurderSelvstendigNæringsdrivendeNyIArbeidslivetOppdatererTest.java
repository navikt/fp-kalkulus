package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdatererTest {

    private static final List<FaktaOmBeregningTilfelle> FAKTA_OM_BEREGNING_TILFELLER = Collections
        .singletonList(FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET);
    public static final String ORGNR = "8934232423";
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);

    private VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer vurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer;
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @BeforeEach
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

        @SuppressWarnings("unused")
        var snAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(1L)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(periode1);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
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
        var faktaAktør = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat().flatMap(FaktaAggregatDto::getFaktaAktør);

        // Assert
        assertThat(faktaAktør.get().getErNyIArbeidslivetSN()).isTrue();
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
        FaktaAktørDto faktaAktørDto = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT).getFaktaAggregat().get().getFaktaAktør().get();

        // Assert
        assertThat(faktaAktørDto.getErNyIArbeidslivetSN()).isFalse();
    }

}
