package no.nav.folketrygdloven.kalkulator.guitjenester;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.BeregningsgrunnlagPrStatusOgAndelDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FaktaOmBeregningDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FaktaOmBeregningTilfelleDtoTjenesteProviderMock;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FastsettGrunnlagGenerell;
import no.nav.folketrygdloven.kalkulator.guitjenester.fakta.YtelsespesifiktGrunnlagTjenesteMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.utils.UnitTestLookupInstanceImpl;


public class BeregningsgrunnlagDtoTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final Inntektskategori INNTEKTSKATEGORI = Inntektskategori.ARBEIDSTAKER;
    private static final BigDecimal AVKORTET_PR_AAR = BigDecimal.valueOf(150000);
    private static final BigDecimal BRUTTO_PR_AAR = BigDecimal.valueOf(300000);
    private static final BigDecimal REDUSERT_PR_AAR = BigDecimal.valueOf(500000);
    private static final BigDecimal OVERSTYRT_PR_AAR = BigDecimal.valueOf(500);
    private static final BigDecimal PGI_SNITT = BigDecimal.valueOf(400000);
    private static final LocalDate ANDEL_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate ANDEL_TOM = LocalDate.now();
    private static final String ORGNR = "973093681";
    private static final Long ANDELSNR = 1L;
    private static final String PRIVATPERSON_NAVN = "Skrue McDuck";
    private static final String VIRKSOMHET_NAVN = "NAV AS";
    private static final BigDecimal RAPPORTERT_PR_AAR = BigDecimal.valueOf(300000);
    private static final BigDecimal AVVIK_OVER_25_PROSENT = BigDecimal.valueOf(500L);
    private static final BigDecimal AVVIK_UNDER_25_PROSENT = BigDecimal.valueOf(30L);
    private static final LocalDate SAMMENLIGNING_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate SAMMENLIGNING_TOM = LocalDate.now();
    private static Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(ORGNR);
    private BigDecimal GRUNNBELØP = BigDecimal.valueOf(99_888);
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock();
    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;
    private BeregningsgrunnlagGrunnlagDto grunnlag;
    private BeregningAktivitetAggregatDto beregningAktiviteter;

    @BeforeEach
    public void setup() {
        FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjeneste(FaktaOmBeregningTilfelleDtoTjenesteProviderMock.getTjenesteInstances());
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste beregningsgrunnlagPrStatusOgAndelDtoTjeneste =
                new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste((new UnitTestLookupInstanceImpl<>(new FastsettGrunnlagGenerell())));
        beregningsgrunnlagDtoTjeneste = new BeregningsgrunnlagDtoTjeneste(
                faktaOmBeregningDtoTjeneste,
                beregningsgrunnlagPrStatusOgAndelDtoTjeneste, new UnitTestLookupInstanceImpl<>(new YtelsespesifiktGrunnlagTjenesteMock()));
        virksomhet.setNavn(VIRKSOMHET_NAVN);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_aktivitetStatus_får_korrekte_verdier() {
        // Arrange
        lagBehandlingMedBgOgOpprettFagsakRelasjon(virksomhet);
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag,
                InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());

        // Assert
        List<AktivitetStatus> aktivitetStatus = beregningsgrunnlagDto.getAktivitetStatus();
        assertThat(aktivitetStatus).isNotNull();
        assertThat(aktivitetStatus.get(0)).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_sammenligningsgrunnlag_får_korrekte_verdier() {
        // Arrange
        lagBehandlingMedBgOgOpprettFagsakRelasjon(virksomhet);

        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());

        // Assert
        SammenligningsgrunnlagDto sammenligningsgrunnlag = beregningsgrunnlagDto.getSammenligningsgrunnlag();
        assertThat(sammenligningsgrunnlag).isNotNull();
        assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(AVVIK_OVER_25_PROSENT);
        assertThat(sammenligningsgrunnlag.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);

        SammenligningsgrunnlagDto sammenligningsgrunnlagPrStatus = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(0);
        assertThat(sammenligningsgrunnlagPrStatus).isNotNull();
        assertThat(sammenligningsgrunnlagPrStatus.getAvvikPromille()).isEqualTo(AVVIK_OVER_25_PROSENT);
        assertThat(sammenligningsgrunnlagPrStatus.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);
        assertThat(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType()).isEqualTo(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
        assertThat(sammenligningsgrunnlagPrStatus.getDifferanseBeregnet()).isEqualTo(BRUTTO_PR_AAR.subtract(RAPPORTERT_PR_AAR));
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_får_korrekte_verdier() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjon(arbeidsgiver);

        // Act
        BeregningsgrunnlagDto grunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());

        // Assert
        assertThat(grunnlagDto).isNotNull();
        assertThat(grunnlagDto.getSkjaeringstidspunktBeregning()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(grunnlagDto.getHalvG().intValue()).isEqualTo(GRUNNBELØP.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP).intValue());
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_får_korrekte_verdier_om_fakta_om_beregning_er_utført_uten_fastsatt_inntekt() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto faktaOmBeregningBg = lagBehandlingMedBgOgOpprettFagsakRelasjon(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto andel = faktaOmBeregningBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BigDecimal beregnetEtterFastsattSteg = BigDecimal.valueOf(10000);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel).medBeregnetPrÅr(beregnetEtterFastsattSteg);

        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();

        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag, builder.build());

        // Assert
        assertBeregningsgrunnlag(beregnetEtterFastsattSteg, beregningsgrunnlagDto);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_beregningsgrunnlagperiode_får_korrekte_verdier() {
        lagBehandlingMedBgOgOpprettFagsakRelasjon(virksomhet);
        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag, builder.build());

        // Assert
        var beregningsgrunnlagPeriodeDtoList = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode();
        assertThat(beregningsgrunnlagPeriodeDtoList).hasSize(1);

        var periodeDto = beregningsgrunnlagPeriodeDtoList.get(0);
        var andelList = periodeDto.getBeregningsgrunnlagPrStatusOgAndel();
        assertThat(andelList).hasSize(1);
        var andelDto = andelList.get(0);
        assertThat(andelDto.getInntektskategori()).isEqualTo(INNTEKTSKATEGORI);
        assertThat(andelDto.getAndelsnr()).isEqualTo(ANDELSNR);
        assertThat(andelDto.getAvkortetPrAar()).isEqualTo(AVKORTET_PR_AAR);
        assertThat(andelDto.getRedusertPrAar()).isEqualTo(REDUSERT_PR_AAR);
        assertThat(andelDto.getBruttoPrAar()).isEqualTo(OVERSTYRT_PR_AAR);
        assertThat(andelDto.getBeregnetPrAar()).isEqualTo(BRUTTO_PR_AAR);
        assertThat(andelDto.getBeregningsgrunnlagFom()).isEqualTo(ANDEL_FOM);
        assertThat(andelDto.getBeregningsgrunnlagTom()).isEqualTo(ANDEL_TOM);
        assertThat(andelDto.getArbeidsforhold()).isNotNull();
        assertThat(andelDto.getArbeidsforhold().getArbeidsgiverIdent()).isEqualTo(ORGNR);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_beregningsgrunnlagperiode_får_korrekte_verdier_ved_arbeidsgiver_privatperson() {
        // Arrange
        AktørId aktørId = AktørId.dummy();
        Arbeidsgiver person = Arbeidsgiver.person(aktørId);
        lagBehandlingMedBgOgOpprettFagsakRelasjon(person);
        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag, builder.build());

        // Assert
        var beregningsgrunnlagPeriodeDtoList = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode();
        assertThat(beregningsgrunnlagPeriodeDtoList).hasSize(1);

        var periodeDto = beregningsgrunnlagPeriodeDtoList.get(0);
        var andelList = periodeDto.getBeregningsgrunnlagPrStatusOgAndel();
        assertThat(andelList).hasSize(1);
        var andelDto = andelList.get(0);
        assertThat(andelDto.getInntektskategori()).isEqualTo(INNTEKTSKATEGORI);
        assertThat(andelDto.getAndelsnr()).isEqualTo(ANDELSNR);
        assertThat(andelDto.getAvkortetPrAar()).isEqualTo(AVKORTET_PR_AAR);
        assertThat(andelDto.getRedusertPrAar()).isEqualTo(REDUSERT_PR_AAR);
        assertThat(andelDto.getBruttoPrAar()).isEqualTo(OVERSTYRT_PR_AAR);
        assertThat(andelDto.getBeregnetPrAar()).isEqualTo(BRUTTO_PR_AAR);
        assertThat(andelDto.getBeregningsgrunnlagFom()).isEqualTo(ANDEL_FOM);
        assertThat(andelDto.getBeregningsgrunnlagTom()).isEqualTo(ANDEL_TOM);
        assertThat(andelDto.getArbeidsforhold()).isNotNull();
        assertThat(andelDto.getArbeidsforhold().getArbeidsgiverIdent()).isEqualTo(aktørId.getAktørId());
    }

    @Test
    public void skalSetteSammenligningsgrunnlagDtoMedDifferanseNårFlereAndeler() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjonFlereAndeler(arbeidsgiver);
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());
        // Assert
        SammenligningsgrunnlagDto sammenligningsgrunnlag = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(0);
        assertThat(sammenligningsgrunnlag).isNotNull();
        assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(AVVIK_OVER_25_PROSENT);
        assertThat(sammenligningsgrunnlag.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagType()).isEqualTo(SammenligningsgrunnlagType.SAMMENLIGNING_AT);
        assertThat(sammenligningsgrunnlag.getDifferanseBeregnet()).isEqualTo(BRUTTO_PR_AAR.subtract(RAPPORTERT_PR_AAR));

        SammenligningsgrunnlagDto sammenligningsgrunnlag2 = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(1);
        assertThat(sammenligningsgrunnlag2).isNotNull();
        assertThat(sammenligningsgrunnlag2.getAvvikPromille()).isEqualTo(AVVIK_UNDER_25_PROSENT);
        assertThat(sammenligningsgrunnlag2.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlag2.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlag2.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);
        assertThat(sammenligningsgrunnlag2.getSammenligningsgrunnlagType()).isEqualTo(SammenligningsgrunnlagType.SAMMENLIGNING_FL);
        assertThat(sammenligningsgrunnlag2.getDifferanseBeregnet()).isEqualTo(BRUTTO_PR_AAR.subtract(RAPPORTERT_PR_AAR));

        SammenligningsgrunnlagDto sammenligningsgrunnlag3 = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(2);
        assertThat(sammenligningsgrunnlag3).isNotNull();
        assertThat(sammenligningsgrunnlag3.getAvvikPromille()).isEqualTo(AVVIK_OVER_25_PROSENT);
        assertThat(sammenligningsgrunnlag3.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlag3.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlag3.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);
        assertThat(sammenligningsgrunnlag3.getSammenligningsgrunnlagType()).isEqualTo(SammenligningsgrunnlagType.SAMMENLIGNING_SN);
        assertThat(sammenligningsgrunnlag3.getDifferanseBeregnet()).isEqualTo(PGI_SNITT.subtract(RAPPORTERT_PR_AAR));
    }

    @Test
    public void skalSetteFastsettingGrunnlagForHverBeregningsgrunnlagPrStatusOgAndelNårFlereAndelerMedUlikeAvvik() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjonFlereAndeler(arbeidsgiver);
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());
        // Assert
        var beregningsgrunnlagPrStatusOgAndelDto = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode().get(0).getBeregningsgrunnlagPrStatusOgAndel().get(0);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto).isNotNull();
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.getSkalFastsetteGrunnlag()).isEqualTo(true);

        var beregningsgrunnlagPrStatusOgAndelDto2 = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode().get(0).getBeregningsgrunnlagPrStatusOgAndel().get(1);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto2).isNotNull();
        assertThat(beregningsgrunnlagPrStatusOgAndelDto2.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto2.getSkalFastsetteGrunnlag()).isEqualTo(false);

        var beregningsgrunnlagPrStatusOgAndelDto3 = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode().get(0).getBeregningsgrunnlagPrStatusOgAndel().get(2);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto3).isNotNull();
        assertThat(beregningsgrunnlagPrStatusOgAndelDto3.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto3.getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalSetteBeregningsgrunnlagPrStatusOgAndelDtoForArbeidstakerNårSammenligningsTypeErATFLSN() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjon(arbeidsgiver);
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());
        // Assert
        var beregningsgrunnlagPrStatusOgAndelDto = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode().get(0).getBeregningsgrunnlagPrStatusOgAndel().get(0);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto).isNotNull();
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalBenytteGammeltSammenligningsgrunnlagNårDetIkkeDetFinnesSammenligningsgrunnlagPrStatus() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjon(arbeidsgiver);
        grunnlag.getBeregningsgrunnlag().get().getSammenligningsgrunnlagPrStatusListe().clear();
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(koblingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());
        // Assert
        assertThat(beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().size()).isOne();
        SammenligningsgrunnlagDto sammenligningsgrunnlag = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(0);
        assertThat(sammenligningsgrunnlag).isNotNull();
        assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(AVVIK_OVER_25_PROSENT);
        assertThat(sammenligningsgrunnlag.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagType()).isEqualTo(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
        assertThat(sammenligningsgrunnlag.getDifferanseBeregnet()).isEqualTo(BRUTTO_PR_AAR.subtract(RAPPORTERT_PR_AAR));
    }

    private KoblingReferanse lagReferanseMedStp(KoblingReferanse koblingReferanse) {
        Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        return koblingReferanse.medSkjæringstidspunkt(skjæringstidspunkt);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagDto(KoblingReferanse ref, BeregningsgrunnlagGrunnlagDto grunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var input = new BeregningsgrunnlagGUIInput(ref, iayGrunnlag, List.of(), List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        return beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(input);
    }

    private void assertBeregningsgrunnlag(BigDecimal beregnet, BeregningsgrunnlagDto grunnlagDto) {
        assertThat(grunnlagDto).isNotNull();
        assertThat(grunnlagDto.getSkjaeringstidspunktBeregning()).as("skjæringstidspunkt").isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(grunnlagDto.getHalvG().intValue()).isEqualTo(GRUNNBELØP.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP).intValue());
        var periodeDto = grunnlagDto.getBeregningsgrunnlagPeriode().get(0);
        assertThat(periodeDto.getBeregningsgrunnlagPeriodeFom()).as("BeregningsgrunnlagPeriodeFom").isEqualTo(ANDEL_FOM);
        assertThat(periodeDto.getBeregningsgrunnlagPeriodeTom()).as("BeregningsgrunnlagPeriodeTom").isNull();
        var andelDto = periodeDto.getBeregningsgrunnlagPrStatusOgAndel().get(0);
        assertThat(andelDto.getBeregnetPrAar()).isEqualByComparingTo(beregnet);
        assertThat(andelDto.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(andelDto.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
    }

    private no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto lagBeregningsgrunnlag(Arbeidsgiver arbeidsgiver) {
        no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto sammenligningsgrunnlagRestDto = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto.builder()
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT).build();
        var beregningsgrunnlag = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medGrunnbeløp(GRUNNBELØP)
                .medSammenligningsgrunnlag(sammenligningsgrunnlagRestDto)
                .leggTilSammenligningsgrunnlag(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto.builder()
                        .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                        .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                        .medSammenligningsgrunnlagType(no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN)
                        .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM))
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7_8_30)
                .build(beregningsgrunnlag);


        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        buildBgPrStatusOgAndel(bgPeriode, arbeidsgiver);
        return beregningsgrunnlag;
    }

    private no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto lagBeregningsgrunnlagMedFlereAndeler(Arbeidsgiver arbeidsgiver) {
        no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto sammenligningsgrunnlagDto = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto.builder()
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT).build();
        var beregningsgrunnlag = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medSammenligningsgrunnlag(sammenligningsgrunnlagDto)
                .medGrunnbeløp(GRUNNBELØP)
                .leggTilSammenligningsgrunnlag(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto.builder()
                        .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                        .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                        .medSammenligningsgrunnlagType(no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType.SAMMENLIGNING_AT)
                        .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM))
                .leggTilSammenligningsgrunnlag(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto.builder()
                        .medAvvikPromilleNy(AVVIK_UNDER_25_PROSENT)
                        .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                        .medSammenligningsgrunnlagType(no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType.SAMMENLIGNING_FL)
                        .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM))
                .leggTilSammenligningsgrunnlag(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto.builder()
                        .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                        .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                        .medSammenligningsgrunnlagType(no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType.SAMMENLIGNING_SN)
                        .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM))
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7_8_30)
                .build(beregningsgrunnlag);


        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        buildBgPrStatusOgAndelForMangeAndeler(bgPeriode, arbeidsgiver);
        return beregningsgrunnlag;
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Arbeidsgiver arbeidsgiver) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(ANDELSNR)
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
                .medBeregnetPrÅr(BRUTTO_PR_AAR)
                .medAvkortetPrÅr(AVKORTET_PR_AAR)
                .medRedusertPrÅr(REDUSERT_PR_AAR)
                .medOverstyrtPrÅr(OVERSTYRT_PR_AAR)
                .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(ANDEL_FOM, null)
                .build(beregningsgrunnlag);
    }

    private void buildBgPrStatusOgAndelForMangeAndeler(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Arbeidsgiver arbeidsgiver) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(ANDELSNR)
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
                .medBeregnetPrÅr(BRUTTO_PR_AAR)
                .medAvkortetPrÅr(AVKORTET_PR_AAR)
                .medRedusertPrÅr(REDUSERT_PR_AAR)
                .medOverstyrtPrÅr(OVERSTYRT_PR_AAR)
                .build(beregningsgrunnlagPeriode);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(ANDELSNR + 1)
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.FRILANSER)
                .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
                .medBeregnetPrÅr(BRUTTO_PR_AAR)
                .build(beregningsgrunnlagPeriode);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(ANDELSNR + 3)
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medPgi(PGI_SNITT, List.of())
                .build(beregningsgrunnlagPeriode);
    }

    private no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto lagBehandlingMedBgOgOpprettFagsakRelasjon(Arbeidsgiver arbeidsgiver) {

        this.beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlag = lagBeregningsgrunnlag(arbeidsgiver);

        this.grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPRETTET);

        return beregningsgrunnlag;
    }

    private no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto lagBehandlingMedBgOgOpprettFagsakRelasjonFlereAndeler(Arbeidsgiver arbeidsgiver) {
        this.beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlag = lagBeregningsgrunnlagMedFlereAndeler(arbeidsgiver);

        this.grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPRETTET);

        return beregningsgrunnlag;
    }

    private BeregningAktivitetAggregatDto lagBeregningAktiviteter(Arbeidsgiver arbeidsgiver) {
        return lagBeregningAktiviteter(BeregningAktivitetAggregatDto.builder(), arbeidsgiver);
    }

    private BeregningAktivitetAggregatDto lagBeregningAktiviteter(BeregningAktivitetAggregatDto.Builder builder, Arbeidsgiver arbeidsgiver) {
        return builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsgiver(arbeidsgiver)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMedTilOgMed(ANDEL_FOM, ANDEL_TOM))
                        .build())
                .build();
    }
}
