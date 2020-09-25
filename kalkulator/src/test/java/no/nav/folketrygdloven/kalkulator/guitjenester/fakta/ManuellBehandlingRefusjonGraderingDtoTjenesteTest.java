package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagArbeidstakerAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.VirksomhetEntitet;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;

public class ManuellBehandlingRefusjonGraderingDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.now();
    private final static String ORGNR = "123456780";
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.fra(new VirksomhetEntitet.Builder().medOrgnr(ORGNR).build());
    private final static String ORGNR2 = "123456781";
    private static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.fra(new VirksomhetEntitet.Builder().medOrgnr(ORGNR2).build());
    public static final int GRUNNBELØP = 90_000;
    private static final long ANDELSNR2 = 2L;


    private BeregningAktivitetAggregatDto aktivitetAggregatEntitet;

    @BeforeEach
    public void setUp() {
        aktivitetAggregatEntitet = BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(lagAktivitet(ARBEIDSGIVER))
                .leggTilAktivitet(lagAktivitet(ARBEIDSGIVER2))
                .leggTilAktivitet(lagNæring())
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING).build();
    }

    private BeregningAktivitetDto lagAktivitet(Arbeidsgiver arbeidsgiver) {
        return BeregningAktivitetDto.builder()
                .medArbeidsgiver(arbeidsgiver).medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1))).build();
    }

    private BeregningAktivitetDto lagNæring() {
        return BeregningAktivitetDto.builder().medOpptjeningAktivitetType(OpptjeningAktivitetType.NÆRING)
                .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1))).build();
    }


    @Test
    public void skalKunneEndreInntektEtterRedusertRefusjonTilUnder6G() {
        // Arrange
        AndelGradering graderinger = lagGradering();
        BeregningsgrunnlagDto bgFørFordeling = lagBeregningsgrunnlagFørFordeling();

        List<InntektsmeldingDto> inntektsmeldinger = lagInntektsmeldingOver6GRefusjon();

        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(aktivitetAggregatEntitet)
                .medBeregningsgrunnlag(bgFørFordeling).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);

        // Act
        boolean kreverManuellBehandling = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereInntekt(grunnlag,
                new AktivitetGradering(graderinger), bgFørFordeling.getBeregningsgrunnlagPerioder().get(0), bgFørFordeling.getBeregningsgrunnlagPerioder(), inntektsmeldinger);

        // Assert
        assertThat(kreverManuellBehandling).isTrue();
    }


    @Test
    public void skalKunneEndreInntektOmTidligerePeriodeHarGraderingForAndelSomVilBliAvkortetTil0() {
        // Arrange
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1);
        AndelGradering graderingNæring = lagGraderingForNæringFraSTP(graderingTom);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(new Beløp(GRUNNBELØP))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .build();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(bg, SKJÆRINGSTIDSPUNKT_OPPTJENING, graderingTom);
        leggTilArbeidstakerOver6GOgNæring(periode);
        BeregningsgrunnlagPeriodeDto periode2 = lagPeriode(bg, graderingTom.plusDays(1), null);
        leggTilArbeidstakerOver6GOgNæring(periode2);

        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(aktivitetAggregatEntitet)
                .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);

        // Act
        boolean kreverManuellBehandling1 = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereInntekt(
                grunnlag,
                new AktivitetGradering(graderingNæring),
                periode,
                bg.getBeregningsgrunnlagPerioder(),
                List.of());

        boolean kreverManuellBehandling2 = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereInntekt(
                grunnlag,
                new AktivitetGradering(graderingNæring),
                periode2,
                bg.getBeregningsgrunnlagPerioder(),
                List.of());


        // Assert
        assertThat(kreverManuellBehandling1).isTrue();
        assertThat(kreverManuellBehandling2).isTrue();
    }

    @Test
    public void skalKunneEndreRefusjonEtterRedusertRefusjonTilUnder6G() {
        // Arrange
        AndelGradering graderinger = lagGradering();
        BeregningsgrunnlagDto bgFørFordeling = lagBeregningsgrunnlagFørFordeling();
        List<InntektsmeldingDto> inntektsmeldinger = lagInntektsmeldingOver6GRefusjon();

        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(aktivitetAggregatEntitet)
                .medBeregningsgrunnlag(bgFørFordeling).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);

        // Act
        boolean kreverManuellBehandlingAvRefusjon = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereRefusjon(
                grunnlag,
            new AktivitetGradering(graderinger),
            bgFørFordeling.getBeregningsgrunnlagPerioder().get(0), inntektsmeldinger, new Beløp(GRUNNBELØP));

        // Assert
        assertThat(kreverManuellBehandlingAvRefusjon).isTrue();
    }

    private AndelGradering lagGraderingForNæringFraSTP(LocalDate graderingTom) {
        return AndelGradering.builder()
                .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .leggTilGradering(new AndelGradering.Gradering(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING,
                        graderingTom), BigDecimal.valueOf(50)))
                .medAndelsnr(3L)
                .build();
    }

    private BeregningsgrunnlagPeriodeDto lagPeriode(BeregningsgrunnlagDto bg, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(fom, tom).build(bg);
    }

    private void leggTilArbeidstakerOver6GOgNæring(BeregningsgrunnlagPeriodeDto periode) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER).medRefusjonskravPrÅr(BigDecimal.valueOf(GRUNNBELØP * 7)))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(BigDecimal.valueOf(3*GRUNNBELØP))
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(ANDELSNR2)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(BigDecimal.valueOf(3*GRUNNBELØP))
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(3L)
                .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .build(periode);
    }

    private no.nav.folketrygdloven.kalkulator.gradering.AndelGradering lagGradering() {
        return no.nav.folketrygdloven.kalkulator.gradering.AndelGradering.builder()
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .leggTilGradering(new AndelGradering.Gradering(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, TIDENES_ENDE), BigDecimal.valueOf(50)))
                .medArbeidsgiver(ARBEIDSGIVER2)
                .medAndelsnr(ANDELSNR2).build();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagFørFordeling() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(new Beløp(GRUNNBELØP))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(bg, SKJÆRINGSTIDSPUNKT_OPPTJENING, null);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER).medRefusjonskravPrÅr(BigDecimal.valueOf(GRUNNBELØP * 7)))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(ANDELSNR2)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .build(periode);
        return bg;
    }

    private List<InntektsmeldingDto> lagInntektsmeldingOver6GRefusjon() {
        return List.of(InntektsmeldingDtoBuilder.builder().medArbeidsgiver(ARBEIDSGIVER).medRefusjon(BigDecimal.valueOf(GRUNNBELØP * 7)).build());
    }


}
