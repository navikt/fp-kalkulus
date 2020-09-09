package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;

public class MapInntektsgrunnlagVLTilRegelTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.now();
    public static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("94632432");
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();

    @Test
    public void skal_mappe_inntektsmelding_for_arbeid_med_fleire_yrkesaktiviteter() {
        // Arrange
        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(BigDecimal.TEN)
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        Intervall p1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12),
                SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1));
        Intervall p2 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12),
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIAYGrunnlagMedArbeidIPerioder(
                List.of(p1, p2),
                List.of(im));

        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(koblingReferanse, null, iayGrunnlag, 100, 2);

        // Act
        Inntektsgrunnlag map = mapInntektsgrunnlagVLTilRegel.map(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        assertThat(map.getPeriodeinntekter().size()).isEqualTo(1);
    }


    @Test
    public void skal_ikkje_mappe_inntektsmelding_for_arbeid_som_slutter_dagen_før_skjæringstidspunktet() {
        // Arrange
        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(BigDecimal.TEN)
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIAYGrunnlagMedArbeidIPeriode(
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1)),
                List.of(im));


        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(koblingReferanse, null, iayGrunnlag, 100, 2);

        // Act
        Inntektsgrunnlag map = mapInntektsgrunnlagVLTilRegel.map(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        assertThat(map.getPeriodeinntekter()).isEmpty();
    }

    @Test
    public void skal_mappe_inntektsmelding_for_arbeid_som_slutter_på_skjæringstidspunktet() {
        // Arrange
        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(BigDecimal.TEN)
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIAYGrunnlagMedArbeidIPeriode(
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING),
                List.of(im));

        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(koblingReferanse, null, iayGrunnlag, 100, 2);


        // Act
        Inntektsgrunnlag map = mapInntektsgrunnlagVLTilRegel.map(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        assertThat(map.getPeriodeinntekter().size()).isEqualTo(1);
    }

    @Test
    public void skal_mappe_inntektsmelding_for_arbeid_som_slutter_dagen_etter_skjæringstidspunktet() {
        // Arrange
        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medBeløp(BigDecimal.TEN)
                .medArbeidsgiver(VIRKSOMHET)
                .build();
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIAYGrunnlagMedArbeidIPeriode(
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1)),
                List.of(im));

        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(koblingReferanse, null, iayGrunnlag, 100, 2);

        // Act
        Inntektsgrunnlag map = mapInntektsgrunnlagVLTilRegel.map(input, SKJÆRINGSTIDSPUNKT_BEREGNING);

        assertThat(map.getPeriodeinntekter().size()).isEqualTo(1);
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYGrunnlagMedArbeidIPeriode(Intervall periode,
                                                                           List<InntektsmeldingDto> inntektsmeldinger) {
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder(koblingReferanse.getAktørId());
        aktørArbeidBuilder.leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(periode))
                .medArbeidsgiver(VIRKSOMHET)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef()));
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(inntektsmeldinger)
                .medData(registerBuilder).build();
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYGrunnlagMedArbeidIPerioder(List<Intervall> perioder,
                                                                            List<InntektsmeldingDto> inntektsmeldinger) {
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder(koblingReferanse.getAktørId());
        perioder.forEach(periode -> aktørArbeidBuilder.leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(periode))
                .medArbeidsgiver(VIRKSOMHET)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef()))
        );
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medInntektsmeldinger(inntektsmeldinger)
                .medData(registerBuilder).build();
    }

}
