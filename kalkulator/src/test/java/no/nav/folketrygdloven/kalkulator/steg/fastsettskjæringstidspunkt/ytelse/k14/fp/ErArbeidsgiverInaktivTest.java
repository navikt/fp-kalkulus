package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k14.fp;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFordelingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektPeriodeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.fpsak.tidsserie.LocalDateInterval;

class ErArbeidsgiverInaktivTest {
    private static final LocalDate STP = LocalDate.of(2021, 10, 1);
    private InntektArbeidYtelseAggregatBuilder data = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
    private InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder arbeidBuilder = data.getAktørArbeidBuilder();
    private InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder ytelseBuilder = data.getAktørYtelseBuilder();
    private InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder inntektBuilder = data.getAktørInntektBuilder();
    private List<InntektsmeldingDto> inntektsmeldinger = new ArrayList<>();

    @Test
    public void er_aktiv_når_iay_mangler() {
        // Act
        var erInaktivt = utled(arbeidsgiver("999999999"), null);

        // Assert
        assertThat(erInaktivt).isFalse();
    }

    @Test
    public void er_aktiv_når_det_er_nyoppstartet() {
        // Arrange
        lagArbeid(arbeidsgiver("999999999"), STP.minusMonths(2), LocalDateInterval.TIDENES_ENDE);

        // Act
        var erInaktivt = utled(arbeidsgiver("999999999"), byggIAY());

        // Assert
        assertThat(erInaktivt).isFalse();
    }

    @Test
    public void er_inaktiv_når_det_er_gammel_uten_inntekt() {
        // Arrange
        lagArbeid(arbeidsgiver("999999999"), STP.minusYears(2), LocalDateInterval.TIDENES_ENDE);

        // Act
        var erInaktivt = utled(arbeidsgiver("999999999"), byggIAY());

        // Assert
        assertThat(erInaktivt).isTrue();
    }

    @Test
    public void er_aktiv_når_det_nylig_er_betalt_inntekt() {
        // Arrange
        lagInntekt(arbeidsgiver("999999999"), STP.minusYears(2), 30);

        // Act
        var erInaktivt = utled(arbeidsgiver("999999999"), byggIAY());

        // Assert
        assertThat(erInaktivt).isFalse();
    }

    @Test
    public void er_inaktivt_når_det_er_lenge_siden_forrige_inntekt_selv_om_det_finnes_i_aareg() {
        // Arrange
        lagInntekt(arbeidsgiver("999999999"), STP.minusYears(2), 12);
        lagArbeid(arbeidsgiver("999999999"), STP.minusYears(2), LocalDateInterval.TIDENES_ENDE);

        // Act
        var erInaktivt = utled(arbeidsgiver("999999999"), byggIAY());

        // Assert
        assertThat(erInaktivt).isTrue();
    }

    @Test
    public void er_aktiv_når_det_er_arbeid_uten_inntekt_men_med_ytelse() {
        // Arrange
        lagArbeid(arbeidsgiver("999999999"), STP.minusYears(2), LocalDateInterval.TIDENES_ENDE);
        lagYtelse(arbeidsgiver("999999999"), STP.minusMonths(2), STP.minusMonths(1), FagsakYtelseType.FORELDREPENGER);

        // Act
        var erInaktivt = utled(arbeidsgiver("999999999"), byggIAY());

        // Assert
        assertThat(erInaktivt).isFalse();
    }

    @Test
    public void er_inaktiv_når_det_er_arbeid_uten_inntekt_med_ytelse_som_ikke_er_relevant_for_vurderingen() {
        // Arrange
        lagArbeid(arbeidsgiver("999999999"), STP.minusYears(2), LocalDateInterval.TIDENES_ENDE);
        lagYtelse(arbeidsgiver("999999999"), STP.minusMonths(2), STP.minusMonths(1), FagsakYtelseType.DAGPENGER);

        // Act
        var erInaktivt = utled(arbeidsgiver("999999999"), byggIAY());

        // Assert
        assertThat(erInaktivt).isTrue();
    }

    @Test
    public void er_aktivt_når_det_har_kommet_inntektsmelding_fra_arbeidsgiver() {
        // Arrange
        lagIM(arbeidsgiver("999999999"));

        // Act
        var erInaktivt = utled(arbeidsgiver("999999999"), byggIAY());

        // Assert
        assertThat(erInaktivt).isFalse();
    }

    private void lagIM(Arbeidsgiver arbeidsgiver) {
        var im = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef())
                .medBeløp(BigDecimal.valueOf(500)).build();
        inntektsmeldinger.add(im);
    }

    private void lagYtelse(Arbeidsgiver ag, LocalDate fom, LocalDate tom, FagsakYtelseType ytelse) {
        YtelseDtoBuilder builder = YtelseDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medYtelseType(ytelse);
        YtelseFordelingDto yf = new YtelseFordelingDto(ag, InntektPeriodeType.DAGLIG, 100, true);
        YtelseGrunnlagDto yg = new YtelseGrunnlagDto(Arbeidskategori.ARBEIDSTAKER, Collections.singletonList(yf));
        builder.medYtelseGrunnlag(yg);
        ytelseBuilder.leggTilYtelse(builder);
    }

    private void lagArbeid(Arbeidsgiver ag, LocalDate fom, LocalDate tom) {
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        var aaBuilder = yaBuilder.getAktivitetsAvtaleBuilder();
        var aa = aaBuilder.medPeriode(Intervall.fraOgMedTilOgMed(fom, tom));
        yaBuilder.leggTilAktivitetsAvtale(aa)
                .medArbeidsgiver(ag)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        arbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
    }

    private void lagInntekt(Arbeidsgiver ag, LocalDate fom, int måneder) {
        InntektDtoBuilder intBuilder = InntektDtoBuilder.oppdatere(Optional.empty());
        intBuilder.medArbeidsgiver(ag).medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING);
        for (int i = 0; i < måneder; i++) {
            LocalDate start = fom.plusMonths(i);
            InntektspostDtoBuilder postBuilder = intBuilder.getInntektspostBuilder();
            postBuilder.medPeriode(start.withDayOfMonth(1), start.with(TemporalAdjusters.lastDayOfMonth()))
                    .medBeløp(BigDecimal.valueOf(100))
                    .medInntektspostType(InntektspostType.LØNN);
            intBuilder.leggTilInntektspost(postBuilder);
        }
        inntektBuilder.leggTilInntekt(intBuilder);
    }

    private Arbeidsgiver arbeidsgiver(String orgnr) {
        return Arbeidsgiver.virksomhet(orgnr);
    }

    private boolean utled(Arbeidsgiver ag, InntektArbeidYtelseGrunnlagDto iay) {
        return ErArbeidsgiverInaktiv.erInaktivt(ag, iay, STP);
    }

    private InntektArbeidYtelseGrunnlagDto byggIAY() {
        data.leggTilAktørArbeid(arbeidBuilder);
        data.leggTilAktørInntekt(inntektBuilder);
        data.leggTilAktørYtelse(ytelseBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medData(data).medInntektsmeldinger(inntektsmeldinger).build();
    }

}
