package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.SvpGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.SvpTilretteleggingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingMedUtbelingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class AutopunktUtlederSvangerskapspengerTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final Arbeidsgiver ORGNR_1 = Arbeidsgiver.virksomhet("974761076");
    private static final Arbeidsgiver ORGNR_2 = Arbeidsgiver.virksomhet("54321");
    private static final Arbeidsgiver ORGNR_3 = Arbeidsgiver.virksomhet("32145");
    private static final BigDecimal HUNDRE_PROSENT = BigDecimal.valueOf(100);
    private static final BigDecimal NULL_PROSENT = BigDecimal.valueOf(0);


    @Test
    public void skal_teste_like_permisjonsstartdatoer() {
        // Arrange
        PeriodeMedUtbetalingsgradDto periode1 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, HUNDRE_PROSENT);
        PeriodeMedUtbetalingsgradDto periode2 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1), HUNDRE_PROSENT);

        TilretteleggingMedUtbelingsgradDto org1 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, ORGNR_1, periode1, periode2);

        PeriodeMedUtbetalingsgradDto periode3 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, HUNDRE_PROSENT);
        TilretteleggingMedUtbelingsgradDto org2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, ORGNR_2, periode3);

        // Act
        boolean resultat = AutopunktUtlederSvangerskapspengerTjeneste.harSøktForskjelligeStartdatoerForPermisjon(List.of(org1, org2));

        // Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skal_teste_ulike_permisjonsstartdatoer() {
        // Arrange
        PeriodeMedUtbetalingsgradDto periode1 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, NULL_PROSENT);
        PeriodeMedUtbetalingsgradDto periode2 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1), HUNDRE_PROSENT);

        TilretteleggingMedUtbelingsgradDto org1 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_1, periode1, periode2);

        PeriodeMedUtbetalingsgradDto periode3 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, HUNDRE_PROSENT);
        TilretteleggingMedUtbelingsgradDto org2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_2, periode3);

        // Act
        boolean resultat = AutopunktUtlederSvangerskapspengerTjeneste.harSøktForskjelligeStartdatoerForPermisjon(List.of(org1, org2));

        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_teste_flere_ulike_permisjonsstartdatoer() {
        // Arrange
        TilretteleggingMedUtbelingsgradDto org1 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_1,
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, NULL_PROSENT),
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1), HUNDRE_PROSENT));

        TilretteleggingMedUtbelingsgradDto org2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_2,
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1), HUNDRE_PROSENT));

        TilretteleggingMedUtbelingsgradDto org3 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_3,
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(4), BigDecimal.valueOf(1)),
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusDays(3), HUNDRE_PROSENT));

        // Act
        boolean resultat = AutopunktUtlederSvangerskapspengerTjeneste.harSøktForskjelligeStartdatoerForPermisjon(List.of(org1, org2, org3));

        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_teste_endringer_i_utbetalingsgrad_flere_aktiviteter() {
        // Arrange
        TilretteleggingMedUtbelingsgradDto org1 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_1,
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, NULL_PROSENT),
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1), HUNDRE_PROSENT));

        TilretteleggingMedUtbelingsgradDto org2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_2,
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, HUNDRE_PROSENT),
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1), HUNDRE_PROSENT));

        TilretteleggingMedUtbelingsgradDto org3 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_3,
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, BigDecimal.valueOf(1)),
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusDays(19), BigDecimal.valueOf(2)));

        // Act
        boolean resultat = AutopunktUtlederSvangerskapspengerTjeneste.harSøktForskjelligeStartdatoerForPermisjon(List.of(org1, org2, org3));

        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_teste_ingen_endringer_i_utbetalingsgrad_flere_aktiviteter() {
        // Arrange
        TilretteleggingMedUtbelingsgradDto org1 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_1,
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, NULL_PROSENT),
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1), HUNDRE_PROSENT));

        TilretteleggingMedUtbelingsgradDto org2 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_2,
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1), HUNDRE_PROSENT),
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(2), HUNDRE_PROSENT));

        TilretteleggingMedUtbelingsgradDto org3 = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.ORDINÆRT_ARBEID, ORGNR_3,
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, NULL_PROSENT),
            lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT.plusMonths(1), BigDecimal.valueOf(2)));

        // Act
        boolean resultat = AutopunktUtlederSvangerskapspengerTjeneste.harSøktForskjelligeStartdatoerForPermisjon(List.of(org1, org2, org3));

        // Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skal_teste_delvis_svp_uten_refusjon() {
        // Arrange
        var tilrettelegginger = List.of(
            lagTilrettelegging(ORGNR_1.getOrgnr()),
            lagTilrettelegging(true));

        SvpGrunnlagDto SvpGrunnlag = lagSVPEntitet(tilrettelegginger);
        List<InntektsmeldingDto> inntektsmeldinger = List.of(lagInntektsmelding(ORGNR_2.getOrgnr(), true));
        List<SvpTilretteleggingDto> aktuelleTilretteleggingerFiltrert = new TilretteleggingFilterDto(SvpGrunnlag).getAktuelleTilretteleggingerFiltrert();


        // Act
        boolean resultat = AutopunktUtlederSvangerskapspengerTjeneste.harSøktDelvisSVPOgHarRefusjonskrav(aktuelleTilretteleggingerFiltrert, inntektsmeldinger);

        // Assert
        assertThat(resultat).isFalse();
    }

    @Test // PFP-8458
    public void skal_teste_delvis_svp_uten_refusjon_på_arbeidsforhold_det_er_søkt_SVP_for() {
        // Arrange
        var tilrettelegginger = List.of(
            lagTilrettelegging(ORGNR_1.getOrgnr()),
            lagTilrettelegging(true));
        SvpGrunnlagDto SvpGrunnlag = lagSVPEntitet(tilrettelegginger);
        List<InntektsmeldingDto> inntektsmeldinger = List.of(lagInntektsmelding(ORGNR_1.getOrgnr(), false));
        List<SvpTilretteleggingDto> aktuelleTilretteleggingerFiltrert = new TilretteleggingFilterDto(SvpGrunnlag).getAktuelleTilretteleggingerFiltrert();

        // Act
        boolean resultat = AutopunktUtlederSvangerskapspengerTjeneste.harSøktDelvisSVPOgHarRefusjonskrav(aktuelleTilretteleggingerFiltrert, inntektsmeldinger);

        // Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skal_teste_delvis_svp_med_refusjon() {
        // Arrange
        var tilrettelegginger = List.of(
            lagTilrettelegging(ORGNR_1.getOrgnr()),
            lagTilrettelegging(true));
        SvpGrunnlagDto SvpGrunnlag = lagSVPEntitet(tilrettelegginger);
        List<InntektsmeldingDto> inntektsmeldinger = List.of(lagInntektsmelding(ORGNR_1.getOrgnr(), true));
        List<SvpTilretteleggingDto> aktuelleTilretteleggingerFiltrert = new TilretteleggingFilterDto(SvpGrunnlag).getAktuelleTilretteleggingerFiltrert();

        // Act
        boolean resultat = AutopunktUtlederSvangerskapspengerTjeneste.harSøktDelvisSVPOgHarRefusjonskrav(aktuelleTilretteleggingerFiltrert, inntektsmeldinger);

        // Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skal_teste_like_permisjonsstartdatoer_SN_FL() {
        // Arrange
        PeriodeMedUtbetalingsgradDto periode1 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, HUNDRE_PROSENT);
        PeriodeMedUtbetalingsgradDto periode2 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, HUNDRE_PROSENT);
        PeriodeMedUtbetalingsgradDto periode3 = lagPeriodeMedUtbetaling(SKJÆRINGSTIDSPUNKT, HUNDRE_PROSENT);

        TilretteleggingMedUtbelingsgradDto frilans = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.FRILANS, null, periode1, periode2);
        TilretteleggingMedUtbelingsgradDto selvstendigNæringsdrivende = lagTilretteleggingMedUtbelingsgrad(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE, null,
            periode3);

        // Act
        boolean resultat = AutopunktUtlederSvangerskapspengerTjeneste.harSøktForskjelligeStartdatoerForPermisjon(List.of(frilans, selvstendigNæringsdrivende));

        // Assert
        assertThat(resultat).isFalse();
    }

    private TilretteleggingMedUtbelingsgradDto lagTilretteleggingMedUtbelingsgrad(UttakArbeidType uttakArbeidType,
                                                                                  Arbeidsgiver arbeidsgiver,
                                                                                  PeriodeMedUtbetalingsgradDto... perioder) {
        var tilretteleggingArbeidsforhold = new TilretteleggingArbeidsforholdDto(arbeidsgiver, InternArbeidsforholdRefDto.nyRef(), uttakArbeidType);
        return new TilretteleggingMedUtbelingsgradDto(tilretteleggingArbeidsforhold, List.of(perioder));
    }

    private PeriodeMedUtbetalingsgradDto lagPeriodeMedUtbetaling(LocalDate skjæringstidspunkt, BigDecimal utbetalingsgrad) {
        return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(skjæringstidspunkt, skjæringstidspunkt.plusWeeks(1)), utbetalingsgrad);
    }


    private SvpTilretteleggingDto lagTilrettelegging(String orgnr) {
        return new SvpTilretteleggingDto.Builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
            .build();
    }

    private SvpTilretteleggingDto lagTilrettelegging(boolean delvis) {
        var tilrettelegging = new SvpTilretteleggingDto.Builder();
        if (delvis) {
            tilrettelegging.medDelvisTilrettelegging();
        }
        return tilrettelegging.build();
    }

    private SvpGrunnlagDto lagSVPEntitet(List<SvpTilretteleggingDto> tilrettelegginger) {
        return new SvpGrunnlagDto.Builder()
            .medBehandlingId(238412L)
            .medOpprinneligeTilrettelegginger(tilrettelegginger)
            .build();
    }

    private InntektsmeldingDto lagInntektsmelding(String orgnr, boolean refusjon) {
        InntektsmeldingDtoBuilder builder = InntektsmeldingDtoBuilder.builder()
            .medBeløp(BigDecimal.valueOf(10000))
            .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr));
        if (refusjon) {
            builder.medRefusjon(BigDecimal.valueOf(10000));
        }
        return builder.build();
    }

}
