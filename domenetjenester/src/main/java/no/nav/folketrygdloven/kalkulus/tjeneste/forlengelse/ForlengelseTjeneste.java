package no.nav.folketrygdloven.kalkulus.tjeneste.forlengelse;

import static java.lang.Boolean.TRUE;
import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.forlengelse.ForlengelseperiodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.forlengelse.ForlengelseperioderEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kopiering.BeregningsgrunnlagDiffSjekker;
import no.nav.folketrygdloven.kalkulus.kopiering.SpolFramoverTjeneste;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregnForRequest;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

@ApplicationScoped
public class ForlengelseTjeneste {

    private ForlengelseRepository forlengelseRepository;
    private Logger logger = LoggerFactory.getLogger(ForlengelseTjeneste.class);

    public ForlengelseTjeneste() {
    }

    @Inject
    public ForlengelseTjeneste(ForlengelseRepository forlengelseRepository) {
        this.forlengelseRepository = forlengelseRepository;
    }

    public Map<Long, Boolean> erForlengelser(Set<Long> koblingIder) {
        var forlengelser = forlengelseRepository.hentAktivePerioderForKoblingId(koblingIder);
        return koblingIder.stream()
                .collect(Collectors.toMap(Function.identity(),
                        k -> forlengelser.stream().anyMatch(f -> f.getKoblingId().equals(k) && !f.getForlengelseperioder().isEmpty())));
    }

