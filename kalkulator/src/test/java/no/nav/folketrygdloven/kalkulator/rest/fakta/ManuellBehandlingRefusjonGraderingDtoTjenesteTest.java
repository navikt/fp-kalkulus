package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapArbeidsgiver;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagArbeidstakerAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.VirksomhetEntitet;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;

public class ManuellBehandlingRefusjonGraderingDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.now();
    private final static String ORGNR = "123456780";
    private static final ArbeidsgiverMedNavn ARBEIDSGIVER = ArbeidsgiverMedNavn.fra(new VirksomhetEntitet.Builder().medOrgnr(ORGNR).build());
    private final static String ORGNR2 = "123456781";
    private static final ArbeidsgiverMedNavn ARBEIDSGIVER2 = ArbeidsgiverMedNavn.fra(new VirksomhetEntitet.Builder().medOrgnr(ORGNR2).build());
    public static final int GRUNNBELØP = 90_000;
    private static final long ANDELSNR2 = 2L;


    private BeregningAktivitetAggregatRestDto aktivitetAggregatEntitet;

    @BeforeEach
    public void setUp() {
        aktivitetAggregatEntitet = BeregningAktivitetAggregatRestDto.builder()
                .leggTilAktivitet(lagAktivitet(ARBEIDSGIVER))
                .leggTilAktivitet(lagAktivitet(ARBEIDSGIVER2))
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING).build();
    }

    private BeregningAktivitetRestDto lagAktivitet(ArbeidsgiverMedNavn arbeidsgiver) {
        return BeregningAktivitetRestDto.builder()
                .medArbeidsgiver(arbeidsgiver).medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1))).build();
    }

    @Test
    public void skalKunneEndreInntektEtterRedusertRefusjonTilUnder6G() {
        // Arrange
        AndelGradering graderinger = lagGradering();
        BeregningsgrunnlagRestDto bgFørFordeling = lagBeregningsgrunnlagFørFordeling();

        List<InntektsmeldingDto> inntektsmeldinger = lagInntektsmeldingOver6GRefusjon();

        // Act
        boolean kreverManuellBehandling = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereInntekt(aktivitetAggregatEntitet,
            new AktivitetGradering(graderinger), bgFørFordeling.getBeregningsgrunnlagPerioder().get(0), inntektsmeldinger);

        // Assert
        assertThat(kreverManuellBehandling).isTrue();
    }

    @Test
    public void skalKunneEndreRefusjonEtterRedusertRefusjonTilUnder6G() {
        // Arrange
        AndelGradering graderinger = lagGradering();
        BeregningsgrunnlagRestDto bgFørFordeling = lagBeregningsgrunnlagFørFordeling();
        List<InntektsmeldingDto> inntektsmeldinger = lagInntektsmeldingOver6GRefusjon();

        // Act
        boolean kreverManuellBehandlingAvRefusjon = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereRefusjon(
            aktivitetAggregatEntitet,
            new AktivitetGradering(graderinger),
            bgFørFordeling.getBeregningsgrunnlagPerioder().get(0), inntektsmeldinger, new Beløp(GRUNNBELØP));

        // Assert
        assertThat(kreverManuellBehandlingAvRefusjon).isTrue();
    }

    private no.nav.folketrygdloven.kalkulator.gradering.AndelGradering lagGradering() {
        return no.nav.folketrygdloven.kalkulator.gradering.AndelGradering.builder()
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .leggTilGradering(new AndelGradering.Gradering(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, TIDENES_ENDE), BigDecimal.valueOf(50)))
                .medArbeidsgiver(mapArbeidsgiver(ARBEIDSGIVER2))
                .medAndelsnr(ANDELSNR2).build();
    }

    private BeregningsgrunnlagRestDto lagBeregningsgrunnlagFørFordeling() {
        BeregningsgrunnlagRestDto bg = BeregningsgrunnlagRestDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(new Beløp(GRUNNBELØP))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusRestDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        BeregningsgrunnlagPeriodeRestDto periode = BeregningsgrunnlagPeriodeRestDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto.builder().medArbeidsgiver(ARBEIDSGIVER).medRefusjonskravPrÅr(BigDecimal.valueOf(GRUNNBELØP * 7)))
                .medBeregningsgrunnlagArbeidstakerAndel(BeregningsgrunnlagArbeidstakerAndelRestDto.builder().medHarInntektsmelding(true).build())
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
                .medAndelsnr(ANDELSNR2)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto.builder().medArbeidsgiver(ARBEIDSGIVER2))
                .medBeregningsgrunnlagArbeidstakerAndel(BeregningsgrunnlagArbeidstakerAndelRestDto.builder().medHarInntektsmelding(false).build())
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(BigDecimal.TEN)
                .build(periode);
        return bg;
    }

    private List<InntektsmeldingDto> lagInntektsmeldingOver6GRefusjon() {
        return List.of(InntektsmeldingDtoBuilder.builder().medArbeidsgiver(mapArbeidsgiver(ARBEIDSGIVER)).medRefusjon(BigDecimal.valueOf(GRUNNBELØP * 7)).build());
    }


}
