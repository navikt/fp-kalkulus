package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrgNummer;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.VirksomhetEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.guitjenester.VisningsnavnForAktivitetTjeneste;

public class VisningsnavnForAktivitetTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    public static final String ORGNR = "49382490";
    private static final String VIRKSOMHET_NAVN = "Virksomheten";
    private static final String KUNSTIG_VIRKSOMHET_NAVN = "Kunstig virksomhet";
    private static final VirksomhetEntitet VIRKSOMHETEN = new VirksomhetEntitet.Builder().medOrgnr(ORGNR).medNavn(VIRKSOMHET_NAVN).build();
    private static final VirksomhetEntitet KUNSTIG_VIRKSOMHET = new VirksomhetEntitet.Builder().medOrgnr(OrgNummer.KUNSTIG_ORG)
        .medNavn(KUNSTIG_VIRKSOMHET_NAVN).build();

    private static final String EKSTERN_ARBEIDSFORHOLD_ID = "EKSTERNREF";
    public static final String AKTØR_ID = "1234567890987";

    private BehandlingReferanse ref = new BehandlingReferanseMock();
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;
    private InntektArbeidYtelseGrunnlagDto iayGrunnlagMock = mock(InntektArbeidYtelseGrunnlagDto.class);

    @BeforeEach
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING).medGrunnbeløp(BigDecimal.valueOf(600000)).build();
        periode = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);

        var arbeidsforholdinformasjonMock = mock(ArbeidsforholdInformasjonDto.class);
        when(arbeidsforholdinformasjonMock.finnEkstern(any(Arbeidsgiver.class), any(InternArbeidsforholdRefDto.class))).thenReturn(EksternArbeidsforholdRef.ref("EKSTERNREF"));
        when(iayGrunnlagMock.getArbeidsforholdInformasjon()).thenReturn(Optional.of(arbeidsforholdinformasjonMock));
    }

    @Test
    public void skal_lage_navn_for_brukers_andel() {
        // Arrange
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medArbforholdType(OpptjeningAktivitetType.UDEFINERT)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .build(periode);

        // Act
        String visningsnavn = VisningsnavnForAktivitetTjeneste.lagVisningsnavn(ref, iayGrunnlagMock, andel);

        // Assert
        assertThat(visningsnavn).isEqualTo("Brukers andel");
    }

    @Test
    public void skal_lage_navn_for_arbeid_i_virksomhet_uten_referanse() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.fra(VIRKSOMHETEN);
        arbeidsgiver.setNavn(VIRKSOMHET_NAVN);
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
            .medArbforholdType(OpptjeningAktivitetType.ARBEID)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysningerDtos = new ArrayList<>();
        arbeidsgiverOpplysningerDtos.add(new ArbeidsgiverOpplysningerDto(ORGNR, VIRKSOMHET_NAVN));
        when(iayGrunnlagMock.getArbeidsgiverOpplysninger()).thenReturn(arbeidsgiverOpplysningerDtos);

        // Act
        String visningsnavn = VisningsnavnForAktivitetTjeneste.lagVisningsnavn(ref, iayGrunnlagMock, andel);

        // Assert
        assertThat(visningsnavn).isEqualTo(VIRKSOMHET_NAVN + " (" + ORGNR + ")");
    }

    @Test
    public void skal_lage_navn_for_arbeid_i_virksomhet_med_ekstern_referanse() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.fra(VIRKSOMHETEN);
        arbeidsgiver.setNavn(VIRKSOMHET_NAVN);
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef("123-234-345-456-6556"))
            .medArbforholdType(OpptjeningAktivitetType.ARBEID)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysningerDtos = new ArrayList<>();
        arbeidsgiverOpplysningerDtos.add(new ArbeidsgiverOpplysningerDto(ORGNR, VIRKSOMHET_NAVN));
        when(iayGrunnlagMock.getArbeidsgiverOpplysninger()).thenReturn(arbeidsgiverOpplysningerDtos);

        // Act
        String visningsnavn = VisningsnavnForAktivitetTjeneste.lagVisningsnavn(ref, iayGrunnlagMock, andel);

        // Assert
        assertThat(visningsnavn).isEqualTo(VIRKSOMHET_NAVN + " (" + ORGNR + ") ..." + EKSTERN_ARBEIDSFORHOLD_ID.substring(EKSTERN_ARBEIDSFORHOLD_ID.length()-4));
    }


    @Test
    public void skal_lage_navn_for_privatperson_med_feilende_TPS_kall() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.person(new AktørId(AKTØR_ID));
        arbeidsgiver.setNavn("N/A");
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
                .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);

        List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysningerDtos = new ArrayList<>();
        arbeidsgiverOpplysningerDtos.add(new ArbeidsgiverOpplysningerDto(AKTØR_ID, "N/A"));
        when(iayGrunnlagMock.getArbeidsgiverOpplysninger()).thenReturn(arbeidsgiverOpplysningerDtos);

        // Act
        String visningsnavn = VisningsnavnForAktivitetTjeneste.lagVisningsnavn(ref, iayGrunnlagMock, andel);

        // Assert
        assertThat(visningsnavn).isEqualTo("Privatperson" + " ..." + AKTØR_ID.substring(AKTØR_ID.length()-4));
    }

    @Test
    public void skal_lage_navn_for_kunstig_virksomhet() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.fra(KUNSTIG_VIRKSOMHET);
        arbeidsgiver.setNavn(KUNSTIG_VIRKSOMHET_NAVN);
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
            .medArbforholdType(OpptjeningAktivitetType.ARBEID)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        // Act
        String visningsnavn = VisningsnavnForAktivitetTjeneste.lagVisningsnavn(ref, lagKunstigArbeidsforholdMock(), andel);

        // Assert
        assertThat(visningsnavn).isEqualTo(KUNSTIG_VIRKSOMHET_NAVN + " (" + OrgNummer.KUNSTIG_ORG + ")");
    }

    private InntektArbeidYtelseGrunnlagDto lagKunstigArbeidsforholdMock() {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = mock(InntektArbeidYtelseGrunnlagDto.class);
        ArbeidsforholdOverstyringDto overstyring = mock(ArbeidsforholdOverstyringDto.class);
        when(overstyring.getArbeidsgiverNavn()).thenReturn(KUNSTIG_VIRKSOMHET_NAVN);
        when(overstyring.getArbeidsgiver()).thenReturn(Arbeidsgiver.fra(KUNSTIG_VIRKSOMHET));
        when(iayGrunnlag.getArbeidsforholdOverstyringer()).thenReturn(List.of(overstyring));
        List<ArbeidsgiverOpplysningerDto> list = new ArrayList<>();
        list.add(new ArbeidsgiverOpplysningerDto(KUNSTIG_VIRKSOMHET.getOrgnr(), KUNSTIG_VIRKSOMHET_NAVN));
        when(iayGrunnlag.getArbeidsgiverOpplysninger()).thenReturn(list);
        return iayGrunnlag;
    }
}
