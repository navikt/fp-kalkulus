package no.nav.folketrygdloven.kalkulator.rest.fakta;

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
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING).build();
    }

    private BeregningAktivitetDto lagAktivitet(Arbeidsgiver arbeidsgiver) {
        return BeregningAktivitetDto.builder()
                .medArbeidsgiver(arbeidsgiver).medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1))).build();
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
                new AktivitetGradering(graderinger), bgFørFordeling.getBeregningsgrunnlagPerioder().get(0), inntektsmeldinger);

        // Assert
        assertThat(kreverManuellBehandling).isTrue();
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
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER).medRefusjonskravPrÅr(BigDecimal.valueOf(GRUNNBELØP * 7)))
                .medBeregningsgrunnlagArbeidstakerAndel(BeregningsgrunnlagArbeidstakerAndelDto.builder().medHarInntektsmelding(true).build())
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medAndelsnr(ANDELSNR2)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2))
                .medBeregningsgrunnlagArbeidstakerAndel(BeregningsgrunnlagArbeidstakerAndelDto.builder().medHarInntektsmelding(false).build())
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
