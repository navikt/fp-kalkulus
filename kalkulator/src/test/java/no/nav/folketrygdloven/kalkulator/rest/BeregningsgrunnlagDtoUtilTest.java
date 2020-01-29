package no.nav.folketrygdloven.kalkulator.rest;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;

public class BeregningsgrunnlagDtoUtilTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final BigDecimal GRUNNBELØP = BigDecimal.TEN;
    private static final String PRIVATPERSON_NAVN = "Donald Duck";
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);



    @Test
    public void arbeidsprosenter_for_uavsluttet_periode() {
        // Arrange
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        List<AndelGradering.Gradering> graderinger = new ArrayList<>();
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));

        // Act
        List<BigDecimal> arbeidsandeler = BeregningsgrunnlagDtoUtil.finnArbeidsprosenterIPeriode(graderinger, Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING));

        // Assert
        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1);
    }

    @Test
    public void arbeidsprosenter_for_uavsluttet_periode_og_uavsluttet_gradering() {
        // Arrange
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        List<AndelGradering.Gradering> graderinger = new ArrayList<>();
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, TIDENES_ENDE, arbeidsprosent1));

        // Act
        List<BigDecimal> arbeidsandeler = BeregningsgrunnlagDtoUtil.finnArbeidsprosenterIPeriode(graderinger, Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING));

        // Assert
        assertThat(arbeidsandeler).containsExactly(arbeidsprosent1);
    }

    @Test
    public void arbeidsprosenter_for_samanhengande_gradering_med_hull_på_slutten() {
        // Arrange
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(30);
        BigDecimal arbeidsprosent3 = BigDecimal.valueOf(40);
        BigDecimal arbeidsprosent4 = BigDecimal.valueOf(50);
        List<AndelGradering.Gradering> graderinger = new ArrayList<>();
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), arbeidsprosent2));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3), arbeidsprosent3));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4), arbeidsprosent4));

        // Act
        List<BigDecimal> arbeidsandeler = BeregningsgrunnlagDtoUtil.finnArbeidsprosenterIPeriode(graderinger, Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4).plusDays(1)));

        // Assert
        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1, arbeidsprosent2, arbeidsprosent3, arbeidsprosent4);
    }

    @Test
    public void arbeidsprosenter_for_ikkje_samanhengande_gradering() {
        // Arrange
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(30);
        BigDecimal arbeidsprosent3 = BigDecimal.valueOf(40);
        BigDecimal arbeidsprosent4 = BigDecimal.valueOf(50);
        BigDecimal arbeidsprosent5 = BigDecimal.valueOf(60);
        List<AndelGradering.Gradering> graderinger = new ArrayList<>();
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), arbeidsprosent2));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3), arbeidsprosent3));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4), arbeidsprosent4));
        graderinger.add(new AndelGradering.Gradering(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4).plusDays(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5), arbeidsprosent5));

        // Act
        List<BigDecimal> arbeidsandeler = BeregningsgrunnlagDtoUtil.finnArbeidsprosenterIPeriode(graderinger, Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5)));

        // Assert
        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1, arbeidsprosent2, arbeidsprosent3, arbeidsprosent4, arbeidsprosent5);
    }

    @Test
    public void skal_returnere_empty_om_ingen_opptjeningaktivitet_på_andel() {
        long andelsnr = 1;
        BeregningsgrunnlagRestDto bg = BeregningsgrunnlagRestDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeRestDto periode = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndelRestDto andel = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medArbforholdType(null)
            .build(periode);
        Optional<BeregningsgrunnlagArbeidsforholdDto> arbeidsforhold = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build());
        assertThat(arbeidsforhold.isPresent()).isFalse();
    }

    @Test
    public void skal_returnere_arbeidsforholdDto_om_virksomhet_som_arbeidsgiver_på_andel() {
        long andelsnr = 1;
        String orgnr = "973093681";
        BeregningsgrunnlagRestDto bg = BeregningsgrunnlagRestDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeRestDto periode = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);

        ArbeidsgiverMedNavn virksomhet = ArbeidsgiverMedNavn.virksomhet(orgnr);
        BeregningsgrunnlagPrStatusOgAndelRestDto andel = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto.builder().medArbeidsgiver(virksomhet))
            .build(periode);

        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        builder.medArbeidsgiverOpplysningerDto(List.of(new ArbeidsgiverOpplysningerDto(virksomhet.getIdentifikator(), virksomhet.getNavn(), LocalDate.of(2000, 1, 1))));

        Optional<BeregningsgrunnlagArbeidsforholdDto> arbeidsforhold = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), builder.build());
        assertThat(arbeidsforhold.isPresent()).isTrue();
        assertThat(arbeidsforhold.get().getArbeidsgiverId()).isEqualTo(orgnr);
    }

    @Test
    public void skal_returnere_arbeidsforholdDto_om_privatperson_som_arbeidsgiver_på_andel() {
        long andelsnr = 1;
        BeregningsgrunnlagRestDto bg = BeregningsgrunnlagRestDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeRestDto periode = BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);

        ArbeidsgiverMedNavn person = ArbeidsgiverMedNavn.person(AktørId.dummy());
        BeregningsgrunnlagPrStatusOgAndelRestDto andel = BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdRestDto.builder().medArbeidsgiver(person))
            .build(periode);
        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        builder.medArbeidsgiverOpplysningerDto(List.of(new ArbeidsgiverOpplysningerDto(person.getIdentifikator(), PRIVATPERSON_NAVN, LocalDate.of(2000, 1, 1))));

        Optional<BeregningsgrunnlagArbeidsforholdDto> arbeidsforhold = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), builder.build());
        assertThat(arbeidsforhold.isPresent()).isTrue();
        assertThat(arbeidsforhold.get().getArbeidsgiverId()).isEqualTo("01.01.2000");
        assertThat(arbeidsforhold.get().getArbeidsgiverNavn()).isEqualTo(PRIVATPERSON_NAVN);
    }
}
