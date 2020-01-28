package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderteArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Virksomhet;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.VirksomhetEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class VurderTidsbegrensetArbeidsforholdOppdatererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);



    @Inject
    private FaktaOmBeregningTilfellerOppdaterer faktaOmBeregningTilfellerOppdaterer;



    private List<VurderteArbeidsforholdDto> tidsbestemteArbeidsforhold;
    private final long FØRSTE_ANDELSNR = 1L;
    private final long ANDRE_ANDELSNR = 2L;
    private final long TREDJE_ANDELSNR = 3L;
    private final LocalDate FOM = LocalDate.now().minusDays(100);
    private final LocalDate TOM = LocalDate.now();
    private final List<VirksomhetEntitet> virksomheter = new ArrayList<>();
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @Before
    public void setup() {
        virksomheter.add(new VirksomhetEntitet.Builder()
                .medOrgnr("123")
                .medNavn("VirksomhetNavn1")
                .oppdatertOpplysningerNå()
                .build());
        virksomheter.add(new VirksomhetEntitet.Builder()
                .medOrgnr("456")
                .medNavn("VirksomhetNavn2")
                .oppdatertOpplysningerNå()
                .build());
        virksomheter.add(new VirksomhetEntitet.Builder()
                .medOrgnr("789")
                .medNavn("VirksomhetNavn3")
                .oppdatertOpplysningerNå()
                .build());
        tidsbestemteArbeidsforhold = lagFastsatteAndelerListe();


    }

    private List<VurderteArbeidsforholdDto> lagFastsatteAndelerListe() {

        VurderteArbeidsforholdDto førsteForhold = new VurderteArbeidsforholdDto(
            FØRSTE_ANDELSNR,
            true,
            null
        );

        VurderteArbeidsforholdDto andreForhold = new VurderteArbeidsforholdDto(
            ANDRE_ANDELSNR,
            false,
            null
        );

        VurderteArbeidsforholdDto tredjeForhold = new VurderteArbeidsforholdDto(
            TREDJE_ANDELSNR,
            true,
            null
        );

        return new ArrayList<>(List.of(førsteForhold, andreForhold, tredjeForhold));
    }


    @Test
    public void skal_markere_korrekte_andeler_som_tidsbegrenset() {
        //Arrange
        lagBehandlingMedBeregningsgrunnlag();

        //Dto
        var faktaBeregningLagreDto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD));
        faktaBeregningLagreDto.setVurderTidsbegrensetArbeidsforhold(new VurderTidsbegrensetArbeidsforholdDto( tidsbestemteArbeidsforhold));


        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        faktaOmBeregningTilfellerOppdaterer.oppdater(faktaBeregningLagreDto, Optional.empty(), input, oppdatere);

        //Assert
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.get(0).getBgAndelArbeidsforhold().get().getErTidsbegrensetArbeidsforhold()).isTrue();
        assertThat(andeler.get(1).getBgAndelArbeidsforhold().get().getErTidsbegrensetArbeidsforhold()).isFalse();
        assertThat(andeler.get(2).getBgAndelArbeidsforhold().get().getErTidsbegrensetArbeidsforhold()).isTrue();
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Virksomhet virksomhet) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
            .builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet.getOrgnr()))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

    private void lagBehandlingMedBeregningsgrunnlag() {

        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();

        BeregningsgrunnlagPeriodeDto periode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
            FOM, TOM);
        buildBgPrStatusOgAndel(periode, virksomheter.get(0));
        buildBgPrStatusOgAndel(periode, virksomheter.get(1));
        buildBgPrStatusOgAndel(periode, virksomheter.get(2));

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }
}
