package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_BEGYNNELSE;
import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Årsgrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FrisinnBehandlingType;

class MapInntektstakFRISINNTest {
    private static final BigDecimal G = BigDecimal.valueOf(99858);
    private static final BigDecimal SEKS_G = G.multiply(KonfigTjeneste.getAntallGØvreGrenseverdi());
    private static BeregningsgrunnlagEntitet beregningsgrunnlagEntitet;
    private static BeregningsgrunnlagPeriode.Builder periode;
    private static OppgittOpptjeningDtoBuilder opptjening;

    @BeforeEach
    public void setup() {
        beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        periode = lagPeriode(LocalDate.of(2020, 3, 1), TIDENES_ENDE);
        opptjening = OppgittOpptjeningDtoBuilder.ny();
    }

    @Test
    public void skal_teste_at_sn_eneste_status_under_6g_får_korrekt_tak() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(300000);
        BeregningsgrunnlagPrStatusOgAndel andel = snAndel(1L, bruttoSN, periode.build(beregningsgrunnlagEntitet));
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagNæringOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(false, true),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(bruttoSN);
    }

    @Test
    public void skal_teste_at_fl_eneste_status_under_6g_får_korrekt_tak() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoFL = BigDecimal.valueOf(300000);
        BeregningsgrunnlagPrStatusOgAndel andel = flAndel(1L, bruttoFL, periode.build(beregningsgrunnlagEntitet));
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(true, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(bruttoFL);
    }

    @Test
    public void at_fl_ingen_redusering() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoFL = BigDecimal.valueOf(300000);
        BigDecimal bruttoAT = BigDecimal.valueOf(200000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        atAndel(2L, bruttoAT, periode);
        BeregningsgrunnlagPrStatusOgAndel andel = flAndel(1L, bruttoFL, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(true, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(bruttoFL);
    }

    @Test
    public void at_fl_med_redusering() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoFL = BigDecimal.valueOf(300000);
        BigDecimal bruttoAT = BigDecimal.valueOf(500000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        atAndel(2L, bruttoAT, periode);
        BeregningsgrunnlagPrStatusOgAndel andel = flAndel(1L, bruttoFL, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(true, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(SEKS_G.subtract(bruttoAT));
    }

    @Test
    public void at_sn_med_redusering() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(300000);
        BigDecimal bruttoAT = BigDecimal.valueOf(500000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        atAndel(2L, bruttoAT, periode);
        BeregningsgrunnlagPrStatusOgAndel andel = snAndel(1L, bruttoSN, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagNæringOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(false, true),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(SEKS_G.subtract(bruttoAT));
    }

    @Test
    public void fl_sn_søker_kun_fl() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(300000);
        BigDecimal bruttoFL = BigDecimal.valueOf(500000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        snAndel(2L, bruttoSN, periode);
        BeregningsgrunnlagPrStatusOgAndel andel = flAndel(1L, bruttoFL, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(true, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(SEKS_G.subtract(bruttoSN));
    }

    @Test
    public void fl_sn_søker_kun_sn_avkortes() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(300000);
        BigDecimal bruttoFL = BigDecimal.valueOf(500000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        flAndel(2L, bruttoFL, periode);
        BeregningsgrunnlagPrStatusOgAndel andel = snAndel(1L, bruttoSN, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagNæringOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(false, true),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(SEKS_G.subtract(bruttoFL));
    }

    @Test
    public void at_fl_sn_søker_kun_sn_avkortes() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(200000);
        BigDecimal bruttoAT = BigDecimal.valueOf(200000);
        BigDecimal bruttoFL = BigDecimal.valueOf(200000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        flAndel(2L, bruttoFL, periode);
        atAndel(3L, bruttoAT, periode);
        BeregningsgrunnlagPrStatusOgAndel andel = snAndel(1L, bruttoSN, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagNæringOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(false, true),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(SEKS_G.subtract(bruttoFL).subtract(bruttoAT));
    }

    @Test
    public void at_fl_sn_søker_kun_sn_avkortes_ikke() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(200000);
        BigDecimal bruttoAT = BigDecimal.valueOf(100000);
        BigDecimal bruttoFL = BigDecimal.valueOf(200000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        flAndel(2L, bruttoFL, periode);
        atAndel(3L, bruttoAT, periode);
        BeregningsgrunnlagPrStatusOgAndel andel = snAndel(1L, bruttoSN, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagNæringOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(false, true),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(bruttoSN);
    }

    @Test
    public void at_fl_sn_søker_kun_fl_avkortes_ikke() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(200000);
        BigDecimal bruttoAT = BigDecimal.valueOf(100000);
        BigDecimal bruttoFL = BigDecimal.valueOf(150000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        snAndel(2L, bruttoSN, periode);
        atAndel(3L, bruttoAT, periode);
        BeregningsgrunnlagPrStatusOgAndel andel = flAndel(1L, bruttoFL, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(true, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(bruttoFL);
    }

    @Test
    public void skal_sette_tak_for_at_til_0() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(200000);
        BigDecimal bruttoAT = BigDecimal.valueOf(100000);
        BigDecimal bruttoFL = BigDecimal.valueOf(150000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        snAndel(2L, bruttoSN, periode);
        flAndel(3L, bruttoAT, periode);
        BeregningsgrunnlagPrStatusOgAndel andel = atAndel(1L, bruttoFL, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(true, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_sette_andel_fl_ikke_søkt_for_til_0() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoFL = BigDecimal.valueOf(150000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        BeregningsgrunnlagPrStatusOgAndel andel = flAndel(1L, bruttoFL, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(false, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_sette_andel_sn_ikke_søkt_for_til_0() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(150000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        BeregningsgrunnlagPrStatusOgAndel andel = snAndel(1L, bruttoSN, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagNæringOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(false, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_gi_0_ved_ingen_andeler_i_perioden() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        periode.build(beregningsgrunnlagEntitet);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagNæringOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, AktivitetStatus.FRILANSER, frisinn(false, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_gi_0_når_andel_ikke_finnes() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BeregningsgrunnlagPeriode p = periode.build(beregningsgrunnlagEntitet);
        flAndel(1L, BigDecimal.valueOf(300000), p);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, frisinn(false, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void at_fl_søker_begge_ingen_avkorting() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoSN = BigDecimal.valueOf(200000);
        BigDecimal bruttoFL = BigDecimal.valueOf(100000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        snAndel(2L, bruttoSN, periode);
        flAndel(1L, bruttoFL, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);
        lagNæringOpptjening(april);

        // Act
        BigDecimal inntektstakFL = MapInntektstakFRISINN.map(andeler, AktivitetStatus.FRILANSER, frisinn(true, true),
                Optional.of(opptjening.build()), G);
        BigDecimal inntektstakSN = MapInntektstakFRISINN.map(andeler, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, frisinn(true, true),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstakFL).isEqualByComparingTo(bruttoFL);
        assertThat(inntektstakSN).isEqualByComparingTo(bruttoSN);
    }

    @Test
    public void at_fl_sn_søker_begge_avkortes_grunnet_at() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoAT = BigDecimal.valueOf(400000);
        BigDecimal bruttoSN = BigDecimal.valueOf(150000);
        BigDecimal bruttoFL = BigDecimal.valueOf(100000);
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        snAndel(2L, bruttoSN, periode);
        atAndel(3L, bruttoAT, periode);
        flAndel(1L, bruttoFL, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);
        lagNæringOpptjening(april);

        // Act
        BigDecimal inntektstakFL = MapInntektstakFRISINN.map(andeler, AktivitetStatus.FRILANSER, frisinn(true, true),
                Optional.of(opptjening.build()), G);
        BigDecimal inntektstakSN = MapInntektstakFRISINN.map(andeler, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, frisinn(true, true),
                Optional.of(opptjening.build()), G);
        BigDecimal inntektstakAT = MapInntektstakFRISINN.map(andeler, AktivitetStatus.ARBEIDSTAKER, frisinn(true, true),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstakFL).isEqualByComparingTo(bruttoFL);
        assertThat(inntektstakSN).isEqualByComparingTo(SEKS_G.subtract(bruttoAT).subtract(bruttoFL));
        assertThat(inntektstakAT).isEqualByComparingTo(BigDecimal.ZERO);

    }


    @Test
    public void søkt_fl_utenfor_bg_periode() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        BigDecimal bruttoFL = BigDecimal.valueOf(300000);
        periode.medBeregningsgrunnlagPeriode(LocalDate.of(2020, 3, 1), LocalDate.of(2020, 3, 31));
        BeregningsgrunnlagPeriode periode = MapInntektstakFRISINNTest.periode.build(beregningsgrunnlagEntitet);
        BeregningsgrunnlagPrStatusOgAndel andel = flAndel(1L, bruttoFL, periode);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = hentAndeler();
        lagFrilansOpptjening(april);

        // Act
        BigDecimal inntektstak = MapInntektstakFRISINN.map(andeler, andel.getAktivitetStatus(), frisinn(true, false),
                Optional.of(opptjening.build()), G);

        // Assert
        assertThat(inntektstak).isEqualByComparingTo(BigDecimal.ZERO);
    }


    private List<BeregningsgrunnlagPrStatusOgAndel> hentAndeler() {
        return beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
    }

    private void lagNæringOpptjening(Intervall periode) {
        OppgittOpptjeningDtoBuilder.EgenNæringBuilder en = OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny().medPeriode(periode)
                .medBruttoInntekt(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp.ZERO);
        opptjening.leggTilEgneNæring(en);
    }

    private void lagFrilansOpptjening(Intervall periode) {
        OppgittFrilansInntektDto inntekt = new OppgittFrilansInntektDto(periode, no.nav.folketrygdloven.kalkulator.modell.typer.Beløp.ZERO);
        OppgittFrilansDto oppgittFL = new OppgittFrilansDto(false, Collections.singletonList(inntekt));
        opptjening.leggTilFrilansOpplysninger(oppgittFL);
    }

    private FrisinnGrunnlag frisinn(boolean søkerFL, boolean søkerSN) {
        Intervall periode = Intervall.fraOgMedTilOgMed(TIDENES_BEGYNNELSE, TIDENES_ENDE);
        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(periode, søkerFL, søkerSN);
        return new FrisinnGrunnlag(Collections.emptyList(), Collections.singletonList(frisinnPeriode), FrisinnBehandlingType.NY_SØKNADSPERIODE);
    }

    private BeregningsgrunnlagPrStatusOgAndel snAndel(long andelsnr, BigDecimal brutto, BeregningsgrunnlagPeriode periode) {
        return byggAndel(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, periode, andelsnr, brutto);
    }

    private BeregningsgrunnlagPrStatusOgAndel flAndel(long andelsnr, BigDecimal brutto, BeregningsgrunnlagPeriode periode) {
        return byggAndel(AktivitetStatus.FRILANSER, periode, andelsnr, brutto);
    }

    private BeregningsgrunnlagPrStatusOgAndel atAndel(long andelsnr, BigDecimal brutto, BeregningsgrunnlagPeriode periode) {
        return byggAndel(AktivitetStatus.ARBEIDSTAKER, periode, andelsnr, brutto);
    }

    private BeregningsgrunnlagPrStatusOgAndel byggAndel(AktivitetStatus status, BeregningsgrunnlagPeriode periode, Long andelsnr, BigDecimal brutto) {
        BeregningsgrunnlagPrStatusOgAndel.Builder builder = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAndelsnr(andelsnr)
                .medAktivitetStatus(status)
                .medFastsattAvSaksbehandler(true)
                .medGrunnlagPrÅr(new Årsgrunnlag(new Beløp(brutto), null, null, null, null, new Beløp(brutto)))
                .medBeregningsperiode(LocalDate.now().minusMonths(3), LocalDate.now());
        if (status.erArbeidstaker()) {
            builder.medBGAndelArbeidsforhold(lagArbfor());
        }
        return builder.build(periode);
    }

    private BGAndelArbeidsforhold.Builder lagArbfor() {
        return BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("999999999"));
    }

    private BeregningsgrunnlagPeriode.Builder lagPeriode(LocalDate start, LocalDate slutt) {
        return BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(start, slutt);
    }

}
