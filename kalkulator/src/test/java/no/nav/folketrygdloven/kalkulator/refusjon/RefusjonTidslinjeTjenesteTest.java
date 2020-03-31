package no.nav.folketrygdloven.kalkulator.refusjon;

import no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.refusjon.modell.RefusjonPeriode;
import no.nav.folketrygdloven.kalkulator.refusjon.modell.RefusjonPeriodeEndring;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;

import no.nav.fpsak.tidsserie.LocalDateTimeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class RefusjonTidslinjeTjenesteTest {
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
    public void tester_at_timeline_lages_korrekt() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(originaltBG);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 500000);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, Intervall.TIDENES_ENDE)
                .build(revurderingBG);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, AG1, REF1, 100000, 400000);
        LocalDateTimeline<RefusjonPeriode> refusjonsdataLocalDateTimeline = RefusjonTidslinjeTjeneste.lagTidslinje(originaltBG);
        LocalDateTimeline<RefusjonPeriode> refusjonsdataLocalDateTimeline1 = RefusjonTidslinjeTjeneste.lagTidslinje(revurderingBG);
        LocalDateTimeline<RefusjonPeriodeEndring> tidslinje = RefusjonTidslinjeTjeneste.kombinerTidslinjer(refusjonsdataLocalDateTimeline, refusjonsdataLocalDateTimeline1);
        assertThat(tidslinje.toSegments()).hasSize(1);
    }

    private void leggTilAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                              AktivitetStatus aktivitetStatus, Arbeidsgiver ag,
                              InternArbeidsforholdRefDto ref,
                              int bruttoPrÅr, int refusjonskravPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier()
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

    }
