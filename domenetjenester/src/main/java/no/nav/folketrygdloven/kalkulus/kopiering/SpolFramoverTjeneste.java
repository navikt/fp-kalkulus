package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.kopiering.KanKopierBeregningsgrunnlag.finnPerioderSomKanKopieres;
import static no.nav.folketrygdloven.kalkulus.kopiering.KanKopierBeregningsgrunnlag.kanKopiereForrigeGrunnlagAvklartIStegUt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

public final class SpolFramoverTjeneste {

    private SpolFramoverTjeneste() {
        // Skjul
    }


    /**
     * Spoler grunnlaget framover en tilstand om dette er mulig.
     * Spolingen kopierer hele eller deler av grunnlaget som er lagret ved håndtering av aksjonspunkter mellom inneværende og neste steg.
     *
     * @param avklaringsbehov          avklaringsbehov som er utledet i steget
     * @param nyttGrunnlag             nytt grunnlag som er opprettet i steget
     * @param forrigeGrunnlagFraSteg   forrige grunnlag fra steget
     * @param forrigeGrunnlagFraStegUt forrige grunnlag fra steg ut
     * @return Builder for grunnlag som det skal spoles fram til
     */
    public static Optional<BeregningsgrunnlagGrunnlagDto> finnGrunnlagDetSkalSpolesTil(Collection<BeregningAvklaringsbehovResultat> avklaringsbehov,
                                                                                              BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                                              Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraSteg,
                                                                                              Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraStegUt) {

        boolean kanSpoleFramHeleGrunnlaget = kanKopiereForrigeGrunnlagAvklartIStegUt(
                avklaringsbehov,
                nyttGrunnlag,
                forrigeGrunnlagFraSteg);
        if (kanSpoleFramHeleGrunnlaget) {
            return forrigeGrunnlagFraStegUt;
        } else if (!avklaringsbehov.isEmpty() && forrigeGrunnlagFraSteg.isPresent() && forrigeGrunnlagFraStegUt.isPresent()) {
            return spolFramLikePerioderOmMulig(nyttGrunnlag, forrigeGrunnlagFraSteg.get(), forrigeGrunnlagFraStegUt.get());
        }
        return Optional.empty();
    }


    private static Optional<BeregningsgrunnlagGrunnlagDto> spolFramLikePerioderOmMulig(BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                                              BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg,
                                                                                              BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt) {
        Set<Intervall> intervallerSomKanKopieres = finnPerioderSomKanKopieres(
                nyttGrunnlag.getBeregningsgrunnlag(),
                forrigeGrunnlagFraSteg.getBeregningsgrunnlag());
        int antallPerioderForrigeUt = finnAntallPerioder(forrigeGrunnlagFraStegUt);
        int antallPerioderForrige = finnAntallPerioder(forrigeGrunnlagFraSteg);
        // Antar at antall perioder ikkje endres i frontend, sjekk for å unngå at feil fra tidligere saker tas med videre
        if (intervallerSomKanKopieres.isEmpty() || antallPerioderForrigeUt != antallPerioderForrige) {
            return Optional.empty();
        } else {
            return Optional.of(kopierPerioderFraForrigeGrunnlag(nyttGrunnlag, forrigeGrunnlagFraStegUt, intervallerSomKanKopieres));
        }

    }

    public static BeregningsgrunnlagGrunnlagDto kopierPerioderFraForrigeGrunnlag(BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                                        BeregningsgrunnlagGrunnlagDto eksisterendeGrunnlag,
                                                                                        Set<Intervall> intervallerSomKanKopieres) {
        BeregningsgrunnlagDto nyttBg = nyttGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag om perioder skal kopieres"));
        BeregningsgrunnlagDto.Builder bgBuilder = BeregningsgrunnlagDto.builder(nyttBg).fjernAllePerioder();
        leggTilPerioder(eksisterendeGrunnlag, nyttBg, bgBuilder, intervallerSomKanKopieres);
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(nyttGrunnlag)
                .medBeregningsgrunnlag(bgBuilder.build())
                .build(eksisterendeGrunnlag.getBeregningsgrunnlagTilstand());
    }

