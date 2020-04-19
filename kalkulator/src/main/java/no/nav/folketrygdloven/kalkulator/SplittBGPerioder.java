package no.nav.folketrygdloven.kalkulator;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.util.KopierBeregningsgrunnlagUtil;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;

public class SplittBGPerioder {
    private SplittBGPerioder() {
        // skjul public constructor
    }

    static void splitt(Beregningsgrunnlag regelBeregningsgrunnlag, Collection<AktivitetsAvtaleDto> ansettelsesPerioder) {
        ansettelsesPerioder.forEach(aa -> {
            LocalDate kortvarigArbeidsforholdTom = aa.getPeriode().getTomDato();
            List<BeregningsgrunnlagPeriode> eksisterendePerioder = regelBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
            ListIterator<BeregningsgrunnlagPeriode> periodeIterator = eksisterendePerioder.listIterator();
            while (periodeIterator.hasNext()) {
                BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = periodeIterator.next();
                Periode bgPeriode = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriode();
                PeriodeÅrsak nyPeriodeÅrsak = PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET;
                if (bgPeriode.getTom().equals(kortvarigArbeidsforholdTom)) {
                    oppdaterPeriodeÅrsakForNestePeriode(eksisterendePerioder, periodeIterator, nyPeriodeÅrsak);
                } else if (bgPeriode.inneholder(kortvarigArbeidsforholdTom)) {
                    splitBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, kortvarigArbeidsforholdTom, nyPeriodeÅrsak);
                }
            }
        });
    }

    private static void oppdaterPeriodeÅrsakForNestePeriode(List<BeregningsgrunnlagPeriode> eksisterendePerioder, ListIterator<BeregningsgrunnlagPeriode> periodeIterator, PeriodeÅrsak nyPeriodeÅrsak) {
        if (periodeIterator.hasNext()) {
            BeregningsgrunnlagPeriode nestePeriode = eksisterendePerioder.get(periodeIterator.nextIndex());
            BeregningsgrunnlagPeriode.builder(nestePeriode)
                .leggTilPeriodeÅrsak(nyPeriodeÅrsak)
                .build();
        }
    }

    public static BeregningsgrunnlagPeriode splitBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, LocalDate nyPeriodeTom, PeriodeÅrsak periodeÅrsak) {
        LocalDate eksisterendePeriodeTom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriode().getTom();
        BeregningsgrunnlagPeriode.builder(beregningsgrunnlagPeriode)
            .medPeriode(Periode.of(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriode().getFom(), nyPeriodeTom))
            .build();
        BeregningsgrunnlagPeriode nyPeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(nyPeriodeTom.plusDays(1), eksisterendePeriodeTom))
            .leggTilPeriodeÅrsak(periodeÅrsak)
            .build();
        KopierBeregningsgrunnlagUtil.kopierBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, nyPeriode);
        leggTilPeriodeIBeregningsgrunnlag(beregningsgrunnlagPeriode.getBeregningsgrunnlag(), nyPeriode);
        return nyPeriode;
    }

    private static void leggTilPeriodeIBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagPeriode nyPeriode) {
        Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medBeregningsgrunnlagPeriode(nyPeriode)
            .build();
    }
}
