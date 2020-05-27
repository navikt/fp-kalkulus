package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BehandlingReferanseMock;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RedigerbarAndelDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.vedtak.util.Tuple;

public class FordelRefusjonTjenesteTest {

    private static final String ARBEIDSGIVER_ORGNR = "910909088";
    private static final int FORRIGE_ARBEIDSTINNTEKT = 100_000;
    private static final Inntektskategori FORRIGE_INNTEKTSKATEGORI = Inntektskategori.ARBEIDSTAKER;
    private final LocalDate FOM = LocalDate.now();
    private final LocalDate TOM = FOM.plusMonths(10);
    private final String ARB_ID1 = InternArbeidsforholdRefDto.nyRef().getReferanse();
    private final long ANDELSNR1 = 1L;
    private final long ANDELSNR2 = 2L;
    private final Integer FASTSATT = 10000;
    private final Integer REFUSJON = 10000;
    private final Integer REFUSJONPRÅR = REFUSJON *12;

    private final RedigerbarAndelDto ANDEL_UTEN_ARBEID_LAGT_TIL_OPPRETTET_INFO = new RedigerbarAndelDto(false, ANDELSNR1, true, AktivitetStatus.DAGPENGER, OpptjeningAktivitetType.DAGPENGER);
    private final RedigerbarAndelDto ANDEL_FRA_OPPRETTET_INFO = new RedigerbarAndelDto(false, ARBEIDSGIVER_ORGNR, ARB_ID1, ANDELSNR1, false, AktivitetStatus.ARBEIDSTAKER, OpptjeningAktivitetType.ARBEID);
    private final RedigerbarAndelDto ANDEL_FRA_OPPRETTET_UTEN_ARBEID_INFO = new RedigerbarAndelDto(false, ANDELSNR1, false, AktivitetStatus.ARBEIDSTAKER, OpptjeningAktivitetType.ARBEID);
    private final RedigerbarAndelDto ANDEL_LAGT_TIL_FORRIGE_INFO = new RedigerbarAndelDto(false, ARBEIDSGIVER_ORGNR, ARB_ID1, ANDELSNR2, true, AktivitetStatus.ARBEIDSTAKER, OpptjeningAktivitetType.ARBEID);
    private final RedigerbarAndelDto ANDEL_NY_INFO = new RedigerbarAndelDto(true, ARBEIDSGIVER_ORGNR, ARB_ID1, ANDELSNR1, true, AktivitetStatus.ARBEIDSTAKER, OpptjeningAktivitetType.ARBEID);

    private final FastsatteVerdierDto REFUSJON_NULL_FASTSATT_STØRRE_ENN_0 = FastsatteVerdierDto.Builder.ny().medFastsattBeløpPrMnd(FASTSATT).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build();
    private final FastsatteVerdierDto REFUSJON_STØRRE_ENN_0_FASTSATT_STØRRE_ENN_0 = FastsatteVerdierDto.Builder.ny().medRefusjonPrÅr(REFUSJONPRÅR).medFastsattBeløpPrMnd(FASTSATT).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build();
    private final FastsatteVerdierDto REFUSJON_STØRRE_ENN_0_FASTSATT_HALVPARTEN = FastsatteVerdierDto.Builder.ny().medRefusjonPrÅr(REFUSJONPRÅR).medFastsattBeløpPrMnd(FASTSATT/2).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build();
    private final FastsatteVerdierDto REFUSJON_NULL_FASTSATT_HALVPARTEN = FastsatteVerdierDto.Builder.ny().medFastsattBeløpPrMnd(FASTSATT/2).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build();
    private final FastsatteVerdierDto REFUSJON_STØRRE_ENN_0_FASTSATT_LIK_0 = FastsatteVerdierDto.Builder.ny().medRefusjonPrÅr(REFUSJONPRÅR).medFastsattBeløpPrMnd(0).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build();
    private final FastsatteVerdierDto REFUSJON_LIK_0_FASTSATT_LIK_0 = FastsatteVerdierDto.Builder.ny().medRefusjonPrÅr(0).medFastsattBeløpPrMnd(0).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build();
    private final FastsatteVerdierDto REFUSJON_LIK_0_FASTSATT_STØRRE_ENN_0 = FastsatteVerdierDto.Builder.ny().medRefusjonPrÅr(0).medFastsattBeløpPrMnd(FASTSATT).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build();
    private final FastsatteVerdierDto REFUSJON_LIK_NULL_FASTSATT_LIK_0 = FastsatteVerdierDto.Builder.ny().medFastsattBeløpPrMnd(0).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build();

