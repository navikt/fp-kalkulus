package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BeregningsgrunnlagPrStatusOgAndelDto;

class MapBeregningsgrunnlagTest {

    @Test
    public void skal_teste_mapping_med_at_virksomhet() {
        // Arrange
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        BGAndelArbeidsforhold.Builder arbforBuilder = lagArbfor(Arbeidsgiver.virksomhet("999999999"));
        BeregningsgrunnlagPeriode periode = lagPeriode(beregningsgrunnlagEntitet, LocalDate.now(), Intervall.TIDENES_ENDE);
        byggAndel(AktivitetStatus.ARBEIDSTAKER, arbforBuilder, periode);

        // Act
        BeregningsgrunnlagDto dto = MapBeregningsgrunnlag.map(beregningsgrunnlagEntitet);

        assertAtBeregningsgrunnlagErMappetKorrekt(beregningsgrunnlagEntitet, dto);
    }

    @Test
    public void skal_teste_mapping_med_at_privatperson() {
        // Arrange
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        BGAndelArbeidsforhold.Builder arbforBuilder = lagArbfor(Arbeidsgiver.person(new AktørId("9999999999999")));
        BeregningsgrunnlagPeriode periode = lagPeriode(beregningsgrunnlagEntitet, LocalDate.now(), Intervall.TIDENES_ENDE);
        byggAndel(AktivitetStatus.ARBEIDSTAKER, arbforBuilder, periode);

        // Act
        BeregningsgrunnlagDto dto = MapBeregningsgrunnlag.map(beregningsgrunnlagEntitet);

        assertAtBeregningsgrunnlagErMappetKorrekt(beregningsgrunnlagEntitet, dto);
    }

    @Test
    public void skal_teste_mapping_med_SN() {
        // Arrange
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        BeregningsgrunnlagPeriode periode = lagPeriode(beregningsgrunnlagEntitet, LocalDate.now(), Intervall.TIDENES_ENDE);
        byggAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, periode);

        // Act
        BeregningsgrunnlagDto dto = MapBeregningsgrunnlag.map(beregningsgrunnlagEntitet);

