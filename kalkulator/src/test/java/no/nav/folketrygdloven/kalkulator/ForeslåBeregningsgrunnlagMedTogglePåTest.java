package no.nav.folketrygdloven.kalkulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ForeldrepengerGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.verdikjede.BeregningTjenesteProvider;
import no.nav.folketrygdloven.kalkulator.verdikjede.VerdikjedeTestHjelper;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.NaturalYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;
import no.nav.vedtak.konfig.Tid;

@ExtendWith(MockitoExtension.class)
public class ForeslåBeregningsgrunnlagMedTogglePåTest {

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

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private AktørId beregningsAkrød1 = AktørId.dummy();
    private BeregningsgrunnlagDto beregningsgrunnlag;

    private VerdikjedeTestHjelper verdikjedeTestHjelper = new VerdikjedeTestHjelper();
    private static final String TOGGLE_SPLITTE_SAMMENLIGNING = "fpsak.splitteSammenligningATFL";
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;
    private static BeregningsgrunnlagInput input;
    private MapInntektsgrunnlagVLTilRegel mapInntektsgrunnlagVLTilRegel = new MapInntektsgrunnlagVLTilRegelFelles();
    private final UnitTestLookupInstanceImpl<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper = new UnitTestLookupInstanceImpl<>(new ForeldrepengerGrunnlagMapper());
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(new UnitTestLookupInstanceImpl<>(mapInntektsgrunnlagVLTilRegel), ytelsesSpesifikkMapper);
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlag = new ForeslåBeregningsgrunnlag(mapBeregningsgrunnlagFraVLTilRegel);

