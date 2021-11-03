package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import static no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlagMedTogglePåTest.NULL_REF;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse.OmsorgspengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.verdikjede.VerdikjedeTestHjelper;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.NaturalYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;

@ExtendWith(MockitoExtension.class)
public class ForeslåBeregningsgrunnlagTest {

    private static final String ORGNR = "987123987";
    private static final double MÅNEDSINNTEKT1 = 12345d;
    private static final double MÅNEDSINNTEKT2 = 6000d;
    private static final double ÅRSINNTEKT1 = MÅNEDSINNTEKT1 * 12;
    private static final double ÅRSINNTEKT2 = MÅNEDSINNTEKT2 * 12;
    private static final double NATURALYTELSE_I_PERIODE_2 = 200d;
    private static final double NATURALYTELSE_I_PERIODE_3 = 400d;
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.APRIL, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    private static final String ARBEIDSFORHOLD_ORGNR1 = "654";
    private static final String ARBEIDSFORHOLD_ORGNR2 = "765";
    private static final LocalDate MINUS_YEARS_2 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(2);
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final LocalDate ARBEIDSPERIODE_FOM = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final LocalDate ARBEIDSPERIODE_TOM = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(2);
    private static final String TOGGLE_SPLITTE_SAMMENLIGNING = "fpsak.splitteSammenligningATFL";
    private final UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new ForeldrepengerGrunnlagMapper());
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private AktørId beregningsAkrød1 = AktørId.dummy();
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private VerdikjedeTestHjelper verdikjedeTestHjelper = new VerdikjedeTestHjelper();
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;
    private MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlag = new ForeslåBeregningsgrunnlag(mapBeregningsgrunnlagFraVLTilRegel);

    @BeforeEach
    public void setup() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        beregningsgrunnlag = lagBeregningsgrunnlagAT(true);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagAT(boolean erArbeidsgiverVirksomhet) {
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                        .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                        .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM,
                                erArbeidsgiverVirksomhet ? Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1) : Arbeidsgiver.person(beregningsAkrød1)))
                        .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                        .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                        .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1),
                                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))));
        return beregningsgrunnlagBuilder.build();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagATFL_SN() {
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.KOMBINERT_AT_SN));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                        .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                        .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1)))
                        .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                        .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                        .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1),
                                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1)))
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                        .medArbforholdType(OpptjeningAktivitetType.UDEFINERT)
                        .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                        .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                        .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                                .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                                .medArbeidsperiodeTom(LocalDate.now().plusYears(2)))));
        return beregningsgrunnlagBuilder.build();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagFL() {
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.FRILANSER));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                        .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1)))
                        .medAktivitetStatus(AktivitetStatus.FRILANSER)
                        .medInntektskategori(Inntektskategori.FRILANSER)
                        .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1),
                                SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))));
        return beregningsgrunnlagBuilder.build();
    }

    private BGAndelArbeidsforholdDto.Builder lagBgAndelArbeidsforhold(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver) {
        return BGAndelArbeidsforholdDto.builder().medArbeidsperiodeFom(fom).medArbeidsperiodeTom(tom).medArbeidsgiver(arbeidsgiver);
    }

    private void lagBehandling(BigDecimal inntektSammenligningsgrunnlag,
                               BigDecimal inntektBeregningsgrunnlag, Arbeidsgiver arbeidsgiver, LocalDate fraOgMed, LocalDate tilOgMed, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {

        var inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);

        verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, arbeidsgiver, fraOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), fraOgMed,
                ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            verdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    arbeidsgiver);
            verdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektBeregningsgrunnlag,
                    arbeidsgiver);
        }
        iayGrunnlagBuilder.medData(inntektArbeidYtelseBuilder);
    }

    private void lagBehandlingFL(BigDecimal inntektSammenligningsgrunnlag,
                                 BigDecimal inntektFrilans, String virksomhetOrgnr) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        verdikjedeTestHjelper.initBehandlingFL(inntektSammenligningsgrunnlag, inntektFrilans, virksomhetOrgnr, fraOgMed, tilOgMed, registerBuilder);
        iayGrunnlagBuilder.medData(registerBuilder);
    }

    private void lagKortvarigArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fomDato, LocalDate tomDato) {
        BeregningsgrunnlagPrStatusOgAndelDto andel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleBuilder = YrkesaktivitetDtoBuilder.nyAktivitetsAvtaleBuilder()
                .medPeriode(Intervall.fraOgMedTilOgMed(fomDato, tomDato));
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder
                .oppdatere(Optional.empty()).leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsforholdId(andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef())
                .medArbeidsgiver(andel.getArbeidsgiver().get()).build();
        Optional<InntektArbeidYtelseAggregatDto> registerVersjon = iayGrunnlagBuilder.getKladd().getRegisterVersjon();
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(registerVersjon, VersjonTypeDto.REGISTER);

        Optional<AktørArbeidDto> aktørArbeid = registerVersjon.map(InntektArbeidYtelseAggregatDto::getAktørArbeid);
        YrkesaktivitetDto ya = aktørArbeid.get().hentAlleYrkesaktiviteter()
                .stream()
                .filter(y -> y.equals(yrkesaktivitet))
                .findFirst().get();

        Intervall periode = ya.getAlleAktivitetsAvtaler().iterator().next().getPeriode();
        builder.getAktørArbeidBuilder()
                .getYrkesaktivitetBuilderForNøkkelAvType(new OpptjeningsnøkkelDto(yrkesaktivitet), ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato()), true)
                .medPeriode(Intervall.fraOgMedTilOgMed(fomDato, tomDato));

        iayGrunnlagBuilder.medData(builder);
    }

    @Test
    public void skalLageEnPeriode() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1 + 1000), null, null);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        verifiserSammenligningsgrunnlag(resultat.getBeregningsgrunnlag().getSammenligningsgrunnlag(), ÅRSINNTEKT1,
                SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1),
                BigDecimal.valueOf(81.004455200d));
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), (MÅNEDSINNTEKT1 + 1000) * 12,
                null, null);
    }

    @Test
    public void skalLageEnPeriodeNårNaturalytelseBortfallerPåSkjæringstidspunktet() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_BEREGNING, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_BEREGNING);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    private BeregningsgrunnlagRegelResultat act(BeregningsgrunnlagDto beregningsgrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger, FaktaAggregatDto faktaAggregat) {
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagGrunnlagDtoBuilder bgGrunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medFaktaAggregat(faktaAggregat)
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, OpptjeningAktivitetType.ARBEID));
        ForeslåBeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagForeslåttBeregningsgrunnlagInput(koblingReferanse, bgGrunnlagBuilder, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);
        Map<String, Boolean> toggles = input.getToggles();
        toggles.put(TOGGLE_SPLITTE_SAMMENLIGNING, false);
        return foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);
    }

    private BeregningsgrunnlagRegelResultat act(BeregningsgrunnlagDto beregningsgrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger, OmsorgspengerGrunnlag omsorgspengerGrunnlag) {
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagGrunnlagDtoBuilder bgGrunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, OpptjeningAktivitetType.ARBEID));
        ForeslåBeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagForeslåttBeregningsgrunnlagInput(koblingReferanse, bgGrunnlagBuilder,
                BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag, omsorgspengerGrunnlag);
        Map<String, Boolean> toggles = input.getToggles();
        toggles.put(TOGGLE_SPLITTE_SAMMENLIGNING, false);
        UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new OmsorgspengerGrunnlagMapper());
        var mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);
        var foreslåBeregningsgrunnlag = new ForeslåBeregningsgrunnlag(mapBeregningsgrunnlagFraVLTilRegel);
        return foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);
    }

    @Test
    public void skalLageToPerioderNaturalYtelseBortfaller() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageToPerioderNaturalYtelseTilkommer() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        var im1 = opprettInntektsmeldingNaturalytelseTilkommer(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        var inntektsmeldinger = List.of(im1);

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null,
                NATURALYTELSE_I_PERIODE_2 * 12);
    }

    @Test
    public void skalLageToPerioderKortvarigArbeidsforhold() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                arbeidsgiver, MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagDto nyttGrunnlag = beregningsgrunnlag;
        FaktaAggregatDto faktaAggregat = lagFakta(arbeidsgiver, true, null);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Før steget er det ingen inntekter på andelen
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, faktaAggregat);
        // Her skulle det vært inntekter på andelen
        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);
    }

    @Test
    public void skalLageTrePerioderKortvarigArbeidsforholdOgNaturalYtelse() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                arbeidsgiver, MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagDto nyttGrunnlag = beregningsgrunnlag;
        FaktaAggregatDto faktaAggregat = lagFakta(arbeidsgiver, true, null);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(arbeidsgiver, BigDecimal.valueOf(MÅNEDSINNTEKT1),
                BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3));
        splitBeregningsgrunnlagPeriode(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        var inntektsmeldinger = List.of(im1);

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, faktaAggregat);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(3);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1,
                PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2 * 12, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageEnPeriodeFrilanser() {
        // Arrange
        BeregningsgrunnlagDto grunnlagFL = lagBeregningsgrunnlagFL();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(grunnlagFL.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0))
                .build(grunnlagFL.getBeregningsgrunnlagPerioder().get(0));
        lagBehandlingFL(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), ARBEIDSFORHOLD_ORGNR1);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(grunnlagFL, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1);
        verifiserBGFL(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1);
    }

    @Test
    public void skal_lage_en_periode_for_private_arbeidsgiver() {
        // Arrange
        Arbeidsgiver privateArbeidsgiver = Arbeidsgiver.person(beregningsAkrød1);
        BeregningsgrunnlagDto grunnlagAT = lagBeregningsgrunnlagAT(false);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(grunnlagAT.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0))
                .build(grunnlagAT.getBeregningsgrunnlagPerioder().get(0));
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), privateArbeidsgiver,
                MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(grunnlagAT, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), privateArbeidsgiver, ÅRSINNTEKT1, null, null);
    }

    private void splitBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate nyPeriodeFom, PeriodeÅrsak nyPeriodeÅrsak) {
        List<BeregningsgrunnlagPeriodeDto> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode = perioder.get(perioder.size() - 1);
        if (beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom().equals(nyPeriodeFom)) {
            BeregningsgrunnlagPeriodeDto.oppdater(beregningsgrunnlagPeriode)
                    .leggTilPeriodeÅrsak(nyPeriodeÅrsak);
            return;
        }
        BeregningsgrunnlagPeriodeDto.oppdater(beregningsgrunnlagPeriode)
                .medBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), nyPeriodeFom.minusDays(1));

        BeregningsgrunnlagPeriodeDto.Builder.kopier(beregningsgrunnlagPeriode)
                .medBeregningsgrunnlagPeriode(nyPeriodeFom, null)
                .leggTilPeriodeÅrsak(nyPeriodeÅrsak).build(beregningsgrunnlag);
    }

    @Test
    public void skalLageToPerioderKortvarigArbeidsforholdHvorTomSammenfallerMedBortfallAvNaturalytelse() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                arbeidsgiver, MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagDto nyttGrunnlag = beregningsgrunnlag;
        FaktaAggregatDto faktaAggregat = lagFakta(arbeidsgiver, true, null);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(arbeidsgiver, BigDecimal.valueOf(MÅNEDSINNTEKT1),
                BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        splitBeregningsgrunnlagPeriode(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        var inntektsmeldinger = List.of(im1);

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(nyttGrunnlag, inntektsmeldinger, faktaAggregat);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET,
                PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageBeregningsgrunnlagMedTrePerioder() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1 + MÅNEDSINNTEKT2), BigDecimal.valueOf(MÅNEDSINNTEKT1 + MÅNEDSINNTEKT2),
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.empty())
                .medArbforholdType(OpptjeningAktivitetType.ARBEID)
                .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2)))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
                .build(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0));
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), BigDecimal.valueOf(MÅNEDSINNTEKT2),
                BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2));
        var im2 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_3), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        var inntektsmeldinger = List.of(im1, im2);

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, (FaktaAggregatDto) null);

        // Assert
        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        assertThat(beregningsgrunnlag).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(3);

        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).minusDays(1), 2);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), ÅRSINNTEKT2, null, null);

        periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 2,
                PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), ÅRSINNTEKT2,
                NATURALYTELSE_I_PERIODE_2 * 12, null);

        periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), TIDENES_ENDE, 2, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
                NATURALYTELSE_I_PERIODE_3 * 12, null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), ÅRSINNTEKT2,
                NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageBeregningsgrunnlagMedTrePerioderKortvarigFørNaturalytelse() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                arbeidsgiver, MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        FaktaAggregatDto faktaAggregat = lagFakta(arbeidsgiver, true, null);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        lagKortvarigArbeidsforhold(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2).minusDays(1));
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, faktaAggregat);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(3);

        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3).minusDays(1), 1,
                PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3), TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), arbeidsgiver, ÅRSINNTEKT1, null, null);
    }

    @Test
    public void skalGiEittAvklaringsbehovForSNNyIArbeidslivetOgKortvarigArbeidsforhold() {
        // Arrange
        BeregningsgrunnlagDto nyttGrunnlag = lagBeregningsgrunnlagATFL_SN();
        InntektArbeidYtelseAggregatBuilder register = verdikjedeTestHjelper.initBehandlingFor_AT_SN(BigDecimal.valueOf(12 * MÅNEDSINNTEKT1),
                2014, SKJÆRINGSTIDSPUNKT_BEREGNING, ARBEIDSFORHOLD_ORGNR1,
                BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
                iayGrunnlagBuilder);
        iayGrunnlagBuilder.medData(register);
        FaktaAggregatDto faktaAggregat = lagFakta(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), true, true);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        List<InntektsmeldingDto> inntektsmeldinger = Collections.emptyList();

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(nyttGrunnlag, inntektsmeldinger, faktaAggregat);

        // Assert
        BeregningsgrunnlagDto bg = resultat.getBeregningsgrunnlag();
        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(2);
        bg.getBeregningsgrunnlagPerioder().forEach(p -> assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2));
        List<BeregningAvklaringsbehovResultat> aps = resultat.getAvklaringsbehov();
        List<AvklaringsbehovDefinisjon> apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET);
    }

    @Test
    public void skalIkkeReturnereAvklaringsbehovNårOmsorgspengerOgATMedOver25ProsentAvvikOOgRefusjonTilsvarerBeregnet() {
        // Arrange
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), BigDecimal.valueOf(100));
        UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto = new UtbetalingsgradArbeidsforholdDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforholdDto, List.of(periodeMedUtbetalingsgradDto));
        OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto));
        BigDecimal inntektBeregnet = BigDecimal.valueOf(MÅNEDSINNTEKT1);
        BigDecimal inntektSammenligningsgrunnlag = BigDecimal.valueOf(MÅNEDSINNTEKT1 * 2);
        lagBehandling(inntektSammenligningsgrunnlag, inntektBeregnet,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                inntektBeregnet, null, inntektBeregnet);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        List<BeregningAvklaringsbehovResultat> aps = resultat.getAvklaringsbehov();
        List<AvklaringsbehovDefinisjon> apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skalIkkeReturnereAvklaringsbehovNårOmsorgspengerOgATMedAvvikOver25ProsentAvvikOOgRefusjon6G() {
        // Arrange
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), BigDecimal.valueOf(100));
        UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto = new UtbetalingsgradArbeidsforholdDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforholdDto, List.of(periodeMedUtbetalingsgradDto));
        OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto));
        BigDecimal inntektBeregnet = BigDecimal.valueOf(70_000);
        BigDecimal refusjon = BigDecimal.valueOf(50_000);
        BigDecimal inntektSammenligningsgrunnlag = BigDecimal.valueOf(40_000);
        lagBehandling(inntektSammenligningsgrunnlag, inntektBeregnet,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                inntektBeregnet, null, refusjon);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        List<BeregningAvklaringsbehovResultat> aps = resultat.getAvklaringsbehov();
        List<AvklaringsbehovDefinisjon> apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skalIkkeReturnereAvklaringsbehovNårOmsorgspengerOgATMedAvvikOver25ProsentAvvikOgAvkorting() {
        // Arrange
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), BigDecimal.valueOf(100));
        UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto = new UtbetalingsgradArbeidsforholdDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforholdDto, List.of(periodeMedUtbetalingsgradDto));
        OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto));
        BigDecimal inntektBeregnet = BigDecimal.valueOf(90_000);
        BigDecimal refusjon = BigDecimal.valueOf(90_000);
        BigDecimal inntektSammenligningsgrunnlag = BigDecimal.valueOf(40_000);
        lagBehandling(inntektSammenligningsgrunnlag, inntektBeregnet,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                inntektBeregnet, null, refusjon);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        List<BeregningAvklaringsbehovResultat> aps = resultat.getAvklaringsbehov();
        List<AvklaringsbehovDefinisjon> apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skalReturnereAvklaringsbehovNårOmsorgspengerOgATMedOver25ProsentAvvikOgUtbetalingDirekteTilBruker() {
        // Arrange
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), BigDecimal.valueOf(100));
        UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto = new UtbetalingsgradArbeidsforholdDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforholdDto, List.of(periodeMedUtbetalingsgradDto));
        OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto));
        BigDecimal inntektBeregnet = BigDecimal.valueOf(MÅNEDSINNTEKT1);
        BigDecimal inntektSammenligningsgrunnlag = BigDecimal.valueOf(MÅNEDSINNTEKT1 * 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(MÅNEDSINNTEKT1).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        lagBehandling(inntektSammenligningsgrunnlag, inntektBeregnet,
                Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1))
                .medBeløp(inntektBeregnet)
                .medRefusjon(refusjonskrav)
                .leggTil(new RefusjonDto(inntektBeregnet, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(4)))
                .leggTil(new RefusjonDto(refusjonskrav, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(8)))
                .build();

        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        List<BeregningAvklaringsbehovResultat> aps = resultat.getAvklaringsbehov();
        List<AvklaringsbehovDefinisjon> apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
    }

    @Test
    public void skalReturnereAvklaringsbehovNårOmsorgspengerOgFLMedOver25ProsentAvvik() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagFL();
        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), BigDecimal.valueOf(100));
        UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto = new UtbetalingsgradArbeidsforholdDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.FRILANS);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforholdDto, List.of(periodeMedUtbetalingsgradDto));
        OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto));
        BigDecimal inntektBeregnet = BigDecimal.valueOf(MÅNEDSINNTEKT1);
        BigDecimal inntektSammenligningsgrunnlag = BigDecimal.valueOf(MÅNEDSINNTEKT1 * 2);

        var inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
        for (LocalDate dt = MINUS_YEARS_2.withDayOfMonth(1); dt.isBefore(MINUS_YEARS_1.withDayOfMonth(1).plusYears(2)); dt = dt.plusMonths(1)) {
            verdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1));
            verdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektBeregnet,
                    Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1));
        }
        iayGrunnlagBuilder.medData(inntektArbeidYtelseBuilder);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, List.of(), omsorgspengerGrunnlag);
        // Assert
        List<BeregningAvklaringsbehovResultat> aps = resultat.getAvklaringsbehov();
        List<AvklaringsbehovDefinisjon> apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
    }

    @Test
    public void skalIkkeReturnereAvklaringsbehovNårOmsorgspengerOgFlereArbeidsforholdMedAvvikOgEttKravMedFullRefusjon() {
        // Arrange
        BigDecimal inntektBeregnetArbeidsgiver1 = BigDecimal.valueOf(40_000);
        BigDecimal refusjon = BigDecimal.valueOf(40_000);
        BigDecimal inntektSammenligningsgrunnlagArbeidsgiver1 = BigDecimal.valueOf(40_000);
        BigDecimal inntektBeregnetArbeidsgiver2 = BigDecimal.valueOf(10_000);
        BigDecimal inntektSammenligningsgrunnlagArbeidsgiver2 = BigDecimal.valueOf(80_000);

        LocalDate fraOgMed = MINUS_YEARS_2.withDayOfMonth(1);
        LocalDate tilOgMed = MINUS_YEARS_1.withDayOfMonth(1).plusYears(2);
        Arbeidsgiver Arbeidsgiver1 = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1);
        Arbeidsgiver Arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2);

        PeriodeMedUtbetalingsgradDto periodeMedUtbetalingsgradDto = new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING,
                SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1)), BigDecimal.valueOf(100));
        UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforholdDto = new UtbetalingsgradArbeidsforholdDto(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                InternArbeidsforholdRefDto.nyRef(), UttakArbeidType.ORDINÆRT_ARBEID);
        UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitetDto = new UtbetalingsgradPrAktivitetDto(utbetalingsgradArbeidsforholdDto, List.of(periodeMedUtbetalingsgradDto));
        OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(utbetalingsgradPrAktivitetDto));

        var inntektArbeidYtelseBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);

        verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver1, fraOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), fraOgMed,
                ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            verdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlagArbeidsgiver1,
                    Arbeidsgiver1);
            verdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektBeregnetArbeidsgiver1,
                    Arbeidsgiver1);
        }
        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            verdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlagArbeidsgiver2,
                    Arbeidsgiver2);
            verdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, dt, dt.plusMonths(1), inntektBeregnetArbeidsgiver2,
                    Arbeidsgiver2);
        }
        iayGrunnlagBuilder.medData(inntektArbeidYtelseBuilder);

        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1),
                inntektBeregnetArbeidsgiver1, null, refusjon);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, omsorgspengerGrunnlag);
        // Assert
        List<BeregningAvklaringsbehovResultat> aps = resultat.getAvklaringsbehov();
        List<AvklaringsbehovDefinisjon> apDefs = aps.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    private void verifiserPeriode(BeregningsgrunnlagPeriodeDto periode, LocalDate fom, LocalDate tom, int antallAndeler,
                                  PeriodeÅrsak... forventedePeriodeÅrsaker) {
        assertThat(periode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(fom);
        assertThat(periode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(tom);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(antallAndeler);
        assertThat(periode.getPeriodeÅrsaker()).containsExactlyInAnyOrder(forventedePeriodeÅrsaker);
    }

    private void verifiserSammenligningsgrunnlag(SammenligningsgrunnlagDto sammenligningsgrunnlag, double rapportertPrÅr, LocalDate fom,
                                                 LocalDate tom, BigDecimal avvikPromille) {
        assertThat(sammenligningsgrunnlag.getRapportertPrÅr().doubleValue()).isEqualTo(rapportertPrÅr);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeFom()).isEqualTo(fom);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeTom()).isEqualTo(tom);
        assertThat(sammenligningsgrunnlag.getAvvikPromilleNy().compareTo(avvikPromille)).isEqualTo(0);
    }

    private void verifiserBGAT(BeregningsgrunnlagPrStatusOgAndelDto bgpsa, Arbeidsgiver arbeidsgiver, double årsinntekt,
                               Double naturalytelseBortfaltPrÅr, Double naturalytelseTilkommerPrÅr) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(bgpsa.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                .hasValueSatisfying(virk -> assertThat(virk).isEqualTo(arbeidsgiver));
        assertThat(bgpsa.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold).orElse(false))
                .as("gjelderSpesifiktArbeidsforhold").isFalse();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
        assertThat(bgpsa.getAvkortetPrÅr()).isNull();
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(årsinntekt);
        assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(årsinntekt);
        assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
        if (naturalytelseBortfaltPrÅr == null) {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).as("naturalytelseBortfalt").isEmpty();
        } else {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseBortfaltPrÅr)).as("naturalytelseBortfalt")
                    .hasValueSatisfying(naturalytelse -> assertThat(naturalytelse.doubleValue()).isEqualTo(naturalytelseBortfaltPrÅr));
        }
        if (naturalytelseTilkommerPrÅr == null) {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseTilkommetPrÅr)).as("naturalytelseTilkommer").isEmpty();
        } else {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getNaturalytelseTilkommetPrÅr)).as("naturalytelseTilkommer")
                    .hasValueSatisfying(naturalytelse -> assertThat(naturalytelse.doubleValue()).isEqualTo(naturalytelseTilkommerPrÅr));
        }
        assertThat(bgpsa.getRedusertPrÅr()).isNull();
    }

    private void verifiserBGFL(BeregningsgrunnlagPrStatusOgAndelDto bgpsa, Arbeidsgiver arbeidsgiver, double årsinntekt) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(bgpsa.getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver))
                .hasValueSatisfying(virk -> assertThat(virk).isEqualTo(arbeidsgiver));
        assertThat(bgpsa.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)
                .map(InternArbeidsforholdRefDto::gjelderForSpesifiktArbeidsforhold).orElse(false))
                .as("gjelderSpesifiktArbeidsforhold").isFalse();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.FRILANS);
        assertThat(bgpsa.getAvkortetPrÅr()).isNull();
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(årsinntekt);
        assertThat(bgpsa.getBruttoPrÅr().doubleValue()).as("BruttoPrÅr").isEqualTo(årsinntekt);
        assertThat(bgpsa.getOverstyrtPrÅr()).as("OverstyrtPrÅr").isNull();
        assertThat(bgpsa.getRedusertPrÅr()).isNull();
    }

    private InntektsmeldingDto opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver arbeidsgiver, BigDecimal inntektInntektsmelding, BigDecimal naturalytelseBortfaller,
                                                                             LocalDate naturalytelseBortfallerDato) {
        return verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(arbeidsgiver, inntektInntektsmelding,
                new NaturalYtelseDto(TIDENES_BEGYNNELSE, naturalytelseBortfallerDato.minusDays(1), naturalytelseBortfaller, NaturalYtelseType.ANNET),
                null);
    }

    private InntektsmeldingDto opprettInntektsmeldingNaturalytelseTilkommer(Arbeidsgiver arbeidsgiver, BigDecimal inntektInntektsmelding, BigDecimal naturalytelseTilkommer,
                                                                            LocalDate naturalytelseTilkommerDato) {
        return verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(arbeidsgiver, inntektInntektsmelding,
                new NaturalYtelseDto(naturalytelseTilkommerDato, TIDENES_ENDE, naturalytelseTilkommer, NaturalYtelseType.ANNET), null);
    }

    private FaktaAggregatDto lagFakta(Arbeidsgiver virksomhet, boolean erTidsbegrenset, Boolean erNyIArbeidslivet) {
        return FaktaAggregatDto.builder()
                .medFaktaAktør(erNyIArbeidslivet == null ? null : FaktaAktørDto.builder().medErNyIArbeidslivetSNFastsattAvSaksbehandler(erNyIArbeidslivet).build())
                .erstattEksisterendeEllerLeggTil(new FaktaArbeidsforholdDto.Builder(virksomhet, NULL_REF)
                        .medErTidsbegrensetFastsattAvSaksbehandler(erTidsbegrenset)
                        .build()).build();
    }

}
