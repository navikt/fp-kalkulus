package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagArbeidsforholdDto;

public class RefusjonEllerGraderingArbeidsforholdDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    public static final String ORGNR = "7238947234423";
    private static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_REF = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    public static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(600000);

    private final Arbeidsgiver arbeidsgiver1 = Arbeidsgiver.virksomhet(ORGNR);

    private final BeregningAktivitetAggregatDto beregningAktivitetAggregat = mock(BeregningAktivitetAggregatDto.class);

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPrStatusOgAndelDto arbeidstakerAndel;
    private BGAndelArbeidsforholdDto.Builder arbeidsforholdBuilder;
    private BeregningsgrunnlagPeriodeDto periode;
    private BehandlingReferanse referanse = new BehandlingReferanseMock();
    private BeregningsgrunnlagGrunnlagDto grunnlagEntitet;

    @BeforeEach
    public void setUp() {

        referanse = referanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING).build());
        when(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening()).thenReturn(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        when(beregningAktivitetAggregat.getBeregningAktiviteter()).thenReturn(Collections.emptyList());

        beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING).medGrunnbeløp(GRUNNBELØP)
            .build();
        periode = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        arbeidsforholdBuilder = BGAndelArbeidsforholdDto
            .builder()
            .medArbeidsgiver(arbeidsgiver1)
            .medArbeidsforholdRef(ARBEIDSFORHOLD_REF);
        arbeidstakerAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder)
            .build(periode);
        grunnlagEntitet = mock(BeregningsgrunnlagGrunnlagDto.class);
        when(grunnlagEntitet.getBeregningsgrunnlag()).thenReturn(Optional.of(beregningsgrunnlag));
        when(grunnlagEntitet.getGjeldendeAktiviteter()).thenReturn(beregningAktivitetAggregat);
    }

    @Test
    public void skal_ikkje_lage_arbeidsforhold_dto_om_ingen_refusjon_eller_gradering() {
        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlag(iayGrunnlag);
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());
        assertThat(listeMedGraderingRefusjonDto).isEmpty();
    }

    // Periode 1:
    // Søker refusjon lik 10
    @Test
    public void skal_lage_dto_med_refusjon_for_refusjon_i_heile_perioden() {
        BeregningsgrunnlagPrStatusOgAndelDto.kopier(arbeidstakerAndel).medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlag(iayGrunnlag);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());
        assertThat(listeMedGraderingRefusjonDto).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon()).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom())
            .isEqualTo(periode.getBeregningsgrunnlagPeriodeFom());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getTom()).isNull();
    }

    // Periode 1:
    // Søker refusjon lik 10
    // Periode 2:
    // Opphører refusjon
    @Test
    public void skal_lage_dto_med_refusjon_for_refusjon_med_opphør() {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeDto andrePeriode = BeregningsgrunnlagPeriodeDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.REFUSJON_OPPHØRER)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, null).build(beregningsgrunnlag);
        andelIAndrePeriode.build(andrePeriode);
        BeregningsgrunnlagPeriodeDto.oppdater(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(arbeidstakerAndel).medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlag(iayGrunnlag);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());
        assertThat(listeMedGraderingRefusjonDto).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon()).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom())
            .isEqualTo(periode.getBeregningsgrunnlagPeriodeFom());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getTom())
            .isEqualTo(periode.getBeregningsgrunnlagPeriodeTom());
    }

    // Periode 1:
    // Søker ikke refusjon
    // Periode 2:
    // Søker refusjon lik 10
    @Test
    public void skal_lage_dto_med_refusjon_for_tilkommet_refusjon() {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeDto andrePeriode = BeregningsgrunnlagPeriodeDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, null).build(beregningsgrunnlag);
        andelIAndrePeriode.medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);
        BeregningsgrunnlagPeriodeDto.oppdater(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlag(iayGrunnlag);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());
        assertThat(listeMedGraderingRefusjonDto).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon()).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom())
            .isEqualTo(andrePeriode.getBeregningsgrunnlagPeriodeFom());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getTom()).isNull();
    }

    // Periode 1:
    // Søker refusjon lik 1
    // Periode 2:
    // Søker refusjon lik 10
    @Test
    public void skal_lage_dto_med_refusjon_for_endret_refusjon() {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeDto andrePeriode = BeregningsgrunnlagPeriodeDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, null).build(beregningsgrunnlag);
        andelIAndrePeriode
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);
        BeregningsgrunnlagPeriodeDto.oppdater(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.ONE));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlag(iayGrunnlag);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());
        assertThat(listeMedGraderingRefusjonDto).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon()).hasSize(2);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom())
            .isEqualTo(periode.getBeregningsgrunnlagPeriodeFom());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getTom())
            .isEqualTo(periode.getBeregningsgrunnlagPeriodeTom());

        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(1).isErRefusjon()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(1).isErGradering()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(1).getFom())
            .isEqualTo(andrePeriode.getBeregningsgrunnlagPeriodeFom());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(1).getTom()).isNull();
    }

    private BeregningsgrunnlagRestInput lagInputMedGrunnlag(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        BeregningsgrunnlagRestInput input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);
        input.leggTilBeregningsgrunnlagIHistorikk(grunnlagEntitet, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        return input;
    }

    // Periode 1:
    // Søker refusjon lik 1
    // Periode 2:
    // Opphører refusjon
    // Periode 3:
    // Søker refusjon lik 10
    @Test
    public void skal_lage_dto_med_refusjon_for_endret_refusjon_med_opphør_mellom_perioder() {
        // Arrange

        // Tredje periode (endring i refusjon)
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelITredjePeriode = BeregningsgrunnlagPrStatusOgAndelDto.kopier(arbeidstakerAndel);
        LocalDate tredjePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(2);
        BeregningsgrunnlagPeriodeDto tredjePeriode = BeregningsgrunnlagPeriodeDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(tredjePeriodeFom, null).build(beregningsgrunnlag);
        andelITredjePeriode
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(tredjePeriode);

        // Andre periode (opphør)
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeDto andrePeriode = BeregningsgrunnlagPeriodeDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.REFUSJON_OPPHØRER)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, tredjePeriodeFom.minusDays(1)).build(beregningsgrunnlag);
        andelIAndrePeriode
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.ZERO))
            .build(andrePeriode);

        BeregningsgrunnlagPeriodeDto.oppdater(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.ONE));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlag(iayGrunnlag);

        // Act
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(listeMedGraderingRefusjonDto).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon()).hasSize(2);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom())
            .isEqualTo(periode.getBeregningsgrunnlagPeriodeFom());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getTom())
            .isEqualTo(periode.getBeregningsgrunnlagPeriodeTom());

        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(1).isErRefusjon()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(1).isErGradering()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(1).getFom())
            .isEqualTo(tredjePeriode.getBeregningsgrunnlagPeriodeFom());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(1).getTom()).isNull();
    }

    // 2 Arbeidsforhold for samme arbeidsgiver
    // Periode 1:
    // Arbeidsforhold 1 søker refusjon lik 10
    // Arbeidsforhold 2 søker ikke refusjon
    // Periode 2:
    // Arbeidsforhold 1 søker fortsatt refusjon lik 10
    // Arbeidsforhold 2 søker refusjon
    // Periode 3:
    // Arbeidsforhold 1 søker fortsatt refusjon lik 10
    // Arbeidsforhold 2 opphører refusjon
    @Test
    public void skal_lage_dto_med_refusjon_for_refusjon_med_fleire_perioder_uten_endring() {
        // Arrange
        InternArbeidsforholdRefDto arbeidsforholdId2 = InternArbeidsforholdRefDto.nyRef();
        // Tredje periode (endring i refusjon)
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelITredjePeriode = BeregningsgrunnlagPrStatusOgAndelDto.kopier(arbeidstakerAndel);
        LocalDate tredjePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(2);
        BeregningsgrunnlagPeriodeDto tredjePeriode = BeregningsgrunnlagPeriodeDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(tredjePeriodeFom, null).build(beregningsgrunnlag);
        andelITredjePeriode
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(tredjePeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbeidsforholdId2).medRefusjonskravPrÅr(BigDecimal.ZERO))
            .build(tredjePeriode);

        // Andre periode (opphør)
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeDto andrePeriode = BeregningsgrunnlagPeriodeDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.REFUSJON_OPPHØRER)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, tredjePeriodeFom.minusDays(1)).build(beregningsgrunnlag);
        andelIAndrePeriode
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbeidsforholdId2).medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);

        BeregningsgrunnlagPeriodeDto.builder(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.TEN));
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbeidsforholdId2).medRefusjonskravPrÅr(BigDecimal.ZERO))
            .build(periode);

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlag(iayGrunnlag);

        // Act
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(listeMedGraderingRefusjonDto).hasSize(2);
        FordelBeregningsgrunnlagArbeidsforholdDto arbeidsforhold1 = listeMedGraderingRefusjonDto.stream()
            .filter(a -> a.getArbeidsforholdId().equals(ARBEIDSFORHOLD_REF.getReferanse())).findFirst().orElseThrow();
        FordelBeregningsgrunnlagArbeidsforholdDto arbeidsforhold2 = listeMedGraderingRefusjonDto.stream()
            .filter(a -> a.getArbeidsforholdId().equals(arbeidsforholdId2.getReferanse())).findFirst().orElseThrow();
        assertThat(arbeidsforhold1.getPerioderMedGraderingEllerRefusjon()).hasSize(1);
        assertThat(arbeidsforhold1.getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isTrue();
        assertThat(arbeidsforhold1.getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isFalse();
        assertThat(arbeidsforhold1.getPerioderMedGraderingEllerRefusjon().get(0).getFom()).isEqualTo(periode.getBeregningsgrunnlagPeriodeFom());
        assertThat(arbeidsforhold1.getPerioderMedGraderingEllerRefusjon().get(0).getTom()).isNull();

        assertThat(arbeidsforhold2.getPerioderMedGraderingEllerRefusjon()).hasSize(1);
        assertThat(arbeidsforhold2.getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isTrue();
        assertThat(arbeidsforhold2.getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isFalse();
        assertThat(arbeidsforhold2.getPerioderMedGraderingEllerRefusjon().get(0).getFom()).isEqualTo(andrePeriode.getBeregningsgrunnlagPeriodeFom());
        assertThat(arbeidsforhold2.getPerioderMedGraderingEllerRefusjon().get(0).getTom()).isEqualTo(andrePeriode.getBeregningsgrunnlagPeriodeTom());
    }

    // Periode 1:
    // Ingen søker refusjon
    // Periode 2:
    // Tilkommet arbeidsforhold søker refusjon
    @Test
    public void skal_lage_dto_med_refusjon_for_tilkommet_arbeidsforhold() {
        // Arrange
        var arbeidsforholdId2 = InternArbeidsforholdRefDto.nyRef();

        // Andre periode
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeDto andrePeriode = BeregningsgrunnlagPeriodeDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, null).build(beregningsgrunnlag);
        andelIAndrePeriode
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.ZERO))
            .build(andrePeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbeidsforholdId2).medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);

        // Første periode
        BeregningsgrunnlagPeriodeDto.builder(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.ZERO));
        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlag(iayGrunnlag);

        // Act
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(listeMedGraderingRefusjonDto).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon()).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom())
            .isEqualTo(andrePeriode.getBeregningsgrunnlagPeriodeFom());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getTom()).isNull();
    }

    @Test
    public void skal_lage_dto_med_gradering() {
        // Arrange
        LocalDate graderingTOM = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2);
        Intervall graderingPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, graderingTOM);
        AndelGradering andelGradering = AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiver1)
            .medArbeidsforholdRef(ARBEIDSFORHOLD_REF)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .leggTilGradering(new AndelGradering.Gradering(graderingPeriode, BigDecimal.TEN))
            .build();

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlagOgGradering(andelGradering, iayGrunnlag);

        // Act
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(listeMedGraderingRefusjonDto).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon()).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom())
            .isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.toString());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getTom()).isEqualTo(graderingTOM.toString());
    }

    private BeregningsgrunnlagRestInput lagInputMedGrunnlagOgGradering(AndelGradering andelGradering, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        BeregningsgrunnlagRestInput input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, new AktivitetGradering(andelGradering), List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);
        input.leggTilBeregningsgrunnlagIHistorikk(grunnlagEntitet, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        return input;
    }

    @Test
    public void skal_ikkje_lage_dto_for_andel_med_lagt_til_av_saksbehandler() {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(arbeidstakerAndel).medLagtTilAvSaksbehandler(true);
        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        BeregningsgrunnlagRestInput input = lagInputMedGrunnlag(iayGrunnlag);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, input.getSkjæringstidspunktForBeregning());
        assertThat(listeMedGraderingRefusjonDto).isEmpty();
    }

}
