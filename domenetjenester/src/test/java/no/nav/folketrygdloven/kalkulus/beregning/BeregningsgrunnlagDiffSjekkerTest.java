package no.nav.folketrygdloven.kalkulus.beregning;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.SammenligningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;

class BeregningsgrunnlagDiffSjekkerTest {

    @Test
    public void skalReturnereFalseNårSammenligningsgrunnlagPrStatusListeErLik(){
        SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusAt = new SammenligningsgrunnlagPrStatus.Builder();
        sammenligningsgrunnlagPrStatusAt.medRapportertPrÅr(BigDecimal.valueOf(100_000));
        sammenligningsgrunnlagPrStatusAt.medAvvikPromilleNy(BigDecimal.ZERO);
        sammenligningsgrunnlagPrStatusAt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusFl = new SammenligningsgrunnlagPrStatus.Builder();
        sammenligningsgrunnlagPrStatusFl.medRapportertPrÅr(BigDecimal.valueOf(200_000));
        sammenligningsgrunnlagPrStatusFl.medAvvikPromilleNy(BigDecimal.valueOf(250));
        sammenligningsgrunnlagPrStatusFl.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusFl.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_FL);

        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAt)
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusFl)
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(beregningsgrunnlagEntitet, beregningsgrunnlagEntitet);
        assertFalse(resultat);
    }

    @Test
    public void skalReturnereTrueNårSammenligningsgrunnlagPrStatusListeIkkeInneholderSammeTyper(){
        SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusAt = new SammenligningsgrunnlagPrStatus.Builder();
        sammenligningsgrunnlagPrStatusAt.medRapportertPrÅr(BigDecimal.valueOf(100_000));
        sammenligningsgrunnlagPrStatusAt.medAvvikPromilleNy(BigDecimal.ZERO);
        sammenligningsgrunnlagPrStatusAt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusFl = new SammenligningsgrunnlagPrStatus.Builder();
        sammenligningsgrunnlagPrStatusFl.medRapportertPrÅr(BigDecimal.valueOf(200_000));
        sammenligningsgrunnlagPrStatusFl.medAvvikPromilleNy(BigDecimal.valueOf(250));
        sammenligningsgrunnlagPrStatusFl.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusFl.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_FL);

        BeregningsgrunnlagEntitet aktivt = BeregningsgrunnlagEntitet.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAt)
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagEntitet forrige = BeregningsgrunnlagEntitet.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusFl)
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(aktivt, forrige);
        assertTrue(resultat);
    }

    @Test
    public void skalReturnereTrueNårSammenligningsgrunnlagPrStatusListeIkkeHarLikeVerdierForSammeType(){
        BigDecimal avvikPromilleAtAktivt = BigDecimal.ZERO;
        BigDecimal avvikPromilleAtForrige = BigDecimal.valueOf(10);
        SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusAtAktivt = new SammenligningsgrunnlagPrStatus.Builder();
        sammenligningsgrunnlagPrStatusAtAktivt.medRapportertPrÅr(BigDecimal.valueOf(100_000));
        sammenligningsgrunnlagPrStatusAtAktivt.medAvvikPromilleNy(avvikPromilleAtAktivt);
        sammenligningsgrunnlagPrStatusAtAktivt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAtAktivt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusFlAktivt = new SammenligningsgrunnlagPrStatus.Builder();
        sammenligningsgrunnlagPrStatusFlAktivt.medRapportertPrÅr(BigDecimal.valueOf(200_000));
        sammenligningsgrunnlagPrStatusFlAktivt.medAvvikPromilleNy(BigDecimal.valueOf(250));
        sammenligningsgrunnlagPrStatusFlAktivt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusFlAktivt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_FL);

        SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusFlForrige = new SammenligningsgrunnlagPrStatus.Builder();
        sammenligningsgrunnlagPrStatusFlForrige.medRapportertPrÅr(BigDecimal.valueOf(200_000));
        sammenligningsgrunnlagPrStatusFlForrige.medAvvikPromilleNy(BigDecimal.valueOf(250));
        sammenligningsgrunnlagPrStatusFlForrige.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusFlForrige.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_FL);

        SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusAtForrige = new SammenligningsgrunnlagPrStatus.Builder();
        sammenligningsgrunnlagPrStatusAtForrige.medRapportertPrÅr(BigDecimal.valueOf(100_000));
        sammenligningsgrunnlagPrStatusAtForrige.medAvvikPromilleNy(avvikPromilleAtForrige);
        sammenligningsgrunnlagPrStatusAtForrige.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAtForrige.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        BeregningsgrunnlagEntitet aktivt = BeregningsgrunnlagEntitet.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAtAktivt)
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusFlAktivt)
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagEntitet forrige = BeregningsgrunnlagEntitet.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAtForrige)
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusFlForrige)
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(aktivt, forrige);
        assertTrue(resultat);
    }

    @Test
    public void skalReturnereFalseNårSammenligningsgrunnlagPrStatusListeIkkeErSattForBeggeBeregningsgrunnlag(){
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(beregningsgrunnlagEntitet, beregningsgrunnlagEntitet);
        assertFalse(resultat);
    }

    @Test
    public void skalReturnereTrueNårSammenligningsgrunnlagPrStatusListeIkkeErSattForDetEneBeregningsgrunnlaget(){
        SammenligningsgrunnlagPrStatus.Builder sammenligningsgrunnlagPrStatusAt = new SammenligningsgrunnlagPrStatus.Builder();
        sammenligningsgrunnlagPrStatusAt.medRapportertPrÅr(BigDecimal.valueOf(100_000));
        sammenligningsgrunnlagPrStatusAt.medAvvikPromilleNy(BigDecimal.ZERO);
        sammenligningsgrunnlagPrStatusAt.medSammenligningsperiode(LocalDate.now().minusYears(1), LocalDate.now());
        sammenligningsgrunnlagPrStatusAt.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT);

        BeregningsgrunnlagEntitet aktivt = BeregningsgrunnlagEntitet.builder()
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagPrStatusAt)
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagEntitet forrige = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        boolean resultat = BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(aktivt, forrige);
        assertTrue(resultat);
    }

}
