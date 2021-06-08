package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import java.time.LocalDate;
import java.util.List;
import java.util.ListIterator;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

public class SplittBGPerioder {
    private SplittBGPerioder() {
        // skjul public constructor
    }

    static BeregningsgrunnlagDto splitt(BeregningsgrunnlagDto beregningsgrunnlag,
                       PeriodeÅrsak periodeårsak,
                       LocalDate tilkommetInntektDato) {

        BeregningsgrunnlagDto nyttBg = new BeregningsgrunnlagDto(beregningsgrunnlag);

            var eksisterendePerioder = nyttBg.getBeregningsgrunnlagPerioder();
            var periodeIterator = eksisterendePerioder.listIterator();
            while (periodeIterator.hasNext()) {
                var beregningsgrunnlagPeriode = periodeIterator.next();
                var bgPeriode = beregningsgrunnlagPeriode.getPeriode();
                // Hva skal skje her om perioden er 1 dag?
                if (bgPeriode.getTomDato().equals(tilkommetInntektDato.minusDays(1))) {
                    oppdaterPeriodeÅrsakForNestePeriode(eksisterendePerioder, periodeIterator, periodeårsak);
                } else if (bgPeriode.inkluderer(tilkommetInntektDato)) {
                    splittBeregningsgrunnlagPeriode(nyttBg,
                            beregningsgrunnlagPeriode,
                            tilkommetInntektDato, periodeårsak);
                }
            }
            return nyttBg;
    }

    private static void oppdaterPeriodeÅrsakForNestePeriode(List<BeregningsgrunnlagPeriodeDto> eksisterendePerioder,
                                                            ListIterator<BeregningsgrunnlagPeriodeDto> periodeIterator, PeriodeÅrsak nyPeriodeÅrsak) {
        if (periodeIterator.hasNext()) {
            var nestePeriode = eksisterendePerioder.get(periodeIterator.nextIndex());
            BeregningsgrunnlagPeriodeDto.builder(nestePeriode)
                .leggTilPeriodeÅrsak(nyPeriodeÅrsak)
                .build();
        }
    }

    private static BeregningsgrunnlagPeriodeDto splittBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                                                               LocalDate nyPeriodeFom, PeriodeÅrsak periodeÅrsak) {
        LocalDate eksisterendePeriodeTom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom();
        BeregningsgrunnlagPeriodeDto.oppdater(beregningsgrunnlagPeriode)
            .medBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), nyPeriodeFom.minusDays(1))
            .build();
        var nyPeriode = BeregningsgrunnlagPeriodeDto.builder(beregningsgrunnlagPeriode)
            .medBeregningsgrunnlagPeriode(nyPeriodeFom, eksisterendePeriodeTom)
            .leggTilPeriodeÅrsak(periodeÅrsak)
            .build(beregningsgrunnlag);
        return nyPeriode;
    }


}
