package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapArbeidsgiver;
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
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.rest.dto.FordelBeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;

public class RefusjonEllerGraderingArbeidsforholdDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    public static final String ORGNR = "7238947234423";
    private static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_REF = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    public static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(600000);

    private final ArbeidsgiverMedNavn arbeidsgiver1 = ArbeidsgiverMedNavn.virksomhet(ORGNR);

    private final FordelBeregningsgrunnlagTjeneste refusjonOgGraderingTjeneste = mock(FordelBeregningsgrunnlagTjeneste.class);
    private final BeregningAktivitetAggregatRestDto beregningAktivitetAggregat = mock(BeregningAktivitetAggregatRestDto.class);

    private BeregningsgrunnlagRestDto beregningsgrunnlag;
    private BeregningsgrunnlagPrStatusOgAndelRestDto arbeidstakerAndel;
    private BGAndelArbeidsforholdRestDto.Builder arbeidsforholdBuilder;
    private BeregningsgrunnlagPeriodeRestDto periode;
    private BehandlingReferanse referanse = new BehandlingReferanseMock();
    private BeregningsgrunnlagGrunnlagRestDto grunnlagEntitet;

    @BeforeEach
    public void setUp() {

        referanse = referanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING).build());
        when(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening()).thenReturn(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        when(beregningAktivitetAggregat.getBeregningAktiviteter()).thenReturn(Collections.emptyList());

        beregningsgrunnlag = BeregningsgrunnlagRestDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING).medGrunnbeløp(GRUNNBELØP)
            .build();
        periode = BeregningsgrunnlagPeriodeRestDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        arbeidsforholdBuilder = BGAndelArbeidsforholdRestDto
            .builder()
            .medArbeidsgiver(arbeidsgiver1)
            .medArbeidsforholdRef(ARBEIDSFORHOLD_REF);
        arbeidstakerAndel = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder)
            .build(periode);
        grunnlagEntitet = mock(BeregningsgrunnlagGrunnlagRestDto.class);
        when(grunnlagEntitet.getBeregningsgrunnlag()).thenReturn(Optional.of(beregningsgrunnlag));
        when(grunnlagEntitet.getGjeldendeAktiviteter()).thenReturn(beregningAktivitetAggregat);
    }

    @Test
    public void skal_ikkje_lage_arbeidsforhold_dto_om_ingen_refusjon_eller_gradering() {
        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());
        assertThat(listeMedGraderingRefusjonDto).isEmpty();
    }

    // Periode 1:
    // Søker refusjon lik 10
    @Test
    public void skal_lage_dto_med_refusjon_for_refusjon_i_heile_perioden() {
        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier(arbeidstakerAndel).medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());
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
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeRestDto andrePeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.REFUSJON_OPPHØRER)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, null).build(beregningsgrunnlag);
        andelIAndrePeriode.build(andrePeriode);
        BeregningsgrunnlagPeriodeRestDto.oppdater(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.oppdatere(arbeidstakerAndel).medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());
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
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeRestDto andrePeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, null).build(beregningsgrunnlag);
        andelIAndrePeriode.medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);
        BeregningsgrunnlagPeriodeRestDto.oppdater(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());
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
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeRestDto andrePeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, null).build(beregningsgrunnlag);
        andelIAndrePeriode
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);
        BeregningsgrunnlagPeriodeRestDto.oppdater(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.ONE));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());
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
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder andelITredjePeriode = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier(arbeidstakerAndel);
        LocalDate tredjePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(2);
        BeregningsgrunnlagPeriodeRestDto tredjePeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(tredjePeriodeFom, null).build(beregningsgrunnlag);
        andelITredjePeriode
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(tredjePeriode);

        // Andre periode (opphør)
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeRestDto andrePeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.REFUSJON_OPPHØRER)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, tredjePeriodeFom.minusDays(1)).build(beregningsgrunnlag);
        andelIAndrePeriode
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.ZERO))
            .build(andrePeriode);

        BeregningsgrunnlagPeriodeRestDto.oppdater(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.ONE));

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);

        // Act
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());

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
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder andelITredjePeriode = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier(arbeidstakerAndel);
        LocalDate tredjePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(2);
        BeregningsgrunnlagPeriodeRestDto tredjePeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(tredjePeriodeFom, null).build(beregningsgrunnlag);
        andelITredjePeriode
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(tredjePeriode);
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbeidsforholdId2).medRefusjonskravPrÅr(BigDecimal.ZERO))
            .build(tredjePeriode);

        // Andre periode (opphør)
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeRestDto andrePeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.REFUSJON_OPPHØRER)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, tredjePeriodeFom.minusDays(1)).build(beregningsgrunnlag);
        andelIAndrePeriode
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbeidsforholdId2).medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);

        BeregningsgrunnlagPeriodeRestDto.builder(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.TEN));
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbeidsforholdId2).medRefusjonskravPrÅr(BigDecimal.ZERO))
            .build(periode);

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);

        // Act
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());

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
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier(arbeidstakerAndel);
        LocalDate andrePeriodeFom = periode.getBeregningsgrunnlagPeriodeFom().plusMonths(1);
        BeregningsgrunnlagPeriodeRestDto andrePeriode = BeregningsgrunnlagPeriodeRestDto.builder()
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV)
            .medBeregningsgrunnlagPeriode(andrePeriodeFom, null).build(beregningsgrunnlag);
        andelIAndrePeriode
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder.medRefusjonskravPrÅr(BigDecimal.ZERO))
            .build(andrePeriode);
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(arbeidsforholdId2).medRefusjonskravPrÅr(BigDecimal.TEN))
            .build(andrePeriode);

        // Første periode
        BeregningsgrunnlagPeriodeRestDto.builder(periode).medBeregningsgrunnlagPeriode(periode.getBeregningsgrunnlagPeriodeFom(), andrePeriodeFom.minusDays(1));
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.oppdatere(arbeidstakerAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto
                .builder()
                .medArbeidsgiver(arbeidsgiver1)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF).medRefusjonskravPrÅr(BigDecimal.ZERO));
        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);

        // Act
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());

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
            .medArbeidsgiver(mapArbeidsgiver(arbeidsgiver1))
            .medArbeidsforholdRef(ARBEIDSFORHOLD_REF)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .leggTilGradering(new AndelGradering.Gradering(graderingPeriode, BigDecimal.TEN))
            .build();

        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, new AktivitetGradering(andelGradering), List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);

        // Act
        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());

        // Assert
        assertThat(listeMedGraderingRefusjonDto).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon()).hasSize(1);
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErRefusjon()).isFalse();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).isErGradering()).isTrue();
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom())
            .isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.toString());
        assertThat(listeMedGraderingRefusjonDto.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getTom()).isEqualTo(graderingTOM.toString());
    }

    @Test
    public void skal_ikkje_lage_dto_for_andel_med_lagt_til_av_saksbehandler() {
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.oppdatere(arbeidstakerAndel).medLagtTilAvSaksbehandler(true);
        var iayGrunnlag =  InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build();
        var input = new BeregningsgrunnlagRestInput(referanse, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlagEntitet);

        List<FordelBeregningsgrunnlagArbeidsforholdDto> listeMedGraderingRefusjonDto = RefusjonEllerGraderingArbeidsforholdDtoTjeneste
            .lagListeMedDtoForArbeidsforholdSomSøkerRefusjonEllerGradering(input, new Beløp(GRUNNBELØP), input.getSkjæringstidspunktForBeregning());
        assertThat(listeMedGraderingRefusjonDto).isEmpty();
    }

}
