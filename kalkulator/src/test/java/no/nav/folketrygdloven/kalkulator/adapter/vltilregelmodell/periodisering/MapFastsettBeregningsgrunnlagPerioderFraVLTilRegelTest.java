package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.folketrygdloven.kalkulator.OpprettRefusjondatoerFraInntektsmeldinger.opprett;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;


public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2018, Month.JANUARY, 9);
    private static final String ORGNR = "984661185";
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);

    private MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel mapperRefusjonGradering;

    @BeforeEach
    public void setup() {
        mapperRefusjonGradering = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering();
    }

    @Test
    void skal_sortere_på_refusjon() {
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InternArbeidsforholdRefDto arbeidsforholdId2 = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto arbeidsforholdId = InternArbeidsforholdRefDto.nyRef();
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder register = registerBuilder
                .leggTilAktørArbeid(registerBuilder.getAktørArbeidBuilder()
                        .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                                .medArbeidsgiver(arbeidsgiver)
                                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                                        .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10), TIDENES_ENDE))
                                        .medErAnsettelsesPeriode(true))
                                .medArbeidsforholdId(arbeidsforholdId))
                        .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                                .medArbeidsgiver(arbeidsgiver)
                                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                                        .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(10), TIDENES_ENDE))
                                        .medErAnsettelsesPeriode(true))
                                .medArbeidsforholdId(arbeidsforholdId2)));
        iayGrunnlagBuilder.medData(register);
        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(arbeidsforholdId)
                .medRefusjon(BigDecimal.TEN).build();
        iayGrunnlagBuilder.medInntektsmeldinger(im);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktiviteter(SKJÆRINGSTIDSPUNKT, arbeidsgiver);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(arbeidsgiver, beregningAktivitetAggregat, arbeidsforholdId);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        var iayGrunnlag = iayGrunnlagBuilder.build();
        ForeldrepengerGrunnlag foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(100, false);
        foreldrepengerGrunnlag.setAktivitetGradering(new AktivitetGradering(List.of(AndelGradering.builder()
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medGradering(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(1), 50)
                .medArbeidsgiver(arbeidsgiver).build())));
        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag,
                null, opprett(koblingReferanse, iayGrunnlag), foreldrepengerGrunnlag);

        // Act
        PeriodeModell regelmodell = mapRefusjonGradering(input.medBeregningsgrunnlagGrunnlag(grunnlag), beregningsgrunnlag);

        assertThat(regelmodell.getArbeidsforholdOgInntektsmeldinger()).hasSize(2);
        ArbeidsforholdOgInntektsmelding mappetMedRefusjon = regelmodell.getArbeidsforholdOgInntektsmeldinger().stream()
                .filter(a -> a.getArbeidsforhold().getArbeidsforholdId().equals(arbeidsforholdId.getReferanse())).findFirst().get();
        assertThat(mappetMedRefusjon.getRefusjoner()).hasSize(1);
        assertThat(mappetMedRefusjon.getGraderinger()).hasSize(1);
        ArbeidsforholdOgInntektsmelding mappetUtenRefusjon = regelmodell.getArbeidsforholdOgInntektsmeldinger().stream()
                .filter(a -> a.getArbeidsforhold().getArbeidsforholdId() == null).findFirst().get();
        assertThat(mappetUtenRefusjon.getRefusjoner()).hasSize(0);
        assertThat(mappetUtenRefusjon.getGraderinger()).hasSize(0);
    }

    @Test
    public void map_utenGraderingEllerRefusjon() {
        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteter(iayGrunnlagBuilder, List.of(ORGNR));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktiviteter(SKJÆRINGSTIDSPUNKT, arbeidsgiver);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(arbeidsgiver, beregningAktivitetAggregat, InternArbeidsforholdRefDto.nullRef());
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null, List.of(), null);

        // Act
        PeriodeModell regelmodell = mapRefusjonGradering(input.medBeregningsgrunnlagGrunnlag(grunnlag), beregningsgrunnlag);

        // Assert
        assertUtenRefusjonOgGradering(regelmodell);
    }

    @Test
    public void mapRefusjonOgGradering_utenGraderingEllerRefusjon() {
        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteter(iayGrunnlagBuilder, List.of(ORGNR));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktiviteter(SKJÆRINGSTIDSPUNKT, arbeidsgiver);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(arbeidsgiver, beregningAktivitetAggregat, InternArbeidsforholdRefDto.nullRef());
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null, List.of(), null);

        PeriodeModell regelmodell = mapRefusjonGradering(input.medBeregningsgrunnlagGrunnlag(grunnlag), beregningsgrunnlag);

        // Assert
        assertUtenRefusjonOgGradering(regelmodell);
        List<PeriodisertBruttoBeregningsgrunnlag> periodisertBruttoBeregningsgrunnlagList = regelmodell.getPeriodisertBruttoBeregningsgrunnlagList();
        assertThat(periodisertBruttoBeregningsgrunnlagList).hasSize(1);
        assertThat(periodisertBruttoBeregningsgrunnlagList.get(0).getPeriode()).isEqualTo(Periode.of(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE));
        List<BruttoBeregningsgrunnlag> bruttoBeregningsgrunnlagList = periodisertBruttoBeregningsgrunnlagList.get(0).getBruttoBeregningsgrunnlag();
        assertThat(bruttoBeregningsgrunnlagList).hasSize(1);
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlagList.get(0);
        assertThat(bruttoBeregningsgrunnlag.getAktivitetStatus()).isEqualTo(AktivitetStatusV2.AT);
        assertThat(bruttoBeregningsgrunnlag.getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(bruttoBeregningsgrunnlag.getArbeidsforhold().get().getOrgnr()).isEqualTo(ORGNR);
        assertThat(bruttoBeregningsgrunnlag.getArbeidsforhold().get().getArbeidsforholdId()).isNull();
    }

    @Test
    public void testMedRefusjon() {
        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteter(iayGrunnlagBuilder, List.of(ORGNR));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktiviteter(SKJÆRINGSTIDSPUNKT, arbeidsgiver);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(arbeidsgiver, beregningAktivitetAggregat, InternArbeidsforholdRefDto.nullRef());
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(20000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(im1).build();
        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null, List.of(), null);

        // Act
        PeriodeModell regelmodell = mapRefusjonGradering(input.medBeregningsgrunnlagGrunnlag(grunnlag), beregningsgrunnlag);

        // Assert
        assertThat(regelmodell.getArbeidsforholdOgInntektsmeldinger()).hasSize(1);
        ArbeidsforholdOgInntektsmelding arbeidsforhold = regelmodell.getArbeidsforholdOgInntektsmeldinger().get(0);
        assertThat(arbeidsforhold.getRefusjoner()).hasSize(1);
        Refusjonskrav refusjonskrav = arbeidsforhold.getRefusjoner().get(0);
        assertThat(refusjonskrav.getFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(refusjonskrav.getMånedsbeløp()).isEqualByComparingTo(inntekt);
        assertThat(refusjonskrav.getPeriode().getTom()).isEqualTo(TIDENES_ENDE);
        assertThat(arbeidsforhold.getGyldigeRefusjonskrav()).isEmpty();
    }

    @Test
    public void refusjon_med_fleire_ya_før_skjæringstidspunktet_i_samme_org() {
        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteter(iayGrunnlagBuilder, List.of(ORGNR, ORGNR, ORGNR),
            List.of(
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.minusYears(1)),
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(1).plusDays(1), SKJÆRINGSTIDSPUNKT.minusMonths(5)),
                Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(5).plusDays(1), TIDENES_ENDE)));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);

        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktiviteter(SKJÆRINGSTIDSPUNKT, arbeidsgiver);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(arbeidsgiver, beregningAktivitetAggregat, InternArbeidsforholdRefDto.nullRef());
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(60000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);

        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(im1).build();
        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null, List.of(), null);

        // Act
        PeriodeModell regelmodell = mapRefusjonGradering(input.medBeregningsgrunnlagGrunnlag(grunnlag), beregningsgrunnlag);

        // Assert
        assertThat(regelmodell.getArbeidsforholdOgInntektsmeldinger()).hasSize(1);
        ArbeidsforholdOgInntektsmelding arbeidsforhold = regelmodell.getArbeidsforholdOgInntektsmeldinger().get(0);
        assertThat(arbeidsforhold.getRefusjoner()).hasSize(1);
        Refusjonskrav refusjonskrav = arbeidsforhold.getRefusjoner().get(0);
        assertThat(refusjonskrav.getFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(refusjonskrav.getMånedsbeløp()).isEqualByComparingTo(inntekt);
        assertThat(refusjonskrav.getPeriode().getTom()).isEqualTo(TIDENES_ENDE);
        assertThat(arbeidsforhold.getGyldigeRefusjonskrav()).isEmpty();
    }

    @Test
    public void testMedGradering() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        LocalDate tom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteter(iayGrunnlagBuilder, List.of(ORGNR));
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktiviteter(SKJÆRINGSTIDSPUNKT, arbeidsgiver);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(arbeidsgiver, beregningAktivitetAggregat, InternArbeidsforholdRefDto.nullRef());
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        var aktivitetGradering = new AktivitetGradering(List.of(
            AndelGradering.builder().medStatus(AktivitetStatus.ARBEIDSTAKER)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(new OrgNummer(ORGNR)))
                .leggTilGradering(Intervall.fraOgMedTilOgMed(fom, tom), BigDecimal.valueOf(50))
                .build()));

        var iayGrunnlag = iayGrunnlagBuilder.build();
        ForeldrepengerGrunnlag foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(100, false);
        foreldrepengerGrunnlag.setAktivitetGradering(aktivitetGradering);
        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null, List.of(), foreldrepengerGrunnlag);

        PeriodeModell regelmodell = mapRefusjonGradering(input.medBeregningsgrunnlagGrunnlag(grunnlag), beregningsgrunnlag);

        // Assert
        ArbeidsforholdOgInntektsmelding arbeidsforhold = regelmodell.getArbeidsforholdOgInntektsmeldinger().get(0);
        assertThat(arbeidsforhold.getGraderinger()).hasSize(1);
        Gradering gradering = arbeidsforhold.getGraderinger().get(0);
        assertThat(gradering.getFom()).isEqualTo(fom);
        assertThat(gradering.getTom()).isEqualTo(tom);
    }

    @Test
    public void testToArbeidsforholdISammeVirksomhetEtTilkommerEtterSkjæringstidspunkt() {
        // Arrange
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        Intervall arbeidsperiode2 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.plusMonths(2), TIDENES_ENDE);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, ORGNR);
        leggTilYrkesaktivitet(arbeidsperiode2, aktørArbeidBuilder, ORGNR);
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(registerBuilder);
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BigDecimal inntekt = BigDecimal.valueOf(20000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);

        BeregningAktivitetAggregatDto beregningAktivitetAggregat = lagBeregningAktiviteter(SKJÆRINGSTIDSPUNKT, arbeidsgiver);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(arbeidsgiver, beregningAktivitetAggregat, InternArbeidsforholdRefDto.nullRef());
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(im1).build();
        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null, List.of(), null);

        // Act
        PeriodeModell regelmodell = mapRefusjonGradering(input.medBeregningsgrunnlagGrunnlag(grunnlag), beregningsgrunnlag);

        // Assert
        List<ArbeidsforholdOgInntektsmelding> arbeidsforholdOgInntektsmeldinger = regelmodell.getArbeidsforholdOgInntektsmeldinger();
        assertThat(arbeidsforholdOgInntektsmeldinger).hasSize(1);
        ArbeidsforholdOgInntektsmelding arbeidsforhold = arbeidsforholdOgInntektsmeldinger.get(0);
        assertThat(arbeidsforhold.getStartdatoPermisjon()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(arbeidsforhold.getAndelsnr()).isEqualTo(1L);
        assertThat(arbeidsforhold.getRefusjoner()).hasSize(1);
        assertThat(arbeidsforhold.getRefusjoner().get(0).getMånedsbeløp()).isEqualByComparingTo(inntekt);
    }

    private PeriodeModell mapRefusjonGradering(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        return mapperRefusjonGradering.map(input, beregningsgrunnlag);
    }

    private void assertUtenRefusjonOgGradering(PeriodeModell regelmodell) {
        assertThat(regelmodell.getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(regelmodell.getGrunnbeløp()).isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpTestKonstanter.GRUNNBELØP_2018));
        assertThat(regelmodell.getEksisterendePerioder()).hasSize(1);
        assertThat(regelmodell.getAndelGraderinger()).isEmpty();
        assertThat(regelmodell.getArbeidsforholdOgInntektsmeldinger()).hasSize(1);
        ArbeidsforholdOgInntektsmelding arbeidsforhold = regelmodell.getArbeidsforholdOgInntektsmeldinger().get(0);
        assertThat(arbeidsforhold.getRefusjoner()).isEmpty();
        assertThat(arbeidsforhold.getGyldigeRefusjonskrav()).isEmpty();
        assertThat(arbeidsforhold.getInnsendingsdatoFørsteInntektsmeldingMedRefusjon()).isNull();
        assertThat(arbeidsforhold.getAndelsnr()).isEqualTo(1L);
        assertThat(arbeidsforhold.getStartdatoPermisjon()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(arbeidsforhold.getNaturalYtelser()).isEmpty();
        assertThat(arbeidsforhold.getGraderinger()).isEmpty();
        assertThat(arbeidsforhold.getAnsettelsesperiode()).isEqualTo(Periode.of(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE));
        assertThat(arbeidsforhold.getArbeidsforhold().getOrgnr()).isEqualTo(ORGNR);
        assertThat(arbeidsforhold.getAktivitetStatus()).isEqualTo(AktivitetStatusV2.AT);
    }

    private void leggTilYrkesaktiviteter(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, List<String> orgnrs) {
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        for (String orgnr : orgnrs) {
            leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, orgnr);
        }
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(registerBuilder);
    }

    private void leggTilYrkesaktiviteter(InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder, List<String> orgnrs, List<Intervall> perioder) {
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        for (int i = 0; i < orgnrs.size(); i++) {
            leggTilYrkesaktivitet(perioder.get(i), aktørArbeidBuilder, orgnrs.get(i));
        }
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(registerBuilder);
    }

    private static YrkesaktivitetDto leggTilYrkesaktivitet(Intervall arbeidsperiode,
                                                           InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                                           String orgnr) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        AktivitetsAvtaleDtoBuilder aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);
        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsforholdId(InternArbeidsforholdRefDto.nyRef())
            .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
        return yaBuilder.build();
    }

    private BeregningAktivitetAggregatDto lagBeregningAktiviteter(LocalDate skjæringstidspunkt, Arbeidsgiver arbeidsgiver) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(skjæringstidspunkt);
        BeregningAktivitetDto beregningAktivitet = BeregningAktivitetDto.builder()
            .medPeriode(Intervall.fraOgMed(skjæringstidspunkt))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medArbeidsgiver(arbeidsgiver)
            .build();
        builder.leggTilAktivitet(beregningAktivitet);
        return builder.build();
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(Arbeidsgiver arbeidsgiver,
                                                                BeregningAktivitetAggregatDto beregningAktivitetAggregat, InternArbeidsforholdRefDto arbeidsforholdRef) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(BigDecimal.valueOf(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(
                    BeregningsgrunnlagPrStatusOgAndelDto.ny()
                        .medAndelsnr(1L)
                        .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                        .medBeregnetPrÅr(BigDecimal.TEN)
                        .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                                .medArbeidsforholdRef(arbeidsforholdRef)
                            .medArbeidsgiver(arbeidsgiver))))
            .build();
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }
}
