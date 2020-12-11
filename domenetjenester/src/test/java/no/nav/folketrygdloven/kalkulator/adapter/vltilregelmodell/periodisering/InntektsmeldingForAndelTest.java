package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class InntektsmeldingForAndelTest {

    public static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("64198612");
    public static final BGAndelArbeidsforholdDto ARBEIDSFORHOLD_UTEN_REFERANSE = BGAndelArbeidsforholdDto.builder()
            .medArbeidsgiver(VIRKSOMHET)
            .build();
    public static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_REF1 = InternArbeidsforholdRefDto.nyRef();
    public static final BGAndelArbeidsforholdDto ARBEIDSFORHOLD_MED_REFERANSE1 = BGAndelArbeidsforholdDto.builder()
            .medArbeidsgiver(VIRKSOMHET)
            .medArbeidsforholdRef(ARBEIDSFORHOLD_REF1)
            .build();
    public static final InntektsmeldingDto INNTEKTSMELDING_UTEN_REFERANSE = InntektsmeldingDtoBuilder.builder()
            .medArbeidsgiver(VIRKSOMHET).medBeløp(BigDecimal.TEN).build();
    public static final InntektsmeldingDto INNTEKTSMELDING_MED_REFERANSE1 = InntektsmeldingDtoBuilder.builder()
            .medArbeidsgiver(VIRKSOMHET).medBeløp(BigDecimal.TEN)
            .medArbeidsforholdId(ARBEIDSFORHOLD_REF1).build();
    public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    @Test
    void skal_ha_inntektsmelding_for_andel_uten_referanse_med_yrkesaktivitet_som_går_over_skjæringstidspunktet() {
        // Arrange
        List<InntektsmeldingDto> ims = List.of(INNTEKTSMELDING_UTEN_REFERANSE);
        List<YrkesaktivitetDto> yrkesaktiviteter = List.of(byggYrkesaktivitet(InternArbeidsforholdRefDto.nyRef(), SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusDays(1)));

        // Act
        boolean harInntektsmelding = InntektsmeldingForAndel.harInntektsmeldingForAndel(ARBEIDSFORHOLD_UTEN_REFERANSE, ims, yrkesaktiviteter, SKJÆRINGSTIDSPUNKT);

        // Assert
        assertThat(harInntektsmelding).isTrue();
    }

    @Test
    void skal_ha_inntektsmelding_for_andel_med_referanse_med_yrkesaktivitet_som_går_over_skjæringstidspunktet() {
        // Arrange
        List<InntektsmeldingDto> ims = List.of(INNTEKTSMELDING_MED_REFERANSE1);
        List<YrkesaktivitetDto> yrkesaktiviteter = List.of(byggYrkesaktivitet(ARBEIDSFORHOLD_REF1, SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusDays(1)));

        // Act
        boolean harInntektsmelding = InntektsmeldingForAndel.harInntektsmeldingForAndel(ARBEIDSFORHOLD_MED_REFERANSE1, ims, yrkesaktiviteter, SKJÆRINGSTIDSPUNKT);

        // Assert
        assertThat(harInntektsmelding).isTrue();
    }

    @Test
    void skal_ikke_ha_inntektsmelding_for_andel_uten_referanse_med_yrkesaktivitet_som_starter_på_skjæringstidspunktet() {
        // Arrange
        List<InntektsmeldingDto> ims = List.of(INNTEKTSMELDING_UTEN_REFERANSE);
        List<YrkesaktivitetDto> yrkesaktiviteter = List.of(byggYrkesaktivitet(ARBEIDSFORHOLD_REF1, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(1)));

        // Act
        boolean harInntektsmelding = InntektsmeldingForAndel.harInntektsmeldingForAndel(ARBEIDSFORHOLD_UTEN_REFERANSE, ims, yrkesaktiviteter, SKJÆRINGSTIDSPUNKT);

        // Assert
        assertThat(harInntektsmelding).isFalse();
    }

    @Test
    void skal_ikke_ha_inntektsmelding_for_andel_uten_referanse_med_yrkesaktivitet_som_starter_på_skjæringstidspunktet_inntektsmelding_med_referanse() {
        // Arrange
        List<InntektsmeldingDto> ims = List.of(INNTEKTSMELDING_MED_REFERANSE1);
        List<YrkesaktivitetDto> yrkesaktiviteter = List.of(byggYrkesaktivitet(ARBEIDSFORHOLD_REF1, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(1)));

        // Act
        boolean harInntektsmelding = InntektsmeldingForAndel.harInntektsmeldingForAndel(ARBEIDSFORHOLD_UTEN_REFERANSE, ims, yrkesaktiviteter, SKJÆRINGSTIDSPUNKT);

        // Assert
        assertThat(harInntektsmelding).isFalse();
    }

    @Test
    void skal_ha_inntektsmelding_for_andel_uten_referanse_med_2_yrkesaktiviteter_som_starter_før_og_slutter_etter_stp_inntektsmelding_uten_ref() {
        // Arrange
        List<InntektsmeldingDto> ims = List.of(INNTEKTSMELDING_UTEN_REFERANSE);
        List<YrkesaktivitetDto> yrkesaktiviteter = List.of(
                byggYrkesaktivitet(InternArbeidsforholdRefDto.nyRef(), SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusDays(1)),
                byggYrkesaktivitet(InternArbeidsforholdRefDto.nyRef(), SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusDays(1)));

        // Act
        boolean harInntektsmelding = InntektsmeldingForAndel.harInntektsmeldingForAndel(ARBEIDSFORHOLD_UTEN_REFERANSE, ims, yrkesaktiviteter, SKJÆRINGSTIDSPUNKT);

        // Assert
        assertThat(harInntektsmelding).isTrue();
    }

    @Test
    void skal_ikke_ha_inntektsmelding_for_andel_uten_referanse_med_yrkesaktivitet_som_starter_før_og_slutter_før_stp_inntektsmelding_uten_ref() {
        // Arrange
        List<InntektsmeldingDto> ims = List.of(INNTEKTSMELDING_UTEN_REFERANSE);
        List<YrkesaktivitetDto> yrkesaktiviteter = List.of(
                byggYrkesaktivitet(InternArbeidsforholdRefDto.nyRef(), SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusDays(1)));

        // Act
        boolean harInntektsmelding = InntektsmeldingForAndel.harInntektsmeldingForAndel(ARBEIDSFORHOLD_UTEN_REFERANSE, ims, yrkesaktiviteter, SKJÆRINGSTIDSPUNKT);

        // Assert
        assertThat(harInntektsmelding).isFalse();
    }

    private YrkesaktivitetDto byggYrkesaktivitet(InternArbeidsforholdRefDto internArbeidsforholdRefDto, LocalDate fomDato, LocalDate tomDato) {
        return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(VIRKSOMHET)
                .medArbeidsforholdId(internArbeidsforholdRefDto)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                        .medPeriode(Intervall.fraOgMedTilOgMed(fomDato, tomDato))
                        .medErAnsettelsesPeriode(true))
                .build();
    }

}
