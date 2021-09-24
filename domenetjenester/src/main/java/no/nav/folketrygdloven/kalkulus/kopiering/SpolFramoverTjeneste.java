package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.kopiering.KanKopierBeregningsgrunnlag.finnPerioderSomKanKopieres;
import static no.nav.folketrygdloven.kalkulus.kopiering.KanKopierBeregningsgrunnlag.kanKopiereForrigeGrunnlagAvklartIStegUt;

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
    public static Optional<BeregningsgrunnlagGrunnlagDtoBuilder> finnGrunnlagDetSkalSpolesTil(List<BeregningAvklaringsbehovResultat> avklaringsbehov,
                                                                                              BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                                              Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraSteg,
                                                                                              Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraStegUt) {

        boolean kanSpoleFramHeleGrunnlaget = kanKopiereForrigeGrunnlagAvklartIStegUt(
                avklaringsbehov,
                nyttGrunnlag,
                forrigeGrunnlagFraSteg);
        if (kanSpoleFramHeleGrunnlaget) {
            return forrigeGrunnlagFraStegUt.map(BeregningsgrunnlagGrunnlagDtoBuilder::oppdatere);
        } else if (!avklaringsbehov.isEmpty() && forrigeGrunnlagFraSteg.isPresent() && forrigeGrunnlagFraStegUt.isPresent()) {
            return spolFramLikePerioderOmMulig(nyttGrunnlag, forrigeGrunnlagFraSteg.get(), forrigeGrunnlagFraStegUt.get());
        }
        return Optional.empty();
    }


    private static Optional<BeregningsgrunnlagGrunnlagDtoBuilder> spolFramLikePerioderOmMulig(BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
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
            BeregningsgrunnlagDto nyttBg = nyttGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag om perioder skal kopieres"));
            BeregningsgrunnlagDto.Builder bgBuilder = BeregningsgrunnlagDto.builder(nyttBg).fjernAllePerioder();
            leggTilPerioder(forrigeGrunnlagFraStegUt, intervallerSomKanKopieres, nyttBg, bgBuilder);
            var stegUtGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(nyttGrunnlag)
                    .medBeregningsgrunnlag(bgBuilder.build());
            return Optional.of(stegUtGrunnlag);
        }

    }

    private static int finnAntallPerioder(BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt) {
        return forrigeGrunnlagFraStegUt.getBeregningsgrunnlag().map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder).orElse(Collections.emptyList()).size();
    }

    private static void leggTilPerioder(BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt, Set<Intervall> intervallerSomKanKopieres, BeregningsgrunnlagDto nyttBg, BeregningsgrunnlagDto.Builder bgBuilder) {
        List<BeregningsgrunnlagPeriodeDto> forrigePerioder = forrigeGrunnlagFraStegUt.getBeregningsgrunnlag()
                .stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .collect(Collectors.toList());

        nyttBg.getBeregningsgrunnlagPerioder().forEach(periode -> {
            if (intervallerSomKanKopieres.contains(periode.getPeriode())) {
                BeregningsgrunnlagPeriodeDto forrigePeriode = forrigePerioder.stream()
                        .filter(p -> p.getPeriode().equals(periode.getPeriode()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Fant ikke periode som skulle kopieres i forrige grunnlag"));
                bgBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.builder(forrigePeriode));
            } else {
                bgBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.builder(periode));
            }
        });
    }


}
