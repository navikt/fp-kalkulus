package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

class MapBeregningAktiviteterFraVLTilRegelK9Test {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final KoblingReferanseMock KOBLING_REFERANSE = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    public static final String ARBEIDSGIVER_ORGNR = "123456789";
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
    public static final InternArbeidsforholdRefDto NULL_REF = InternArbeidsforholdRefDto.nullRef();
    public static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_ID = InternArbeidsforholdRefDto.nyRef();
    private MapBeregningAktiviteterFraVLTilRegelK9 mapper = new MapBeregningAktiviteterFraVLTilRegelK9();

    @Test
    void skal_mappe_et_arbeidsforhold_med_inntektsmelding_uten_referanse() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusYears(1);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, Collections.emptyList());
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, NULL_REF);

        // Act
        AktivitetStatusModell aktivitetStatusModell = mapForSkjæringstidspunkt(iayGrunnlagBuilder, opptjeningAktiviteterDto);

        // Assert
        var beregningsModell = aktivitetStatusModell.getAktivePerioder();
        assertThat(beregningsModell).hasSize(1);
        var aktivitet = beregningsModell.get(0);
        assertThat(aktivitet.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(aktivitet.getArbeidsforhold().getArbeidsforholdId()).isNull();
    }

    @Test
    void skal_mappe_et_arbeidsforhold_med_inntektsmelding_med_referanse() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusYears(1);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, ARBEIDSFORHOLD_ID, Collections.emptyList());
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, ARBEIDSFORHOLD_ID);

        // Act
        AktivitetStatusModell aktivitetStatusModell = mapForSkjæringstidspunkt(iayGrunnlagBuilder, opptjeningAktiviteterDto);

        // Assert
        var beregningsModell = aktivitetStatusModell.getAktivePerioder();
        assertThat(beregningsModell).hasSize(1);
        var aktivitet = beregningsModell.get(0);
        assertThat(aktivitet.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(aktivitet.getArbeidsforhold().getArbeidsforholdId()).isEqualTo(ARBEIDSFORHOLD_ID.getReferanse());
        assertThat(aktivitet.getPeriode().getTom()).isEqualTo(TIDENES_ENDE);
    }

    @Test
    void skal_mappe_et_arbeidsforhold_med_full_permisjon() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusYears(1);
        var permisjonsPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusDays(1));
        List<PermisjonDtoBuilder> permisjonDtoBuilders = List.of(PermisjonDtoBuilder.ny().medPeriode(permisjonsPeriode).medProsentsats(BigDecimal.valueOf(100)));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, permisjonDtoBuilders);
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, NULL_REF);

        // Act
        AktivitetStatusModell aktivitetStatusModell = mapForSkjæringstidspunkt(iayGrunnlagBuilder, opptjeningAktiviteterDto);

        // Assert
        var beregningsModell = aktivitetStatusModell.getAktivePerioder();
        assertThat(beregningsModell).hasSize(1);
        var aktivitet = beregningsModell.get(0);
        assertThat(aktivitet.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(aktivitet.getArbeidsforhold().getArbeidsforholdId()).isNull();
        assertThat(aktivitet.getPeriode().getTom()).isEqualTo(opptjeningAktiviteterDto.getOpptjeningPerioder().get(0).getPeriode().getTomDato());
    }

    @Test
    void skal_mappe_et_arbeidsforhold_med_full_permisjon_når_andre_aktiviteter_slutter_før_stp() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusYears(1);
        var permisjonsPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusDays(1));
        List<PermisjonDtoBuilder> permisjonDtoBuilders = List.of(PermisjonDtoBuilder.ny().medPeriode(permisjonsPeriode).medProsentsats(BigDecimal.valueOf(100)));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, permisjonDtoBuilders);
        OpptjeningAktiviteterDto.OpptjeningPeriodeDto op1 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMedTilOgMed(ansettelsesDato, ansettelsesDato.plusDays(4)), ARBEIDSGIVER_ORGNR, null, NULL_REF);
        OpptjeningAktiviteterDto.OpptjeningPeriodeDto op2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMed(ansettelsesDato), ARBEIDSGIVER_ORGNR, null, NULL_REF);


        // Act
        AktivitetStatusModell aktivitetStatusModell = mapForSkjæringstidspunkt(iayGrunnlagBuilder, new OpptjeningAktiviteterDto(op1, op2));

        // Assert
        var beregningsModell = aktivitetStatusModell.getAktivePerioder();
        assertThat(beregningsModell).hasSize(1);
        var aktivitet = beregningsModell.get(0);
        assertThat(aktivitet.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(aktivitet.getArbeidsforhold().getArbeidsforholdId()).isNull();
        assertThat(aktivitet.getPeriode().getTom()).isEqualTo(op2.getPeriode().getTomDato());
    }


    @Test
    void skal_mappe_et_arbeidsforhold_med_delvis_permisjon() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusYears(1);
        var permisjonsPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.plusDays(1));
        List<PermisjonDtoBuilder> permisjonDtoBuilders = List.of(PermisjonDtoBuilder.ny().medPeriode(permisjonsPeriode).medProsentsats(BigDecimal.valueOf(60)));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, permisjonDtoBuilders);
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, NULL_REF);

        // Act
        AktivitetStatusModell aktivitetStatusModell = mapForSkjæringstidspunkt(iayGrunnlagBuilder, opptjeningAktiviteterDto);

        // Assert
        var beregningsModell = aktivitetStatusModell.getAktivePerioder();
        assertThat(beregningsModell).hasSize(1);
        var aktivitet = beregningsModell.get(0);
        assertThat(aktivitet.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(aktivitet.getArbeidsforhold().getArbeidsforholdId()).isNull();
        assertThat(aktivitet.getPeriode().getTom()).isEqualTo(TIDENES_ENDE);
    }

    private AktivitetStatusModell mapForSkjæringstidspunkt(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, OpptjeningAktiviteterDto opptjeningAktiviteterDto) {
        var beregningsgrunnlaginput = new BeregningsgrunnlagInput(KOBLING_REFERANSE, iayGrunnlagBuilder.build(), opptjeningAktiviteterDto, null, null);
        var stegInput = new StegProsesseringInput(beregningsgrunnlaginput, BeregningsgrunnlagTilstand.OPPRETTET);
        var input = new FastsettBeregningsaktiviteterInput(stegInput);
        return mapper.mapForSkjæringstidspunkt(input);
    }

    private OpptjeningAktiviteterDto lagOpptjeningsAktivitet(LocalDate ansettelsesDato, InternArbeidsforholdRefDto nullRef) {
        return new OpptjeningAktiviteterDto(List.of(OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMed(ansettelsesDato), ARBEIDSGIVER_ORGNR, null, nullRef)));
    }

    private InntektArbeidYtelseGrunnlagDtoBuilder lagIAY(LocalDate ansettelsesDato, InternArbeidsforholdRefDto arbeidsforholdReferanse, List<PermisjonDtoBuilder> permisjoner) {
        var register = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = register.getAktørArbeidBuilder();
        aktørArbeidBuilder.leggTilYrkesaktivitet(lagYrkesaktivitet(ansettelsesDato, permisjoner));
        register.leggTilAktørArbeid(aktørArbeidBuilder);

        InntektsmeldingDto inntektsmelding = InntektsmeldingDtoBuilder.builder().medArbeidsgiver(ARBEIDSGIVER).medArbeidsforholdId(arbeidsforholdReferanse).medBeløp(BigDecimal.valueOf(300000)).build();
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(inntektsmelding));
        iayGrunnlagBuilder.medData(register);
        return iayGrunnlagBuilder;
    }

    private YrkesaktivitetDto lagYrkesaktivitet(LocalDate ansettelsesDato, List<PermisjonDtoBuilder> permisjoner) {
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        AktivitetsAvtaleDtoBuilder aktivitetsavtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        Intervall periode = Intervall.fraOgMedTilOgMed(ansettelsesDato, TIDENES_ENDE);
        lagAktivitetsavtale(aktivitetsavtaleBuilder, periode);

        AktivitetsAvtaleDtoBuilder ansettelsesPeriode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, true);

        YrkesaktivitetDtoBuilder yrkesaktivitetDtoBuilder = yrkesaktivitetBuilder.medArbeidsgiver(ARBEIDSGIVER)
                .medArbeidsforholdId(ARBEIDSFORHOLD_ID)
                .leggTilAktivitetsAvtale(aktivitetsavtaleBuilder)
                .leggTilAktivitetsAvtale(ansettelsesPeriode)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        permisjoner.forEach(yrkesaktivitetDtoBuilder::leggTilPermisjon);

        return yrkesaktivitetDtoBuilder
                .build();
    }

    private void lagAktivitetsavtale(AktivitetsAvtaleDtoBuilder aktivitetsavtaleBuilder, Intervall periode) {
        aktivitetsavtaleBuilder.medPeriode(periode)
                .medErAnsettelsesPeriode(false);
    }
}
