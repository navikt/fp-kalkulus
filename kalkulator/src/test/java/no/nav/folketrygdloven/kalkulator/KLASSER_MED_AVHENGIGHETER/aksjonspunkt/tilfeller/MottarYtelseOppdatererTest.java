package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.ArbeidstakerandelUtenIMMottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.MottarYtelseDto;
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
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.VirksomhetRepository;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningArbeidsgiverTestUtil;

public class MottarYtelseOppdatererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final String ORGNR = "3482934982384";
    private static final InternArbeidsforholdRefDto ARB_ID = InternArbeidsforholdRefDto.namedRef("TEST-REF");

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;
    private MottarYtelseOppdaterer oppdaterer;
    private BeregningsgrunnlagInput input;

    @Before
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .leggTilFaktaOmBeregningTilfeller(singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE))
            .build();
        periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        arbeidsgiverTestUtil = new BeregningArbeidsgiverTestUtil(new VirksomhetRepository());
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        this.oppdaterer = new MottarYtelseOppdaterer();
    }

    @Test
    public void skal_sette_mottar_ytelse_kun_for_frilans() {
        // Arrange
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE));
        dto.setMottarYtelse(new MottarYtelseDto(true, emptyList()));
        BeregningsgrunnlagPrStatusOgAndelDto frilansAndel = byggFrilansAndel(null);
        BeregningsgrunnlagPrStatusOgAndelDto arbeidsforholdAndel = byggArbeidsforholdMedBgAndel(null);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        oppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);

        // Assert
        assertThat(frilansAndel.mottarYtelse()).isPresent();
        assertThat(frilansAndel.mottarYtelse().get()).isTrue();
        assertThat(arbeidsforholdAndel.mottarYtelse()).isNotPresent();
    }

    @Test
    public void skal_sette_mottar_ytelse_kun_for_frilans_og_arbeidstakerandel() {
        // Arrange
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE));
        BeregningsgrunnlagPrStatusOgAndelDto frilansAndel = byggFrilansAndel(null);
        BeregningsgrunnlagPrStatusOgAndelDto arbeidsforholdAndel = byggArbeidsforholdMedBgAndel(null);
        dto.setMottarYtelse(new MottarYtelseDto(false,
            singletonList(new ArbeidstakerandelUtenIMMottarYtelseDto(arbeidsforholdAndel.getAndelsnr(), true))));

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        oppdaterer.oppdater(dto, Optional.empty(), input, oppdatere);

        BeregningsgrunnlagPrStatusOgAndelDto oppdatertFrilansAndel = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag()
            .getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(a -> a.equals(frilansAndel)).findFirst().get();

        BeregningsgrunnlagPrStatusOgAndelDto oppdatertArbeidsforholdAndel = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag()
            .getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(a -> a.equals(arbeidsforholdAndel)).findFirst().get();

        // Assert
        assertThat(oppdatertFrilansAndel.mottarYtelse()).isPresent();
        assertThat(oppdatertFrilansAndel.mottarYtelse().get()).isFalse();
        assertThat(oppdatertArbeidsforholdAndel.mottarYtelse()).isPresent();
        assertThat(oppdatertArbeidsforholdAndel.mottarYtelse().get()).isTrue();
    }

    private BeregningsgrunnlagPrStatusOgAndelDto byggFrilansAndel(Boolean mottarYtelse) {
        return BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medInntektskategori(Inntektskategori.FRILANSER)
            .medMottarYtelse(mottarYtelse, AktivitetStatus.FRILANSER)
            .build(periode);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto byggArbeidsforholdMedBgAndel(Boolean mottarYtelse) {
        Arbeidsgiver arbeidsgiver = arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORGNR);
        return BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medMottarYtelse(mottarYtelse, AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
            .build(periode);
    }


}
