package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse.omp;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

class FastsettPerioderBrukersSøknad {

    public static BeregningsgrunnlagDto fastsettPerioderForBrukersSøknad(OmsorgspengerGrunnlag omsorgspengerGrunnlag,
                                                                         BeregningsgrunnlagDto beregningsgrunnlag) {

        var søkteSegmenter = omsorgspengerGrunnlag.getSøknadsperioderPrAktivitet()
                .stream()
                .flatMap(s -> s.getPeriode().stream())
                .map(s -> new LocalDateSegment<>(s.getFomDato(), s.getTomDato(), true))
                .toList();
        var brukerSøknadTidslinje = new LocalDateTimeline<Boolean>(søkteSegmenter, StandardCombinators::coalesceLeftHandSide)
                .compress();

        var eksisterendePerioderSegmenter = beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .stream().map(BeregningsgrunnlagPeriodeDto::getPeriode)
                .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))
                .toList();
        var eksisterendePerioderTidslinje = new LocalDateTimeline<Boolean>(eksisterendePerioderSegmenter);

        var sammenfallendeIntervaller = brukerSøknadTidslinje.intersection(eksisterendePerioderTidslinje)
                .toSegments()
                .stream().map(s -> Intervall.fraOgMedTilOgMed(s.getFom(), s.getTom()))
                .toList();

        var nyttBg = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();

        oppdaterBeregningsgrunnlagForNyeFomDatoer(eksisterendePerioderSegmenter, sammenfallendeIntervaller, nyttBg);
        oppdaterBeregningsgrunnlagForNyeTomDatoer(eksisterendePerioderSegmenter, sammenfallendeIntervaller, nyttBg);


        return nyttBg;

    }

    private static void oppdaterBeregningsgrunnlagForNyeTomDatoer(List<LocalDateSegment<Boolean>> eksisterendePerioderSegmenter, List<Intervall> sammenfallendeIntervaller, BeregningsgrunnlagDto nyttBg) {
        var nyeTomDatoer = sammenfallendeIntervaller.stream()
                .map(Intervall::getTomDato)
                .filter(tomDato -> eksisterendePerioderSegmenter.stream().noneMatch(s -> s.getTom().equals(tomDato)))
                .toList();


        for (var tom : nyeTomDatoer) {
            var overlappendePeriode = nyttBg.getBeregningsgrunnlagPerioder().stream()
                    .filter(p -> p.getPeriode().inkluderer(tom))
                    .findFirst()
                    .orElseThrow();
            var fomDato1 = overlappendePeriode.getBeregningsgrunnlagPeriodeFom();
            var periode1 = BeregningsgrunnlagPeriodeDto.kopier(overlappendePeriode)
                    .medBeregningsgrunnlagPeriode(fomDato1, tom)
                    .build();
            nyttBg.leggTilBeregningsgrunnlagPeriode(periode1);

            var fomDato2 = tom.plusDays(1);
            var tomDato2 = overlappendePeriode.getBeregningsgrunnlagPeriodeTom();
            BeregningsgrunnlagPeriodeDto.oppdater(overlappendePeriode)
                    .medBeregningsgrunnlagPeriode(fomDato2, tomDato2)
                    .build();
        }
    }

    private static void oppdaterBeregningsgrunnlagForNyeFomDatoer(List<LocalDateSegment<Boolean>> eksisterendePerioderSegmenter, List<Intervall> sammenfallendeIntervaller, BeregningsgrunnlagDto nyttBg) {
        var nyeFomDatoer = sammenfallendeIntervaller.stream()
                .map(Intervall::getFomDato)
                .filter(fomDato -> eksisterendePerioderSegmenter.stream().noneMatch(s -> s.getFom().equals(fomDato)))
                .toList();


        for (var fom : nyeFomDatoer) {
            var overlappendePeriode = nyttBg.getBeregningsgrunnlagPerioder().stream()
                    .filter(p -> p.getPeriode().inkluderer(fom))
                    .findFirst()
                    .orElseThrow();
            var tomDato1 = fom.minusDays(1);
            var fomDato1 = overlappendePeriode.getBeregningsgrunnlagPeriodeFom();
            var periode1 = BeregningsgrunnlagPeriodeDto.kopier(overlappendePeriode)
                    .medBeregningsgrunnlagPeriode(fomDato1, tomDato1)
                    .build();
            nyttBg.leggTilBeregningsgrunnlagPeriode(periode1);

            var tomDato2 = overlappendePeriode.getBeregningsgrunnlagPeriodeTom();
            BeregningsgrunnlagPeriodeDto.oppdater(overlappendePeriode)
                    .medBeregningsgrunnlagPeriode(fom, tomDato2)
                    .build();
        }
    }

}