    private static int finnAntallPerioder(BeregningsgrunnlagGrunnlagDto eksisterende) {
        return eksisterende.getBeregningsgrunnlag().map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder).orElse(Collections.emptyList()).size();
    }

    private static void leggTilPerioder(BeregningsgrunnlagGrunnlagDto eksisterende,
                                        BeregningsgrunnlagDto nyttBg, BeregningsgrunnlagDto.Builder bgBuilder,
                                        Set<Intervall> intervallerSomKanKopieres) {
        List<BeregningsgrunnlagPeriodeDto> eksisterendePerioder = eksisterende.getBeregningsgrunnlag()
                .stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .collect(Collectors.toList());

        nyttBg.getBeregningsgrunnlagPerioder().forEach(periode -> {
            if (intervallerSomKanKopieres.contains(periode.getPeriode())) {
                bgBuilder.leggTilBeregningsgrunnlagPeriode(finnPeriodeFraEksisterende(eksisterendePerioder,
                        periode.getPeriodeÅrsaker(),
                        periode.getPeriode(),
                        intervallerSomKanKopieres));
            } else {
                bgBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.kopier(periode));
            }
        });
    }

    private static BeregningsgrunnlagPeriodeDto.Builder finnPeriodeFraEksisterende(List<BeregningsgrunnlagPeriodeDto> forrigePerioder,
                                                                                   List<PeriodeÅrsak> nyePeriodeÅrsaker,
                                                                                   Intervall periodeSomSkalKopieres,
                                                                                   Set<Intervall> intervallerSomKanKopieres) {
        var tilstøterEndretIntervall = intervallerSomKanKopieres.stream().anyMatch(p -> periodeSomSkalKopieres.getTomDato().equals(p.getTomDato()) ||
                periodeSomSkalKopieres.getFomDato().equals(p.getFomDato()));
        var forrigePeriode = forrigePerioder.stream()
                .filter(p -> p.getPeriode().equals(periodeSomSkalKopieres))
                .findFirst();

        if (forrigePeriode.isPresent()) {
            var periodeBuilder = BeregningsgrunnlagPeriodeDto.kopier(forrigePeriode.get());
            leggTilManglendePeriodeårsaker(nyePeriodeÅrsaker, forrigePeriode.get(), periodeBuilder, tilstøterEndretIntervall);
            return periodeBuilder;
        }

        forrigePeriode = forrigePerioder.stream()
                .filter(p -> p.getPeriode().inkluderer(periodeSomSkalKopieres))
                .findFirst();

        if (forrigePeriode.isPresent()) {
            var periodeBuilder = BeregningsgrunnlagPeriodeDto.kopier(forrigePeriode.get())
                    .medBeregningsgrunnlagPeriode(periodeSomSkalKopieres.getFomDato(), periodeSomSkalKopieres.getTomDato());
            leggTilManglendePeriodeårsaker(nyePeriodeÅrsaker, forrigePeriode.get(), periodeBuilder, tilstøterEndretIntervall);
            return periodeBuilder;
        }

        throw new IllegalStateException("Fant ikke periode fra forrige grunnlag som inkluderer " + periodeSomSkalKopieres);
    }

    private static void leggTilManglendePeriodeårsaker(List<PeriodeÅrsak> nyePeriodeÅrsaker, BeregningsgrunnlagPeriodeDto forrigePeriode, BeregningsgrunnlagPeriodeDto.Builder periodeBuilder, boolean tilstøterEndretIntervall) {
        if (tilstøterEndretIntervall && !forrigePeriode.getPeriodeÅrsaker().containsAll(nyePeriodeÅrsaker)) {
            nyePeriodeÅrsaker.stream().filter(p -> !forrigePeriode.getPeriodeÅrsaker().contains(p))
                    .forEach(periodeBuilder::leggTilPeriodeÅrsak);
        }
    }


}