        assertAtBeregningsgrunnlagErMappetKorrekt(beregningsgrunnlagEntitet, dto);
    }

    @Test
    public void skal_teste_flere_mapping_med_flere_perioder() {
        // Arrange
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        BeregningsgrunnlagPeriode periode1 = lagPeriode(beregningsgrunnlagEntitet, LocalDate.now().minusDays(300), LocalDate.now().minusDays(200), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        BeregningsgrunnlagPeriode periode2 = lagPeriode(beregningsgrunnlagEntitet, LocalDate.now().minusDays(199), LocalDate.now().minusDays(100), PeriodeÅrsak.GRADERING, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        BeregningsgrunnlagPeriode periode3 = lagPeriode(beregningsgrunnlagEntitet, LocalDate.now().minusDays(99), LocalDate.now().minusDays(0), PeriodeÅrsak.GRADERING_OPPHØRER);

        byggAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, periode1);
        BGAndelArbeidsforhold.Builder arbforBuilder = lagArbfor(Arbeidsgiver.person(new AktørId("9999999999999")));
        byggAndel(AktivitetStatus.ARBEIDSTAKER, arbforBuilder, periode1, 2L);
        byggAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, periode2);
        byggAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, periode3);

        // Act
        BeregningsgrunnlagDto dto = MapBeregningsgrunnlag.map(beregningsgrunnlagEntitet);

        assertAtBeregningsgrunnlagErMappetKorrekt(beregningsgrunnlagEntitet, dto);
    }


    private BeregningsgrunnlagPrStatusOgAndel byggAndel(AktivitetStatus status, BeregningsgrunnlagPeriode periode) {
        return byggAndel(status, null, periode);
    }

    private BeregningsgrunnlagPrStatusOgAndel byggAndel(AktivitetStatus status, BGAndelArbeidsforhold.Builder arbforBuilder, BeregningsgrunnlagPeriode periode) {
        return byggAndel(status, arbforBuilder, periode, 1L);
    }

    private BeregningsgrunnlagPrStatusOgAndel byggAndel(AktivitetStatus status, BGAndelArbeidsforhold.Builder arbforBuilder, BeregningsgrunnlagPeriode periode, Long andelsnr) {
        BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAndelsnr(andelsnr)
                .medAktivitetStatus(status)
                .medFastsattAvSaksbehandler(true)
                .medBeregnetPrÅr(BigDecimal.valueOf(33000))
                .medOverstyrtPrÅr(BigDecimal.valueOf(10000))
                .medFordeltPrÅr(BigDecimal.valueOf(99000))
                .medBeregningsperiode(LocalDate.now().minusMonths(3), LocalDate.now());
        if (arbforBuilder != null) {
            return andelBuilder
                    .medBGAndelArbeidsforhold(arbforBuilder)
                    .build(periode);
        } else {
            return andelBuilder
                    .build(periode);
        }
    }

    private BeregningsgrunnlagPeriode lagPeriode(BeregningsgrunnlagEntitet beregningsgrunnlagEntitet, LocalDate now, LocalDate tidenesEnde, PeriodeÅrsak... årsaker) {
        return BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(LocalDate.now(), Intervall.TIDENES_ENDE)
                .medRedusertPrÅr(BigDecimal.valueOf(35000))
                .medAvkortetPrÅr(BigDecimal.valueOf(66000))
                .medBruttoPrÅr(BigDecimal.valueOf(45756))
                .leggTilPeriodeÅrsaker(Arrays.asList(årsaker))
                .build(beregningsgrunnlagEntitet);
    }

    private BGAndelArbeidsforhold.Builder lagArbfor(Arbeidsgiver arbeidsgiver) {
        return BGAndelArbeidsforhold.builder()
                .medRefusjonskravPrÅr(BigDecimal.valueOf(901000))
                .medNaturalytelseTilkommetPrÅr(BigDecimal.valueOf(200))
                .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(100))
                .medArbeidsperiodeTom(LocalDate.now())
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsgiver(arbeidsgiver);
    }

    private static void assertAtBeregningsgrunnlagErMappetKorrekt(BeregningsgrunnlagEntitet forventet, BeregningsgrunnlagDto faktisk) {
        assertThat(forventet.getSkjæringstidspunkt()).isEqualTo(faktisk.getSkjæringstidspunkt());
        assertThat(forventet.getBeregningsgrunnlagPerioder().size()).isEqualTo(faktisk.getBeregningsgrunnlagPerioder().size());

        List<BeregningsgrunnlagPeriodeDto> faktiskePerioder = faktisk.getBeregningsgrunnlagPerioder();
        forventet.getBeregningsgrunnlagPerioder().forEach(periode -> assertAtPeriodeErMappetKorrekt(periode, faktiskePerioder));
    }

    private static void assertAtPeriodeErMappetKorrekt(BeregningsgrunnlagPeriode forventetPeriode, List<BeregningsgrunnlagPeriodeDto> faktiskePerioder) {
        BeregningsgrunnlagPeriodeDto faktiskPeriode = finnMatchendePeriode(forventetPeriode, faktiskePerioder);
        assertPeriode(forventetPeriode, faktiskPeriode);
    }

    private static void assertPeriode(BeregningsgrunnlagPeriode forventet, BeregningsgrunnlagPeriodeDto faktisk) {
        assertThat(forventet.getBeregningsgrunnlagPeriodeFom()).isEqualTo(faktisk.getBeregningsgrunnlagPeriodeFom());
        assertThat(forventet.getBeregningsgrunnlagPeriodeTom()).isEqualTo(faktisk.getBeregningsgrunnlagPeriodeTom());
        assertThat(forventet.getBruttoPrÅr().compareTo(faktisk.getBruttoPrÅr())).isEqualTo(0);
        assertThat(forventet.getAvkortetPrÅr().compareTo(faktisk.getAvkortetPrÅr())).isEqualTo(0);
        assertThat(forventet.getRedusertPrÅr().compareTo(faktisk.getRedusertPrÅr())).isEqualTo(0);

        assertPeriodeÅrsaker(forventet.getPeriodeÅrsaker(), faktisk.getPeriodeÅrsaker());
        assertLikeAndeler(forventet, faktisk);
    }

    private static void assertPeriodeÅrsaker(List<PeriodeÅrsak> forventet, List<no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak> faktisk) {
        List<String> forventetKoder = forventet.stream().map(PeriodeÅrsak::getKode).collect(Collectors.toList());
        List<String> faktiskKoder = faktisk.stream().map(no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak::getKode).collect(Collectors.toList());

        assertThat(forventetKoder.size()).isEqualTo(faktiskKoder.size());
        assertThat(forventetKoder.containsAll(faktiskKoder)).isTrue();
    }

    private static void assertLikeAndeler(BeregningsgrunnlagPeriode forventet, BeregningsgrunnlagPeriodeDto faktisk) {
        forventet.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel -> finnOgAssertAndel(andel, faktisk.getBeregningsgrunnlagPrStatusOgAndelList()));
    }

    private static void finnOgAssertAndel(BeregningsgrunnlagPrStatusOgAndel forventet, List<BeregningsgrunnlagPrStatusOgAndelDto> faktiskeAndeler) {
        boolean likAndelFinnes = faktiskeAndeler.stream().anyMatch(faktisk -> matcherAndel(forventet, faktisk));
        assertThat(likAndelFinnes).isTrue();
    }

    private static boolean matcherAndel(BeregningsgrunnlagPrStatusOgAndel forventet, BeregningsgrunnlagPrStatusOgAndelDto faktisk) {
        if (!faktisk.getAktivitetStatus().getKode().equals(forventet.getAktivitetStatus().getKode())) {
            return false;
        }
        if (!Objects.equals(faktisk.getArbeidsforholdType().getKode(), forventet.getArbeidsforholdType().getKode())) {
            return false;
        }
        if (!Objects.equals(faktisk.getInntektskategori().getKode(), forventet.getInntektskategori().getKode())) {
            return false;
        }
        if (!Objects.equals(faktisk.getBeregningsperiodeFom(), forventet.getBeregningsperiodeFom())) {
            return false;
        }
        if (!Objects.equals(faktisk.getBeregningsperiodeTom(), forventet.getBeregningsperiodeTom())) {
            return false;
        }
        if (!Objects.equals(faktisk.getBruttoPrÅr(), forventet.getBruttoPrÅr())) {
            return false;
        }
        if (!Objects.equals(faktisk.getDagsats(), forventet.getDagsats())) {
            return false;
        }
        if (!arbeidsforholdMatcher(faktisk.getBgAndelArbeidsforhold(), forventet.getBgAndelArbeidsforhold())) {
            return false;
        }
        return true;
    }

    private static boolean arbeidsforholdMatcher(no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.fastsatt.BGAndelArbeidsforhold faktisk, Optional<BGAndelArbeidsforhold> forventetOpt) {
        if (faktisk == null || forventetOpt.isEmpty()) {
            return faktisk == null && forventetOpt.isEmpty();
        }
        BGAndelArbeidsforhold forventet = forventetOpt.get();

        if (!matcherArbeidsgiver(faktisk.getArbeidsgiver(), forventet.getArbeidsgiver())) {
            return false;
        }
        if (!(Optional.ofNullable(faktisk.getNaturalytelseBortfaltPrÅr()).orElse(BigDecimal.ZERO).compareTo(forventet.getNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO)) == 0)) {
            return false;
        }
        if (!(Optional.ofNullable(faktisk.getNaturalytelseTilkommetPrÅr()).orElse(BigDecimal.ZERO).compareTo(forventet.getNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO)) == 0)) {
            return false;
        }
        if (!(faktisk.getRefusjonskravPrÅr().compareTo(forventet.getGjeldendeRefusjonPrÅr()) == 0)) {
            return false;
        }
        if (!(Objects.equals(faktisk.getArbeidsperiodeFom(), forventet.getArbeidsperiodeFom()))) {
            return false;
        }
        if (!(Objects.equals(faktisk.getArbeidsperiodeTom(), forventet.getArbeidsperiodeTom().orElse(null)))) {
            return false;
        }
        if (!(Objects.equals(faktisk.getArbeidsforholdRef(), forventet.getArbeidsforholdRef().getReferanse()))) {
            return false;
        }
        return true;
    }

    private static boolean matcherArbeidsgiver(no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver faktisk, Arbeidsgiver forventet) {
        if (faktisk == null || forventet == null) {
            return Objects.isNull(faktisk) && Objects.isNull(forventet);
        }
        if (!Objects.equals(faktisk.getArbeidsgiverOrgnr(), forventet.getOrgnr())) {
            return false;
        }
        return forventet.getAktørId() == null ? faktisk.getArbeidsgiverAktørId() == null : Objects.equals(forventet.getAktørId().getId(), faktisk.getArbeidsgiverAktørId());
    }

    private static BeregningsgrunnlagPeriodeDto finnMatchendePeriode(BeregningsgrunnlagPeriode forventetPeriode, List<BeregningsgrunnlagPeriodeDto> faktiskePerioder) {
        return faktiskePerioder.stream()
                .filter(faktiskPeriode -> matcherPeriode(forventetPeriode, faktiskPeriode))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Finner ikke matchende periode etter mapping"));
    }

    private static boolean matcherPeriode(BeregningsgrunnlagPeriode periode, BeregningsgrunnlagPeriodeDto p) {
        return p.getBeregningsgrunnlagPeriodeFom().equals(periode.getBeregningsgrunnlagPeriodeFom())
                && p.getBeregningsgrunnlagPeriodeTom().equals(periode.getBeregningsgrunnlagPeriodeTom());
    }
}
