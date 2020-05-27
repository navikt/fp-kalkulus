package no.nav.folketrygdloven.kalkulator.refusjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.refusjon.modell.RefusjonAndel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;

class BeregningRefusjonTjenesteTest {
    private static final Arbeidsgiver AG1 = Arbeidsgiver.virksomhet("999999999");
    private static final Arbeidsgiver AG2 = Arbeidsgiver.virksomhet("111111111");
    private static final Arbeidsgiver AG3 = Arbeidsgiver.virksomhet("222222222");
    private static final InternArbeidsforholdRefDto REF1 = InternArbeidsforholdRefDto.nyRef();
    private static final InternArbeidsforholdRefDto REF2 = InternArbeidsforholdRefDto.nyRef();
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static BeregningsgrunnlagDto originaltBG;
    private static BeregningsgrunnlagDto revurderingBG;

    @BeforeEach
    public void setup() {
        originaltBG = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(BigDecimal.valueOf(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7)
                .build(originaltBG);
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medHjemmel(Hjemmel.F_14_7)
                .build(originaltBG);

        revurderingBG = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(BigDecimal.valueOf(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7)
                .build(revurderingBG);
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medHjemmel(Hjemmel.F_14_7)
                .build(revurderingBG);

    }

    @Test
    public void skal_ikke_finne_andeler_når_det_ikke_har_vært_endring_i_refusjon() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);

        Map<Intervall, List<RefusjonAndel>> resultat = BeregningRefusjonTjeneste.finnPerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_finne_andel_hvis_refusjonskrav_har_økt() {
        // Arrange
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);

        // Act
        Map<Intervall, List<RefusjonAndel>> resultat = BeregningRefusjonTjeneste.finnPerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG);

        // Assert
        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AktivitetStatus.ARBEIDSTAKER, AG1, 50000, 100000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }



    @Test
    public void skal_ikke_finne_andel_hvis_refusjonskrav_har_sunket() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 100000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);

        Map<Intervall, List<RefusjonAndel>> resultat = BeregningRefusjonTjeneste.finnPerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_finne_korrekt_andel_når_flere_finnes() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 200000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG3, REF1, 300000, 300000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 200000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG3, REF1, 300000, 300000);

        Map<Intervall, List<RefusjonAndel>> resultat = BeregningRefusjonTjeneste.finnPerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AktivitetStatus.ARBEIDSTAKER, AG2, 200000, 200000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    @Test
    public void skal_ikke_finne_andel_hvis_inntekt_og_ref_økes_like_mye() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 0);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 300000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 300000);

        Map<Intervall, List<RefusjonAndel>> resultat = BeregningRefusjonTjeneste.finnPerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_finne_begge_andeler_hvis_ref_økes() {
        // Original
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 200000, 100000);

        // Revurdering
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 100000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 200000, 200000);

        Map<Intervall, List<RefusjonAndel>> resultat = BeregningRefusjonTjeneste.finnPerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel1 = lagForventetAndel(AktivitetStatus.ARBEIDSTAKER, AG1, 100000, 100000);
        RefusjonAndel forventetAndel2 = lagForventetAndel(AktivitetStatus.ARBEIDSTAKER, AG2, 200000, 200000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Arrays.asList(forventetAndel1, forventetAndel2));
    }

    @Test
    public void skal_finne_nytt_ref_krav() {
        // Original
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 0);

        // Revurdering
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 300000);

        Map<Intervall, List<RefusjonAndel>> resultat = BeregningRefusjonTjeneste.finnPerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AktivitetStatus.ARBEIDSTAKER, AG2, 300000, 300000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    @Test
    public void skal_ikke_finne_tilkommet_arbfor_når_det_ikke_endrer_brukers_andel() {
        // Original
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);

        // Revurdering
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(20))
                .build(revurderingBG);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode3 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(21), Intervall.TIDENES_ENDE)
                .build(revurderingBG);

        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);

        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 200000);
        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG2, REF1, 300000, 300000);

        Map<Intervall, List<RefusjonAndel>> resultat = BeregningRefusjonTjeneste.finnPerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG);

        assertThat(resultat).isEmpty();
    }

    // TODO Bør egenlig denne ikke gi ap?
    @Test
    public void skal_finne_riktig_andel_i_ritktig_periode_når_brutto_senkes() {
        // Original
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(20))
                .build(originaltBG);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(21), Intervall.TIDENES_ENDE)
                .build(originaltBG);

        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 50000);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 50000);

        // Revurdering
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode3 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(20))
                .build(revurderingBG);
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode4 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(21), Intervall.TIDENES_ENDE)
                .build(revurderingBG);

        leggTilAndel(beregningsgrunnlagPeriode3, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 200000, 50000);
        leggTilAndel(beregningsgrunnlagPeriode4, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 50000, 50000);

        Map<Intervall, List<RefusjonAndel>> resultat = BeregningRefusjonTjeneste.finnPerioderMedAndelerMedØktRefusjon(revurderingBG, originaltBG);

        assertThat(resultat).hasSize(1);
        RefusjonAndel forventetAndel = lagForventetAndel(AktivitetStatus.ARBEIDSTAKER, AG1, 50000, 50000);
        Intervall forventetInterval = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(21), Intervall.TIDENES_ENDE);
        assertMap(resultat, forventetInterval, Collections.singletonList(forventetAndel));
    }

    private void leggTilAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                              AktivitetStatus aktivitetStatus, Arbeidsgiver ag,
                              InternArbeidsforholdRefDto ref,
                              int bruttoPrÅr, int refusjonskravPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(aktivitetStatus)
                .medBeregnetPrÅr(BigDecimal.valueOf(bruttoPrÅr));
        if (ag != null) {
            BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(ag)
                    .medArbeidsforholdRef(ref)
                    .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonskravPrÅr));
            andelBuilder.medBGAndelArbeidsforhold(bgAndelArbeidsforholdBuilder);
        }
        andelBuilder.build(beregningsgrunnlagPeriode);
    }

    private void assertMap(Map<Intervall, List<RefusjonAndel>> resultat, Intervall forventetInterval, List<RefusjonAndel> forventedeAndeler) {
        List<RefusjonAndel> faktiskeAndeler = resultat.get(forventetInterval);
        assertThat(faktiskeAndeler).hasSameSizeAs(forventedeAndeler);
        forventedeAndeler.forEach(forventet -> {
            Optional<RefusjonAndel> faktiskOpt = faktiskeAndeler.stream().filter(a -> a.matcher(forventet)).findFirst();
            assertThat(faktiskOpt).isPresent();
            RefusjonAndel faktisk = faktiskOpt.get();
            assertThat(faktisk.getRefusjon()).isEqualByComparingTo(forventet.getRefusjon());
            assertThat(faktisk.getBrutto()).isEqualByComparingTo(forventet.getBrutto());
        });

    }

    private RefusjonAndel lagForventetAndel(AktivitetStatus status, int refusjon, int brutto) {
        return lagForventetAndel(status, null, refusjon, brutto);
    }

    private RefusjonAndel lagForventetAndel(AktivitetStatus status, Arbeidsgiver ag, int refusjon, int brutto) {
        return new RefusjonAndel(ag, BigDecimal.valueOf(brutto), BigDecimal.valueOf(refusjon));
    }

}
