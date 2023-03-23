package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static no.nav.folketrygdloven.kalkulus.kopiering.KanKopierBeregningsgrunnlag.finnPerioderSomKanKopieres;
import static no.nav.folketrygdloven.kalkulus.kopiering.KanKopierBeregningsgrunnlag.kanKopiereForrigeGrunnlagAvklartIStegUt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.PeriodeSplitter;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.SplittPeriodeConfig;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.StandardPeriodeCompressLikhetspredikat;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

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
        if (intervallerSomKanKopieres.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(kopierPerioderFraForrigeGrunnlag(nyttGrunnlag, forrigeGrunnlagFraStegUt, intervallerSomKanKopieres));
        }

    }

    public static BeregningsgrunnlagGrunnlagDto kopierPerioderFraForrigeGrunnlag(BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                                 BeregningsgrunnlagGrunnlagDto eksisterendeGrunnlag,
                                                                                 Set<Intervall> intervallerSomKanKopieres) {
        BeregningsgrunnlagDto nyttBg = nyttGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag om perioder skal kopieres"));
        var medKopiertePerioder = leggTilPerioder(eksisterendeGrunnlag, nyttBg, intervallerSomKanKopieres);
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(nyttGrunnlag)
                .medBeregningsgrunnlag(medKopiertePerioder)
                .build(eksisterendeGrunnlag.getBeregningsgrunnlagTilstand());
    }

    private static BeregningsgrunnlagDto leggTilPerioder(BeregningsgrunnlagGrunnlagDto eksisterende,
                                                         BeregningsgrunnlagDto nyttBg,
                                                         Set<Intervall> intervallerSomKanKopieres) {
        List<BeregningsgrunnlagPeriodeDto> eksisterendePerioder = eksisterende.getBeregningsgrunnlag()
                .stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .collect(Collectors.toList());
        LocalDateTimeline<BeregningsgrunnlagPeriodeDto> tidslinjeFraStegUt = lagTidslinjeFraPerioder(eksisterendePerioder);
        SplittPeriodeConfig<BeregningsgrunnlagPeriodeDto> splittConfig = new SplittPeriodeConfig<>(kopiCombinator(intervallerSomKanKopieres));
        splittConfig.setLikhetsPredikatForCompress(StandardPeriodeCompressLikhetspredikat::aldriKomprimer);
        var splitter = new PeriodeSplitter<>(splittConfig);
        return splitter.splittPerioder(nyttBg, tidslinjeFraStegUt);
    }

    private static LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, BeregningsgrunnlagPeriodeDto, BeregningsgrunnlagPeriodeDto> kopiCombinator(Set<Intervall> intervallerSomKanKopieres) {
        return (datoInterval, lhs, rhs) -> kopierRhsDersomSegmentOverlapperIntervallSomKopieres(intervallerSomKanKopieres, datoInterval, lhs, rhs);
    }

    private static LocalDateSegment<BeregningsgrunnlagPeriodeDto> kopierRhsDersomSegmentOverlapperIntervallSomKopieres(Set<Intervall> intervallerSomKanKopieres, LocalDateInterval datoInterval, LocalDateSegment<BeregningsgrunnlagPeriodeDto> lhs, LocalDateSegment<BeregningsgrunnlagPeriodeDto> rhs) {
        if (lhs == null || rhs == null) {
            throw new IllegalStateException("Forventer at alle perioder overlapper");
        }
        var periode = Intervall.fraOgMedTilOgMed(datoInterval.getFomDato(), datoInterval.getTomDato());
        if (intervallerSomKanKopieres.stream().anyMatch(it -> it.inkluderer(periode))) {
            var tilstøterEndretIntervall = tilstøterSegmentSomIkkeKopieres(intervallerSomKanKopieres, datoInterval);
            var periodeBuilder = BeregningsgrunnlagPeriodeDto.kopier(rhs.getValue())
                    .medBeregningsgrunnlagPeriode(datoInterval.getFomDato(), datoInterval.getTomDato());
            leggTilManglendePeriodeårsaker(lhs.getValue().getPeriodeÅrsaker(), rhs.getValue(), periodeBuilder, tilstøterEndretIntervall);
            return new LocalDateSegment<>(datoInterval, periodeBuilder.build());

        }
        return new LocalDateSegment<>(datoInterval, BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                .medBeregningsgrunnlagPeriode(datoInterval.getFomDato(), datoInterval.getTomDato())
                .build()
        );
    }

    private static boolean tilstøterSegmentSomIkkeKopieres(Set<Intervall> intervallerSomKanKopieres, LocalDateInterval datoInterval) {
        return intervallerSomKanKopieres.stream().noneMatch(p -> inkludererForrige(datoInterval, p) || inkludererNeste(datoInterval, p));
    }

    private static boolean inkludererNeste(LocalDateInterval datoInterval, Intervall p) {
        return !datoInterval.getTomDato().equals(TIDENES_ENDE) && p.inkluderer(datoInterval.getTomDato().plusDays(1));
    }

    private static boolean inkludererForrige(LocalDateInterval datoInterval, Intervall p) {
        return p.inkluderer(datoInterval.getFomDato().minusDays(1));
    }

    private static LocalDateTimeline<BeregningsgrunnlagPeriodeDto> lagTidslinjeFraPerioder(List<BeregningsgrunnlagPeriodeDto> eksisterendePerioder) {
        var eksisterendeSegmenter = eksisterendePerioder.stream()
                .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), p))
                .toList();
        return new LocalDateTimeline<>(eksisterendeSegmenter);
    }

    private static void leggTilManglendePeriodeårsaker(List<PeriodeÅrsak> nyePeriodeÅrsaker, BeregningsgrunnlagPeriodeDto forrigePeriode, BeregningsgrunnlagPeriodeDto.Builder periodeBuilder, boolean tilstøterEndretIntervall) {
        if (tilstøterEndretIntervall && !forrigePeriode.getPeriodeÅrsaker().containsAll(nyePeriodeÅrsaker)) {
            nyePeriodeÅrsaker.stream().filter(p -> !forrigePeriode.getPeriodeÅrsaker().contains(p))
                    .forEach(periodeBuilder::leggTilPeriodeÅrsak);
        }
    }


}