    private BGAndelArbeidsforholdDto.Builder afBuilder1 = BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR)).medArbeidsforholdRef(ARB_ID1).medRefusjonskravPrÅr(BigDecimal.ZERO);

    private BeregningsgrunnlagPeriodeDto periode;
    private FastsettBeregningsgrunnlagPeriodeDto endretPeriode;
    private List<FastsettBeregningsgrunnlagAndelDto> andelListe = new ArrayList<>();
    private BeregningsgrunnlagDto oppdatertBg;
    private BehandlingReferanse behandlingReferanse = new BehandlingReferanseMock(FOM);
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setUp() {
        oppdatertBg = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(BigDecimal.TEN)
            .medSkjæringstidspunkt(FOM).build();
        periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(FOM, TOM)
            .build(oppdatertBg);
        endretPeriode = new FastsettBeregningsgrunnlagPeriodeDto(andelListe, FOM, TOM);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, oppdatertBg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    @Test
    public void skal_filtrere_ut_andel_uten_arbeidsforhold() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_UTEN_ARBEID_LAGT_TIL_OPPRETTET_INFO, REFUSJON_NULL_FASTSATT_STØRRE_ENN_0,
            Inntektskategori.DAGPENGER, null, 123_723);
        andelListe.add(fordeltAndel);
        lagDPAndelLagtTilAvSaksbehandler();

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel)).isNull();
    }


    @Test
    public void skal_sette_refusjon_lik_0_for_arbeidsforhold_uten_refusjonskrav() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_LIK_0_FASTSATT_STØRRE_ENN_0, FORRIGE_INNTEKTSKATEGORI, 0, FORRIGE_ARBEIDSTINNTEKT);
        andelListe.add(fordeltAndel);
        lagArbeidstakerAndel();

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(0);
    }

    @Test
    public void skal_ikkje_fordele_refusjon_for_andeler_uten_arbeidsforhold() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_UTEN_ARBEID_INFO, REFUSJON_NULL_FASTSATT_STØRRE_ENN_0,
            Inntektskategori.ARBEIDSTAKER, 0, FORRIGE_ARBEIDSTINNTEKT);
        andelListe.add(fordeltAndel);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(FORRIGE_ARBEIDSTINNTEKT))
            .medAndelsnr(1L)
            .build(periode);

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel)).isNull();
    }

    @Test
    public void skal_endre_refusjon_for_en_andel_med_refusjon_fastsatt_større_enn_0() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_STØRRE_ENN_0, FORRIGE_INNTEKTSKATEGORI,
            REFUSJONPRÅR - 12, FORRIGE_ARBEIDSTINNTEKT);
        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf(REFUSJONPRÅR - 12));
        andelListe.add(fordeltAndel);
        lagArbeidstakerAndel();

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(REFUSJONPRÅR);
    }

    @Test
    public void skal_endre_refusjon_for_en_andel_med_refusjon_fastsatt_lik_0() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_LIK_0,
            Inntektskategori.ARBEIDSTAKER, REFUSJONPRÅR - 12, FORRIGE_ARBEIDSTINNTEKT);
        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf(REFUSJONPRÅR - 12));
        andelListe.add(fordeltAndel);
        lagArbeidstakerAndel();

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(REFUSJONPRÅR);
    }

    @Test
    public void skal_ikkje_endre_refusjon_for_en_andel_uten_refusjon_fastsatt_lik_0() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_LIK_NULL_FASTSATT_LIK_0, FORRIGE_INNTEKTSKATEGORI, REFUSJONPRÅR - 12, FORRIGE_ARBEIDSTINNTEKT);
        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf(REFUSJONPRÅR - 12));
        andelListe.add(fordeltAndel);
        lagArbeidstakerAndel();

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel)).isNull();
    }

    @Test
    public void skal_sette_refusjon_lik_0_en_andel_med_refusjon_og_fastsatt_lik_0() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_LIK_0_FASTSATT_LIK_0, FORRIGE_INNTEKTSKATEGORI, REFUSJONPRÅR - 12, FORRIGE_ARBEIDSTINNTEKT);
        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf(REFUSJONPRÅR - 12));
        andelListe.add(fordeltAndel);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(afBuilder1)
            .build(periode);

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);


        // Assert
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(0);
    }

    @Test
    public void skal_fordele_refusjon_for_2_andeler_en_fra_opprettet_med_og_en_lagt_til_forrige_med_lik_refusjon_og_fordeling_større_enn_0() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_STØRRE_ENN_0, FORRIGE_INNTEKTSKATEGORI, REFUSJONPRÅR - 12, FORRIGE_ARBEIDSTINNTEKT);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel2 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_LAGT_TIL_FORRIGE_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_STØRRE_ENN_0, null, null, null);
        andelListe.add(fordeltAndel);
        andelListe.add(fordeltAndel2);

        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf((REFUSJONPRÅR - 12)));
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregnetPrÅr(BigDecimal.valueOf(FORRIGE_ARBEIDSTINNTEKT))
            .medAndelsnr(1L)
            .medBGAndelArbeidsforhold(afBuilder1)
            .build(periode);

        BGAndelArbeidsforholdDto.Builder afBuilder2 = lagArbeidsforholdMedRefusjonskrav(BigDecimal.valueOf((REFUSJONPRÅR - 12)));
        BeregningsgrunnlagDto forrigeBg = BeregningsgrunnlagDto.builder(oppdatertBg).build();
        BeregningsgrunnlagPeriodeDto periodeForrige = forrigeBg.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(afBuilder2)
            .medAndelsnr(2L)
            .build(periodeForrige);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, new Tuple<>(oppdatertBg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER), new Tuple<>(forrigeBg, BeregningsgrunnlagTilstand.FASTSATT_INN));

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(REFUSJONPRÅR);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel2).intValue()).isEqualTo(REFUSJONPRÅR);
    }

    @Test
    public void skal_fordele_refusjon_for_2_andeler_en_fra_opprettet_med_og_en_lagt_til_forrige_med_lik_refusjon_og_ulik_fordeling_større_enn_0() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_STØRRE_ENN_0, FORRIGE_INNTEKTSKATEGORI, 54654, FORRIGE_ARBEIDSTINNTEKT);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel2 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_LAGT_TIL_FORRIGE_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_HALVPARTEN, null, null, null);
        andelListe.add(fordeltAndel);
        andelListe.add(fordeltAndel2);

        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf((54654)));
        lagArbeidstakerAndel();

        BGAndelArbeidsforholdDto.Builder afBuilder2 = lagArbeidsforholdMedRefusjonskrav(BigDecimal.valueOf((5465)));
        BeregningsgrunnlagDto forrigeBg = BeregningsgrunnlagDto.builder(oppdatertBg).build();
        BeregningsgrunnlagPeriodeDto periodeForrige = forrigeBg.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(afBuilder2)
            .medAndelsnr(2L)
            .build(periodeForrige);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, new Tuple<>(oppdatertBg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER),
            new Tuple<>(forrigeBg, BeregningsgrunnlagTilstand.FASTSATT_INN));

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        double totalRefusjon = 2* REFUSJONPRÅR;
        int forventetRefusjon1 = (int) (2*totalRefusjon/3);
        int forventetRefusjon2 = (int) (totalRefusjon/3);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel2).intValue()).isEqualTo(forventetRefusjon2);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(forventetRefusjon1);
    }

    @Test
    public void skal_fordele_refusjon_for_2_andeler_en_fra_opprettet_med_og_en_lagt_til_forrige_med_refusjon_lik_null_og_ulik_fordeling() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_NULL_FASTSATT_STØRRE_ENN_0, FORRIGE_INNTEKTSKATEGORI, 2* REFUSJONPRÅR, FORRIGE_ARBEIDSTINNTEKT);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel2 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_LAGT_TIL_FORRIGE_INFO, REFUSJON_NULL_FASTSATT_HALVPARTEN, null, null, null);
        andelListe.add(fordeltAndel);
        andelListe.add(fordeltAndel2);

        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf((2* REFUSJONPRÅR)));
        lagArbeidstakerAndel();

        BGAndelArbeidsforholdDto.Builder afBuilder2 = lagArbeidsforholdMedRefusjonskrav();
        BGAndelArbeidsforholdDto.Builder afBuilder3 = lagArbeidsforholdMedRefusjonskrav();
        BeregningsgrunnlagDto forrigeBg = BeregningsgrunnlagDto.builder(oppdatertBg).build();
        BeregningsgrunnlagPeriodeDto periodeForrige = forrigeBg.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier(periodeForrige.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medBGAndelArbeidsforhold(afBuilder3);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(afBuilder2)
            .medAndelsnr(2L)
            .build(periodeForrige);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, new Tuple<>(oppdatertBg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER), new Tuple<>(forrigeBg, BeregningsgrunnlagTilstand.FASTSATT_INN));

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        double totalRefusjon = 2* REFUSJONPRÅR;
        int forventetRefusjon1 = (int) (2*totalRefusjon/3);
        int forventetRefusjon2 = (int) (totalRefusjon/3);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel2).intValue()).isEqualTo(forventetRefusjon2);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(forventetRefusjon1);
    }

    @Test
    public void skal_fordele_refusjon_for_3_andeler_en_fra_opprettet_med_og_en_lagt_til_forrige_en_ny_andel_lik_fordeling_ulik_0() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_STØRRE_ENN_0, FORRIGE_INNTEKTSKATEGORI, 54654, FORRIGE_ARBEIDSTINNTEKT);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel2 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_LAGT_TIL_FORRIGE_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_STØRRE_ENN_0, null, null, null);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel3 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_NY_INFO, REFUSJON_NULL_FASTSATT_STØRRE_ENN_0, null, null, null);
        andelListe.add(fordeltAndel);
        andelListe.add(fordeltAndel2);
        andelListe.add(fordeltAndel3);

        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf((54654)));
        lagArbeidstakerAndel();

        BGAndelArbeidsforholdDto.Builder afBuilder2 = lagArbeidsforholdMedRefusjonskrav(BigDecimal.valueOf((5465)));
        BeregningsgrunnlagDto forrigeBg = BeregningsgrunnlagDto.builder(oppdatertBg).build();
        BeregningsgrunnlagPeriodeDto periodeForrige = forrigeBg.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(afBuilder2)
            .medAndelsnr(2L)
            .build(periodeForrige);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, new Tuple<>(oppdatertBg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER), new Tuple<>(forrigeBg, BeregningsgrunnlagTilstand.FASTSATT_INN));

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        double totalRefusjon = 2* REFUSJONPRÅR;
        int forventetRefusjon = (int) (totalRefusjon/3);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(forventetRefusjon);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel2).intValue()).isEqualTo(forventetRefusjon);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel3).intValue()).isEqualTo(forventetRefusjon);
    }

    @Test
    public void skal_fordele_refusjon_for_3_andeler_en_fra_opprettet_med_og_en_lagt_til_forrige_en_ny_andel_ulik_fordeling() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_STØRRE_ENN_0, FORRIGE_INNTEKTSKATEGORI, 54654, FORRIGE_ARBEIDSTINNTEKT);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel2 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_LAGT_TIL_FORRIGE_INFO, REFUSJON_STØRRE_ENN_0_FASTSATT_HALVPARTEN,null, null, null);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel3 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_NY_INFO, REFUSJON_NULL_FASTSATT_HALVPARTEN, null, null, null);
        andelListe.add(fordeltAndel);
        andelListe.add(fordeltAndel2);
        andelListe.add(fordeltAndel3);

        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf((54654)));
        lagArbeidstakerAndel();

        BGAndelArbeidsforholdDto.Builder afBuilder2 = lagArbeidsforholdMedRefusjonskrav(BigDecimal.valueOf((5465)));
        BeregningsgrunnlagDto forrigeBg = BeregningsgrunnlagDto.builder(oppdatertBg).build();
        BeregningsgrunnlagPeriodeDto periodeForrige = forrigeBg.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(afBuilder2)
            .medAndelsnr(2L)
            .build(periodeForrige);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, new Tuple<>(oppdatertBg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER), new Tuple<>(forrigeBg, BeregningsgrunnlagTilstand.FASTSATT_INN));

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        double totalRefusjon = 2* REFUSJONPRÅR;
        int forventetRefusjon1 = (int) (2*totalRefusjon/4);
        int forventetRefusjon2 = (int) (totalRefusjon/4);
        int forventetRefusjon3 = (int) (totalRefusjon/4);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(forventetRefusjon1);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel2).intValue()).isEqualTo(forventetRefusjon2);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel3).intValue()).isEqualTo(forventetRefusjon3);
    }

    @Test
    public void skal_fordele_refusjon_for_3_andeler_en_fra_opprettet_med_og_en_lagt_til_forrige_en_ny_andel_ulik_fordeling_refusjon_lik_null() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_NULL_FASTSATT_STØRRE_ENN_0, FORRIGE_INNTEKTSKATEGORI, 2* REFUSJONPRÅR, FORRIGE_ARBEIDSTINNTEKT);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel2 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_LAGT_TIL_FORRIGE_INFO, REFUSJON_NULL_FASTSATT_HALVPARTEN, null, null, null);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel3 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_NY_INFO, REFUSJON_NULL_FASTSATT_HALVPARTEN, null, null, null);
        andelListe.add(fordeltAndel);
        andelListe.add(fordeltAndel2);
        andelListe.add(fordeltAndel3);

        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf((2* REFUSJONPRÅR)));
        lagArbeidstakerAndel();

        BGAndelArbeidsforholdDto.Builder afBuilder2 = lagArbeidsforholdMedRefusjonskrav();
        BGAndelArbeidsforholdDto.Builder afBuilder3 = lagArbeidsforholdMedRefusjonskrav();
        BeregningsgrunnlagDto forrigeBg = BeregningsgrunnlagDto.builder(oppdatertBg).build();
        BeregningsgrunnlagPeriodeDto periodeForrige = forrigeBg.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier(periodeForrige.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medBGAndelArbeidsforhold(afBuilder3);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(afBuilder2)
            .medAndelsnr(2L)
            .build(periodeForrige);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, new Tuple<>(oppdatertBg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER), new Tuple<>(forrigeBg, BeregningsgrunnlagTilstand.FASTSATT_INN));

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        double totalRefusjon = 2* REFUSJONPRÅR;
        int forventetRefusjon1 = (int) (2*totalRefusjon/4);
        int forventetRefusjon2 = (int) (totalRefusjon/4);
        int forventetRefusjon3 = (int) (totalRefusjon/4);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(forventetRefusjon1);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel2).intValue()).isEqualTo(forventetRefusjon2);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel3).intValue()).isEqualTo(forventetRefusjon3);
    }

    @Test
    public void skal_fordele_refusjon_for_3_andeler_en_fra_opprettet_med_og_en_lagt_til_forrige_en_ny_andel_lik_fordeling_refusjon_lik_null() {
        // Arrange
        FastsettBeregningsgrunnlagAndelDto fordeltAndel = new FastsettBeregningsgrunnlagAndelDto(ANDEL_FRA_OPPRETTET_INFO, REFUSJON_NULL_FASTSATT_STØRRE_ENN_0, FORRIGE_INNTEKTSKATEGORI,  2* REFUSJONPRÅR, FORRIGE_ARBEIDSTINNTEKT);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel2 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_LAGT_TIL_FORRIGE_INFO, REFUSJON_NULL_FASTSATT_STØRRE_ENN_0, null, null, null);
        FastsettBeregningsgrunnlagAndelDto fordeltAndel3 = new FastsettBeregningsgrunnlagAndelDto(ANDEL_NY_INFO, REFUSJON_NULL_FASTSATT_STØRRE_ENN_0, null, null, null);
        andelListe.add(fordeltAndel);
        andelListe.add(fordeltAndel2);
        andelListe.add(fordeltAndel3);

        afBuilder1.medRefusjonskravPrÅr(BigDecimal.valueOf((2* REFUSJONPRÅR)));
        lagArbeidstakerAndel();

        BGAndelArbeidsforholdDto.Builder afBuilder2 = lagArbeidsforholdMedRefusjonskrav();
        BGAndelArbeidsforholdDto.Builder afBuilder3 = lagArbeidsforholdMedRefusjonskrav();
        BeregningsgrunnlagDto forrigeBg = BeregningsgrunnlagDto.builder(oppdatertBg).build();
        BeregningsgrunnlagPeriodeDto periodeForrige = forrigeBg.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto.kopier(periodeForrige.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medBGAndelArbeidsforhold(afBuilder3);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(afBuilder2)
            .medAndelsnr(2L)
            .build(periodeForrige);

        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(behandlingReferanse, new Tuple<>(oppdatertBg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER), new Tuple<>(forrigeBg, BeregningsgrunnlagTilstand.FASTSATT_INN));

        // Act
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> map = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, periode);

        // Assert
        double totalRefusjon = 2* REFUSJONPRÅR;
        int forventetRefusjon = (int) (totalRefusjon/3);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel).intValue()).isEqualTo(forventetRefusjon);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel2).intValue()).isEqualTo(forventetRefusjon);
        AssertionsForClassTypes.assertThat(map.get(fordeltAndel3).intValue()).isEqualTo(forventetRefusjon);
    }

    private BGAndelArbeidsforholdDto.Builder lagArbeidsforholdMedRefusjonskrav() {
        return BGAndelArbeidsforholdDto.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR))
            .medArbeidsforholdRef(ARB_ID1)
            .medRefusjonskravPrÅr(BigDecimal.valueOf((REFUSJONPRÅR)));
    }

    private BGAndelArbeidsforholdDto.Builder lagArbeidsforholdMedRefusjonskrav(BigDecimal refusjonskrav) {
        return BGAndelArbeidsforholdDto.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR))
            .medArbeidsforholdRef(ARB_ID1)
            .medRefusjonskravPrÅr(refusjonskrav);
    }

    private void lagArbeidstakerAndel() {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(FORRIGE_INNTEKTSKATEGORI)
            .medBGAndelArbeidsforhold(afBuilder1)
            .medBeregnetPrÅr(BigDecimal.valueOf(FORRIGE_ARBEIDSTINNTEKT))
            .medAndelsnr(1L)
            .build(periode);
    }

    private void lagDPAndelLagtTilAvSaksbehandler() {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER)
            .medInntektskategori(Inntektskategori.DAGPENGER)
            .medAndelsnr(1L)
            .medLagtTilAvSaksbehandler(true)
            .build(periode);
    }
}
