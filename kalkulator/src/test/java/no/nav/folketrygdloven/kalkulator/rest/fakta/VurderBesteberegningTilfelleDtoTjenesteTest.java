package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class VurderBesteberegningTilfelleDtoTjenesteTest {

    private static final LocalDate STP = LocalDate.of(2019, 1, 1);
    private static final Intervall OPPTJENINGSPERIODE = Intervall.fraOgMedTilOgMed(STP.minusYears(1), STP.plusYears(10));
    private static final BGAndelArbeidsforholdRestDto.Builder bgAndelArbeidsforholdBuilder = BGAndelArbeidsforholdRestDto.builder();
    private VurderBesteberegningTilfelleDtoTjeneste dtoTjeneste;

    @Before
    public void setUp() {
        var orgnr = "347289324";
        bgAndelArbeidsforholdBuilder
            .medArbeidsgiver(ArbeidsgiverMedNavn.virksomhet(orgnr));
        dtoTjeneste = new VurderBesteberegningTilfelleDtoTjeneste();
    }

    @Test
    public void skal_ikke_sette_verdier_på_dto_om_man_ikkje_har_tilfelle() {
        // Arrange
        var beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(STP, OPPTJENINGSPERIODE,
            OpptjeningAktivitetType.ARBEID);
        var beregningsgrunnlag = BeregningsgrunnlagRestDto.builder().medSkjæringstidspunkt(STP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)).build();
        var grunnlag = BeregningsgrunnlagGrunnlagRestDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRegisterAktiviteter(beregningAktiviteter)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        var faktaOmBeregningDto = new FaktaOmBeregningDto();

         var input = new BeregningsgrunnlagRestInput(lagReferanse(), null, null, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        dtoTjeneste.lagDto(input, faktaOmBeregningDto);

        // Assert
        assertThat(faktaOmBeregningDto.getVurderBesteberegning()).isNull();

    }

    @Test
    public void skal_sette_verdier_på_dto() {
        // Arrange
        var beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(STP, OPPTJENINGSPERIODE,
            OpptjeningAktivitetType.ARBEID);
        var beregningsgrunnlag = BeregningsgrunnlagRestDto.builder().medSkjæringstidspunkt(STP)
            .leggTilFaktaOmBeregningTilfeller(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING))
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)).build();
        var periode = BeregningsgrunnlagPeriodeRestDto.builder().medBeregningsgrunnlagPeriode(STP, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(bgAndelArbeidsforholdBuilder)
            .medInntektskategori(Inntektskategori.JORDBRUKER)
            .build(periode);
        var grunnlag = BeregningsgrunnlagGrunnlagRestDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRegisterAktiviteter(beregningAktiviteter)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        var faktaOmBeregningDto = new FaktaOmBeregningDto();

         var input = new BeregningsgrunnlagRestInput(lagReferanse(), null, null, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        dtoTjeneste.lagDto(input, faktaOmBeregningDto);

        // Assert
        assertThat(faktaOmBeregningDto.getVurderBesteberegning().getSkalHaBesteberegning()).isNull();
    }

    private BehandlingReferanse lagReferanse() {
        return new BehandlingReferanseMock(STP);
    }
}