    /**
     * Forlenger eksisterende beregningsgrunnlag
     * <p>
     * Danner et nytt beregnningsgrunnlag som er kombinasjon mellom eksisterende og nytt grunnlag.
     * Innenfor forlengelseperiodene er grunnlaget lik nyttGrunnlag, ellers er det kopi av eksisterende.
     *
     * @param forlengelseperioder  Perioder for forlengelse/viderbehandling
     * @param nyttGrunnlag         Nytt grunnlag
     * @param eksisterendeGrunnlag Forrige grunnlag produsert i samme steg som nyttGrunnlag
     * @return Forlenget grunnlag
     */
    public BeregningsgrunnlagGrunnlagDto forlengEksisterendeBeregningsgrunnlag(List<Intervall> forlengelseperioder,
                                                                               BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                               Optional<BeregningsgrunnlagGrunnlagDto> eksisterendeGrunnlag) {
        var forlengetGrunnlag = kopierPerioderUtenforForlengelse(
                nyttGrunnlag,
                eksisterendeGrunnlag,
                forlengelseperioder);

        // Validerer ingen endring utenfor forlengelse
        forlengetGrunnlag.getBeregningsgrunnlagHvisFinnes().ifPresent(bg ->
                validerIngenEndringUtenforForlengelse(
                        bg,
                        eksisterendeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes),
                        forlengelseperioder)
        );
        return forlengetGrunnlag;
    }

    private BeregningsgrunnlagGrunnlagDto kopierPerioderUtenforForlengelse(BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                                           Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlagFraStegOpt,
                                                                           List<Intervall> forlengelseperioder) {

        if (forlengelseperioder.isEmpty() || forrigeGrunnlagFraStegOpt.isEmpty() || forrigeGrunnlagFraStegOpt.get().getBeregningsgrunnlagHvisFinnes().isEmpty()) {
            return nyttGrunnlag;
        }

        var forrigeGrunnlag = forrigeGrunnlagFraStegOpt.get();
        var forrigeBeregningsgrunnlag = forrigeGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow();
        var skjæringstidspunkt = forrigeBeregningsgrunnlag.getSkjæringstidspunkt();
        var bgTidslinje = new LocalDateTimeline<>(skjæringstidspunkt, TIDENES_ENDE, TRUE);
        var forlengelseTidslinje = new LocalDateTimeline<>(forlengelseperioder.stream().map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), TRUE)).toList());
        var tidslinjeUtenForlengelse = bgTidslinje.disjoint(forlengelseTidslinje);
        var intervallerSomKanKopieres = tidslinjeUtenForlengelse.compress().toSegments().stream().map(Intervall::fraSegment).collect(Collectors.toSet());

        logger.info("Kopierer perioder fra eksisterende grunnlag: " + intervallerSomKanKopieres);

        return SpolFramoverTjeneste.kopierPerioderFraForrigeGrunnlag(nyttGrunnlag, forrigeGrunnlag, intervallerSomKanKopieres, false);
    }

    public void lagrePerioderForForlengelse(BeregningSteg steg, List<BeregnForRequest> beregnForListe, List<KoblingEntitet> koblinger) {
        var requesterMedForlengelse = beregnForListe.stream()
                .filter(r -> r.getForlengelsePerioder() != null && !r.getForlengelsePerioder().isEmpty())
                .toList();
        var forlengelser = requesterMedForlengelse
                .stream()
                .map(r -> {
                    var perioder = r.getForlengelsePerioder().stream().map(p -> new ForlengelseperiodeEntitet(IntervallEntitet.fraOgMedTilOgMed(p.getFom(), p.getTom()))).toList();
                    return new ForlengelseperioderEntitet(finnKobligForRequest(koblinger, r).getId(), perioder);
                }).toList();
        if (steg.erEtter(BeregningSteg.VURDER_REF_BERGRUNN)) {
            validerIngenEndringer(steg, koblinger, requesterMedForlengelse, forlengelser);
        } else {
            logger.info("Oppretter perioder med forlengelse " + forlengelser);
            forlengelseRepository.lagre(forlengelser);
        }
    }

    private void validerIngenEndringer(BeregningSteg steg, List<KoblingEntitet> koblinger, List<BeregnForRequest> requesterMedForlengelse, List<ForlengelseperioderEntitet> forlengelser) {
        var koblingIderMedForlengelse = requesterMedForlengelse.stream().map(r -> finnKobligForRequest(koblinger, r)).map(KoblingEntitet::getId).collect(Collectors.toSet());
        var eksisterendeForlengelser = forlengelseRepository.hentAktivePerioderForKoblingId(koblingIderMedForlengelse);
        forlengelser.forEach(f -> validerIngenEndringForForlengelse(steg, eksisterendeForlengelser, f));
    }

    private void validerIngenEndringForForlengelse(BeregningSteg steg, List<ForlengelseperioderEntitet> eksisterendeForlengelser, ForlengelseperioderEntitet f) {


        var aktivForlengelse = eksisterendeForlengelser.stream().filter(e -> e.getKoblingId().equals(f.getKoblingId())).findFirst();

        var sorterteNyeForlengelser = f.getForlengelseperioder().stream()
                .map(ForlengelseperiodeEntitet::getPeriode).sorted(Comparator.naturalOrder())
                .toList();

        var sorterteEksisterendeForlengelser = aktivForlengelse.stream()
                .flatMap(it -> it.getForlengelseperioder().stream())
                .map(ForlengelseperiodeEntitet::getPeriode).sorted(Comparator.naturalOrder()).toList();


        if (KonfigurasjonVerdi.instance().get("SKAL_VALIDERE_FORLENGELSE_ENDRING", false)) {
            if (aktivForlengelse.isEmpty()) {
                throw new IllegalStateException("Kan ikke lagre ny periode med forlengelse i steg " + steg);
            }


            if (sorterteNyeForlengelser.size() != sorterteEksisterendeForlengelser.size()) {
                throw new IllegalStateException("Kan ikke ha endring i forlengelse i steg " + steg);
            }
            var nyIterator = sorterteNyeForlengelser.iterator();
            var eksisterendeIterator = sorterteEksisterendeForlengelser.iterator();
            while (nyIterator.hasNext()) {
                if (!nyIterator.next().equals(eksisterendeIterator.next())) {
                    throw new IllegalStateException("Kan ikke ha endring i forlengelse i steg " + steg);
                }
            }
        } else {
            if (aktivForlengelse.isPresent()) {
                var alleNyeErSubsetAvGammel = sorterteNyeForlengelser.stream().allMatch(p -> sorterteEksisterendeForlengelser.stream().anyMatch(it -> !it.getFomDato().isAfter(p.getFomDato()) && !it.getTomDato().isBefore(p.getTomDato())));
                if (!alleNyeErSubsetAvGammel) {
                    throw new IllegalStateException("Alle nye forlengelser skal vere subset av eksisterende. Fikk " + sorterteNyeForlengelser + " og eksisterende er " + sorterteEksisterendeForlengelser);
                }
            }

        }
    }

    private KoblingEntitet finnKobligForRequest(List<KoblingEntitet> koblinger, BeregnForRequest r) {
        return koblinger.stream().filter(k -> k.getKoblingReferanse().getReferanse().equals(r.getEksternReferanse())).findFirst().orElseThrow(() -> new IllegalStateException("Forventer å finne kobling"));
    }

    private void validerIngenEndringUtenforForlengelse(BeregningsgrunnlagDto nyttBg,
                                                       Optional<BeregningsgrunnlagDto> forrigeBg,
                                                       List<Intervall> forlengelseperioder) {
        if (forlengelseperioder.isEmpty() || forrigeBg.isEmpty()) {
            return;
        }
        LocalDateTimeline<Boolean> noDiffTimeline = finnTidslinjeUtenDifferanse(nyttBg, forrigeBg);
        LocalDateTimeline<Boolean> førForlengelseTimeline = finnTidslinjeFørForlengelse(nyttBg, forlengelseperioder);
        var tidslinjeMedDiff = førForlengelseTimeline.disjoint(noDiffTimeline);
        boolean harDiffUtenforForlengelse = !tidslinjeMedDiff.isEmpty();
        if (harDiffUtenforForlengelse && !erKunHelg(tidslinjeMedDiff)) {
            logger.info("Perioder med diff:" + tidslinjeMedDiff.toSegments().stream().map(LocalDateSegment::getLocalDateInterval).toList());
            loggPerioderMedDiff(nyttBg, forrigeBg.get(), tidslinjeMedDiff);
            throw new IllegalStateException("Fant differanse i beregnet grunnlag utenfor oppgitt periode for forlengelse. Skjæringstidspunkt: " + nyttBg.getSkjæringstidspunkt() + " Forlengelseperioder: " + forlengelseperioder);
        }
    }

    private void loggPerioderMedDiff(BeregningsgrunnlagDto nyttBg, BeregningsgrunnlagDto forrigeBg, LocalDateTimeline<Boolean> tidslinjeMedDiff) {
        var nyTidslinje = nyttBg.getBeregningsgrunnlagPerioder().stream()
                .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), p))
                .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));
        var forrigeTidslinje = forrigeBg.getBeregningsgrunnlagPerioder().stream()
                .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), p))
                .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));
        var diff = nyTidslinje.intersection(forrigeTidslinje, (di, lhs, rhs) -> new LocalDateSegment<>(di, BeregningsgrunnlagDiffSjekker.getDiff(lhs.getValue(), rhs.getValue()).getLeafDifferences()))
                .intersection(tidslinjeMedDiff, StandardCombinators::leftOnly);

        diff.toSegments().forEach(s -> {
            logger.info("Periode: " + s.getLocalDateInterval() + " Diff: " + s.getValue());
        });
    }

    private boolean erKunHelg(LocalDateTimeline<Boolean> tidslinjeMedDiff) {
        return tidslinjeMedDiff.getLocalDateIntervals().stream().allMatch(this::erHelg);
    }

    private boolean erHelg(LocalDateInterval p) {
        return (p.getFomDato().getDayOfWeek().equals(DayOfWeek.SATURDAY) || p.getFomDato().getDayOfWeek().equals(DayOfWeek.SUNDAY)) &&
                (p.getTomDato().getDayOfWeek().equals(DayOfWeek.SATURDAY) || p.getTomDato().getDayOfWeek().equals(DayOfWeek.SUNDAY)) &&
                p.days() <= 2;
    }

    private LocalDateTimeline<Boolean> finnTidslinjeFørForlengelse(BeregningsgrunnlagDto nyttBg, List<Intervall> forlengelseperioder) {
        var førsteDagMedForlengelse = forlengelseperioder.stream().map(Intervall::getFomDato)
                .min(Comparator.naturalOrder()).orElse(TIDENES_ENDE);
        return nyttBg.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode)
                .filter(p -> p.getTomDato().isBefore(førsteDagMedForlengelse))
                .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))
                .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));
    }

    private LocalDateTimeline<Boolean> finnTidslinjeUtenDifferanse(BeregningsgrunnlagDto nyttBg, Optional<BeregningsgrunnlagDto> forrigeBg) {
        var perioderUtenDiff = BeregningsgrunnlagDiffSjekker.finnPerioderUtenDiff(nyttBg, forrigeBg.get())
                .stream()
                .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(perioderUtenDiff);
    }

    public void deaktiverVedTilbakerulling(Set<Long> koblingIder, BeregningsgrunnlagTilstand tilstand) {
        if (tilstand.erFør(BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT)) {
            logger.info("Deaktiverer forlengelseperioder for koblinger " + koblingIder);
            forlengelseRepository.deaktiverAlle(koblingIder);
        }
    }
}
