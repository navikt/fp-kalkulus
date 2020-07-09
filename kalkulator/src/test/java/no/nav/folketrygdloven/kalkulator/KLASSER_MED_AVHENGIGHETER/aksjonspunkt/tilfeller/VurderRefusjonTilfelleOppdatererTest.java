package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RefusjonskravPrArbeidsgiverVurderingDto;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;

public class VurderRefusjonTilfelleOppdatererTest {
    private static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("973861778");
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);

    private VurderRefusjonTilfelleOppdaterer vurderRefusjonTilfelleOppdaterer;
    private BehandlingReferanse referanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setUp() {
        vurderRefusjonTilfelleOppdaterer = new VurderRefusjonTilfelleOppdaterer();
    }

    @Test
    public void oppdater_når_ikkje_gyldig_utvidelse() {
        // Arrange
        LocalDate førsteInnsendingAvRefusjonskrav = SKJÆRINGSTIDSPUNKT.plusMonths(4);
        LocalDate førsteMuligDatoMedRefusjonFørAksjonspunkt = førsteInnsendingAvRefusjonskrav.minusMonths(3).withDayOfMonth(1);
        LocalDate førsteDatoMedRefusjonskrav = SKJÆRINGSTIDSPUNKT;
        RefusjonskravDatoDto refusjonskravDatoDto = lagArbeidsgiverSøktForSent(førsteDatoMedRefusjonskrav, førsteInnsendingAvRefusjonskrav, true);
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlag();
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(referanse, null, null, AktivitetGradering.INGEN_GRADERING, List.of(refusjonskravDatoDto), null);
        BeregningsgrunnlagInput beregningsgrunnlagInput = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()).medBeregningsgrunnlag(beregningsgrunnlagDto).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));
        FaktaBeregningLagreDto dto = lagDto(false);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag());
        vurderRefusjonTilfelleOppdaterer.oppdater(dto, Optional.empty(), beregningsgrunnlagInput, oppdatere);

        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Assert
        assertOverstyringAvRefusjon(nyttGrunnlag, førsteMuligDatoMedRefusjonFørAksjonspunkt);
    }

    @Test
    public void oppdater_når_gyldig_utvidelse() {
        // Arrange
        LocalDate førsteInnsendingAvRefusjonskrav = SKJÆRINGSTIDSPUNKT.plusMonths(4);
        LocalDate førsteDatoMedRefusjonskrav = SKJÆRINGSTIDSPUNKT;
        RefusjonskravDatoDto refusjonskravDatoDto = lagArbeidsgiverSøktForSent(førsteDatoMedRefusjonskrav, førsteInnsendingAvRefusjonskrav, true);
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlag();
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(referanse, null, null, AktivitetGradering.INGEN_GRADERING, List.of(refusjonskravDatoDto), null);
        BeregningsgrunnlagInput beregningsgrunnlagInput = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()).medBeregningsgrunnlag(beregningsgrunnlagDto).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));
        FaktaBeregningLagreDto dto = lagDto(true);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag());
        vurderRefusjonTilfelleOppdaterer.oppdater(dto, Optional.empty(), beregningsgrunnlagInput, oppdatere);

        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Assert
        assertOverstyringAvRefusjon(nyttGrunnlag, førsteDatoMedRefusjonskrav);
    }


    private void assertOverstyringAvRefusjon(BeregningsgrunnlagGrunnlagDto nyttGrunnlag, LocalDate førsteMuligeDato) {
        assertThat(nyttGrunnlag.getRefusjonOverstyringer()).isPresent();
        BeregningRefusjonOverstyringerDto beregningRefusjonOverstyringer = nyttGrunnlag.getRefusjonOverstyringer().get();
        List<BeregningRefusjonOverstyringDto> overstyringer = beregningRefusjonOverstyringer.getRefusjonOverstyringer();
        AssertionsForClassTypes.assertThat(overstyringer.size()).isEqualTo(1);
        assertThat(overstyringer.get(0).getArbeidsgiver()).isEqualTo(VIRKSOMHET);
        assertThat(overstyringer.get(0).getFørsteMuligeRefusjonFom().orElse(null)).isEqualTo(førsteMuligeDato);
    }

    private FaktaBeregningLagreDto lagDto(boolean skalUtvideGyldighet) {
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(List.of(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT));

        RefusjonskravPrArbeidsgiverVurderingDto ref1 = new RefusjonskravPrArbeidsgiverVurderingDto(VIRKSOMHET.getIdentifikator(), skalUtvideGyldighet);
        dto.setRefusjonskravGyldighet(List.of(ref1));
        return dto;
    }

    private RefusjonskravDatoDto lagArbeidsgiverSøktForSent(LocalDate førsteDagMedRefusjonskrav, LocalDate førsteInnsendingAvRefusjonskrav, boolean harRefusjonFraStart) {
        return new RefusjonskravDatoDto(VIRKSOMHET, førsteDagMedRefusjonskrav, førsteInnsendingAvRefusjonskrav, harRefusjonFraStart);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilFaktaOmBeregningTilfeller(List.of(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT))
            .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(VIRKSOMHET))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode1);
        return beregningsgrunnlag;
    }
}