    @BeforeEach
    public void setup() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        beregningsgrunnlag = lagBeregningsgrunnlagAT(true);
    }

    @AfterAll
    public static void teardown(){
        input.leggTilToggle(TOGGLE_SPLITTE_SAMMENLIGNING, false);
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

    private BeregningsgrunnlagDto lagBeregningsgrunnlagATFL_SN(boolean nyIArbeidslivet) {
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
                .medNyIArbeidslivet(nyIArbeidslivet)
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

        AktørId aktørId = behandlingReferanse.getAktørId();
        verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, aktørId, arbeidsgiver, fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        verdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, aktørId, Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), fraOgMed, tilOgMed,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            verdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, aktørId, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                arbeidsgiver);
            verdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, aktørId, dt, dt.plusMonths(1), inntektBeregningsgrunnlag,
                arbeidsgiver);
        }
        iayGrunnlagBuilder.medData(inntektArbeidYtelseBuilder);
    }

    private void lagBehandlingFL(BigDecimal inntektSammenligningsgrunnlag,
                                 BigDecimal inntektFrilans, String virksomhetOrgnr) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        verdikjedeTestHjelper.initBehandlingFL(inntektSammenligningsgrunnlag, inntektFrilans, virksomhetOrgnr, fraOgMed, tilOgMed, behandlingReferanse.getAktørId(), registerBuilder);
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

        Optional<AktørArbeidDto> aktørArbeid = registerVersjon.stream()
        .flatMap(iay -> iay.getAktørArbeid().stream())
        .filter(a -> a.getAktørId().equals(behandlingReferanse.getAktørId())).findFirst();
        YrkesaktivitetDto ya = aktørArbeid.get().hentAlleYrkesaktiviteter()
        .stream()
        .filter(y -> y.equals(yrkesaktivitet))
        .findFirst().get();

        Intervall periode = ya.getAlleAktivitetsAvtaler().iterator().next().getPeriode();
        builder.getAktørArbeidBuilder(behandlingReferanse.getAktørId())
            .getYrkesaktivitetBuilderForNøkkelAvType(new OpptjeningsnøkkelDto(yrkesaktivitet), ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .getAktivitetsAvtaleBuilder(Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato()), true)
            .medPeriode(Intervall.fraOgMedTilOgMed(fomDato, tomDato));

        iayGrunnlagBuilder.medData(builder);
    }

    @Test
    public void skalLageEnPeriode() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1 + 1000), null, null);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, false);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        verifiserSammenligningsgrunnlag(resultat.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().get(0), ÅRSINNTEKT1,
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1),
            BigDecimal.valueOf(81.004455200d), SammenligningsgrunnlagType.SAMMENLIGNING_AT);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), (MÅNEDSINNTEKT1 + 1000) * 12,
            null, null);
    }

    @Test
    public void skalLageEnPeriodeNårNaturalytelseBortfallerPåSkjæringstidspunktet() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_BEREGNING, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_BEREGNING);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, false);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
            NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    private BeregningsgrunnlagRegelResultat act(BeregningsgrunnlagDto beregningsgrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger, boolean skalSplitte) {
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagGrunnlagDtoBuilder bgGrunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .medRegisterAktiviteter(BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING, OpptjeningAktivitetType.ARBEID));
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(behandlingReferanse, bgGrunnlagBuilder, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);
        input.leggTilToggle(TOGGLE_SPLITTE_SAMMENLIGNING, true);

        return foreslåBeregningsgrunnlag.foreslåBeregningsgrunnlag(input);
    }

    @Test
    public void skalLageToPerioderNaturalYtelseBortfaller() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, false);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), Intervall.TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
            NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageToPerioderNaturalYtelseTilkommer() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        var im1 = opprettInntektsmeldingNaturalytelseTilkommer(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        var inntektsmeldinger = List.of(im1);

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, false);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), Intervall.TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null,
            NATURALYTELSE_I_PERIODE_2 * 12);
    }

    @Test
    public void skalLageToPerioderKortvarigArbeidsforhold() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagDto nyttGrunnlag = beregningsgrunnlag;
        BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()
            .get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(eksisterendeAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null))
                .medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Før steget er det ingen inntekter på andelen
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, false);
        // Her skulle det vært inntekter på andelen
        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), Intervall.TIDENES_ENDE, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
    }

    @Test
    public void skalLageTrePerioderKortvarigArbeidsforholdOgNaturalYtelse() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagDto nyttGrunnlag = beregningsgrunnlag;
        BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()
            .get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(eksisterendeAndel))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder(eksisterendeAndel
                .getBgAndelArbeidsforhold().orElse(null)).medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3));
        splitBeregningsgrunnlagPeriode(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        var inntektsmeldinger = List.of(im1);

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, false);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(3);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1,
            PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
            NATURALYTELSE_I_PERIODE_2 * 12, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), Intervall.TIDENES_ENDE, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
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
        BeregningsgrunnlagRegelResultat resultat = act(grunnlagFL, inntektsmeldinger, false);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
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
            MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_2.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(grunnlagAT, inntektsmeldinger, false);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
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
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagDto nyttGrunnlag = beregningsgrunnlag;
        BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()
            .get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(eksisterendeAndel)
            .medBGAndelArbeidsforhold(
                BGAndelArbeidsforholdDto.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null)).medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        var im1 = opprettInntektsmeldingNaturalytelseBortfaller(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        splitBeregningsgrunnlagPeriode(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        var inntektsmeldinger = List.of(im1);

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(nyttGrunnlag, inntektsmeldinger, false);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), Intervall.TIDENES_ENDE, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET,
            PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
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
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, false);

        // Assert
        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        assertThat(beregningsgrunnlag).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
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
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), Intervall.TIDENES_ENDE, 2, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1,
            NATURALYTELSE_I_PERIODE_3 * 12, null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR2), ÅRSINNTEKT2,
            NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageBeregningsgrunnlagMedTrePerioderKortvarigFørNaturalytelse() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(eksisterendeAndel)
            .medBGAndelArbeidsforhold(
                BGAndelArbeidsforholdDto.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null)).medTidsbegrensetArbeidsforhold(true))
            .build(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0));
        splitBeregningsgrunnlagPeriode(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        lagKortvarigArbeidsforhold(beregningsgrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2).minusDays(1));
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, false);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(3);

        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3).minusDays(1), 1,
            PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3), Intervall.TIDENES_ENDE, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1, null, null);
    }

    @Test
    public void skalGiEittAksjonspunktForSNNyIArbeidslivetOgKortvarigArbeidsforhold() {
        // Arrange
        BeregningsgrunnlagDto nyttGrunnlag = lagBeregningsgrunnlagATFL_SN(true);
        InntektArbeidYtelseAggregatBuilder register = verdikjedeTestHjelper.initBehandlingFor_AT_SN(BigDecimal.valueOf(12 * MÅNEDSINNTEKT1),
            2014, SKJÆRINGSTIDSPUNKT_BEREGNING, ARBEIDSFORHOLD_ORGNR1,
            BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), new BehandlingReferanseMock(),
            iayGrunnlagBuilder);
        iayGrunnlagBuilder.medData(register);

        BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()
            .get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(eksisterendeAndel)
            .medBGAndelArbeidsforhold(
                BGAndelArbeidsforholdDto.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null)).medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        List<InntektsmeldingDto> inntektsmeldinger = Collections.emptyList();

        // Act
        BeregningsgrunnlagRegelResultat resultat = act(nyttGrunnlag, inntektsmeldinger, false);

        // Assert
        BeregningsgrunnlagDto bg = resultat.getBeregningsgrunnlag();
        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(2);
        bg.getBeregningsgrunnlagPerioder().forEach(p -> assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2));
        List<BeregningAksjonspunktResultat> aps = resultat.getAksjonspunkter();
        List<BeregningAksjonspunktDefinisjon> apDefs = aps.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(BeregningAksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET);
    }

    @Test
    public void skalSetteRiktigSammenligningsgrunnlagNårEnPeriodeMedArbeidstakerUtenNaturalytelserOgMedInntektsmeldingNårToggleErPå() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1+1000), null, null);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, true);
        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(resultat.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe()).hasSize(1);
        verifiserSammenligningsgrunnlag(resultat.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().get(0), ÅRSINNTEKT1,
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1),
            BigDecimal.valueOf(81.004455200), SammenligningsgrunnlagType.SAMMENLIGNING_AT);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), (MÅNEDSINNTEKT1 + 1000) * 12,
            null, null);
    }

    @Test
    public void skalReturnereAksjonspunktNårArbeidstakerMedInntektsmeldingOgAvvikMellomBeregnetOgSammenligningsgrunnlagUtenTidsbegrensetArbeidsforholdOgToggleErPå() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_2.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1+(MÅNEDSINNTEKT1/2)), null, null);
        var inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, true);
        // Assert
        List<BeregningAksjonspunktResultat> aps = resultat.getAksjonspunkter();
        List<BeregningAksjonspunktDefinisjon> apDefs = aps.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(BeregningAksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
        verifiserSammenligningsgrunnlag(resultat.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().get(0), ÅRSINNTEKT1,
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1),
            BigDecimal.valueOf(500L), SammenligningsgrunnlagType.SAMMENLIGNING_AT);
    }

    @Test
    public void skalSetteRiktigSammenligningsgrunnlagNårEnPeriodeMedFrilanserOgToggleErPå() {
        // Arrange
        BeregningsgrunnlagDto grunnlagFL = lagBeregningsgrunnlagFL();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(grunnlagFL.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .build(grunnlagFL.getBeregningsgrunnlagPerioder().get(0));
        lagBehandlingFL(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), ARBEIDSFORHOLD_ORGNR1);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(grunnlagFL, inntektsmeldinger, true);
        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        assertThat(resultat.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe()).hasSize(1);
        verifiserSammenligningsgrunnlag(resultat.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().get(0),ÅRSINNTEKT1,
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1),
            BigDecimal.ZERO, SammenligningsgrunnlagType.SAMMENLIGNING_FL);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE, 1);
        verifiserBGFL(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), ÅRSINNTEKT1);
    }

    @Test
    public void skalReturnereAksjonspunktNårFrilanserMedAvvikMellomBeregnetOgSammenligningsgrunnlagOgToggleErPå() {
        // Arrange
        BeregningsgrunnlagDto grunnlagFL = lagBeregningsgrunnlagFL();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(grunnlagFL.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .build(grunnlagFL.getBeregningsgrunnlagPerioder().get(0));
        lagBehandlingFL(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(3*MÅNEDSINNTEKT1), ARBEIDSFORHOLD_ORGNR1);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(grunnlagFL, inntektsmeldinger, true);
        // Assert
        List<BeregningAksjonspunktResultat> aps = resultat.getAksjonspunkter();
        List<BeregningAksjonspunktDefinisjon> apDefs = aps.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(BeregningAksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
        verifiserSammenligningsgrunnlag(resultat.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().get(0),ÅRSINNTEKT1,
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1),
            BigDecimal.valueOf(2000L), SammenligningsgrunnlagType.SAMMENLIGNING_FL);
    }

    @Test
    public void skalReturnereAksjonspunktNårAtOgEtterTidsbegrensetArbeidsforholdOgToggleErPå() {
        // Arrange
        lagBehandling(BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1),
            Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), MINUS_YEARS_1.withDayOfMonth(1), MINUS_YEARS_1.withDayOfMonth(1).plusYears(2), iayGrunnlagBuilder);
        BeregningsgrunnlagDto nyttGrunnlag = beregningsgrunnlag;
        BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()
            .get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(eksisterendeAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null))
                .medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        lagKortvarigArbeidsforhold(nyttGrunnlag, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        var im1 = verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver.virksomhet(ARBEIDSFORHOLD_ORGNR1), BigDecimal.valueOf(MÅNEDSINNTEKT1 + 10000), null, null);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of(im1);
        // Act
        BeregningsgrunnlagRegelResultat resultat = act(beregningsgrunnlag, inntektsmeldinger, true);

        // Assert
        List<BeregningAksjonspunktResultat> aps = resultat.getAksjonspunkter();
        List<BeregningAksjonspunktDefinisjon> apDefs = aps.stream().map(BeregningAksjonspunktResultat::getBeregningAksjonspunktDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(BeregningAksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
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

    private void verifiserSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagPrStatus, double rapportertPrÅr, LocalDate fom,
                                                 LocalDate tom, BigDecimal avvikPromille, SammenligningsgrunnlagType sammenligningsgrunnlagType) {
        assertThat(sammenligningsgrunnlagPrStatus.getRapportertPrÅr().doubleValue()).isEqualTo(rapportertPrÅr);
        assertThat(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom()).isEqualTo(fom);
        assertThat(sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()).isEqualTo(tom);
        assertThat(sammenligningsgrunnlagPrStatus.getAvvikPromilleNy().compareTo(avvikPromille)).isEqualTo(0);
        assertThat(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType()).isEqualTo(sammenligningsgrunnlagType);
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
            new NaturalYtelseDto(Tid.TIDENES_BEGYNNELSE, naturalytelseBortfallerDato.minusDays(1), naturalytelseBortfaller, NaturalYtelseType.ANNET),
            null);
    }

    private InntektsmeldingDto opprettInntektsmeldingNaturalytelseTilkommer(Arbeidsgiver arbeidsgiver, BigDecimal inntektInntektsmelding, BigDecimal naturalytelseTilkommer,
                                                              LocalDate naturalytelseTilkommerDato) {
        return verdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(arbeidsgiver, inntektInntektsmelding,
            new NaturalYtelseDto(naturalytelseTilkommerDato, Tid.TIDENES_ENDE, naturalytelseTilkommer, NaturalYtelseType.ANNET), null);
    }
}
