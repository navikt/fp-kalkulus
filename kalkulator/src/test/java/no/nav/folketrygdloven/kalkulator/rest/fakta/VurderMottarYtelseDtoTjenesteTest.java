package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagArbeidstakerAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagFrilansAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.rest.dto.ArbeidstakerUtenInntektsmeldingAndelDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.VurderMottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidsforholdHandlingType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;


public class VurderMottarYtelseDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final String ORGNR = "973093681";
    private static final EksternArbeidsforholdRef EKSTERN_ARB_ID = EksternArbeidsforholdRef.ref("TEST_REF1");
    private static final BigDecimal INNTEKT1 = BigDecimal.valueOf(10000);
    private static final BigDecimal INNTEKT2 = BigDecimal.valueOf(20000);
    private static final BigDecimal INNTEKT3 = BigDecimal.valueOf(30000);
    private static final List<BigDecimal> INNTEKT_PR_MND = List.of(INNTEKT1, INNTEKT2, INNTEKT3);
    private static final BigDecimal INNTEKT_SNITT = INNTEKT1.add(INNTEKT2.add(INNTEKT3)).divide(BigDecimal.valueOf(3), 10, RoundingMode.HALF_EVEN);
    private static final String FRILANS_ORGNR = "853498598934";

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;
    private VurderMottarYtelseDtoTjeneste dtoTjeneste;
    private BeregningsgrunnlagGrunnlagDto grunnlag;
    private InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag;

    @BeforeEach
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(BigDecimal.valueOf(91425L))
                .leggTilFaktaOmBeregningTilfeller(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE))
                .build();
        periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(beregningsgrunnlag);
        grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(BeregningsgrunnlagTilstand.OPPRETTET);
        dtoTjeneste = new VurderMottarYtelseDtoTjeneste();
    }

    @Test
    public void skal_lage_dto_for_mottar_ytelse_uten_mottar_ytelse_satt() {
        // Arrange
        FaktaOmBeregningDto dto = new FaktaOmBeregningDto();
        byggFrilansAndel(null);
        BeregningsgrunnlagPrStatusOgAndelDto arbeidsforholdAndel = byggArbeidsforholdMedBgAndel(null);

        // Act
        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, inntektArbeidYtelseGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);

        dtoTjeneste.lagDto(input, dto);

        // Assert
        VurderMottarYtelseDto mottarYtelseDto = dto.getVurderMottarYtelse();
        assertThat(mottarYtelseDto.getErFrilans()).isTrue();
        assertThat(mottarYtelseDto.getFrilansMottarYtelse()).isNull();
        assertThat(mottarYtelseDto.getFrilansInntektPrMnd()).isEqualByComparingTo(INNTEKT_SNITT);
        assertThat(mottarYtelseDto.getArbeidstakerAndelerUtenIM()).hasSize(1);
        ArbeidstakerUtenInntektsmeldingAndelDto andelUtenIM = mottarYtelseDto.getArbeidstakerAndelerUtenIM().get(0);
        assertThat(andelUtenIM.getMottarYtelse()).isNull();
        assertThat(andelUtenIM.getArbeidsforhold().getArbeidsgiverId()).isEqualTo(ORGNR);
        assertThat(andelUtenIM.getAndelsnr()).isEqualTo(arbeidsforholdAndel.getAndelsnr());
        assertThat(andelUtenIM.getInntektPrMnd()).isEqualByComparingTo(INNTEKT_SNITT);
    }

    @Test
    public void skal_lage_dto_for_mottar_ytelse_med_mottar_ytelse_satt() {
        // Arrange
        FaktaOmBeregningDto dto = new FaktaOmBeregningDto();
        byggFrilansAndel(false);
        BeregningsgrunnlagPrStatusOgAndelDto arbeidsforholdAndel = byggArbeidsforholdMedBgAndel(true);

        // Act
        var input = new BeregningsgrunnlagRestInput(behandlingReferanse, inntektArbeidYtelseGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        dtoTjeneste.lagDto(input, dto);

        // Assert
        VurderMottarYtelseDto mottarYtelseDto = dto.getVurderMottarYtelse();
        assertThat(mottarYtelseDto.getErFrilans()).isTrue();
        assertThat(mottarYtelseDto.getFrilansMottarYtelse()).isFalse();
        assertThat(mottarYtelseDto.getFrilansInntektPrMnd()).isEqualByComparingTo(INNTEKT_SNITT);
        assertThat(mottarYtelseDto.getArbeidstakerAndelerUtenIM()).hasSize(1);
        ArbeidstakerUtenInntektsmeldingAndelDto andelUtenIM = mottarYtelseDto.getArbeidstakerAndelerUtenIM().get(0);
        assertThat(andelUtenIM.getMottarYtelse()).isTrue();
        assertThat(andelUtenIM.getArbeidsforhold().getArbeidsgiverId()).isEqualTo(ORGNR);
        assertThat(andelUtenIM.getAndelsnr()).isEqualTo(arbeidsforholdAndel.getAndelsnr());
        assertThat(andelUtenIM.getInntektPrMnd()).isEqualByComparingTo(INNTEKT_SNITT);
    }

    private void byggFrilansAndel(Boolean mottarYtelse) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(FRILANS_ORGNR);
        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        Intervall frilansPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10));
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder(behandlingReferanse.getAktørId());
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilderForType = aktørArbeidBuilder
                .getYrkesaktivitetBuilderForType(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
        yrkesaktivitetBuilderForType
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(frilansPeriode))
                .medArbeidsgiver(arbeidsgiver);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilderForType);
        oppdatere.leggTilAktørArbeid(aktørArbeidBuilder);
        BeregningIAYTestUtil.byggInntektForBehandling(behandlingReferanse.getAktørId(),
                SKJÆRINGSTIDSPUNKT_OPPTJENING, oppdatere, INNTEKT_PR_MND, true, arbeidsgiver);

        inntektArbeidYtelseGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(oppdatere)
                .medOppgittOpptjening(BeregningIAYTestUtil.leggTilOppgittOpptjeningForFL(false, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10)))
                .build();

        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusMonths(3), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .medBeregningsgrunnlagFrilansAndel(BeregningsgrunnlagFrilansAndelDto.builder().medMottarYtelse(mottarYtelse).build())
                .build(this.periode);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto byggArbeidsforholdMedBgAndel(Boolean mottarYtelse) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(inntektArbeidYtelseGrunnlag.getRegisterVersjon(), VersjonTypeDto.REGISTER);
        Intervall ansettelsesPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10));
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder(behandlingReferanse.getAktørId());
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilderForType = aktørArbeidBuilder
                .getYrkesaktivitetBuilderForType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesaktivitetBuilderForType
                .medArbeidsgiver(arbeidsgiver)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesPeriode))
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesPeriode)
                        .medErAnsettelsesPeriode(false)
                        .medSisteLønnsendringsdato(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2)));
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilderForType);
        oppdatere.leggTilAktørArbeid(aktørArbeidBuilder);
        oppdatere.medNyInternArbeidsforholdRef(arbeidsgiver, EKSTERN_ARB_ID);

        ArbeidsforholdOverstyringDtoBuilder arbeidsforholdOverstyringDtoBuilder = ArbeidsforholdOverstyringDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medHandling(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING);

        BeregningIAYTestUtil.byggInntektForBehandling(behandlingReferanse.getAktørId(),
                SKJÆRINGSTIDSPUNKT_OPPTJENING, oppdatere, INNTEKT_PR_MND, true, arbeidsgiver);

        inntektArbeidYtelseGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(inntektArbeidYtelseGrunnlag)
                .medData(oppdatere)
                .medInformasjon(ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty()).leggTil(arbeidsforholdOverstyringDtoBuilder).build())
                .leggTilArbeidsgiverOpplysninger(new ArbeidsgiverOpplysningerDto(ORGNR, "Arbeidsgiveren", null))
                .build();

        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsgrunnlagArbeidstakerAndel(BeregningsgrunnlagArbeidstakerAndelDto.builder().medMottarYtelse(mottarYtelse).build())
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusMonths(3), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
                .build(periode);
    }


}
