package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegelK9;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
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
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

public class FastsettBeregningsaktiviteterOMPTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    public static final FagsakYtelseType OMSORGSPENGER = FagsakYtelseType.OMSORGSPENGER;
    private static final KoblingReferanseMock KOBLING_REFERANSE = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT, OMSORGSPENGER);
    public static final String ARBEIDSGIVER_ORGNR = "123456789";
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
    public static final InternArbeidsforholdRefDto NULL_REF = InternArbeidsforholdRefDto.nullRef();
    public static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_ID = InternArbeidsforholdRefDto.nyRef();

    private FastsettBeregningAktiviteter fastsettBeregningAktiviteter = new FastsettBeregningAktiviteter(
            new UnitTestLookupInstanceImpl<>(new MapBeregningAktiviteterFraVLTilRegelK9())
    );

    @Test
    void skal_inkludere_aktiviteter_som_starter_en_dag_skjæringstidspunkt() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusDays(1);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, Collections.emptyList());
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, NULL_REF);

        // Act
        FastsettBeregningsaktiviteterInput input = lagFastsettBeregningsaktiviteterInput(iayGrunnlagBuilder, opptjeningAktiviteterDto);
        BeregningAktivitetAggregatDto beregningAktivitetAggregatDto = fastsettBeregningAktiviteter.fastsettAktiviteter(input);

        // Assert
        assertThat(beregningAktivitetAggregatDto.getBeregningAktiviteter()).hasSize(1);
    }

    @Test
    void skal_inkludere_aktiviteter_som_starter_på_skjæringstidspunkt() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT;
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, Collections.emptyList());
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, NULL_REF);

        // Act
        FastsettBeregningsaktiviteterInput input = lagFastsettBeregningsaktiviteterInput(iayGrunnlagBuilder, opptjeningAktiviteterDto);
        BeregningAktivitetAggregatDto beregningAktivitetAggregatDto = fastsettBeregningAktiviteter.fastsettAktiviteter(input);

        // Assert
        assertThat(beregningAktivitetAggregatDto.getBeregningAktiviteter()).hasSize(1);
    }

    @Test
    void skal_inkludere_aktiviteter_som_starter_to_dager_før_skjæringstidspunkt() {
        // Arrange
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.minusDays(2);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = lagIAY(ansettelsesDato, NULL_REF, Collections.emptyList());
        var opptjeningAktiviteterDto = lagOpptjeningsAktivitet(ansettelsesDato, NULL_REF);

        // Act
        FastsettBeregningsaktiviteterInput input = lagFastsettBeregningsaktiviteterInput(iayGrunnlagBuilder, opptjeningAktiviteterDto);
        BeregningAktivitetAggregatDto beregningAktivitetAggregatDto = fastsettBeregningAktiviteter.fastsettAktiviteter(input);

        // Assert
        assertThat(beregningAktivitetAggregatDto.getBeregningAktiviteter()).hasSize(1);
    }

    private FastsettBeregningsaktiviteterInput lagFastsettBeregningsaktiviteterInput(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, OpptjeningAktiviteterDto opptjeningAktiviteterDto) {
        var beregningsgrunnlaginput = new BeregningsgrunnlagInput(KOBLING_REFERANSE, iayGrunnlagBuilder.build(), opptjeningAktiviteterDto, null, null);
        var stegInput = new StegProsesseringInput(beregningsgrunnlaginput, BeregningsgrunnlagTilstand.OPPRETTET);
        return new FastsettBeregningsaktiviteterInput(stegInput);
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
