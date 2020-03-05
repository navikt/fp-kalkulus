package no.nav.folketrygdloven.kalkulator.endringsresultat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

public class LagBeregningsgrunnlagTjeneste {
    public static BeregningsgrunnlagDto lagBeregningsgrunnlag(LocalDate skjæringstidspunktBeregning,
                                                           boolean medOppjustertDagsat,
                                                           boolean skalDeleAndelMellomArbeidsgiverOgBruker,
                                                           List<Periode> perioder,
                                                           LagAndelTjeneste lagAndelTjeneste) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(skjæringstidspunktBeregning)
            .medGrunnbeløp(BigDecimal.valueOf(91425L))
            .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlag);
        for (Periode datoPeriode : perioder) {
            BeregningsgrunnlagPeriodeDto periode = byggBGPeriode(beregningsgrunnlag, datoPeriode, medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker, lagAndelTjeneste);
            BeregningsgrunnlagPeriodeDto.oppdater(periode).build(beregningsgrunnlag);
        }
        return beregningsgrunnlag;
    }

    private static BeregningsgrunnlagPeriodeDto byggBGPeriode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           Periode datoPeriode,
                                                           boolean medOppjustertDagsat,
                                                           boolean skalDeleAndelMellomArbeidsgiverOgBruker,
                                                           LagAndelTjeneste lagAndelTjeneste) {
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(datoPeriode.getFom(), datoPeriode.getTom())
            .build(beregningsgrunnlag);
        lagAndelTjeneste.lagAndeler(periode, medOppjustertDagsat, skalDeleAndelMellomArbeidsgiverOgBruker);
        return periode;
    }
}
