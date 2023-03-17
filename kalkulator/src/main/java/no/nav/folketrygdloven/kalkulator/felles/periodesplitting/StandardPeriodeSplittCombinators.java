package no.nav.folketrygdloven.kalkulator.felles.periodesplitting;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;

public class StandardPeriodeSplittCombinators {

    public static <V> LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> splittPerioderOgSettÅrsakCombinator(PeriodeÅrsak periodeårsak) {
        return (di, lhs, rhs) -> {
            if (lhs != null && rhs != null) {
                var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                        .leggTilPeriodeÅrsak(periodeårsak)
                        .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato())
                        .build();
                return new LocalDateSegment<>(di, nyPeriode);
            } else if (lhs != null) {
                var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                        .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato())
                        .build();
                return new LocalDateSegment<>(di, nyPeriode);
            }
            return null;
        };
    }


}
