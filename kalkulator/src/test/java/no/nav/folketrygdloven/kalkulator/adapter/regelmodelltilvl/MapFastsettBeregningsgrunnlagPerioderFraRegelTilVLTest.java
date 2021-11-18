package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpMock;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, 1, 1);
    private static final BigDecimal RAPPORTERT_PR_ÅR = BigDecimal.valueOf(100000);
    public static final Beløp GRUNNBELØP = new Beløp(BigDecimal.valueOf(GrunnbeløpMock.finnGrunnbeløp(SKJÆRINGSTIDSPUNKT)));
    private static final BigDecimal AVVIK_PROMILLE = BigDecimal.valueOf(20L);
    private static final List<FaktaOmBeregningTilfelle> FAKTA_OM_BEREGNING_TILFELLER = List.of(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE);
    private static final BigDecimal BRUTTO_PR_ÅR = BigDecimal.valueOf(10000);
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.fra(AktørId.dummy());
    private static final BigDecimal NATURALYTELSE_TILKOMMET_PR_ÅR = BigDecimal.valueOf(2000);
    private static final BigDecimal BEREGNET_PR_ÅR = BigDecimal.valueOf(1000);
    private MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL mapTilVlNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse();
    private MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL mapTilVlRefusjonOgGradering = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad();


    @Test
    public void skalMappeBeregningsgrunnlagUtenSplittNaturalytelse() {
        // Arrange
        BeregningsgrunnlagDto vlBeregningsgrunnlag = lagBeregningsgrunnlag();
        byggAktivitetStatus(vlBeregningsgrunnlag);
        BeregningsgrunnlagPeriodeDto periode = lagBeregningsgrunnlagPeriode(vlBeregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto andel = lagBeregnignsgrunnlagPrStatusOgAndel(periode);
        List<SplittetPeriode> splittetPerioder = List.of(SplittetPeriode.builder()
            .medPeriodeÅrsaker(Collections.emptyList())
            .medFørstePeriodeAndeler(List.of(mapTilBeregningsgrunnlagPrArbeidsforhold(andel)))
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .build());

        // Act
        BeregningsgrunnlagDto nyttBg = mapTilVlNaturalytelse.mapFraRegel(splittetPerioder, vlBeregningsgrunnlag);

        // Assert
        assertThat(nyttBg.getBeregningsgrunnlagPerioder().size()).isEqualTo(vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().size());
    }

    @Test
    public void skalMappeBeregningsgrunnlagUtenSplittRefusjonOgGradering() {
        // Arrange
        BeregningsgrunnlagDto vlBeregningsgrunnlag = lagBeregningsgrunnlag();
        byggAktivitetStatus(vlBeregningsgrunnlag);
        BeregningsgrunnlagPeriodeDto periode = lagBeregningsgrunnlagPeriode(vlBeregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto andel = lagBeregnignsgrunnlagPrStatusOgAndel(periode);
        List<SplittetPeriode> splittetPerioder = List.of(SplittetPeriode.builder()
            .medPeriodeÅrsaker(Collections.emptyList())
            .medFørstePeriodeAndeler(List.of(mapTilBeregningsgrunnlagPrArbeidsforhold(andel)))
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .build());

        // Act
        BeregningsgrunnlagDto nyttBg = mapTilVlRefusjonOgGradering.mapFraRegel(splittetPerioder, vlBeregningsgrunnlag);

        // Assert
        assertThat(nyttBg.getBeregningsgrunnlagPerioder().size()).isEqualTo(vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().size());
    }


    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        BeregningsgrunnlagDto vlBeregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medOverstyring(true)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilFaktaOmBeregningTilfeller(FAKTA_OM_BEREGNING_TILFELLER)
            .build();
        lagSammenligningsgrunnlag(vlBeregningsgrunnlag);
        return vlBeregningsgrunnlag;
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagBeregnignsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPeriodeDto periode) {
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBeregnetPrÅr(BEREGNET_PR_ÅR)
            .medOverstyrtPrÅr(BRUTTO_PR_ÅR)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER)
                .medNaturalytelseTilkommetPrÅr(NATURALYTELSE_TILKOMMET_PR_ÅR))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(periode);
    }

    private BeregningsgrunnlagPeriodeDto lagBeregningsgrunnlagPeriode(BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .medBruttoPrÅr(BRUTTO_PR_ÅR)
                .build(vlBeregningsgrunnlag);
    }

    private EksisterendeAndel mapTilBeregningsgrunnlagPrArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return EksisterendeAndel.builder()
            .medAndelNr(andel.getAndelsnr())
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(ARBEIDSGIVER.getIdentifikator())).build();
    }

    private void byggAktivitetStatus(BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).build(vlBeregningsgrunnlag);
    }

    private void lagSammenligningsgrunnlag(BeregningsgrunnlagDto vlBeregningsgrunnlag) {
        SammenligningsgrunnlagDto.builder()
            .medSammenligningsperiode(SKJÆRINGSTIDSPUNKT.minusMonths(3), SKJÆRINGSTIDSPUNKT)
            .medRapportertPrÅr(RAPPORTERT_PR_ÅR)
            .medAvvikPromilleNy(AVVIK_PROMILLE).build(vlBeregningsgrunnlag);
    }
}
