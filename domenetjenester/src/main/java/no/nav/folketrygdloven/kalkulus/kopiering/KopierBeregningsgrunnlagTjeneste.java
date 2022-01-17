package no.nav.folketrygdloven.kalkulus.kopiering;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.KopierBeregningRequest;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

@ApplicationScoped
public class KopierBeregningsgrunnlagTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    public KopierBeregningsgrunnlagTjeneste() {
    }

    @Inject
    public KopierBeregningsgrunnlagTjeneste(KoblingTjeneste koblingTjeneste, BeregningsgrunnlagRepository beregningsgrunnlagRepository, KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }


    /**
     * Oppretter nye koblinger og kopierer data fra eksisterende kobling
     * <p>
     * Tilstand for grunnlaget som kopieres er VURDERT_VILKÅR.
     * Dersom det ikke finnes grunnlag med denne tilstanden kopieres grunnlag fra foreslå (FORSLÅTT_UT eller FORSLÅTT)
     *
     * @param kopiRequests Referanser som skal kopieres
     * @param ytelseType          Ytelsetype
     * @param saksnummer          Saksnummer
     */
    public void kopierGrunnlagOgOpprettKoblinger(List<KopierBeregningRequest> kopiRequests,
                                                 YtelseTyperKalkulusStøtterKontrakt ytelseType,
                                                 Saksnummer saksnummer) {
            var eksisterendeKoblinger = finnEksisterendeKoblinger(kopiRequests, ytelseType);
            var aktørId = validerKoblingerOgFinnAktørId(eksisterendeKoblinger, ytelseType, saksnummer);
            var nyeKoblinger = opprettNyeKoblinger(kopiRequests, ytelseType, aktørId, saksnummer);
            kopierBeregningsgrunnlag(kopiRequests, nyeKoblinger, eksisterendeKoblinger);
            opprettKoblingrelasjoner(kopiRequests);
    }


    private void opprettKoblingrelasjoner(List<KopierBeregningRequest> kopiRequests) {
        var koblingrelasjoner = kopiRequests.stream()
                .collect(Collectors.toMap(
                        KopierBeregningRequest::getEksternReferanse,
                        f -> List.of(f.getKopierFraReferanse())));
        koblingTjeneste.finnOgOpprettKoblingRelasjoner(koblingrelasjoner);
    }

    private AktørId validerKoblingerOgFinnAktørId(List<KoblingEntitet> eksisterendeKoblinger, YtelseTyperKalkulusStøtterKontrakt ytelseType, Saksnummer saksnummer) {

        if (eksisterendeKoblinger.stream().anyMatch(k -> !k.getSaksnummer().equals(saksnummer))) {
            throw new IllegalStateException("Prøvde å forlenge periode for en annen sak");
        }

        if (eksisterendeKoblinger.stream().anyMatch(k -> !k.getYtelseTyperKalkulusStøtter().equals(ytelseType))) {
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

    private List<KoblingEntitet> opprettNyeKoblinger(List<KopierBeregningRequest> kopiRequests, YtelseTyperKalkulusStøtterKontrakt ytelseType, AktørId aktørId, Saksnummer saksnummer) {
        var nyeKoblingReferanser = kopiRequests.stream().map(KopierBeregningRequest::getEksternReferanse)
                .map(KoblingReferanse::new)
                .collect(Collectors.toList());
        return koblingTjeneste.finnEllerOpprett(nyeKoblingReferanser, ytelseType, aktørId, saksnummer, true);
    }

    private List<KoblingEntitet> finnEksisterendeKoblinger(List<KopierBeregningRequest> kopiRequests, YtelseTyperKalkulusStøtterKontrakt ytelseType) {
        var eksisterendeKoblingReferanser = kopiRequests.stream().map(KopierBeregningRequest::getKopierFraReferanse)
                .map(KoblingReferanse::new)
                .collect(Collectors.toList());
        var eksisterendeKoblinger = koblingTjeneste.hentKoblinger(eksisterendeKoblingReferanser, ytelseType);
        return eksisterendeKoblinger;
    }

    private void kopierBeregningsgrunnlag(List<KopierBeregningRequest> kopiRequests, List<KoblingEntitet> nyeKoblinger, List<KoblingEntitet> eksisterendeKoblinger) {
        List<BeregningsgrunnlagGrunnlagEntitet> grunnlagSomSkalKopieres = finnGrunnlagSomSkalKopieres(eksisterendeKoblinger);

        grunnlagSomSkalKopieres.forEach(gr -> {
            var nyKoblingId = finnNyKoblingId(gr.getKoblingId(), eksisterendeKoblinger, nyeKoblinger, kopiRequests);
            var kopi = BeregningsgrunnlagGrunnlagBuilder.kopiere(gr);
            beregningsgrunnlagRepository.lagre(nyKoblingId, kopi, BeregningsgrunnlagTilstand.VURDERT_VILKÅR);
        });
    }

    private List<BeregningsgrunnlagGrunnlagEntitet> finnGrunnlagSomSkalKopieres(List<KoblingEntitet> eksisterendeKoblinger) {
        var vurdertVilkårGrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(mapTilId(eksisterendeKoblinger), BeregningsgrunnlagTilstand.VURDERT_VILKÅR);
        List<Long> koblingIdUtenVurdertVilkårGrunnlag = finnKoblingIdIkkeInkludertIListeMedGrunnlag(mapTilId(eksisterendeKoblinger), vurdertVilkårGrunnlag);
        var foreslåttUtGrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingIdUtenVurdertVilkårGrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT_UT);
        var koblingIdUtenForeslåttUtGrunnlag = finnKoblingIdIkkeInkludertIListeMedGrunnlag(koblingIdUtenVurdertVilkårGrunnlag, foreslåttUtGrunnlag);
        var foreslåttGrunnlag = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingIdUtenForeslåttUtGrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT);

        List<BeregningsgrunnlagGrunnlagEntitet> grunnlagSomSkalKopieres = new ArrayList<>();
        grunnlagSomSkalKopieres.addAll(vurdertVilkårGrunnlag);
        grunnlagSomSkalKopieres.addAll(foreslåttUtGrunnlag);
        grunnlagSomSkalKopieres.addAll(foreslåttGrunnlag);
        return grunnlagSomSkalKopieres;
    }

    private List<Long> finnKoblingIdIkkeInkludertIListeMedGrunnlag(List<Long> listeMedId, List<BeregningsgrunnlagGrunnlagEntitet> vurdertVilkårGrunnlag) {
        var idForGrunnlagMedVilkårvurdering = vurdertVilkårGrunnlag.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId);
        return listeMedId.stream().filter(i -> idForGrunnlagMedVilkårvurdering.noneMatch(id -> id.equals(i)))
                .collect(Collectors.toList());
    }

    private List<Long> mapTilId(List<KoblingEntitet> eksisterendeKoblinger) {
        return eksisterendeKoblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toList());
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
        var nyKoblingReferanse = finnNyKoblingReferanse(eksisterendeKoblingId, eksisterendeKoblinger, kopiRequests);
        return nyeKoblinger.stream()
                .filter(k -> k.getKoblingReferanse().getReferanse().equals(nyKoblingReferanse))
                .map(KoblingEntitet::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Forventer å finne ny koblingid for forlengelse"));
    }



}
