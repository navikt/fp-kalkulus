package no.nav.folketrygdloven.kalkulus.kopiering;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;
import static no.nav.folketrygdloven.kalkulus.kopiering.KanKopierBeregningsgrunnlag.finnPerioderSomKanKopieres;
import static no.nav.folketrygdloven.kalkulus.kopiering.KanKopierBeregningsgrunnlag.kanKopiereForrigeGrunnlagAvklartIStegUt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger LOG = LoggerFactory.getLogger(SpolFramoverTjeneste.class);

    private SpolFramoverTjeneste() {
        // Skjul
    }


    /**
     * Spoler grunnlaget framover en tilstand om dette er mulig.
     * Spolingen kopierer hele eller deler av grunnlaget som er lagret ved håndtering av aksjonspunkter mellom inneværende og neste steg.
     *
     * @param avklaringsbehov          avklaringsbehov som er utledet i steget
     * @param forlengelseperioder
     * @param nyttGrunnlag             nytt grunnlag som er opprettet i steget
     * @param forrigeGrunnlagFraSteg   forrige grunnlag fra steget
     * @param forrigeGrunnlagFraStegUt forrige grunnlag fra steg ut
     * @return Builder for grunnlag som det skal spoles fram til
     */
    public static Optional<BeregningsgrunnlagGrunnlagDto> finnGrunnlagDetSkalSpolesTil(Collection<BeregningAvklaringsbehovResultat> avklaringsbehov,
                                                                                       List<Intervall> forlengelseperioder, BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                                       Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraSteg,
                                                                                       Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraStegUt) {

        boolean kanSpoleFramHeleGrunnlaget = kanKopiereForrigeGrunnlagAvklartIStegUt(
                avklaringsbehov,
                nyttGrunnlag,
                forrigeGrunnlagFraSteg);
        if (kanSpoleFramHeleGrunnlaget) {
            return forrigeGrunnlagFraStegUt;
        }
        Optional<BeregningsgrunnlagGrunnlagDto> spoltGrunnlag = Optional.empty();
        if (!avklaringsbehov.isEmpty() && forrigeGrunnlagFraSteg.isPresent() && forrigeGrunnlagFraStegUt.isPresent()) {
            spoltGrunnlag = spolFramLikePerioderOmMulig(nyttGrunnlag, forrigeGrunnlagFraSteg.get(), forrigeGrunnlagFraStegUt.get());
        }
        if (!forlengelseperioder.isEmpty() && forrigeGrunnlagFraSteg.isPresent() && forrigeGrunnlagFraStegUt.isPresent()) {
            var perioderSomKopieres = finnPerioderUtenforForlengelseperioder(forlengelseperioder, nyttGrunnlag);
            LOG.info("Kopierer intervaller for manuelt avklart grunnlag ved forlengelse: " + perioderSomKopieres);
            spoltGrunnlag = Optional.of(kopierPerioderFraForrigeGrunnlag(spoltGrunnlag.orElse(nyttGrunnlag), forrigeGrunnlagFraStegUt.get(), perioderSomKopieres, true));
        }
        return spoltGrunnlag;
    }

    private static Set<Intervall> finnPerioderUtenforForlengelseperioder(List<Intervall> forlengelseperioder, BeregningsgrunnlagGrunnlagDto grunnlag) {
        return grunnlag.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()
                .stream().map(BeregningsgrunnlagPeriodeDto::getPeriode)
                .filter(p -> forlengelseperioder.stream().noneMatch(fp -> fp.overlapper(p)))
                .collect(Collectors.toSet());
    }

    private static Optional<BeregningsgrunnlagGrunnlagDto> spolFramLikePerioderOmMulig(BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                                       BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg,
                                                                                       BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt) {
        Set<Intervall> intervallerSomKanKopieres = finnPerioderSomKanKopieres(
                nyttGrunnlag.getBeregningsgrunnlagHvisFinnes(),
                forrigeGrunnlagFraSteg.getBeregningsgrunnlagHvisFinnes());
        if (intervallerSomKanKopieres.isEmpty()) {
            return Optional.empty();
        } else {
            LOG.info("Kopierer intervaller for manuelt avklart grunnlag: " + intervallerSomKanKopieres);
            return Optional.of(kopierPerioderFraForrigeGrunnlag(nyttGrunnlag, forrigeGrunnlagFraStegUt, intervallerSomKanKopieres, true));
        }

    }

    public static BeregningsgrunnlagGrunnlagDto kopierPerioderFraForrigeGrunnlag(BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                                 BeregningsgrunnlagGrunnlagDto eksisterendeGrunnlag,
                                                                                 Set<Intervall> intervallerSomKanKopieres, boolean skalBeholdePeriodisering) {
        BeregningsgrunnlagDto nyttBg = nyttGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag om perioder skal kopieres"));
        var medKopiertePerioder = leggTilPerioder(eksisterendeGrunnlag, nyttBg, intervallerSomKanKopieres, skalBeholdePeriodisering);
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(nyttGrunnlag)
                .medBeregningsgrunnlag(medKopiertePerioder)
                .build(eksisterendeGrunnlag.getBeregningsgrunnlagTilstand());
    }

    private static BeregningsgrunnlagDto leggTilPerioder(BeregningsgrunnlagGrunnlagDto eksisterende,
                                                         BeregningsgrunnlagDto nyttBg,
                                                         Set<Intervall> intervallerSomKanKopieres,
                                                         boolean skalBeholdePeriodisering) {
        List<BeregningsgrunnlagPeriodeDto> eksisterendePerioder = eksisterende.getBeregningsgrunnlagHvisFinnes()
                .stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .collect(Collectors.toList());
        LocalDateTimeline<BeregningsgrunnlagPeriodeDto> tidslinjeFraStegUt = lagTidslinjeFraPerioder(eksisterendePerioder)
                .intersection(tidslinjeFra(intervallerSomKanKopieres));
        SplittPeriodeConfig<BeregningsgrunnlagPeriodeDto> splittConfig = new SplittPeriodeConfig<>(kopiCombinator(intervallerSomKanKopieres));
        if (skalBeholdePeriodisering) {
            splittConfig.setLikhetsPredikatForCompress(StandardPeriodeCompressLikhetspredikat::aldriKomprimer);
        } else {
            splittConfig.setLikhetsPredikatForCompress(SpolFramoverTjeneste::erLike);
            splittConfig.setAbutsPredikatForCompress((d1, d2) -> d1.abuts(d2) &&
                    skalKopieres(intervallerSomKanKopieres, d1) &&
                    skalKopieres(intervallerSomKanKopieres, d2));
        }
        var splitter = new PeriodeSplitter<>(splittConfig);
        return splitter.splittPerioder(nyttBg, tidslinjeFraStegUt);
    }

    private static boolean skalKopieres(Set<Intervall> intervallerSomKanKopieres, LocalDateInterval d1) {
        return intervallerSomKanKopieres.stream().anyMatch(p -> p.overlapper(Intervall.fra(d1)));
    }

    private static LocalDateTimeline<Boolean> tidslinjeFra(Set<Intervall> intervallerSomKanKopieres) {
        return new LocalDateTimeline<>(intervallerSomKanKopieres.stream().map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), Boolean.TRUE)).toList());
    }

    private static boolean erLike(BeregningsgrunnlagPeriodeDto dtoObject1, BeregningsgrunnlagPeriodeDto dtoObject2) {
        return BeregningsgrunnlagDiffSjekker.getDiff(dtoObject1, dtoObject2).isEmpty();
    }

    private static LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, BeregningsgrunnlagPeriodeDto, BeregningsgrunnlagPeriodeDto> kopiCombinator(Set<Intervall> intervallerSomKanKopieres) {
        return (datoInterval, lhs, rhs) -> kopierRhs(intervallerSomKanKopieres, datoInterval, lhs, rhs);
    }

    private static LocalDateSegment<BeregningsgrunnlagPeriodeDto> kopierRhs(Set<Intervall> intervallerSomKanKopieres, LocalDateInterval datoInterval, LocalDateSegment<BeregningsgrunnlagPeriodeDto> lhs, LocalDateSegment<BeregningsgrunnlagPeriodeDto> rhs) {
        if (lhs == null) {
            throw new IllegalStateException("Forventer at lhs alltid er definert");
        }
        if (rhs == null) {
            return new LocalDateSegment<>(datoInterval, BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                    .medBeregningsgrunnlagPeriode(datoInterval.getFomDato(), datoInterval.getTomDato())
                    .build()
            );
        }
        var tilstøterEndretIntervall = tilstøterSegmentSomIkkeKopieres(intervallerSomKanKopieres, datoInterval);
        var periodeBuilder = BeregningsgrunnlagPeriodeDto.kopier(rhs.getValue())
                .medBeregningsgrunnlagPeriode(datoInterval.getFomDato(), datoInterval.getTomDato());
        leggTilManglendePeriodeårsaker(lhs.getValue().getPeriodeÅrsaker(), rhs.getValue(), periodeBuilder, tilstøterEndretIntervall);
        return new LocalDateSegment<>(datoInterval, periodeBuilder.build());
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
        return eksisterendePerioder.stream()
                .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), p))
                .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));
    }

    private static void leggTilManglendePeriodeårsaker(List<PeriodeÅrsak> nyePeriodeÅrsaker, BeregningsgrunnlagPeriodeDto forrigePeriode, BeregningsgrunnlagPeriodeDto.Builder periodeBuilder, boolean tilstøterEndretIntervall) {
        if (tilstøterEndretIntervall && !forrigePeriode.getPeriodeÅrsaker().containsAll(nyePeriodeÅrsaker)) {
            nyePeriodeÅrsaker.stream().filter(p -> !forrigePeriode.getPeriodeÅrsaker().contains(p))
                    .forEach(periodeBuilder::leggTilPeriodeÅrsak);
        }
    }


}
