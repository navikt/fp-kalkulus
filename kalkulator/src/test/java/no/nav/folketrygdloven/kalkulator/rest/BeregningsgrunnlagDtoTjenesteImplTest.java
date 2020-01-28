package no.nav.folketrygdloven.kalkulator.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.rest.fakta.AndelerForFaktaOmBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.rest.fakta.FaktaOmBeregningDtoTjeneste;
import no.nav.folketrygdloven.kalkulator.rest.fakta.FaktaOmBeregningTilfelleDtoTjenesteProviderMock;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class BeregningsgrunnlagDtoTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private BigDecimal GRUNNBELØP = BigDecimal.valueOf(99_888);
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


    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock();
    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;
    private BeregningsgrunnlagGrunnlagRestDto grunnlag;
    private OpptjeningAktiviteterDto opptjeningAktiviteter;
    private BeregningAktivitetAggregatRestDto beregningAktiviteter;
    private static ArbeidsgiverMedNavn virksomhet = ArbeidsgiverMedNavn.virksomhet(ORGNR);

    @Before
    public void setup() {
        AndelerForFaktaOmBeregningTjeneste andelerForFaktaOmBeregningTjeneste = new AndelerForFaktaOmBeregningTjeneste();
        FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjeneste(
            FaktaOmBeregningTilfelleDtoTjenesteProviderMock.getTjenesteInstances(),
            andelerForFaktaOmBeregningTjeneste
        );
        beregningsgrunnlagDtoTjeneste = new BeregningsgrunnlagDtoTjeneste(
            faktaOmBeregningDtoTjeneste
        );
        virksomhet.setNavn(VIRKSOMHET_NAVN);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_aktivitetStatus_får_korrekte_verdier() {
        // Arrange
        lagBehandlingMedBgOgOpprettFagsakRelasjon(virksomhet);
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());

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
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());

        // Assert
        no.nav.folketrygdloven.kalkulator.rest.dto.SammenligningsgrunnlagDto sammenligningsgrunnlag = beregningsgrunnlagDto.getSammenligningsgrunnlag();
        assertThat(sammenligningsgrunnlag).isNotNull();
        assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(AVVIK_OVER_25_PROSENT);
        assertThat(sammenligningsgrunnlag.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);

        no.nav.folketrygdloven.kalkulator.rest.dto.SammenligningsgrunnlagDto sammenligningsgrunnlagPrStatus = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(0);
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
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjon(arbeidsgiver);

        // Act
        BeregningsgrunnlagDto grunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());

        // Assert
        assertThat(grunnlagDto).isNotNull();
        assertThat(grunnlagDto.getSkjaeringstidspunktBeregning()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(grunnlagDto.getLedetekstAvkortet()).isNotNull();
        assertThat(grunnlagDto.getLedetekstBrutto()).isNotNull();
        assertThat(grunnlagDto.getLedetekstRedusert()).isNotNull();
        assertThat(grunnlagDto.getHalvG().intValue()).isEqualTo(GRUNNBELØP.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP).intValue());
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_får_korrekte_verdier_om_fakta_om_beregning_er_utført_uten_fastsatt_inntekt() {
        // Arrange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        BeregningsgrunnlagRestDto faktaOmBeregningBg = lagBehandlingMedBgOgOpprettFagsakRelasjon(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelRestDto andel = faktaOmBeregningBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BigDecimal beregnetEtterFastsattSteg = BigDecimal.valueOf(10000);
        BeregningsgrunnlagPrStatusOgAndelRestDto.Builder.oppdatere(andel).medBeregnetPrÅr(beregnetEtterFastsattSteg);

        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        builder.medArbeidsgiverOpplysningerDto(List.of(new ArbeidsgiverOpplysningerDto(virksomhet.getIdentifikator(), virksomhet.getNavn(), LocalDate.of(2000, 1, 1))));

        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, builder.build());

        // Assert
        assertBeregningsgrunnlag(beregnetEtterFastsattSteg, beregningsgrunnlagDto);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_beregningsgrunnlagperiode_får_korrekte_verdier() {
        lagBehandlingMedBgOgOpprettFagsakRelasjon(virksomhet);
        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        builder.medArbeidsgiverOpplysningerDto(List.of(new ArbeidsgiverOpplysningerDto(virksomhet.getIdentifikator(), virksomhet.getNavn(), LocalDate.of(2000, 1, 1))));
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, builder.build());

        // Assert
        List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPeriodeDtoList = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode();
        assertThat(beregningsgrunnlagPeriodeDtoList).hasSize(1);

        BeregningsgrunnlagPeriodeDto periodeDto = beregningsgrunnlagPeriodeDtoList.get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelList = periodeDto.getBeregningsgrunnlagPrStatusOgAndel();
        assertThat(andelList).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andelDto = andelList.get(0);
        assertThat(andelDto.getInntektskategori()).isEqualByComparingTo(INNTEKTSKATEGORI);
        assertThat(andelDto.getAndelsnr()).isEqualTo(ANDELSNR);
        assertThat(andelDto.getAvkortetPrAar()).isEqualTo(AVKORTET_PR_AAR);
        assertThat(andelDto.getRedusertPrAar()).isEqualTo(REDUSERT_PR_AAR);
        assertThat(andelDto.getBruttoPrAar()).isEqualTo(OVERSTYRT_PR_AAR);
        assertThat(andelDto.getBeregnetPrAar()).isEqualTo(BRUTTO_PR_AAR);
        assertThat(andelDto.getBeregningsgrunnlagFom()).isEqualTo(ANDEL_FOM);
        assertThat(andelDto.getBeregningsgrunnlagTom()).isEqualTo(ANDEL_TOM);
        assertThat(andelDto.getArbeidsforhold()).isNotNull();
        assertThat(andelDto.getArbeidsforhold().getArbeidsgiverNavn()).isEqualTo(VIRKSOMHET_NAVN);
        assertThat(andelDto.getArbeidsforhold().getArbeidsgiverId()).isEqualTo(ORGNR);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_beregningsgrunnlagperiode_får_korrekte_verdier_ved_arbeidsgiver_privatperson() {
        // Arrange
        ArbeidsgiverMedNavn person = ArbeidsgiverMedNavn.person(AktørId.dummy());
        lagBehandlingMedBgOgOpprettFagsakRelasjon(person);
        InntektArbeidYtelseGrunnlagDtoBuilder builder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        builder.medArbeidsgiverOpplysningerDto(List.of(new ArbeidsgiverOpplysningerDto(person.getIdentifikator(), PRIVATPERSON_NAVN, LocalDate.of(2000, 1, 1))));
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, builder.build());

        // Assert
        List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPeriodeDtoList = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode();
        assertThat(beregningsgrunnlagPeriodeDtoList).hasSize(1);

        BeregningsgrunnlagPeriodeDto periodeDto = beregningsgrunnlagPeriodeDtoList.get(0);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelList = periodeDto.getBeregningsgrunnlagPrStatusOgAndel();
        assertThat(andelList).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andelDto = andelList.get(0);
        assertThat(andelDto.getInntektskategori()).isEqualByComparingTo(INNTEKTSKATEGORI);
        assertThat(andelDto.getAndelsnr()).isEqualTo(ANDELSNR);
        assertThat(andelDto.getAvkortetPrAar()).isEqualTo(AVKORTET_PR_AAR);
        assertThat(andelDto.getRedusertPrAar()).isEqualTo(REDUSERT_PR_AAR);
        assertThat(andelDto.getBruttoPrAar()).isEqualTo(OVERSTYRT_PR_AAR);
        assertThat(andelDto.getBeregnetPrAar()).isEqualTo(BRUTTO_PR_AAR);
        assertThat(andelDto.getBeregningsgrunnlagFom()).isEqualTo(ANDEL_FOM);
        assertThat(andelDto.getBeregningsgrunnlagTom()).isEqualTo(ANDEL_TOM);
        assertThat(andelDto.getArbeidsforhold()).isNotNull();
        assertThat(andelDto.getArbeidsforhold().getArbeidsgiverNavn()).isEqualTo(PRIVATPERSON_NAVN);
        assertThat(andelDto.getArbeidsforhold().getArbeidsgiverId()).isEqualTo("01.01.2000");
    }

    @Test
    public void skalSetteSammenligningsgrunnlagDtoMedDifferanseNårFlereAndeler() {
        // Arrange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjonFlereAndeler(arbeidsgiver);
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());
        // Assert
        no.nav.folketrygdloven.kalkulator.rest.dto.SammenligningsgrunnlagDto sammenligningsgrunnlag = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(0);
        assertThat(sammenligningsgrunnlag).isNotNull();
        assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(AVVIK_OVER_25_PROSENT);
        assertThat(sammenligningsgrunnlag.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagType()).isEqualTo(SammenligningsgrunnlagType.SAMMENLIGNING_AT);
        assertThat(sammenligningsgrunnlag.getDifferanseBeregnet()).isEqualTo(BRUTTO_PR_AAR.subtract(RAPPORTERT_PR_AAR));

        no.nav.folketrygdloven.kalkulator.rest.dto.SammenligningsgrunnlagDto sammenligningsgrunnlag2 = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(1);
        assertThat(sammenligningsgrunnlag2).isNotNull();
        assertThat(sammenligningsgrunnlag2.getAvvikPromille()).isEqualTo(AVVIK_UNDER_25_PROSENT);
        assertThat(sammenligningsgrunnlag2.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlag2.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlag2.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);
        assertThat(sammenligningsgrunnlag2.getSammenligningsgrunnlagType()).isEqualTo(SammenligningsgrunnlagType.SAMMENLIGNING_FL);
        assertThat(sammenligningsgrunnlag2.getDifferanseBeregnet()).isEqualTo(BRUTTO_PR_AAR.subtract(RAPPORTERT_PR_AAR));

        no.nav.folketrygdloven.kalkulator.rest.dto.SammenligningsgrunnlagDto sammenligningsgrunnlag3 = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(2);
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
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjonFlereAndeler(arbeidsgiver);
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());
        // Assert
        BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndelDto = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode().get(0).getBeregningsgrunnlagPrStatusOgAndel().get(0);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto).isNotNull();
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.getSkalFastsetteGrunnlag()).isEqualTo(true);

        BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndelDto2 = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode().get(0).getBeregningsgrunnlagPrStatusOgAndel().get(1);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto2).isNotNull();
        assertThat(beregningsgrunnlagPrStatusOgAndelDto2.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto2.getSkalFastsetteGrunnlag()).isEqualTo(false);

        BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndelDto3 = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode().get(0).getBeregningsgrunnlagPrStatusOgAndel().get(2);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto3).isNotNull();
        assertThat(beregningsgrunnlagPrStatusOgAndelDto3.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto3.getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalSetteBeregningsgrunnlagPrStatusOgAndelDtoForArbeidstakerNårSammenligningsTypeErATFLSN() {
        // Arrange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjon(arbeidsgiver);
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());
        // Assert
        BeregningsgrunnlagPrStatusOgAndelDto beregningsgrunnlagPrStatusOgAndelDto = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode().get(0).getBeregningsgrunnlagPrStatusOgAndel().get(0);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto).isNotNull();
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalBenytteGammeltSammenligningsgrunnlagNårDetIkkeDetFinnesSammenligningsgrunnlagPrStatus() {
        // Arrange
        ArbeidsgiverMedNavn arbeidsgiver = ArbeidsgiverMedNavn.virksomhet(ORGNR);
        lagBehandlingMedBgOgOpprettFagsakRelasjon(arbeidsgiver);
        grunnlag.getBeregningsgrunnlag().get().getSammenligningsgrunnlagPrStatusListe().clear();
        // Act
        BeregningsgrunnlagDto beregningsgrunnlagDto = lagBeregningsgrunnlagDto(lagReferanseMedStp(behandlingReferanse), grunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of()).build());
        // Assert
        assertThat(beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().size()).isOne();
        no.nav.folketrygdloven.kalkulator.rest.dto.SammenligningsgrunnlagDto sammenligningsgrunnlag = beregningsgrunnlagDto.getSammenligningsgrunnlagPrStatus().get(0);
        assertThat(sammenligningsgrunnlag).isNotNull();
        assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(AVVIK_OVER_25_PROSENT);
        assertThat(sammenligningsgrunnlag.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagType()).isEqualTo(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
        assertThat(sammenligningsgrunnlag.getDifferanseBeregnet()).isEqualTo(BRUTTO_PR_AAR.subtract(RAPPORTERT_PR_AAR));
    }

    private BehandlingReferanse lagReferanseMedStp(BehandlingReferanse behandlingReferanse) {
        Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medUtledetSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        return behandlingReferanse.medSkjæringstidspunkt(skjæringstidspunkt);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagDto(BehandlingReferanse ref, BeregningsgrunnlagGrunnlagRestDto grunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(100, false);
        var input = new BeregningsgrunnlagRestInput(ref, iayGrunnlag, AktivitetGradering.INGEN_GRADERING, List.of(), ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        return beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(input);
    }

    private void assertBeregningsgrunnlag(BigDecimal beregnet, BeregningsgrunnlagDto grunnlagDto) {
        assertThat(grunnlagDto).isNotNull();
        assertThat(grunnlagDto.getSkjaeringstidspunktBeregning()).as("skjæringstidspunkt").isEqualTo(SKJÆRINGSTIDSPUNKT);
        assertThat(grunnlagDto.getLedetekstAvkortet()).isNotNull();
        assertThat(grunnlagDto.getLedetekstBrutto()).isNotNull();
        assertThat(grunnlagDto.getLedetekstRedusert()).isNotNull();
        assertThat(grunnlagDto.getHalvG().intValue()).isEqualTo(GRUNNBELØP.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP).intValue());
        BeregningsgrunnlagPeriodeDto periodeDto = grunnlagDto.getBeregningsgrunnlagPeriode().get(0);
        assertThat(periodeDto.getBeregningsgrunnlagPeriodeFom()).as("BeregningsgrunnlagPeriodeFom").isEqualTo(ANDEL_FOM);
        assertThat(periodeDto.getBeregningsgrunnlagPeriodeTom()).as("BeregningsgrunnlagPeriodeTom").isNull();
        BeregningsgrunnlagPrStatusOgAndelDto andelDto = periodeDto.getBeregningsgrunnlagPrStatusOgAndel().get(0);
        assertThat(andelDto.getBeregnetPrAar()).isEqualByComparingTo(beregnet);
        assertThat(andelDto.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(andelDto.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
    }

    private BeregningsgrunnlagRestDto lagBeregningsgrunnlag(ArbeidsgiverMedNavn arbeidsgiver) {
        var beregningsgrunnlag = BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusRestDto.builder()
                .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN)
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM))
            .build();
        BeregningsgrunnlagAktivitetStatusRestDto.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medHjemmel(Hjemmel.F_14_7_8_30)
            .build(beregningsgrunnlag);
        SammenligningsgrunnlagRestDto.builder()
            .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
            .medRapportertPrÅr(RAPPORTERT_PR_AAR)
            .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT).build(beregningsgrunnlag);


        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        buildBgPrStatusOgAndel(bgPeriode, arbeidsgiver);
        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagRestDto lagBeregningsgrunnlagMedFlereAndeler(ArbeidsgiverMedNavn arbeidsgiver) {
        var beregningsgrunnlag = BeregningsgrunnlagRestDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusRestDto.builder()
                .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT)
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM))
            .leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusRestDto.builder()
                .medAvvikPromilleNy(AVVIK_UNDER_25_PROSENT)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_FL)
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM))
            .leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusRestDto.builder()
                .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_SN)
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM))
            .build();
        BeregningsgrunnlagAktivitetStatusRestDto.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medHjemmel(Hjemmel.F_14_7_8_30)
            .build(beregningsgrunnlag);
        SammenligningsgrunnlagRestDto.builder()
            .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
            .medRapportertPrÅr(RAPPORTERT_PR_AAR)
            .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT).build(beregningsgrunnlag);


        BeregningsgrunnlagPeriodeRestDto bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        buildBgPrStatusOgAndelForMangeAndeler(bgPeriode, arbeidsgiver);
        return beregningsgrunnlag;
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode, ArbeidsgiverMedNavn arbeidsgiver) {
        BGAndelArbeidsforholdRestDto.Builder bga = BGAndelArbeidsforholdRestDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(ANDELSNR)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
            .medBeregnetPrÅr(BRUTTO_PR_AAR)
            .medAvkortetPrÅr(AVKORTET_PR_AAR)
            .medRedusertPrÅr(REDUSERT_PR_AAR)
            .medOverstyrtPrÅr(OVERSTYRT_PR_AAR)
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriodeRestDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeRestDto.builder()
            .medBeregningsgrunnlagPeriode(ANDEL_FOM, null)
            .build(beregningsgrunnlag);
    }

    private void buildBgPrStatusOgAndelForMangeAndeler(BeregningsgrunnlagPeriodeRestDto beregningsgrunnlagPeriode, ArbeidsgiverMedNavn arbeidsgiver) {
        BGAndelArbeidsforholdRestDto.Builder bga = BGAndelArbeidsforholdRestDto
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(ANDELSNR)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
            .medBeregnetPrÅr(BRUTTO_PR_AAR)
            .medAvkortetPrÅr(AVKORTET_PR_AAR)
            .medRedusertPrÅr(REDUSERT_PR_AAR)
            .medOverstyrtPrÅr(OVERSTYRT_PR_AAR)
            .build(beregningsgrunnlagPeriode);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(ANDELSNR+1)
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
            .medBeregnetPrÅr(BRUTTO_PR_AAR)
            .build(beregningsgrunnlagPeriode);

        BeregningsgrunnlagPrStatusOgAndelRestDto.kopier()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(ANDELSNR+3)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medPgi(PGI_SNITT, List.of())
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagRestDto lagBehandlingMedBgOgOpprettFagsakRelasjon(ArbeidsgiverMedNavn arbeidsgiver) {

        this.beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlag = lagBeregningsgrunnlag(arbeidsgiver);

        this.grunnlag = BeregningsgrunnlagGrunnlagRestDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPRETTET);

        this.opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID, Periode.of( SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.minusDays(1)), arbeidsgiver.getOrgnr());

        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagRestDto lagBehandlingMedBgOgOpprettFagsakRelasjonFlereAndeler(ArbeidsgiverMedNavn arbeidsgiver) {
        this.beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        var beregningsgrunnlag = lagBeregningsgrunnlagMedFlereAndeler(arbeidsgiver);

        this.grunnlag = BeregningsgrunnlagGrunnlagRestDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktiviteter)
            .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPRETTET);

        this.opptjeningAktiviteter = OpptjeningAktiviteterDto.fraOrgnr(OpptjeningAktivitetType.ARBEID, Periode.of( SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.minusDays(1)), arbeidsgiver.getOrgnr());

        return beregningsgrunnlag;
    }

    private BeregningAktivitetAggregatRestDto lagBeregningAktiviteter(ArbeidsgiverMedNavn arbeidsgiver) {
        return lagBeregningAktiviteter(BeregningAktivitetAggregatRestDto.builder(), arbeidsgiver);
    }

    private BeregningAktivitetAggregatRestDto lagBeregningAktiviteter(BeregningAktivitetAggregatRestDto.Builder builder, ArbeidsgiverMedNavn arbeidsgiver) {
        return builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
            .leggTilAktivitet(BeregningAktivitetRestDto.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .medPeriode(Intervall.fraOgMedTilOgMed(ANDEL_FOM, ANDEL_TOM))
                .build())
            .build();
    }
}
