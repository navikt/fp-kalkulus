package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettMånedsinntektUtenInntektsmeldingAndelDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettMånedsinntektUtenInntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

public class FastsettMånedsinntektUtenInntektsmeldingOppdatererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    public static final String ORGNR = "78327942834";
    private static final String ORGNR2 = "43253634231";
    private static final int ARBEIDSINNTEKT = 120000;

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private Arbeidsgiver arbeidsgiver;
    private Arbeidsgiver arbeidsgiver2;

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setUp() {
        arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);

        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .leggTilFaktaOmBeregningTilfeller(singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE))
            .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2))
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPeriodeDto periode2 = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2).plusDays(1), null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier().medAndelsnr(1L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver)).build(periode1);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier().medAndelsnr(5L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver)).build(periode2);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier().medAndelsnr(2L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver2)).build(periode1);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier().medAndelsnr(1L)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver2)).build(periode2);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    @Test
    public void skal_sette_inntekt_på_riktige_andeler_i_alle_perioder(){
        var fastsettMånedsinntektUtenInntektsmeldingOppdaterer = new FastsettMånedsinntektUtenInntektsmeldingOppdaterer();
        // Arrange
        FastsettMånedsinntektUtenInntektsmeldingDto dto = new FastsettMånedsinntektUtenInntektsmeldingDto();
        FastsettMånedsinntektUtenInntektsmeldingAndelDto andelDto = new FastsettMånedsinntektUtenInntektsmeldingAndelDto(1L,
            new FastsatteVerdierDto(ARBEIDSINNTEKT, null, null));
        List<FastsettMånedsinntektUtenInntektsmeldingAndelDto> andelListe = singletonList(andelDto);
        dto.setAndelListe(andelListe);
        FaktaBeregningLagreDto faktaLagreDto = new FaktaBeregningLagreDto(singletonList(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING));
        faktaLagreDto.setFastsattUtenInntektsmelding(dto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        fastsettMånedsinntektUtenInntektsmeldingOppdaterer.oppdater(faktaLagreDto, Optional.empty(), input, oppdatere);

        // Assert
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedFastsattInntekt = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getBgAndelArbeidsforhold().isPresent() && andel.getBgAndelArbeidsforhold().get().getArbeidsgiver().equals(arbeidsgiver))
            .collect(Collectors.toList());
        andelerMedFastsattInntekt.forEach(andel -> assertThat(andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(ARBEIDSINNTEKT)));
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenFastsattInntekt = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getBgAndelArbeidsforhold().isPresent() && andel.getBgAndelArbeidsforhold().get().getArbeidsgiver().equals(arbeidsgiver2))
            .collect(Collectors.toList());
        andelerUtenFastsattInntekt.forEach(andel -> assertThat(andel.getBeregnetPrÅr()).isNull());
        }
}
