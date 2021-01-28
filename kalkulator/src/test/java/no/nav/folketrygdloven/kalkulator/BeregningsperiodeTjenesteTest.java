package no.nav.folketrygdloven.kalkulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.felles.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;

public class BeregningsperiodeTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 1);
    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private Arbeidsgiver arbeidsgiverA = Arbeidsgiver.virksomhet("123456789");
    private Arbeidsgiver arbeidsgiverB = Arbeidsgiver.virksomhet("987654321");
    private BeregningsgrunnlagInput input;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjeneste();


    @BeforeEach
    public void setUp() {
        input = new BeregningsgrunnlagInput(koblingReferanse, null, null, null, null, null);
        input.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
    }

    @Test
    public void skalTesteAtBeregningsperiodeBlirSattRiktig() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 15);

        // Act
        Intervall periode = beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunkt);

        // Assert
        assertThat(periode.getFomDato()).isEqualTo(LocalDate.of(2019, 2, 1));
        assertThat(periode.getTomDato()).isEqualTo(LocalDate.of(2019, 4, 30));
    }

    @Test
    public void skalIkkeSettesPåVentNårIkkeErATFL() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag1SNAndel();

        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(), dagensdato);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalIkkeSettesPåVentNårNåtidErEtterFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(7); // 8. januar
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag1ArbeidstakerAndel(false);

        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(arbeidsgiverA), dagensdato);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalIkkeSettesPåVentNårNåtidErLengeEtterFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(45);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag1ArbeidstakerAndel(false);

        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(arbeidsgiverA), dagensdato);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalAlltidSettesPåVentNårBrukerErFrilanserFørFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(4);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag1FrilansAndel();

        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(), dagensdato);

        // Assert
        assertThat(resultat).isPresent();
        assertThat(resultat).hasValue(LocalDate.of(2019,1,8));
    }

    @Test
    public void skalIkkeSettesPåVentNårHarInntektsmeldingFørFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(3);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag1ArbeidstakerAndel(false);
        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(arbeidsgiverA), dagensdato);
        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalSettesPåVentNårFørFristUtenInntektsmelding() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(4);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag1ArbeidstakerAndel(false);
        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(), dagensdato);

        // Assert
        assertThat(resultat).isPresent();
        assertThat(resultat).hasValue(LocalDate.of(2019,1,8));
    }

    @Test
    public void skalSettesPåVentNårUtenInntektsmeldingFørFristFlereArbeidsforhold() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(4);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag2ArbeidstakerAndeler();

        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(), dagensdato);

        // Assert
        assertThat(resultat).isPresent();
        assertThat(resultat).hasValue(LocalDate.of(2019,1,8));
    }

    @Test
    public void skalSettesPåVentNårHarInntektsmeldingFørFristForBareEttAvFlereArbeidsforhold() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(2);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag2ArbeidstakerAndeler();

        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(arbeidsgiverA), dagensdato);

        // Assert
        assertThat(resultat).isPresent();
        assertThat(resultat).hasValue(LocalDate.of(2019,1,8));
    }

    @Test
    public void skalIkkeSettesPåVentNårAlleHarInntektsmeldingFørFristFlereArbeidsforhold() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(2);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag2ArbeidstakerAndeler();

        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(arbeidsgiverA, arbeidsgiverB), dagensdato);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalIkkeSettesPåVentNårArbeidsforholdUtenInntektsmeldingErLagtTilAvSaksbehandler() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(2);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag1ArbeidstakerAndel(true);
        // Act
        Optional<LocalDate> resultat = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(arbeidsgiverA), dagensdato);
        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skalUtledeRiktigFrist() {
        // Arrange
        LocalDate dagensdato = SKJÆRINGSTIDSPUNKT.plusDays(2);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag2ArbeidstakerAndeler();

        // Act
        Optional<LocalDate> frist = BeregningsperiodeTjeneste.skalVentePåInnrapporteringAvInntekt(input, beregningsgrunnlag, List.of(arbeidsgiverA), dagensdato);

        // Assert
        assertThat(frist).isPresent();
        assertThat(frist).hasValue(SKJÆRINGSTIDSPUNKT.plusDays(7));
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag1FrilansAndel() {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.FRILANSER))
            .build();

        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT.minusMonths(3), SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()))
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .build(periode);
        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag1ArbeidstakerAndel(boolean lagtTilAvSaksbehandler) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();

        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT.minusMonths(3), SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiverA))
            .medKilde(lagtTilAvSaksbehandler ? AndelKilde.SAKSBEHANDLER_KOFAKBER : AndelKilde.PROSESS_START)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);
        return beregningsgrunnlag;
    }


    private BeregningsgrunnlagDto lagBeregningsgrunnlag1SNAndel() {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();

        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(periode);
        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag2ArbeidstakerAndeler() {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();

        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT.minusMonths(3), SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiverA))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT.minusMonths(3), SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiverB))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        return beregningsgrunnlag;
    }

}
