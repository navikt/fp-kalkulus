package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.VirksomhetEntitet;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;


public class FinnStartdatoPermisjonTest {

    private final static String ORGNR = "123456780";
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.fra(new VirksomhetEntitet.Builder().medOrgnr(ORGNR).build());
    private InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
    private LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusMonths(1);

    @Test
    public void finnStartdatoPermisjonNårAktivitetTilkommerEtterStpOgHarInntektsmeldingMedStartdatoEtterAnsettelsesdato() {
        // Arrange
        LocalDate ansettelsesDato = LocalDate.now();
        YrkesaktivitetDto ya = lagYrkesaktivitet(ansettelsesDato);
        LocalDate startPermisjon = ansettelsesDato.plusMonths(1);
        InntektsmeldingDto inntektsmelding = InntektsmeldingDtoBuilder.builder()
            .medArbeidsforholdId(ref)
            .medArbeidsgiver(ARBEIDSGIVER)
            .medStartDatoPermisjon(startPermisjon)
            .build();

        // Act
        LocalDate startDato = FinnStartdatoPermisjon.finnStartdatoPermisjon(ya, SKJÆRINGSTIDSPUNKT, ansettelsesDato, List.of(inntektsmelding));

        // Assert
        assertThat(startDato).isEqualTo(startPermisjon);
    }

    @Test
    public void finnStartdatoPermisjonNårAktivitetTilkommerEtterStpOgHarInntektsmeldingMedStartdatoFørAnsettelsesdato() {
        // Arrange
        LocalDate ansettelsesDato = LocalDate.now();
        YrkesaktivitetDto ya = lagYrkesaktivitet(ansettelsesDato);
        LocalDate startPermisjon = ansettelsesDato.minusMonths(1);
        InntektsmeldingDto inntektsmelding = InntektsmeldingDtoBuilder.builder()
            .medArbeidsforholdId(ref)
            .medArbeidsgiver(ARBEIDSGIVER)
            .medStartDatoPermisjon(startPermisjon)
            .build();

        // Act
        LocalDate startDato = FinnStartdatoPermisjon.finnStartdatoPermisjon(ya, SKJÆRINGSTIDSPUNKT, ansettelsesDato, List.of(inntektsmelding));

        // Assert
        assertThat(startDato).isEqualTo(ansettelsesDato);
    }


    @Test
    public void finnStartdatoPermisjonNårAktivitetTilkommerEtterStpUtenInntektsmelding() {
        // Arrange
        LocalDate ansettelsesDato = LocalDate.now();
        YrkesaktivitetDto ya = lagYrkesaktivitet(ansettelsesDato);

        // Act
        LocalDate startDato = FinnStartdatoPermisjon.finnStartdatoPermisjon(ya, SKJÆRINGSTIDSPUNKT, ansettelsesDato, List.of());

        // Assert
        assertThat(startDato).isEqualTo(ansettelsesDato);
    }

    private YrkesaktivitetDto lagYrkesaktivitet(LocalDate ansettelsesDato) {
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        AktivitetsAvtaleDtoBuilder aktivitetsavtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        Intervall periode = Intervall.fraOgMedTilOgMed(ansettelsesDato, TIDENES_ENDE);
        lagAktivitetsavtale(aktivitetsavtaleBuilder, periode);

        AktivitetsAvtaleDtoBuilder ansettelsesPeriode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, true);

        return yrkesaktivitetBuilder.medArbeidsgiver(ARBEIDSGIVER)
            .medArbeidsforholdId(ref)
            .leggTilAktivitetsAvtale(aktivitetsavtaleBuilder)
            .leggTilAktivitetsAvtale(ansettelsesPeriode)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .build();
    }

    private void lagAktivitetsavtale(AktivitetsAvtaleDtoBuilder aktivitetsavtaleBuilder, Intervall periode) {
        aktivitetsavtaleBuilder.medPeriode(periode)
            .medErAnsettelsesPeriode(false);
    }
}
