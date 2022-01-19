package no.nav.folketrygdloven.kalkulus.tjeneste.forlengelse;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.forlengelse.ForlengelseperiodeEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.forlengelse.ForlengelseperioderEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.jpa.IntervallEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kopiering.BeregningsgrunnlagDiffSjekker;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregnForRequest;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
public class ForlengelseTjeneste {

    private ForlengelseRepository forlengelseRepository;
    private boolean skalVurdereForlengelse;

    public ForlengelseTjeneste() {
    }

    @Inject
    public ForlengelseTjeneste(ForlengelseRepository forlengelseRepository, @KonfigVerdi(value = "SKAL_VURDERE_FORLENGELSE", defaultVerdi = "false", required = false) boolean skalVurdereForlengelse) {
        this.forlengelseRepository = forlengelseRepository;
        this.skalVurdereForlengelse = skalVurdereForlengelse;
    }

    public void lagrePerioderForForlengelse(BeregningSteg steg, List<BeregnForRequest> beregnForListe, List<KoblingEntitet> koblinger) {
        if (skalVurdereForlengelse) {
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
                forlengelseRepository.lagre(forlengelser);
            }
        }
    }

    private void validerIngenEndringer(BeregningSteg steg, List<KoblingEntitet> koblinger, List<BeregnForRequest> requesterMedForlengelse, List<ForlengelseperioderEntitet> forlengelser) {
        var koblingIderMedForlengelse = requesterMedForlengelse.stream().map(r -> finnKobligForRequest(koblinger, r)).map(KoblingEntitet::getId).collect(Collectors.toSet());
        var eksisterendeForlengelser = forlengelseRepository.hentAktivePerioderForKoblingId(koblingIderMedForlengelse);
        forlengelser.forEach(f -> {
            validerIngenEndringForForlengelse(steg, eksisterendeForlengelser, f);
        });
    }

    private void validerIngenEndringForForlengelse(BeregningSteg steg, List<ForlengelseperioderEntitet> eksisterendeForlengelser, ForlengelseperioderEntitet f) {
        var aktivForlengelse = eksisterendeForlengelser.stream().filter(e -> e.getKoblingId().equals(f.getKoblingId())).findFirst();
        if (aktivForlengelse.isEmpty()) {
            throw new IllegalStateException("Kan ikke lagre ny periode med forlengelse i steg " + steg);
        }
        var sorterteNyeForlengelser = f.getForlengelseperioder().stream()
                .map(ForlengelseperiodeEntitet::getPeriode).sorted(Comparator.naturalOrder())
                .toList();
        var sorterteEksisterendeForlengelser = aktivForlengelse.get().getForlengelseperioder().stream()
                .map(ForlengelseperiodeEntitet::getPeriode).sorted(Comparator.naturalOrder()).toList();
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
    }

    private KoblingEntitet finnKobligForRequest(List<KoblingEntitet> koblinger, BeregnForRequest r) {
        return koblinger.stream().filter(k -> k.getKoblingReferanse().getReferanse().equals(r.getEksternReferanse())).findFirst().orElseThrow(() -> new IllegalStateException("Forventer å finne kobling"));
    }

    public void validerIngenEndringUtenforForlengelse(StegProsesseringInput input, BeregningsgrunnlagDto nyttBg, Optional<BeregningsgrunnlagDto> forrigeBg) {
        if (!skalVurdereForlengelse) {
            return;
        }
        if (input.getForlengelseperioder().isEmpty()) {
            return;
        }
        if (forrigeBg.isEmpty()) {
            throw new IllegalStateException("Forventer å finne forrige grunnlag ved forlengelse");
        }
        LocalDateTimeline<Boolean> noDiffTimeline = finnTidslinjeUtenDifferanse(nyttBg, forrigeBg);
        LocalDateTimeline<Boolean> førForlengelseTimeline = finnTidslinjeFørForlengelse(nyttBg, input);
        boolean harDiffUtenforForlengelse = !førForlengelseTimeline.disjoint(noDiffTimeline).isEmpty();
        if (harDiffUtenforForlengelse) {
            throw new IllegalStateException("Fant differanse i beregnet grunnlag utenfor oppgitt periode for forlengelse.");
        }
    }

    private LocalDateTimeline<Boolean> finnTidslinjeFørForlengelse(BeregningsgrunnlagDto nyttBg, BeregningsgrunnlagInput input) {
        var førsteDagMedForlengelse = input.getForlengelseperioder().stream().map(Intervall::getFomDato)
                .min(Comparator.naturalOrder()).orElse(TIDENES_ENDE);
        var perioderFørForlengelse = nyttBg.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode)
                .filter(p -> p.getTomDato().isBefore(førsteDagMedForlengelse))
                .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(perioderFørForlengelse);
    }

    private LocalDateTimeline<Boolean> finnTidslinjeUtenDifferanse(BeregningsgrunnlagDto nyttBg, Optional<BeregningsgrunnlagDto> forrigeBg) {
        var perioderUtenDiff = BeregningsgrunnlagDiffSjekker.finnPerioderUtenDiff(nyttBg, forrigeBg.get())
                .stream()
                .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(perioderUtenDiff);
    }

    public void deaktiverVedTilbakerulling(Set<Long> koblingIder, BeregningsgrunnlagTilstand tilstand) {
        if (skalVurdereForlengelse) {
            if (tilstand.erFør(BeregningsgrunnlagTilstand.VURDERT_REFUSJON)) {
                forlengelseRepository.deaktiverAlle(koblingIder);
            }
        }
    }
}
