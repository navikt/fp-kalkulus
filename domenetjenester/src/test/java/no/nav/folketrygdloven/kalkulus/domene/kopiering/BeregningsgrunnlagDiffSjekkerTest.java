package no.nav.folketrygdloven.kalkulus.domene.kopiering;

import static no.nav.folketrygdloven.kalkulus.domene.felles.jpa.AbstractIntervall.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

class BeregningsgrunnlagDiffSjekkerTest {

    @Test
    void skalReturnereTrueOmUlikeArbeidsforholdsreferanser() {
        // Arrange
        var stp = LocalDate.now();
        BeregningsgrunnlagDto aktivt = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(stp)
                .build();

        var aktivPeriode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(stp, TIDENES_ENDE)
                .build(aktivt);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(Arbeidsgiver.virksomhet("12346778")))
                .build(aktivPeriode);

        BeregningsgrunnlagDto forrige = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(stp)
                .build();

        var forrigePeriode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(stp, TIDENES_ENDE)
                .build(forrige);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(Arbeidsgiver.virksomhet("12346778"))
                        .medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef()))
                .build(forrigePeriode);

        // Act

        boolean harDiff = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(aktivt, forrige);

        // Assert
        assertThat(harDiff).isTrue();
    }

    @Test
    void skalReturnereTrueOmUlikeGrunnbeløp() {
        // Arrange
        BeregningsgrunnlagDto aktivt = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(Beløp.fra(10))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.FRILANSER))
                .medSkjæringstidspunkt(LocalDate.now())
                .build();

        BeregningsgrunnlagDto forrige = BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(Beløp.fra(1))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.FRILANSER))
                .medSkjæringstidspunkt(LocalDate.now())
                .build();

        // Act

        boolean harDiff = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(aktivt, forrige);

        // Assert
        assertThat(harDiff).isTrue();
    }

    @Test
    void skalReturnereFalseNårSammenligningsgrunnlagPrStatusListeErLik(){
        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagPrStatusAt = new SammenligningsgrunnlagPrStatusDto.Builder();
        sammenligningsgrunnlagPrStatusAt.medRapportertPrÅr(Beløp.fra(100_000));
        sammenligningsgrunnlagPrStatusAt.medAvvikPromilleNy(BigDecimal.ZERO);
        sammenligningsgrunnlagPrStatusAt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagPrStatusFl = new SammenligningsgrunnlagPrStatusDto.Builder();
        sammenligningsgrunnlagPrStatusFl.medRapportertPrÅr(Beløp.fra(200_000));
        sammenligningsgrunnlagPrStatusFl.medAvvikPromilleNy(BigDecimal.valueOf(250));
        sammenligningsgrunnlagPrStatusFl.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusFl.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_FL);

        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagDto.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAt.build())
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusFl.build())
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(beregningsgrunnlagDto, beregningsgrunnlagDto);
        assertFalse(resultat);
    }

    @Test
    void skalReturnereTrueNårSammenligningsgrunnlagPrStatusListeIkkeInneholderSammeTyper(){
        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagPrStatusAt = new SammenligningsgrunnlagPrStatusDto.Builder();
        sammenligningsgrunnlagPrStatusAt.medRapportertPrÅr(Beløp.fra(100_000));
        sammenligningsgrunnlagPrStatusAt.medAvvikPromilleNy(BigDecimal.ZERO);
        sammenligningsgrunnlagPrStatusAt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagPrStatusFl = new SammenligningsgrunnlagPrStatusDto.Builder();
        sammenligningsgrunnlagPrStatusFl.medRapportertPrÅr(Beløp.fra(200_000));
        sammenligningsgrunnlagPrStatusFl.medAvvikPromilleNy(BigDecimal.valueOf(250));
        sammenligningsgrunnlagPrStatusFl.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusFl.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_FL);

        BeregningsgrunnlagDto aktivt = BeregningsgrunnlagDto.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAt.build())
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagDto forrige = BeregningsgrunnlagDto.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusFl.build())
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(aktivt, forrige);
        assertTrue(resultat);
    }

    @Test
    void skalReturnereTrueNårSammenligningsgrunnlagPrStatusListeIkkeHarLikeVerdierForSammeType(){
        BigDecimal avvikPromilleAtAktivt = BigDecimal.ZERO;
        BigDecimal avvikPromilleAtForrige = BigDecimal.valueOf(10);
        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagPrStatusAtAktivt = new SammenligningsgrunnlagPrStatusDto.Builder();
        sammenligningsgrunnlagPrStatusAtAktivt.medRapportertPrÅr(Beløp.fra(100_000));
        sammenligningsgrunnlagPrStatusAtAktivt.medAvvikPromilleNy(avvikPromilleAtAktivt);
        sammenligningsgrunnlagPrStatusAtAktivt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAtAktivt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagPrStatusFlAktivt = new SammenligningsgrunnlagPrStatusDto.Builder();
        sammenligningsgrunnlagPrStatusFlAktivt.medRapportertPrÅr(Beløp.fra(200_000));
        sammenligningsgrunnlagPrStatusFlAktivt.medAvvikPromilleNy(BigDecimal.valueOf(250));
        sammenligningsgrunnlagPrStatusFlAktivt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusFlAktivt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_FL);

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagPrStatusFlForrige = new SammenligningsgrunnlagPrStatusDto.Builder();
        sammenligningsgrunnlagPrStatusFlForrige.medRapportertPrÅr(Beløp.fra(200_000));
        sammenligningsgrunnlagPrStatusFlForrige.medAvvikPromilleNy(BigDecimal.valueOf(250));
        sammenligningsgrunnlagPrStatusFlForrige.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusFlForrige.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_FL);

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagPrStatusAtForrige = new SammenligningsgrunnlagPrStatusDto.Builder();
        sammenligningsgrunnlagPrStatusAtForrige.medRapportertPrÅr(Beløp.fra(100_000));
        sammenligningsgrunnlagPrStatusAtForrige.medAvvikPromilleNy(avvikPromilleAtForrige);
        sammenligningsgrunnlagPrStatusAtForrige.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAtForrige.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        BeregningsgrunnlagDto aktivt = BeregningsgrunnlagDto.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAtAktivt.build())
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusFlAktivt.build())
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagDto forrige = BeregningsgrunnlagDto.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAtForrige.build())
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusFlForrige.build())
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(aktivt, forrige);
        assertTrue(resultat);
    }

    @Test
    void skalReturnereFalseNårSammenligningsgrunnlagPrStatusListeIkkeErSattForBeggeBeregningsgrunnlag(){
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(beregningsgrunnlagDto, beregningsgrunnlagDto);
        assertFalse(resultat);
    }

    @Test
    void skalReturnereTrueNårSammenligningsgrunnlagPrStatusListeIkkeErSattForDetEneBeregningsgrunnlaget(){
        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagPrStatusAt = new SammenligningsgrunnlagPrStatusDto.Builder();
        sammenligningsgrunnlagPrStatusAt.medRapportertPrÅr(Beløp.fra(100_000));
        sammenligningsgrunnlagPrStatusAt.medAvvikPromilleNy(BigDecimal.ZERO);
        sammenligningsgrunnlagPrStatusAt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        BeregningsgrunnlagDto aktivt = BeregningsgrunnlagDto.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAt.build())
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagDto forrige = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(aktivt, forrige);
        assertTrue(resultat);
    }

}
