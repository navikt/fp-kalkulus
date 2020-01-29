package no.nav.folketrygdloven.kalkulator.verdikjede.komponenttest;

import static java.math.BigDecimal.ZERO;
import static no.nav.folketrygdloven.kalkulator.verdikjede.komponenttest.KomponenttestBeregningAssertUtil.assertBeregningsgrunnlag;
import static no.nav.folketrygdloven.kalkulator.verdikjede.komponenttest.KomponenttestBeregningAssertUtil.assertBeregningsgrunnlagAndel;
import static no.nav.folketrygdloven.kalkulator.verdikjede.komponenttest.KomponenttestBeregningAssertUtil.assertBeregningsgrunnlagPeriode;
import static no.nav.folketrygdloven.kalkulator.verdikjede.komponenttest.KomponenttestBeregningAssertUtil.assertSammenligningsgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBeregningsgrunnlagATFLHåndterer;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagATFLDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.InntektPrAndelDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningArbeidsgiverTestUtil;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.vedtak.konfig.Tid;

@QuarkusTest
public class KomponenttestBeregningArbeidstakerTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.DECEMBER, 1);
    private static final LocalDateTime INNSENDINGSTIDSPUNKT = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(1).atStartOfDay();
    private static final String ORGNR = "974761076";

    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;
    @Inject
    private BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste;

    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);

    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;

    @BeforeEach
    public void setUp() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
    }

    // Arbeidsgivere: 1 (virksomhet)
    // Arbeidsforhold: 1
    // Inntekt: < 6G
    // Refusjon: Full
    // Aksjonspunkt: Nei
    @Test
    public void skal_utføre_beregning_for_arbeidstaker_uten_aksjonspunkter() {
        // Arrange
        var arbeidsforholdId = InternArbeidsforholdRefDto.nyRef();
        BigDecimal inntektPrMnd = BigDecimal.valueOf(35000L);
        BigDecimal inntektPrÅr = inntektPrMnd.multiply(BigDecimal.valueOf(12L));
        BigDecimal refusjonPrMnd = BigDecimal.valueOf(35000L);
        BigDecimal refusjonskravPrÅr = refusjonPrMnd.multiply(BigDecimal.valueOf(12L));

        Periode periode = Periode.of(Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 7).getFom(), null);

        BeregningIAYTestUtil.byggArbeidForBehandlingMedVirksomhetPåInntekt(behandlingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7),
            Tid.TIDENES_ENDE, arbeidsforholdId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORGNR), inntektPrMnd, iayGrunnlagBuilder);
        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjonPrMnd, inntektPrMnd);
        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID, periode, ORGNR);
        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1);

        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        var input = lagInput(behandlingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act steg FastsettBeregningAktiviteter
        BeregningResultatAggregat resultat = doStegFastsettSkjæringstidspunkt(input);
        input = utvidMedOppdaterGrunnlag(input, resultat.getBeregningsgrunnlagGrunnlag());

        // Assert
        assertFastsettSkjæringstidspunktSteg(resultat, true, ORGNR);

        // Act steg KontrollerFaktaBeregning
        resultat = doStegKontrollerFaktaBeregning(input);
        input = utvidMedOppdaterGrunnlag(input, resultat.getBeregningsgrunnlagGrunnlag());

        // Assert steg KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(resultat);

        // Act steg ForeslåBeregningsgrunnlag
        resultat = doStegForeslåBeregningsgrunnlag(input);
        input = utvidMedOppdaterGrunnlag(input, resultat.getBeregningsgrunnlagGrunnlag());

        // Assert steg ForeslåBeregningsgrunnlag
        assertForeslåBeregningsgrunnlagSteg(
            resultat, inntektPrÅr,
            inntektPrÅr,
            ZERO,
            false);

        // Act steg fordel
        resultat = doStegFordelBeregningsgrunnlag(input);
        input = utvidMedOppdaterGrunnlag(input, resultat.getBeregningsgrunnlagGrunnlag());

        // Assert steg KontrollerFaktaBeregning
        assertKontrollerFaktaOmFordelingSteg(resultat, refusjonskravPrÅr, inntektPrÅr);

        // Act steg FastsettBeregningsgrunnlag
        resultat = doStegFastsettBeregningsgrunnlag(input);
        utvidMedOppdaterGrunnlag(input, resultat.getBeregningsgrunnlagGrunnlag());

        // Assert steg FastsettBeregningsgrunnlag
        // Assert steg FastsettBeregningsgrunnlag og oppdaterer
        assertFastsettBeregningsgrunnlag(resultat, refusjonskravPrÅr,
            inntektPrÅr,
            null);
    }

    // Arbeidsgivere: 1 (virksomhet)
    // Arbeidsforhold: 1
    // Inntekt: < 6G
    // Refusjon: Ingen
    // Aksjonspunkt: 5038 - FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS
    @Test
    public void skal_utføre_beregning_for_arbeidstaker_med_aksjonspunkter() {
        // Arrange
        var arbeidsforholdId = InternArbeidsforholdRefDto.nyRef();
        BigDecimal inntektIRegister = BigDecimal.valueOf(70000L);
        BigDecimal inntektFraIM = BigDecimal.valueOf(35000L);
        BigDecimal inntektPrÅrRegister = inntektIRegister.multiply(BigDecimal.valueOf(12L));
        BigDecimal inntektPrÅrIM = inntektFraIM.multiply(BigDecimal.valueOf(12L));
        Integer overstyrtPrÅr = 500000;

        Periode periode = Periode.of(Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 7).getFom(), null);

        BeregningIAYTestUtil.byggArbeidForBehandlingMedVirksomhetPåInntekt(behandlingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7),
            Tid.TIDENES_ENDE, arbeidsforholdId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORGNR), inntektIRegister, iayGrunnlagBuilder);

        var im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT_OPPTJENING, ZERO, inntektFraIM);
        var opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID, periode, ORGNR);

        List<InntektsmeldingDto> inntektsmeldinger = List.of(im1);
        var iayGrunnlag = iayGrunnlagBuilder.medInntektsmeldinger(inntektsmeldinger).build();
        var input = lagInput(behandlingReferanse, opptjeningAktiviteter, iayGrunnlag, 100);

        // Act steg FastsettBeregningAktiviteter
        BeregningResultatAggregat resultat = doStegFastsettSkjæringstidspunkt(input);
        input = utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);

        // Assert
        assertFastsettSkjæringstidspunktSteg(resultat, true, ORGNR);

        // Act steg KontrollerFaktaBeregning
        resultat = doStegKontrollerFaktaBeregning(input);
        input = utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Assert steg KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(resultat);

        // Act steg ForeslåBeregningsgrunnlag
        resultat = doStegForeslåBeregningsgrunnlag(input);
        input = utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.FORESLÅTT);

        // Assert steg ForeslåBeregningsgrunnlag
        assertForeslåBeregningsgrunnlagSteg(
            resultat, inntektPrÅrRegister,
            inntektPrÅrIM,
            BigDecimal.valueOf(500),
            true);

        // Act steg fordel
        resultat = doStegFordelBeregningsgrunnlag(input);
        input = utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);

        // Assert steg KontrollerFaktaBeregning
        assertKontrollerFaktaOmFordelingSteg(resultat, ZERO, inntektPrÅrIM);

        // Act oppdaterer
        FastsettBeregningsgrunnlagATFLDto dto = lagATFLOppdatererDto(overstyrtPrÅr);
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = FastsettBeregningsgrunnlagATFLHåndterer.håndter(input, dto);
        input = utvidMedOppdaterGrunnlagOGTilstand(input, nyttGrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);

        // Act steg FastsettBeregningsgrunnlag
        resultat = doStegFastsettBeregningsgrunnlag(input);
        utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.FASTSATT);

        // Assert steg FastsettBeregningsgrunnlag og oppdaterer
        assertFastsettBeregningsgrunnlag(resultat, ZERO,
            inntektPrÅrIM,
            overstyrtPrÅr);
    }

    private BeregningsgrunnlagInput utvidMedOppdaterGrunnlagOGTilstand(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto grunnlagDto, BeregningsgrunnlagTilstand tilstand) {
        BeregningsgrunnlagInput newInput = input.medBeregningsgrunnlagGrunnlag(grunnlagDto);
        newInput.leggTilBeregningsgrunnlagIHistorikk(grunnlagDto, tilstand);
        return newInput;
    }

    private BeregningsgrunnlagInput utvidMedOppdaterGrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto grunnlagDto) {
        BeregningsgrunnlagInput newInput = input.medBeregningsgrunnlagGrunnlag(grunnlagDto);
        return newInput;
    }

    // Arbeidsgivere: 1 (privatperson)
    // Arbeidsforhold: 1
    // Inntekt: < 6G
    // Refusjon: Ingen
    // Aksjonspunkt: 5038 - FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS
    @Test
    public void skal_utføre_beregning_for_arbeidstaker_med_aksjonspunkter_arbeidsgiver_er_privatperson() {
        // Arrange
        BigDecimal inntektIRegister = BigDecimal.valueOf(70000L);
        BigDecimal inntektPrÅrRegister = inntektIRegister.multiply(BigDecimal.valueOf(12L));
        String arbeidsgiverAktørId = behandlingReferanse.getAktørId().getId();

        BeregningIAYTestUtil.byggArbeidForBehandlingMedVirksomhetPåInntekt(behandlingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7),
            Tid.TIDENES_ENDE, null, arbeidsgiverTestUtil.forArbeidsgiverpPrivatperson(behandlingReferanse.getAktørId()), inntektIRegister, iayGrunnlagBuilder);

        Periode periode = Periode.of(Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 7).getFom(), null);

        var opptjeningAktiviteteter = OpptjeningAktiviteterDto.fraAktørId(OpptjeningAktivitetType.ARBEID, periode, arbeidsgiverAktørId);

        var iayGrunnlag = iayGrunnlagBuilder.build();
        var input = lagInput(behandlingReferanse, opptjeningAktiviteteter, iayGrunnlag, 100);

        // Act steg FastsettBeregningAktiviteter
        var resultat = doStegFastsettSkjæringstidspunkt(input);
        input = utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);

        // Assert
        assertFastsettSkjæringstidspunktSteg(resultat, false, arbeidsgiverAktørId);

        // Act steg KontrollerFaktaBeregning
        resultat = doStegKontrollerFaktaBeregning(input);
        input = utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Assert steg KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(resultat);

        // Act steg ForeslåBeregningsgrunnlag
        resultat = doStegForeslåBeregningsgrunnlag(input);
        input = utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.FORESLÅTT);

        // Assert steg ForeslåBeregningsgrunnlag
        assertForeslåBeregningsgrunnlagSteg(
            resultat, inntektPrÅrRegister,
            null,
            ZERO,
            false);

        // Act steg fordel
        resultat = doStegFordelBeregningsgrunnlag(input);
        input = utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);

        // Assert steg KontrollerFaktaBeregning
        assertKontrollerFaktaOmFordelingSteg(resultat, ZERO, inntektPrÅrRegister);

        // Act steg FastsettBeregningsgrunnlag
        resultat = doStegFastsettBeregningsgrunnlag(input);
        utvidMedOppdaterGrunnlagOGTilstand(input, resultat.getBeregningsgrunnlagGrunnlag(), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);

        // Assert steg FastsettBeregningsgrunnlag og oppdaterer
        assertFastsettBeregningsgrunnlag(resultat, BigDecimal.ZERO,
            inntektPrÅrRegister,
            null);
    }

    private BeregningResultatAggregat doStegFastsettBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        return beregningsgrunnlagTjeneste.fastsettBeregningsgrunnlag(input);
    }

    private BeregningResultatAggregat doStegFordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        return beregningsgrunnlagTjeneste.fordelBeregningsgrunnlag(input);
    }

    private BeregningResultatAggregat doStegForeslåBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        return beregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(input);
    }

    private BeregningResultatAggregat kontrollerFaktaBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        return beregningsgrunnlagTjeneste.kontrollerFaktaBeregningsgrunnlag(input);
    }

    private BeregningResultatAggregat doStegKontrollerFaktaBeregning(BeregningsgrunnlagInput input) {
        return kontrollerFaktaBeregningsgrunnlag(input);
    }

    private BeregningResultatAggregat doStegFastsettSkjæringstidspunkt(BeregningsgrunnlagInput input) {
        return beregningsgrunnlagTjeneste.fastsettBeregningsaktiviteter(input);
    }

    private FastsettBeregningsgrunnlagATFLDto lagATFLOppdatererDto(Integer overstyrtPrÅr) {
        return new FastsettBeregningsgrunnlagATFLDto(lagInntektPrAndelDto(overstyrtPrÅr), null);
    }

    private List<InntektPrAndelDto> lagInntektPrAndelDto(Integer overstyrtPrÅr) {
        return Collections.singletonList(new InntektPrAndelDto(overstyrtPrÅr, 1L));
    }

    private void assertFastsettSkjæringstidspunktSteg(BeregningResultatAggregat resultat, boolean erVirksomhet, String identifikator) {
        assertThat(resultat.getBeregningAksjonspunktResultater()).isEmpty();
        List<BeregningAktivitetDto> beregningAktiviteter = resultat.getBeregningsgrunnlagGrunnlag().getRegisterAktiviteter().getBeregningAktiviteter();
        assertThat(beregningAktiviteter).hasSize(1);
        assertThat(beregningAktiviteter.get(0)).satisfies(aktivitet -> {
            assertThat(aktivitet.getPeriode().getFomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7));
            assertThat(aktivitet.getPeriode().getTomDato()).isEqualTo(Tid.TIDENES_ENDE);
            assertThat(aktivitet.getOpptjeningAktivitetType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
            assertThat(aktivitet.getArbeidsgiver().getErVirksomhet()).isEqualTo(erVirksomhet);
            assertThat(aktivitet.getArbeidsgiver().getIdentifikator()).isEqualTo(identifikator);
            assertThat(aktivitet.getArbeidsforholdRef()).isEqualTo(InternArbeidsforholdRefDto.nullRef());
        });
    }

    private void assertKontrollerFaktaOmBeregningSteg(BeregningResultatAggregat resultat) {

        // Assert steg KontrollerFaktaBeregning
        assertThat(resultat.getBeregningAksjonspunktResultater()).isEmpty();
        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        assertBeregningsgrunnlag(beregningsgrunnlag,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(AktivitetStatus.ARBEIDSTAKER));

        // Beregningsgrunnlagperiode
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertBeregningsgrunnlagPeriode(førstePeriode,
            Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING),
            ZERO,
            null,
            null,
            null);

        // BeregningsgrunnlagPrStatusOgAndelDto
        assertThat(førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertBeregningsgrunnlagAndel(
            førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            null,
            AktivitetStatus.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
            null, null);
    }

    private void assertKontrollerFaktaOmFordelingSteg(BeregningResultatAggregat resultat, BigDecimal refusjonskravPrÅr,
                                                      BigDecimal beregnetPrÅr) {

        // Assert steg KontrollerFaktaBeregning
        assertThat(resultat.getBeregningAksjonspunktResultater()).isEmpty();
        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        assertBeregningsgrunnlag(beregningsgrunnlag,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(AktivitetStatus.ARBEIDSTAKER));

        // Beregningsgrunnlagperiode
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertBeregningsgrunnlagPeriode(førstePeriode,
            Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING),
            beregnetPrÅr,
            null,
            null,
            refusjonskravPrÅr);

        // BeregningsgrunnlagPrStatusOgAndelDto
        assertThat(førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertBeregningsgrunnlagAndel(
            førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            beregnetPrÅr,
            AktivitetStatus.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
            refusjonskravPrÅr, null);
    }

    private void assertForeslåBeregningsgrunnlagSteg(BeregningResultatAggregat resultat, BigDecimal inntektPrÅrRegister,
                                                     BigDecimal inntektPrÅrIM,
                                                     BigDecimal avvik,
                                                     boolean medAksjonspunkt) {
        // Aksjonspunkter
        if (medAksjonspunkt) {
            assertThat(resultat.getBeregningAksjonspunktResultater()).isNotEmpty().hasSize(1);
            assertThat(resultat.getBeregningAksjonspunktResultater().get(0).getBeregningAksjonspunktDefinisjon())
                .isEqualTo(BeregningAksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
        } else {
            assertThat(resultat.getBeregningAksjonspunktResultater()).isEmpty();
        }

        // Sammenligningsgrunnlag
        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        assertSammenligningsgrunnlag(beregningsgrunnlag.getSammenligningsgrunnlag(), inntektPrÅrRegister, avvik);

        BigDecimal gjeldendeInntekt = inntektPrÅrIM == null ? inntektPrÅrRegister : inntektPrÅrIM;
        // Periodenivå
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto førstePeriodeStegTo = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertBeregningsgrunnlagPeriode(førstePeriodeStegTo,
            Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING), gjeldendeInntekt, null, null, BigDecimal.ZERO);

        // Andelsnivå
        for (BeregningsgrunnlagPeriodeDto bgp : beregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            for (BeregningsgrunnlagPrStatusOgAndelDto andel : bgp.getBeregningsgrunnlagPrStatusOgAndelList()) {
                assertBeregningsgrunnlagAndel(andel,
                    gjeldendeInntekt,
                    AktivitetStatus.ARBEIDSTAKER,
                    Inntektskategori.ARBEIDSTAKER,
                    SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3),
                    SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), null, null);
            }
        }
    }

    private void assertFastsettBeregningsgrunnlag(BeregningResultatAggregat resultat, BigDecimal refusjonskravPrÅr,
                                                  BigDecimal beregnetPrÅr,
                                                  Integer overstyrtPrÅr) {
        // BeregningsgrunnlagDto
        BeregningsgrunnlagDto beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BigDecimal overstyrtViØnskerAssertPå = null;
        BigDecimal inntektDagsatsBeregnesFra = beregnetPrÅr;
        if (overstyrtPrÅr != null) {
            inntektDagsatsBeregnesFra = BigDecimal.valueOf(overstyrtPrÅr);
            overstyrtViØnskerAssertPå = BigDecimal.valueOf(overstyrtPrÅr);
        }
        BigDecimal forventetDagsats = inntektDagsatsBeregnesFra.divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).min(BigDecimal.valueOf(2236));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);

        // Periodenivå
        BeregningsgrunnlagPeriodeDto førstePeriodeTredjeSteg = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(førstePeriodeTredjeSteg.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertBeregningsgrunnlagPeriode(førstePeriodeTredjeSteg,
            Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING),
            beregnetPrÅr,
            forventetDagsats.longValue(),
            overstyrtViØnskerAssertPå, refusjonskravPrÅr);

        // Andelsnivå
        BeregningsgrunnlagPrStatusOgAndelDto andel = førstePeriodeTredjeSteg.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertBeregningsgrunnlagAndel(andel,
            beregnetPrÅr,
            AktivitetStatus.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
            refusjonskravPrÅr,
            overstyrtViØnskerAssertPå);
    }

    private BeregningsgrunnlagInput lagInput(BehandlingReferanse ref, OpptjeningAktiviteterDto opptjeningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag, int dekningsgrad) {
        return BeregningsgrunnlagInputTestUtil.lagInputMedIAYOgOpptjeningsaktiviteter(ref, opptjeningAktiviteter, iayGrunnlag, dekningsgrad, 2);
    }

}
