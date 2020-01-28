package no.nav.folketrygdloven.kalkulator.kontrollerfakta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingSomIkkeKommerDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class ArbeidstakerUtenInntektsmeldingTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final String ORGNR = "3482934982384";
    private static final InternArbeidsforholdRefDto ARB_ID = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    private static final EksternArbeidsforholdRef ARB_ID_EKSTERN = EksternArbeidsforholdRef.ref("A");
    private static final AktørId AKTØR_ID_ARBEIDSGIVER = AktørId.dummy();

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;

    @Before
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .build();
        periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
    }

    @Test
    public void skal_returnere_andeler_uten_inntektsmelding() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        InntektsmeldingSomIkkeKommerDto imSomIkkeKommer = new InntektsmeldingSomIkkeKommerDto(arbeidsgiver, ARB_ID, ARB_ID_EKSTERN);
        InntektArbeidYtelseGrunnlagDto iayGrunnlagMock = mock(InntektArbeidYtelseGrunnlagDto.class);
        when(iayGrunnlagMock.getInntektsmeldingerSomIkkeKommer()).thenReturn(Collections.singletonList(imSomIkkeKommer));
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
            .build(periode);

        // Act
        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
            .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, iayGrunnlagMock);

        // Assert
        assertThat(andelerUtenInntektsmelding).hasSize(1);
    }

    @Test
    public void skal_returnere_andeler_uten_inntektsmelding_privatperson_som_arbeidsgiver() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.person(AKTØR_ID_ARBEIDSGIVER);
        InntektsmeldingSomIkkeKommerDto imSomIkkeKommer = new InntektsmeldingSomIkkeKommerDto(arbeidsgiver, ARB_ID, ARB_ID_EKSTERN);
        InntektArbeidYtelseGrunnlagDto iayGrunnlagMock = mock(InntektArbeidYtelseGrunnlagDto.class);
        when(iayGrunnlagMock.getInntektsmeldingerSomIkkeKommer()).thenReturn(Collections.singletonList(imSomIkkeKommer));
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
            .build(periode);

        // Act
        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
            .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, iayGrunnlagMock);

        // Assert
        assertThat(andelerUtenInntektsmelding).hasSize(1);
    }


    @Test
    public void skal_tom_liste_med_andeler_om_ingen_arbeidstakere_uten_inntektsmelding() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        InntektArbeidYtelseGrunnlagDto iayGrunnlagMock = mock(InntektArbeidYtelseGrunnlagDto.class);
        when(iayGrunnlagMock.getInntektsmeldingerSomIkkeKommer()).thenReturn(Collections.emptyList());
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
            .build(periode);

        // Act
        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
            .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, iayGrunnlagMock);

        // Assert
        assertThat(andelerUtenInntektsmelding).isEmpty();
    }

    @Test
    public void skal_returnere_tom_liste_med_andeler_arbeidstaker_uten_inntektsmelding_status_DP_på_skjæringstidspunktet() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        InntektsmeldingSomIkkeKommerDto imSomIkkeKommer = new InntektsmeldingSomIkkeKommerDto(arbeidsgiver, ARB_ID, ARB_ID_EKSTERN);
        InntektArbeidYtelseGrunnlagDto iayGrunnlagMock = mock(InntektArbeidYtelseGrunnlagDto.class);
        when(iayGrunnlagMock.getInntektsmeldingerSomIkkeKommer()).thenReturn(Collections.singletonList(imSomIkkeKommer));
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER)
            .medInntektskategori(Inntektskategori.DAGPENGER)
            .build(periode);

        // Act
        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
            .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, iayGrunnlagMock);

        // Assert
        assertThat(andelerUtenInntektsmelding).isEmpty();
    }
}
