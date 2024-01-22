package no.nav.folketrygdloven.kalkulus.kopiering;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.request.v1.KopierBeregningRequest;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

@ApplicationScoped
public class KopierBeregningsgrunnlagTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;

    public KopierBeregningsgrunnlagTjeneste() {
    }

    @Inject
    public KopierBeregningsgrunnlagTjeneste(KoblingTjeneste koblingTjeneste,
                                            BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                            AvklaringsbehovTjeneste avklaringsbehovTjeneste, KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
    }


    /**
     * Oppretter nye koblinger og kopierer data fra eksisterende kobling
     * <p>
     * Tilstand for grunnlaget som kopieres er VURDERT_VILKÅR.
     * Dersom det ikke finnes grunnlag med denne tilstanden kopieres grunnlag fra foreslå (FORSLÅTT_UT eller FORSLÅTT)
     *  @param kopiRequests Referanser som skal kopieres
     * @param ytelseType   Ytelsetype
     * @param saksnummer   Saksnummer
     * @param steg         Definerer steget som vi kopierer beregningsgrunnlag fra
     * @param opprettetTidMax Opprettet tid max
     */
    public void kopierGrunnlagOgOpprettKoblinger(List<KopierBeregningRequest> kopiRequests,
                                                 FagsakYtelseType ytelseType,
                                                 Saksnummer saksnummer, BeregningSteg steg, LocalDateTime opprettetTidMax) {
        var eksisterendeKoblinger = finnEksisterendeKoblinger(kopiRequests, ytelseType);
        var aktørId = validerKoblingerOgFinnAktørId(eksisterendeKoblinger, ytelseType, saksnummer);
        var nyeKoblinger = opprettNyeKoblinger(kopiRequests, ytelseType, aktørId, saksnummer);
        opprettKoblingrelasjoner(kopiRequests);
        kopierBeregningsgrunnlag(kopiRequests, nyeKoblinger, eksisterendeKoblinger, steg, opprettetTidMax);
        kopierKalkulatorInput(kopiRequests, eksisterendeKoblinger, nyeKoblinger);
        kopierAvklaringsbehov(kopiRequests, eksisterendeKoblinger, nyeKoblinger, steg);
    }

    private void kopierKalkulatorInput(List<KopierBeregningRequest> kopiRequests, List<KoblingEntitet> eksisterendeKoblinger, List<KoblingEntitet> nyeKoblinger) {
        kalkulatorInputTjeneste.hentForKoblinger(eksisterendeKoblinger.stream().map(KoblingEntitet::getId).toList())
                .forEach((key, value) -> kalkulatorInputTjeneste.lagreKalkulatorInput(finnNyKoblingId(key, eksisterendeKoblinger, nyeKoblinger, kopiRequests), value));
    }


    private void opprettKoblingrelasjoner(List<KopierBeregningRequest> kopiRequests) {
        var koblingrelasjoner = kopiRequests.stream()
                .collect(Collectors.toMap(
                        KopierBeregningRequest::getEksternReferanse,
                        f -> List.of(f.getKopierFraReferanse())));
        koblingTjeneste.finnOgOpprettKoblingRelasjoner(koblingrelasjoner);
    }

    private AktørId validerKoblingerOgFinnAktørId(List<KoblingEntitet> eksisterendeKoblinger, FagsakYtelseType ytelseType, Saksnummer saksnummer) {

        if (eksisterendeKoblinger.stream().anyMatch(k -> !k.getSaksnummer().equals(saksnummer))) {
            throw new IllegalStateException("Prøvde å forlenge periode for en annen sak");
        }

        if (eksisterendeKoblinger.stream().anyMatch(k -> !k.getYtelseType().equals(ytelseType))) {
            throw new IllegalStateException("Prøvde å forlenge periode for en annen ytelse");
        }

        var aktørIder = eksisterendeKoblinger.stream().map(KoblingEntitet::getAktørId)
                .distinct()
                .collect(Collectors.toList());

        if (aktørIder.size() != 1) {
            throw new IllegalStateException("Forventet en aktørId, fant " + aktørIder.size());

        }
        return aktørIder.get(0);
    }

    private List<KoblingEntitet> opprettNyeKoblinger(List<KopierBeregningRequest> kopiRequests, FagsakYtelseType ytelseType, AktørId aktørId, Saksnummer saksnummer) {
        var nyeKoblingReferanser = kopiRequests.stream().map(KopierBeregningRequest::getEksternReferanse)
                .map(KoblingReferanse::new)
                .collect(Collectors.toList());
        return koblingTjeneste.finnEllerOpprett(nyeKoblingReferanser, ytelseType, aktørId, saksnummer);
    }

    private List<KoblingEntitet> finnEksisterendeKoblinger(List<KopierBeregningRequest> kopiRequests, FagsakYtelseType ytelseType) {
        var eksisterendeKoblingReferanser = kopiRequests.stream().map(KopierBeregningRequest::getKopierFraReferanse)
                .map(KoblingReferanse::new)
                .collect(Collectors.toList());
        return koblingTjeneste.hentKoblinger(eksisterendeKoblingReferanser, ytelseType);
    }

    private void kopierBeregningsgrunnlag(List<KopierBeregningRequest> kopiRequests, List<KoblingEntitet> nyeKoblinger, List<KoblingEntitet> eksisterendeKoblinger, BeregningSteg steg, LocalDateTime opprettetTidMax) {
        var grunnlagSomSkalKopieres = finnGrunnlagSomSkalKopieres(
                eksisterendeKoblinger,
                steg,
                opprettetTidMax);

        grunnlagSomSkalKopieres.forEach((koblingId, gr) -> {
            var nyKoblingId = finnNyKoblingId(koblingId, eksisterendeKoblinger, nyeKoblinger, kopiRequests);
            var kopi = BeregningsgrunnlagGrunnlagBuilder.kopiere(gr);
            beregningsgrunnlagRepository.lagre(nyKoblingId, kopi, MapStegTilTilstand.mapTilStegTilstand(steg));
            koblingTjeneste.lagreGrunnlagskopiSporing(nyKoblingId, gr.getKoblingId(), gr.getId());
        });
    }

    private Map<Long, BeregningsgrunnlagGrunnlagEntitet> finnGrunnlagSomSkalKopieres(List<KoblingEntitet> kopierFraKoblinger, BeregningSteg steg, LocalDateTime opprettetTidMax) {
        var eksisterendeKoblingIder = mapTilId(kopierFraKoblinger);
        var grunnlagsliste = finnSisteGrunnlagForStegPrKobling(steg, opprettetTidMax, eksisterendeKoblingIder);
        var resultMap = grunnlagsliste.stream().collect(Collectors.toMap(BeregningsgrunnlagGrunnlagEntitet::getKoblingId, Function.identity()));
        var koblingerUtenGrunnlag = finnKoblingIdIkkeInkludertIListeMedGrunnlag(eksisterendeKoblingIder, resultMap.values());
        var koblingMap = koblingTjeneste.hentKoblingRelasjoner(koblingerUtenGrunnlag).stream()
                .collect(Collectors.toMap(KoblingRelasjon::getOriginalKoblingId, KoblingRelasjon::getKoblingId));

        // Finner siste i originale koblinger
        while (!koblingerUtenGrunnlag.isEmpty() && !koblingMap.isEmpty()) {
            var originalKoblinger = koblingMap.keySet();
            var grunnlagFraOriginaleKoblinger = finnSisteGrunnlagForStegPrKobling(steg, opprettetTidMax, originalKoblinger);
            oppdaterResultatMap(resultMap, koblingMap, grunnlagFraOriginaleKoblinger);
            koblingerUtenGrunnlag = finnKoblingIdIkkeInkludertIListeMedGrunnlag(originalKoblinger, grunnlagFraOriginaleKoblinger);
            koblingMap = finnKoblingMap(koblingerUtenGrunnlag, koblingMap);
        }

        var kopierFraKoblingerUtenGrunnlag = eksisterendeKoblingIder.stream().filter(id -> !resultMap.containsKey(id)).collect(Collectors.toSet());
        if (!kopierFraKoblingerUtenGrunnlag.isEmpty()) {
            throw new IllegalStateException("Prøvde å kopiere grunnlag fra steg " + steg.getKode() + " for koblinger " + kopierFraKoblingerUtenGrunnlag + ", men grunnlag fant ikke grunnlag med riktig tilstand." );
        }
        return resultMap;
    }

    private void oppdaterResultatMap(Map<Long, BeregningsgrunnlagGrunnlagEntitet> resultMap, Map<Long, Long> koblingMap, List<BeregningsgrunnlagGrunnlagEntitet> grunnlagFraOriginaleKoblinger) {
        grunnlagFraOriginaleKoblinger.forEach(gr -> resultMap.put(koblingMap.get(gr.getKoblingId()), gr));
    }

    private Map<Long, Long> finnKoblingMap(Set<Long> koblingerUtenGrunnlag, Map<Long, Long> koblingMap) {
        return koblingTjeneste.hentKoblingRelasjoner(koblingerUtenGrunnlag).stream()
                .collect(Collectors.toMap(KoblingRelasjon::getOriginalKoblingId, r -> koblingMap.get(r.getKoblingId())));
    }

    private List<BeregningsgrunnlagGrunnlagEntitet> finnSisteGrunnlagForStegPrKobling(BeregningSteg steg, LocalDateTime opprettetTidMax, Set<Long> koblingIder) {
        if (steg.equals(BeregningSteg.VURDER_VILKAR_BERGRUNN)) {
            return finnGrunnlagForKopiAvVilkårsvurdering(opprettetTidMax, koblingIder);
        }
        return beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingIder, MapStegTilTilstand.mapTilStegTilstand(steg), opprettetTidMax);
    }

    private List<BeregningsgrunnlagGrunnlagEntitet> finnGrunnlagForKopiAvVilkårsvurdering(LocalDateTime opprettetTidMax, Set<Long> koblingIder) {
        // Fordi det finnes grunnlag uten tilstand VURDER_VILKÅR må vi sjekke andre tilstander
        var vurdertVilkårGrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingIder, BeregningsgrunnlagTilstand.VURDERT_VILKÅR, opprettetTidMax);
        var koblingIdUtenVurdertVilkårGrunnlag = finnKoblingIdIkkeInkludertIListeMedGrunnlag(koblingIder, vurdertVilkårGrunnlag);
        var foreslåttUtGrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingIdUtenVurdertVilkårGrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT_UT, opprettetTidMax);
        var koblingIdUtenForeslåttUtGrunnlag = finnKoblingIdIkkeInkludertIListeMedGrunnlag(koblingIdUtenVurdertVilkårGrunnlag, foreslåttUtGrunnlag);
        var foreslåttGrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingIdUtenForeslåttUtGrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT, opprettetTidMax);

        List<BeregningsgrunnlagGrunnlagEntitet> grunnlagSomSkalKopieres = new ArrayList<>();
        grunnlagSomSkalKopieres.addAll(vurdertVilkårGrunnlag);
        grunnlagSomSkalKopieres.addAll(foreslåttUtGrunnlag);
        grunnlagSomSkalKopieres.addAll(foreslåttGrunnlag);
        return grunnlagSomSkalKopieres;
    }

    private Set<Long> finnKoblingIdIkkeInkludertIListeMedGrunnlag(Set<Long> listeMedId, Collection<BeregningsgrunnlagGrunnlagEntitet> grunnlagsliste) {
        var koblingerMedGrunnlag = grunnlagsliste.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet());
        return listeMedId.stream().filter(i -> koblingerMedGrunnlag.stream().noneMatch(id -> id.equals(i)))
                .collect(Collectors.toSet());
    }

    private void kopierAvklaringsbehov(List<KopierBeregningRequest> kopiRequests, List<KoblingEntitet> eksisterendeKoblinger, List<KoblingEntitet> nyeKoblinger, BeregningSteg steg) {
        var eksisterendeKoblingIder = eksisterendeKoblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var avklaringsbehovSomMåKopieres = finnAvklaringsbehovSomSkalKopieres(eksisterendeKoblingIder, steg);
        // Avbryter først alle eksisterende på kopi-kobling (kan finnes i caser der vi flipper status fra ikke-forlengelse til forlengelse)
        nyeKoblinger.forEach(k -> avklaringsbehovTjeneste.avbrytAlleAvklaringsbehov(k.getId()));
        // Kopierer alle fra eksisterende koblinger til kopi-koblinger
        avklaringsbehovSomMåKopieres.forEach(ab -> {
            var nyKobling = finnNyKobling(ab.getKoblingId(), eksisterendeKoblinger, nyeKoblinger, kopiRequests);
            avklaringsbehovTjeneste.kopierAvklaringsbehov(nyKobling, ab);
        });
    }

    private Set<AvklaringsbehovEntitet> finnAvklaringsbehovSomSkalKopieres(Set<Long> eksisterendeKoblingIder, BeregningSteg steg) {
        return avklaringsbehovTjeneste.hentAlleAvklaringsbehovForKoblinger(eksisterendeKoblingIder)
                .stream()
                .filter(ap -> ap.getStegFunnet().erFør(steg))
                .filter(ap -> ap.getStatus().equals(AvklaringsbehovStatus.UTFØRT))
                .collect(Collectors.toSet());
    }


    private Set<Long> mapTilId(List<KoblingEntitet> eksisterendeKoblinger) {
        return eksisterendeKoblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
    }

    private UUID finnNyKoblingReferanse(Long eksisterendeKoblingId,
                                        List<KoblingEntitet> eksisterendeKoblinger,
                                        List<KopierBeregningRequest> kopiRequests) {
        var eksisterendeReferanser = eksisterendeKoblinger.stream().filter(k -> k.getId().equals(eksisterendeKoblingId))
                .map(KoblingEntitet::getKoblingReferanse)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Forventer å finne eksisterende kobling i liste"));
        return kopiRequests.stream()
                .filter(f -> f.getKopierFraReferanse().equals(eksisterendeReferanser.getReferanse()))
                .map(KopierBeregningRequest::getEksternReferanse)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Forventer å finne ny referanse som matcher eksisterende"));

    }

    private Long finnNyKoblingId(Long eksisterendeKoblingId,
                                 List<KoblingEntitet> eksisterendeKoblinger,
                                 List<KoblingEntitet> nyeKoblinger,
                                 List<KopierBeregningRequest> kopiRequests) {
        return finnNyKobling(eksisterendeKoblingId, eksisterendeKoblinger, nyeKoblinger, kopiRequests).getId();
    }

    private KoblingEntitet finnNyKobling(Long eksisterendeKoblingId,
                                         List<KoblingEntitet> eksisterendeKoblinger,
                                         List<KoblingEntitet> nyeKoblinger,
                                         List<KopierBeregningRequest> kopiRequests) {
        var nyKoblingReferanse = finnNyKoblingReferanse(eksisterendeKoblingId, eksisterendeKoblinger, kopiRequests);
        return nyeKoblinger.stream()
                .filter(k -> k.getKoblingReferanse().getReferanse().equals(nyKoblingReferanse))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Forventer å finne ny kobling for forlengelse"));
    }


}
