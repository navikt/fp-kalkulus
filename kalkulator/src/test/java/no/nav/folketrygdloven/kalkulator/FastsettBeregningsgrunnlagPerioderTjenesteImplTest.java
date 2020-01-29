package no.nav.folketrygdloven.kalkulator;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.BeregningsgrunnlagTestUtil;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.BekreftetPermisjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.BekreftetPermisjonStatus;
import no.nav.folketrygdloven.kalkulator.modell.iay.kodeverk.NaturalYtelseType;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.exception.TekniskException;

import no.nav.vedtak.konfig.Tid;

public class FastsettBeregningsgrunnlagPerioderTjenesteImplTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 4);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000L);
    private static final String ORG_NUMMER = "915933149";
    private static final String ORG_NUMMER_2 = "974760673";
    private static final String ORG_NUMMER_3 = "976967631";

    private static final AktørId ARBEIDSGIVER_AKTØR_ID = AktørId.dummy();
    private static final BigDecimal ANTALL_MÅNEDER_I_ÅR = BigDecimal.valueOf(12);

    private BeregningAktivitetAggregatDto beregningAktivitetAggregat;
    private List<BeregningAktivitetDto> aktiviteter = new ArrayList<>();

    private final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);
    private final Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORG_NUMMER_2);

    private FastsettBeregningsgrunnlagPerioderTjeneste tjeneste;

    private BehandlingReferanse behandlingRef = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private static final Intervall ARBEIDSPERIODE = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);

    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;

    @BeforeEach
    public void setUp() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        tjeneste = lagTjeneste();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER), iayGrunnlagBuilder);

    }

    private FastsettBeregningsgrunnlagPerioderTjeneste lagTjeneste() {
        var oversetterTilRegelNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelNaturalYtelse();
        var oversetterTilRegelRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGradering();
        var oversetterFraRegelTilVLNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse();
        MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering oversetterFraRegelTilVLRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjonOgGradering();
        return new FastsettBeregningsgrunnlagPerioderTjeneste(oversetterTilRegelNaturalytelse,
            new UnitTestLookupInstanceImpl<>(oversetterTilRegelRefusjonOgGradering), oversetterFraRegelTilVLNaturalytelse,
            oversetterFraRegelTilVLRefusjonOgGradering);
    }

    private void leggTilYrkesaktiviteterOgBeregningAktiviteter(List<String> orgnrs, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getAktørArbeidFraRegister(behandlingRef.getAktørId()))
            .medAktørId(behandlingRef.getAktørId());
        for (String orgnr : orgnrs) {
            Arbeidsgiver arbeidsgiver = leggTilYrkesaktivitet(ARBEIDSPERIODE, aktørArbeidBuilder, orgnr);
            fjernOgLeggTilNyBeregningAktivitet(ARBEIDSPERIODE.getFomDato(), ARBEIDSPERIODE.getTomDato(), arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        }

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(iayGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER)
            .leggTilAktørArbeid(aktørArbeidBuilder);
        iayGrunnlagBuilder.medData(inntektArbeidYtelseAggregatBuilder);
    }

    private Arbeidsgiver leggTilYrkesaktivitet(Intervall arbeidsperiode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder,
                                               String orgnr) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        AktivitetsAvtaleDtoBuilder aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);
        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
        return arbeidsgiver;
    }

    private void fjernOgLeggTilNyBeregningAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (fom.isAfter(SKJÆRINGSTIDSPUNKT)) {
            throw new IllegalArgumentException("Kan ikke lage BeregningAktivitet som starter etter skjæringstidspunkt");
        }
        fjernAktivitet(arbeidsgiver, arbeidsforholdRef);
        aktiviteter.add(lagAktivitet(fom, tom, arbeidsgiver, arbeidsforholdRef));
        lagAggregatEntitetFraListe(aktiviteter);
    }

    private void fjernAktivitet(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        aktiviteter.stream()
            .filter(a -> a.gjelderFor(arbeidsgiver, arbeidsforholdRef)).findFirst()
            .ifPresent(a -> aktiviteter.remove(a));
        lagAggregatEntitetFraListe(aktiviteter);
    }

    private void lagAggregatEntitetFraListe(List<BeregningAktivitetDto> aktiviteter) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        aktiviteter.forEach(builder::leggTilAktivitet);
        beregningAktivitetAggregat = builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
    }

    private BeregningAktivitetDto lagAktivitet(LocalDate fom, LocalDate tom, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return BeregningAktivitetDto.builder()
            .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdRef(arbeidsforholdRef)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .build();
    }

    @Test
    public void ikkeLagPeriodeForRefusjonHvisKunEnInntektsmeldingIngenEndringIRefusjon() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(23987);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
        Optional<BGAndelArbeidsforholdDto> bgaOpt = finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER);
        assertThat(bgaOpt).hasValueSatisfying(bga -> assertThat(bga.getRefusjonskravPrÅr()).isEqualByComparingTo(inntekt.multiply(ANTALL_MÅNEDER_I_ÅR)));
    }

    private BeregningsgrunnlagDto fastsettPerioderForRefusjonOgGradering(BehandlingReferanse ref,
                                                                         BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                         BeregningsgrunnlagDto beregningsgrunnlag,
                                                                         AktivitetGradering aktivitetGradering,
                                                                         InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        List<RefusjonskravDatoDto> refusjonskravDatoDtos = OpprettRefusjondatoerFraInntektsmeldinger.opprett(ref, iayGrunnlag);
        var input = new BeregningsgrunnlagInput(ref, iayGrunnlag, null, aktivitetGradering, refusjonskravDatoDtos, null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
        return tjeneste.fastsettPerioderForRefusjonOgGradering(input, beregningsgrunnlag);
    }


    private BeregningsgrunnlagDto fastsettPerioderForRefusjonOgGradering(BehandlingReferanse ref,
                                                                         BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                         BeregningsgrunnlagDto beregningsgrunnlag,
                                                                         AktivitetGradering aktivitetGradering,
                                                                         InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder,
                                                                         List<RefusjonskravDatoDto> refusjonskravDatoer) {
        var input = new BeregningsgrunnlagInput(ref, iayGrunnlagBuilder.build(), null, aktivitetGradering, refusjonskravDatoer, null)
            .medBeregningsgrunnlagGrunnlag(grunnlag);
        return tjeneste.fastsettPerioderForRefusjonOgGradering(input, beregningsgrunnlag);
    }

    private BeregningsgrunnlagDto fastsettPerioderForNaturalytelse(BehandlingReferanse ref,
                                                                   BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                   BeregningsgrunnlagDto beregningsgrunnlag,
                                                                   AktivitetGradering aktivitetGradering) {
        var input = new BeregningsgrunnlagInput(ref, iayGrunnlagBuilder.build(), null, aktivitetGradering, List.of(), null)
            .medBeregningsgrunnlagGrunnlag(grunnlag);
        return tjeneste.fastsettPerioderForNaturalytelse(input, beregningsgrunnlag);
    }


    @Test
    public void lagPeriodeForRefusjonHvisKunEnInntektsmeldingIngenEndringIRefusjonArbeidsgiverSøkerForSent() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(23987);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        List<RefusjonskravDatoDto> refusjonskravDatoDtoer = List.of(new RefusjonskravDatoDto(Arbeidsgiver.virksomhet(ORG_NUMMER),
            SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.MAY, 2)));

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef,
            grunnlag,
            beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder, refusjonskravDatoDtoer);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 31));
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER))
            .hasValueSatisfying(bga -> assertThat(bga.getRefusjonskravPrÅr()).isEqualByComparingTo(BigDecimal.ZERO));
        assertBeregningsgrunnlagPeriode(perioder.get(1), LocalDate.of(2019, Month.FEBRUARY, 1), TIDENES_ENDE, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(1), ORG_NUMMER))
            .hasValueSatisfying(bga -> assertThat(bga.getRefusjonskravPrÅr()).isEqualByComparingTo(inntekt.multiply(ANTALL_MÅNEDER_I_ÅR)));
    }

    @Test
    public void ikkeLagPeriodeForZeroRefusjonHvisKunEnInntektsmeldingIngenEndringIRefusjon() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(23987);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, BigDecimal.ZERO);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
    }

    @Test
    public void lagPeriodeForNaturalytelseTilkommer() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        NaturalYtelseDto naturalYtelseTilkommer = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.plusDays(30), TIDENES_ENDE, BigDecimal.valueOf(350),
            NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
            naturalYtelseTilkommer);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(29));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(30), TIDENES_ENDE, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
    }

    @Test
    public void lagPeriodeForNaturalytelseBortfalt() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        NaturalYtelseDto naturalYtelseBortfall = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusDays(30),
            BigDecimal.valueOf(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
            naturalYtelseBortfall);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(30));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(31), TIDENES_ENDE, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
    }

    @Test
    public void ikkeLagPeriodeForNaturalytelseBortfaltPåStp() {
        // Arrange
BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        NaturalYtelseDto naturalYtelseBortfall = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.minusDays(1),
            BigDecimal.valueOf(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
            naturalYtelseBortfall);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
    }

    @Test
    public void lagPeriodeForNaturalytelseBortfaltDagenEtterStp() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        NaturalYtelseDto naturalYtelseBortfall = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT, BigDecimal.valueOf(350),
            NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
            naturalYtelseBortfall);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT);
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
    }

    @Test
    public void lagPerioderForNaturalytelseBortfaltOgTilkommer() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        NaturalYtelseDto naturalYtelseBortfalt = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusDays(30),
            BigDecimal.valueOf(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        NaturalYtelseDto naturalYtelseTilkommer = new NaturalYtelseDto(SKJÆRINGSTIDSPUNKT.plusDays(90), TIDENES_ENDE, BigDecimal.valueOf(350),
            NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null,
            naturalYtelseBortfalt, naturalYtelseTilkommer);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForNaturalytelse(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(30));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(31), SKJÆRINGSTIDSPUNKT.plusDays(89), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusDays(90), TIDENES_ENDE, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
    }

    @Test
    public void lagPeriodeForRefusjonOpphører() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, SKJÆRINGSTIDSPUNKT.plusDays(100)
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);
        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(100));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(101), TIDENES_ENDE, PeriodeÅrsak.REFUSJON_OPPHØRER);
    }

    @Test
    public void lagPeriodeForGraderingLik6G() {
        // Arrange
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1);
        LocalDate graderingFom = refusjonOpphørerDato.plusDays(1);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);
        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), iayGrunnlagBuilder);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, GRUNNBELØP.multiply(BigDecimal.valueOf(6)), inntekt1
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiverGradering)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    @Test
    public void lagPeriodeForGraderingSN_refusjon_over_6G() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);

        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), iayGrunnlagBuilder);
        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0));

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    @Test
    public void lagPeriodeForGraderingSN_bg_over_6g() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER_2), iayGrunnlagBuilder);
        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BeregningsgrunnlagPeriodeDto bgPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(bgPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier(bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medBeregnetPrÅr(inntekt1.multiply(ANTALL_MÅNEDER_I_ÅR));

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1), PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusWeeks(18), Intervall.TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    @Test
    public void lagPeriodeForGraderingOver6GOgOpphørRefusjonSammeDag() {
        // Arrange
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1);
        LocalDate graderingFom = refusjonOpphørerDato.plusDays(1);
        LocalDate graderingTom = Tid.TIDENES_ENDE;

        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();

        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        List<String> orgnrs = List.of(ORG_NUMMER, ORG_NUMMER_3, ORG_NUMMER_2);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(orgnrs, newGrunnlagBuilder);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(orgnrs, beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_3, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1);
        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, refusjonOpphørerDato);
        newGrunnlagBuilder.medInntektsmeldinger(im1, im2);

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiverGradering)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());
        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef,
            grunnlag,
            beregningsgrunnlag,
            aktivitetGradering,
            newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, refusjonOpphørerDato);
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, TIDENES_ENDE, PeriodeÅrsak.GRADERING, PeriodeÅrsak.REFUSJON_OPPHØRER);

    }

    @Test
    public void lagPeriodeForGraderingOver6GFL() {
        // Arrange
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1);
        LocalDate graderingFom = refusjonOpphørerDato.plusDays(1);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);
        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), newGrunnlagBuilder);
        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1);
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medBeregningsperiode(SKJÆRINGSTIDSPUNKT, AbstractIntervall.TIDENES_ENDE)
            .build(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0));

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiverGradering)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);

    }

    @Test
    public void lagPeriodeForGraderingOgRefusjonArbeidsforholdTilkomEtterStp() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);


        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbId2 = InternArbeidsforholdRefDto.namedRef("B");

        LocalDate ansettelsesDato = graderingFom;
        LocalDate startDatoRefusjon = graderingFom;
        InntektArbeidYtelseGrunnlagDtoBuilder iayBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, ansettelsesDato, ansettelsesDato.plusMonths(5).minusDays(2), arbId, arbeidsgiverGradering,
            BigDecimal.TEN, iayBuilder);
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, ansettelsesDato, ansettelsesDato.plusMonths(5).minusDays(2), arbId2, arbeidsgiverGradering,
            BigDecimal.TEN, iayBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiverGradering.getIdentifikator(), startDatoRefusjon, BigDecimal.valueOf(20000), BigDecimal.valueOf(20000)
        );
        iayBuilder.medInntektsmeldinger(im1);
        fjernAktivitet(arbeidsgiverGradering, arbId);

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiverGradering)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, iayBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, TIDENES_ENDE, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER_2)).isPresent();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }

    @Test
    public void lagPeriodeForGraderingOgRefusjonToArbeidsforholdTilkomEtterStpInntektsmeldingMedId() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);


        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbId2 = InternArbeidsforholdRefDto.namedRef("B");

        LocalDate ansettelsesDato = graderingFom;
        LocalDate startDatoRefusjon = graderingFom;
        InntektArbeidYtelseGrunnlagDtoBuilder iayBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, ansettelsesDato, ansettelsesDato.plusMonths(5).minusDays(2), arbId, arbeidsgiverGradering,
            BigDecimal.TEN, iayBuilder);
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, ansettelsesDato, ansettelsesDato.plusMonths(5).minusDays(2), arbId2, arbeidsgiverGradering,
            BigDecimal.TEN, iayBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiverGradering, arbId, startDatoRefusjon, BigDecimal.valueOf(20000), BigDecimal.valueOf(20000),
            null,
            List.of(),
            List.of()
        );
        iayBuilder.medInntektsmeldinger(im1);
        fjernAktivitet(arbeidsgiverGradering, arbId);

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiverGradering)
            .medArbeidsforholdRef(arbId2)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, iayBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, graderingTom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1L), Intervall.TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER_2)).isPresent();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);
        assertThat(perioder.get(2).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(2), ORG_NUMMER_2)).isPresent();
    }



    @Test
    public void lagPeriodeForGraderingOgRefusjonArbeidsforholdTilkomEtterStpInntektsmeldingMedId() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);


        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        var arbId = InternArbeidsforholdRefDto.namedRef("A");

        LocalDate ansettelsesDato = graderingFom;
        LocalDate startDatoRefusjon = graderingFom;
        InntektArbeidYtelseGrunnlagDtoBuilder iayBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, ansettelsesDato, ansettelsesDato.plusMonths(5).minusDays(2), arbId, arbeidsgiverGradering,
            BigDecimal.TEN, iayBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiverGradering, arbId, startDatoRefusjon, BigDecimal.valueOf(20000), BigDecimal.valueOf(20000),
            null,
            List.of(),
            List.of()
        );
        iayBuilder.medInntektsmeldinger(im1);
        fjernAktivitet(arbeidsgiverGradering, arbId);

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiverGradering)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, iayBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, graderingFom.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), graderingFom, TIDENES_ENDE, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER_2)).isPresent();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }


    @Test
    public void lagPeriodeForGraderingArbeidsforholdTilkomEtterStp() {
     // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);


        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        Arbeidsgiver arbeidsgiver3 = Arbeidsgiver.virksomhet(ORG_NUMMER);
        Arbeidsgiver arbeidsgiver4 = Arbeidsgiver.virksomhet(ORG_NUMMER_2);
        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9), SKJÆRINGSTIDSPUNKT.plusMonths(5).minusDays(2),
            arbId, arbeidsgiver3, BigDecimal.TEN, newGrunnlagBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernAktivitet(arbeidsgiver3, arbId);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE, arbeidsgiver4, arbId);

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiverGradering)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1), PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusWeeks(18), Intervall.TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(0), ORG_NUMMER_2)).isPresent();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(2).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(finnBGAndelArbeidsforhold(perioder.get(2), ORG_NUMMER_2)).isPresent();
    }

    @Test
    public void lagPeriodeForRefusjonArbeidsforholdTilkomEtterStp() {
        var arbId = InternArbeidsforholdRefDto.namedRef("A");

        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        LocalDate ansettelsesDato = SKJÆRINGSTIDSPUNKT.plusWeeks(2);
        LocalDate startDatoRefusjon = SKJÆRINGSTIDSPUNKT.plusWeeks(1);
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, ansettelsesDato, ansettelsesDato.plusMonths(5).minusDays(2), arbId, arbeidsgiver,
            BigDecimal.TEN, newGrunnlagBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, startDatoRefusjon, BigDecimal.valueOf(20000), BigDecimal.valueOf(20000)
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernAktivitet(arbeidsgiver, arbId);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, ansettelsesDato.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), ansettelsesDato, TIDENES_ENDE, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty();
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
    }

    @Test
    public void ikkeLagAndelForRefusjonForArbeidsforholdSomBortfallerFørSkjæringstidspunkt() {
        var arbId = InternArbeidsforholdRefDto.namedRef("A");

        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        LocalDate startDatoRefusjon = SKJÆRINGSTIDSPUNKT.plusWeeks(1);
        LocalDate ansattFom = SKJÆRINGSTIDSPUNKT.minusYears(2);
        LocalDate ansattTom = SKJÆRINGSTIDSPUNKT.minusMonths(2);
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, ansattFom, ansattTom, arbId, Arbeidsgiver.virksomhet(ORG_NUMMER), BigDecimal.TEN, newGrunnlagBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, startDatoRefusjon, BigDecimal.valueOf(20000), BigDecimal.valueOf(20000)
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty();
    }

    @Test
    public void lagPeriodeForRefusjonArbeidsforholdTilkomEtterStpFlerePerioder() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        Arbeidsgiver arbeidsgiver3 = Arbeidsgiver.virksomhet(ORG_NUMMER);

        LocalDate startDatoPermisjon = SKJÆRINGSTIDSPUNKT.plusWeeks(1);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5),
            arbId, Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, startDatoPermisjon, SKJÆRINGSTIDSPUNKT.plusMonths(5).minusDays(2),
            arbId, Arbeidsgiver.virksomhet(ORG_NUMMER_2), iayGrunnlagBuilder);

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, BigDecimal.valueOf(30000), BigDecimal.valueOf(30000),
            SKJÆRINGSTIDSPUNKT.plusWeeks(12));
        var im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, startDatoPermisjon, BigDecimal.valueOf(20000), BigDecimal.valueOf(20000)
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1, im2);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbeidsgiver3, arbId);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, startDatoPermisjon.minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), startDatoPermisjon, SKJÆRINGSTIDSPUNKT.plusWeeks(12), PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusWeeks(12).plusDays(1), TIDENES_ENDE, PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(2).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }

    @Test
    public void skalSetteRefusjonskravForSøktRefusjonFraSkjæringstidspunktUtenOpphørsdato() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId,
            Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        List<LocalDateInterval> berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(behandlingRef, SKJÆRINGSTIDSPUNKT, berPerioder, iayGrunnlagBuilder.getKladd());
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(refusjonskrav1.multiply(ANTALL_MÅNEDER_I_ÅR));
    }

    @Test
    public void skalSetteRefusjonskravForSøktRefusjonFraSkjæringstidspunktUtenOpphørsdatoPrivatpersonSomArbeidsgiver() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.person(ARBEIDSGIVER_AKTØR_ID);
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT,
            SKJÆRINGSTIDSPUNKT.minusYears(2),
            SKJÆRINGSTIDSPUNKT.plusYears(5), arbId, arbeidsgiver, iayGrunnlagBuilder);
        List<LocalDateInterval> berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(behandlingRef, SKJÆRINGSTIDSPUNKT, berPerioder, iayGrunnlagBuilder.getKladd());
        BeregningAktivitetDto aktivitetEntitet = BeregningAktivitetDto.builder().medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdRef(arbId)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5))).build();
        beregningAktivitetAggregat = leggTilAktivitet(aktivitetEntitet);
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(arbeidsgiver, arbId, SKJÆRINGSTIDSPUNKT,
            refusjonskrav1, inntekt1, null, emptyList(), emptyList());
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(refusjonskrav1.multiply(ANTALL_MÅNEDER_I_ÅR));
    }

    private BeregningAktivitetAggregatDto leggTilAktivitet(BeregningAktivitetDto aktivitetEntitet) {
        aktiviteter.add(aktivitetEntitet);
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        aktiviteter.forEach(builder::leggTilAktivitet);
        return builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
    }

    @Test
    public void skalSetteRefusjonskravForSøktRefusjonFraSkjæringstidspunktMedOpphørsdato() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT,
            SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId, arbeidsgiver, iayGrunnlagBuilder);
        List<LocalDateInterval> berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(behandlingRef, SKJÆRINGSTIDSPUNKT,
            berPerioder, iayGrunnlagBuilder.getKladd());
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(6).minusDays(1);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, refusjonOpphørerDato
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbeidsgiver, arbId);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertThat(andelerIFørstePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(refusjonskrav1.multiply(ANTALL_MÅNEDER_I_ÅR));

        assertThat(perioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(refusjonOpphørerDato.plusDays(1));
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIAndrePeriode = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIAndrePeriode).hasSize(1);
        assertThat(andelerIAndrePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skalIkkeSetteRefusjonForAktivitetSomErFjernetIOverstyring() {
        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningAktivitetHandlingType handlingIkkeBenytt = BeregningAktivitetHandlingType.IKKE_BENYTT;
        BeregningAktivitetOverstyringerDto overstyring = BeregningAktivitetOverstyringerDto.builder().leggTilOverstyring(lagOverstyringForAktivitet(InternArbeidsforholdRefDto.nullRef(), arbeidsgiver, handlingIkkeBenytt)).build();
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(List.of(), beregningAktivitetAggregat, overstyring);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt1, inntekt1,
            null);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(0);
    }

    @Test
    public void skalSetteRefusjonForAktivitetSomErFjernetISaksbehandlet() {
        // Arrange
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedSaksbehandlet(List.of(), behandlingRef,
            beregningAktivitetAggregat, BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build());
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt1, inntekt1,
            null);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef,
            grunnlag,
            beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING,
            iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
    }

    @Test
    public void skalSetteRefusjonskravForSøktRefusjonFraEtterSkjæringstidspunktMedOpphørsdato() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId,
            Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        List<LocalDateInterval> berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(behandlingRef, SKJÆRINGSTIDSPUNKT,
            berPerioder, iayGrunnlagBuilder.getKladd());
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(6).minusDays(1);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, refusjonOpphørerDato
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbeidsgiver, arbId);

        // Act

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertThat(andelerIFørstePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(refusjonskrav1.multiply(ANTALL_MÅNEDER_I_ÅR));
        assertThat(perioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(refusjonOpphørerDato.plusDays(1));
        assertThat(perioder.get(1).getPeriodeÅrsaker()).isEqualTo(singletonList(PeriodeÅrsak.REFUSJON_OPPHØRER));
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIAndrePeriode = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIAndrePeriode).hasSize(1);
        assertThat(andelerIAndrePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skalSetteRefusjonskravForSøktRefusjonFraEtterSkjæringstidspunktUtenOpphørsdato() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId,
            Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        List<LocalDateInterval> berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(behandlingRef, SKJÆRINGSTIDSPUNKT,
            berPerioder, iayGrunnlagBuilder.getKladd());
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, null
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbeidsgiver, arbId);

        // Act

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertThat(andelerIFørstePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(refusjonskrav1.multiply(ANTALL_MÅNEDER_I_ÅR));
    }

    @Test
    public void skalTesteEndringIRefusjon() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.namedRef("A");
        var arbeidsgiver = Arbeidsgiver.virksomhet(ORG_NUMMER);

        Intervall arbeidsperiode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5));
        List<String> orgnrs = List.of();
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(orgnrs, beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        List<RefusjonDto> refusjonsListe = List.of(
            new RefusjonDto(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT.plusMonths(3)),
            new RefusjonDto(BigDecimal.valueOf(10000), SKJÆRINGSTIDSPUNKT.plusMonths(6)));
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusMonths(9).minusDays(1);
        BeregningIAYTestUtil.byggArbeidForBehandling(behandlingRef, SKJÆRINGSTIDSPUNKT.minusDays(1),
            arbeidsperiode, arbId, Arbeidsgiver.virksomhet(ORG_NUMMER), iayGrunnlagBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmeldingMedEndringerIRefusjon(ORG_NUMMER, arbId, SKJÆRINGSTIDSPUNKT, inntekt,
            inntekt, refusjonOpphørerDato, refusjonsListe);
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        fjernOgLeggTilNyBeregningAktivitet(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5),
            arbeidsgiver, arbId);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                .medArbeidsforholdRef(arbId.getReferanse());

            BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        });

        // Act

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(4);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(3).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusMonths(3), SKJÆRINGSTIDSPUNKT.plusMonths(6).minusDays(1),
            PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusMonths(6), refusjonOpphørerDato, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertBeregningsgrunnlagPeriode(perioder.get(3), refusjonOpphørerDato.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.REFUSJON_OPPHØRER);
        Map<LocalDate, BeregningsgrunnlagPrStatusOgAndelDto> andeler = perioder.stream()
            .collect(Collectors.toMap(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom, p -> p.getBeregningsgrunnlagPrStatusOgAndelList().get(0)));
        assertThat(andeler.get(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
            .orElse(null))
                .isEqualByComparingTo(inntekt.multiply(ANTALL_MÅNEDER_I_ÅR));
        assertThat(andeler.get(perioder.get(1).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
            .orElse(null)).isEqualByComparingTo(BigDecimal.valueOf(20000 * 12));
        assertThat(andeler.get(perioder.get(2).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
            .orElse(null)).isEqualByComparingTo(BigDecimal.valueOf(10000 * 12));
        assertThat(andeler.get(perioder.get(3).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
            .orElse(null)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // Beregningsgrunnlag: En andel hos arbeidsgiver
    // Yrkesaktivitet har to ansettelsesperioder med to dagers mellomrom
    // Inntektsmelding: Inneholder orgnr, ingen arbId, inntekt = refusjon

    @Test
    public void skalIkkeLeggeTilArbeidsforholdSomTilkommerEtterSkjæringstidspunktDersomDetAlleredeEksisterer() {
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusMonths(1));
        Intervall arbeidsperiode2 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.plusMonths(1).plusDays(3), TIDENES_ENDE);

        var aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode1);
        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode2);
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder1)
            .leggTilAktivitetsAvtale(aaBuilder2);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .leggTilYrkesaktivitet(yaBuilder)
            .medAktørId(behandlingRef.getAktørId());
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        newGrunnlagBuilder.medData(registerBuilder);

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, InternArbeidsforholdRefDto.nyRef(), SKJÆRINGSTIDSPUNKT,
            inntekt.intValue(), inntekt.intValue());
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(arbeidsperiode2.getFomDato())
                .medArbeidsperiodeTom(arbeidsperiode2.getTomDato())
                .medArbeidsgiver(arbeidsgiver);

            BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        });

        // Act

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        Optional<BGAndelArbeidsforholdDto> bgaOpt = andeler.get(0).getBgAndelArbeidsforhold();
        assertThat(bgaOpt).hasValueSatisfying(bga -> {
            assertThat(bga.getArbeidsgiver()).isEqualTo(arbeidsgiver);
            assertThat(bga.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()).isFalse();
            assertThat(bga.getRefusjonskravPrÅr()).isEqualByComparingTo(inntekt.multiply(ANTALL_MÅNEDER_I_ÅR));
        });
    }

    @Test
    public void skalSplitteBeregningsgrunnlagOgLeggeTilNyAndelVedEndringssøknadNårSelvstendigNæringsdrivendeTilkommerOgGraderes() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusDays(10);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusDays(20);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medGradering(graderingFom, graderingTom, 50)
            .build());
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), newGrunnlagBuilder);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(9));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(10), graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);
        assertAndelStatuser(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList(),
            Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));

    }

    @Test
    public void skalLeggeTilAndelSomTilkommerEtterSkjæringstidspunktForSøktGraderingUtenRefusjon() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusMonths(5);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        BigDecimal inntekt = BigDecimal.valueOf(40000);
        Intervall arbeidsperiode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);

        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder2);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .leggTilYrkesaktivitet(yaBuilder)
            .medAktørId(behandlingRef.getAktørId());
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        newGrunnlagBuilder.medData(registerBuilder);

        leggTilYrkesaktiviteterOgBeregningAktiviteter(singletonList(ORG_NUMMER_2), newGrunnlagBuilder);
        fjernAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef());

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        });

        // Act

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BGAndelArbeidsforholdDto> baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode2, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).as("BGAndelArbeidsforhold")
            .hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).as("RefusjonskravPrÅr").isEqualByComparingTo(BigDecimal.ZERO));

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode3 = perioder.get(2);
        assertThat(beregningsgrunnlagPeriode3.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(beregningsgrunnlagPeriode3.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
    }

    @Test
    public void skalLeggeTilAndelHvorBrukerErIPermisjonPåSkjæringstidspunktetOgSøkerRefusjon() {
        InternArbeidsforholdRefDto arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        LocalDate permisjonFom = SKJÆRINGSTIDSPUNKT.minusMonths(1);
        LocalDate permisjonTom = SKJÆRINGSTIDSPUNKT.plusMonths(1);

        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), Tid.TIDENES_ENDE);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .medAktørId(behandlingRef.getAktørId());
        Arbeidsgiver arbeidsgiver = leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, ORG_NUMMER);
        fjernOgLeggTilNyBeregningAktivitet(arbeidsperiode1.getFomDato(), permisjonFom.minusDays(1), arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        aktiviteter.add(lagAktivitet(arbeidsperiode1.getFomDato(), arbeidsperiode1.getTomDato(), arbeidsgiver, arbeidsforholdRef));

        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        newGrunnlagBuilder.medData(registerBuilder);
        ArbeidsforholdInformasjonDtoBuilder bekreftetPermisjon = bekreftetPermisjon(arbeidsgiver, arbeidsforholdRef,
            permisjonFom, permisjonTom);
        newGrunnlagBuilder.medInformasjon(bekreftetPermisjon.build());
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        // Act

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).isEmpty();

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriode().getFomDato()).isEqualTo(permisjonTom.plusDays(1));
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        Optional<BGAndelArbeidsforholdDto> baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode2, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).as("BGAndelArbeidsforhold")
            .hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).as("RefusjonskravPrÅr").isEqualByComparingTo(BigDecimal.valueOf(480_000)));
    }

    private ArbeidsforholdInformasjonDtoBuilder bekreftetPermisjon(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto ref, LocalDate fom, LocalDate tom) {
        ArbeidsforholdInformasjonDtoBuilder arbeidsforholdInformasjonBuilder = ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty());
        ArbeidsforholdOverstyringDtoBuilder overstyringBuilder = arbeidsforholdInformasjonBuilder.getOverstyringBuilderFor(arbeidsgiver, ref);
        overstyringBuilder.medBekreftetPermisjon(new BekreftetPermisjonDto(fom, tom, BekreftetPermisjonStatus.BRUK_PERMISJON));
        return arbeidsforholdInformasjonBuilder
            .leggTil(overstyringBuilder);
    }

    @Test
    public void skalLeggeTilAndelSomTilkommerPåSkjæringstidspunkt() {
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        Intervall arbeidsperiode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);

        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder2);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .leggTilYrkesaktivitet(yaBuilder)
            .medAktørId(behandlingRef.getAktørId());
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        newGrunnlagBuilder.medData(registerBuilder);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(singletonList(ORG_NUMMER_2), newGrunnlagBuilder);
        fjernAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef());

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        });

        // Act

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BGAndelArbeidsforholdDto> baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).as("BGAndelArbeidsforhold").hasValueSatisfying(
            baa -> assertThat(baa.getRefusjonskravPrÅr()).as("RefusjonskravPrÅr").isEqualByComparingTo(inntekt.multiply(ANTALL_MÅNEDER_I_ÅR)));
    }

    @Test
    public void skalLeggeTilAndelSomTilkommerPåSkjæringstidspunktOgSletteVedOpphør() {
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        Intervall arbeidsperiode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder2);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .leggTilYrkesaktivitet(yaBuilder)
            .medAktørId(behandlingRef.getAktørId());
        iayGrunnlagBuilder.medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER).leggTilAktørArbeid(aktørArbeidBuilder));

        LocalDate refusjonOpphørerFom = SKJÆRINGSTIDSPUNKT.plusMonths(1);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, refusjonOpphørerFom
        );
        iayGrunnlagBuilder.medInntektsmeldinger(im1);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        });

        // Act

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag,
            AktivitetGradering.INGEN_GRADERING, iayGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BGAndelArbeidsforholdDto> baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).isEqualByComparingTo(inntekt.multiply(ANTALL_MÅNEDER_I_ÅR)));

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactlyInAnyOrder(PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode2, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).isNotPresent();
    }

    @Test
    public void skalLeggeTilAndelSomTilkommerPåSkjæringstidspunktForSøktGraderingUtenRefusjon() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT;
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        BigDecimal inntekt = BigDecimal.valueOf(40000);
        Intervall arbeidsperiode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);

        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder2);

        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = registerBuilder.getAktørArbeidBuilder(behandlingRef.getAktørId())
            .leggTilYrkesaktivitet(yaBuilder);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        InntektArbeidYtelseGrunnlagDtoBuilder grunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        grunnlagBuilder.medData(registerBuilder);
        leggTilYrkesaktiviteterOgBeregningAktiviteter(singletonList(ORG_NUMMER_2), iayGrunnlagBuilder);
        fjernAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef());
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);
        grunnlagBuilder.medInntektsmeldinger(im1);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        });

        // Act

        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, grunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode1.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING);
        assertThat(beregningsgrunnlagPeriode1.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BGAndelArbeidsforholdDto> baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode1, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).as("BGAndelArbeidsforhold")
            .hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).as("RefusjonskravPrÅr").isEqualByComparingTo(BigDecimal.ZERO));

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.GRADERING_OPPHØRER);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
    }

    @Test
    public void skalLeggeTilAndelSomTilkommerPåSkjæringstidspunktMedOpphørUtenSlettingPgaGradering() {
     // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT;
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusMonths(2);

        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        BigDecimal inntekt = BigDecimal.valueOf(40000);
        Intervall arbeidsperiode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE);

        var aaBuilder2 = AktivitetsAvtaleDtoBuilder.ny()
            .medPeriode(arbeidsperiode);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aaBuilder2);

        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
            .leggTilYrkesaktivitet(yaBuilder)
            .medAktørId(behandlingRef.getAktørId());
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        registerBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        newGrunnlagBuilder.medData(registerBuilder);

        leggTilYrkesaktiviteterOgBeregningAktiviteter(singletonList(ORG_NUMMER_2), iayGrunnlagBuilder);
        fjernAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef());

        LocalDate refusjonOpphørerFom = SKJÆRINGSTIDSPUNKT.plusMonths(1);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, refusjonOpphørerFom
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto.builder()
                .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                .medArbeidsperiodeTom(arbeidsperiode.getTomDato())
                .medArbeidsgiver(arbeidsgiver2);

            BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        });
        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode = perioder.get(0);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).isEmpty();
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BGAndelArbeidsforholdDto> baaOpt = finnBGAndelArbeidsforhold(beregningsgrunnlagPeriode, arbeidsgiver.getIdentifikator());
        assertThat(baaOpt).hasValueSatisfying(baa -> assertThat(baa.getRefusjonskravPrÅr()).isEqualByComparingTo(inntekt.multiply(ANTALL_MÅNEDER_I_ÅR)));

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = perioder.get(1);
        assertThat(beregningsgrunnlagPeriode2.getPeriodeÅrsaker()).containsExactly(PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(beregningsgrunnlagPeriode2.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
    }

    @Test
    public void skalKasteFeilHvisAntallPerioderErMerEnn1() {
        // Arrange
        BeregningsgrunnlagPeriodeDto.Builder periode1 = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT, List.of(ORG_NUMMER));
        BeregningsgrunnlagPeriodeDto.Builder periode2 = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT.plusDays(1), null, List.of(ORG_NUMMER));
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilBeregningsgrunnlagPeriode(periode1)
            .leggTilBeregningsgrunnlagPeriode(periode2)
            .build();
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Assert
        // Act
        Assertions.assertThrows(TekniskException.class, () -> {
            var input = new BeregningsgrunnlagInput(behandlingRef, null, null, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);
            tjeneste.fastsettPerioderForNaturalytelse(input, beregningsgrunnlag);
        });
    }

    @Test
    public void lagPeriodeForGraderingOver6G() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusWeeks(9);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusWeeks(18).minusDays(1);

        var arbeidsgiverGradering = Arbeidsgiver.virksomhet(ORG_NUMMER);
        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), newGrunnlagBuilder);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medArbeidsgiver(arbeidsgiverGradering)
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(9).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(9), graderingTom, PeriodeÅrsak.GRADERING);
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);
    }

    @Test
    public void skalSplitteBeregningsgrunnlagOgLeggeTilNyAndelVedEndringssøknadNårFrilansTilkommerOgGraderes() {
        // Arrange
        LocalDate graderingFom = SKJÆRINGSTIDSPUNKT.plusDays(10);
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT.plusDays(20);

        InntektArbeidYtelseGrunnlagDtoBuilder newGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();

        leggTilYrkesaktiviteterOgBeregningAktiviteter(List.of(ORG_NUMMER, ORG_NUMMER_2), newGrunnlagBuilder);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlagMedOverstyring(List.of(ORG_NUMMER, ORG_NUMMER_2), beregningAktivitetAggregat);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().get();

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1
        );
        newGrunnlagBuilder.medInntektsmeldinger(im1);
        AktivitetGradering aktivitetGradering = new AktivitetGradering(AndelGradering.builder()
            .medStatus(AktivitetStatus.FRILANSER)
            .medGradering(graderingFom, graderingTom, 50)
            .build());

        // Act
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = fastsettPerioderForRefusjonOgGradering(behandlingRef, grunnlag, beregningsgrunnlag, aktivitetGradering, newGrunnlagBuilder);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(9));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(10), graderingTom, PeriodeÅrsak.GRADERING);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);
        assertAndelStatuser(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList(),
            Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER));
        assertBeregningsgrunnlagPeriode(perioder.get(2), graderingTom.plusDays(1), TIDENES_ENDE, PeriodeÅrsak.GRADERING_OPPHØRER);

    }

    private BeregningAktivitetOverstyringDto lagOverstyringForAktivitet(InternArbeidsforholdRefDto arbId, Arbeidsgiver arbeidsgiver, BeregningAktivitetHandlingType handlingIkkeBenytt) {
        return BeregningAktivitetOverstyringDto.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdRef(arbId)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMedTilOgMed(ARBEIDSPERIODE.getFomDato(), ARBEIDSPERIODE.getTomDato()))
            .medHandling(handlingIkkeBenytt).build();
    }

    private void assertAndelStatuser(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, List<AktivitetStatus> statuser) {
        List<AktivitetStatus> aktivitetStatuser = andeler.stream().map(BeregningsgrunnlagPrStatusOgAndelDto::getAktivitetStatus).collect(Collectors.toList());
        assertThat(aktivitetStatuser).containsAll(statuser);

    }

    private Optional<BGAndelArbeidsforholdDto> finnBGAndelArbeidsforhold(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, String orgnr) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .flatMap(andel -> andel.getBgAndelArbeidsforhold().stream())
            .filter(bga -> bga.getArbeidsforholdOrgnr().equals(orgnr))
            .findFirst();
    }

    private void assertBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, LocalDate expectedFom, LocalDate expectedTom,
                                                 PeriodeÅrsak... perioderÅrsaker) {
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()).as("fom").isEqualTo(expectedFom);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()).as("tom").isEqualTo(expectedTom);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).as("periodeÅrsaker").containsExactlyInAnyOrder(perioderÅrsaker);
    }


    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlagMedOverstyring(List<String> orgnrs,
                                                                              BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        return lagBeregningsgrunnlag(orgnrs, beregningAktivitetAggregat, null);
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlagMedSaksbehandlet(List<String> orgnrs, BehandlingReferanse behandlingRef,
                                                                                BeregningAktivitetAggregatDto beregningAktivitetAggregat, BeregningAktivitetAggregatDto saksbehandlet) {
        BeregningsgrunnlagPeriodeDto.Builder beregningsgrunnlagPeriodeBuilder = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, null, orgnrs);
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
        BeregningsgrunnlagDto bg = beregningsgrunnlagBuilder.build();
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg)
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medSaksbehandletAktiviteter(saksbehandlet)
            .build(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(List<String> orgnrs,
                                                                BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                BeregningAktivitetOverstyringerDto BeregningAktivitetOverstyringer) {
        BeregningsgrunnlagPeriodeDto.Builder beregningsgrunnlagPeriodeBuilder = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, null, orgnrs);
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medHjemmel(Hjemmel.F_14_7));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
        BeregningsgrunnlagDto bg = beregningsgrunnlagBuilder.build();
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg)
            .medRegisterAktiviteter(beregningAktivitetAggregat)
            .medOverstyring(BeregningAktivitetOverstyringer)
            .build(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagBeregningsgrunnlagPerioderBuilder(LocalDate fom, LocalDate tom, List<String> orgnrs) {
        BeregningsgrunnlagPeriodeDto.Builder builder = BeregningsgrunnlagPeriodeDto.builder();
        for (String orgnr : orgnrs) {
            Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
            BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(arbeidsgiver)
                    .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1)));
            builder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder);
        }
        return builder
            .medBeregningsgrunnlagPeriode(fom, tom);
    }

}
