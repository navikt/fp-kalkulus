package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

class MapBeregningsgrunnlagFraRegelTilVLFordelTest {

    public static final String ARBEIDSGIVER_ORGNR = "134678202";
    public static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_REF = InternArbeidsforholdRefDto.nyRef();

    @Test
    void skal_kunne_legge_til_andel_for_arbeidsforhold() {
        // Arrange
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder().medBeregningsgrunnlagPeriode(LocalDate.now(), null).build();
        lagVLAndel(periode);
        Arbeidsforhold arbeidsforhold = lagRegelArbeidsforhold();
        BeregningsgrunnlagPrStatus atflStatus = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(lagEksisterendeRegelArbeidsforholdAndel(arbeidsforhold))
                .medArbeidsforhold(lagNyRegelArbeidsforholdAndel(arbeidsforhold))
                .build();

        // Act
        new MapBeregningsgrunnlagFraRegelTilVLFordel().mapAndelMedArbeidsforhold(periode, atflStatus);

        // Assert
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        BeregningsgrunnlagPrStatusOgAndelDto andel1 = periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(andel1.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(andel1.getGjeldendeInntektskategori()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER);
        assertThat(andel1.getBeregnetPrÅr()).isEqualTo(BigDecimal.valueOf(50_000));
        assertThat(andel1.getFordeltPrÅr()).isNull();
        assertThat(andel1.getAndelsnr()).isEqualTo(1L);
        assertThat(andel1.getBgAndelArbeidsforhold().get().getArbeidsgiver().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(andel1.getBgAndelArbeidsforhold().get().getArbeidsforholdRef()).isEqualTo(ARBEIDSFORHOLD_REF);
        assertThat(andel1.getBgAndelArbeidsforhold().get().getGjeldendeRefusjonPrÅr()).isEqualTo(BigDecimal.valueOf(75_000));

        BeregningsgrunnlagPrStatusOgAndelDto andel2 = periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1);
        assertThat(andel2.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER);
        assertThat(andel2.getGjeldendeInntektskategori()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(andel2.getFordeltPrÅr()).isEqualTo(BigDecimal.valueOf(25_000));
        assertThat(andel2.getBeregnetPrÅr()).isNull();
        assertThat(andel2.getAndelsnr()).isEqualTo(2L);
        assertThat(andel2.getBgAndelArbeidsforhold().get().getArbeidsgiver().getOrgnr()).isEqualTo(ARBEIDSGIVER_ORGNR);
        assertThat(andel2.getBgAndelArbeidsforhold().get().getArbeidsforholdRef()).isEqualTo(ARBEIDSFORHOLD_REF);
        assertThat(andel2.getBgAndelArbeidsforhold().get().getGjeldendeRefusjonPrÅr()).isEqualTo(BigDecimal.valueOf(25_000));
    }

    private void lagVLAndel(BeregningsgrunnlagPeriodeDto periode) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medRefusjonskravPrÅr(BigDecimal.valueOf(100_000), Utfall.GODKJENT)
                        .medArbeidsforholdRef(ARBEIDSFORHOLD_REF)
                        .medArbeidsgiver(Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR)))
                .medBeregnetPrÅr(BigDecimal.valueOf(50_000))
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori.ARBEIDSTAKER).build(periode);
    }

    private BeregningsgrunnlagPrArbeidsforhold lagNyRegelArbeidsforholdAndel(Arbeidsforhold arbeidsforhold) {
        return BeregningsgrunnlagPrArbeidsforhold.builder()
                .erNytt(true)
                .medArbeidsforhold(arbeidsforhold)
                .medFordeltPrÅr(BigDecimal.valueOf(25_000))
                .medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(25_000))
                .medFordeltRefusjonPrÅr(BigDecimal.valueOf(25_000))
                .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                .build();
    }

    private BeregningsgrunnlagPrArbeidsforhold lagEksisterendeRegelArbeidsforholdAndel(Arbeidsforhold arbeidsforhold) {
        return BeregningsgrunnlagPrArbeidsforhold.builder()
                .medArbeidsforhold(arbeidsforhold)
                .medAndelNr(1L)
                .medBeregnetPrÅr(BigDecimal.valueOf(50_000))
                .medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(75_000))
                .medFordeltRefusjonPrÅr(BigDecimal.valueOf(75_000))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .build();
    }

    private Arbeidsforhold lagRegelArbeidsforhold() {
        return Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
                    .medOrgnr(ARBEIDSGIVER_ORGNR)
                    .medArbeidsforholdId(ARBEIDSFORHOLD_REF.getReferanse())
                    .build();
    }
}
