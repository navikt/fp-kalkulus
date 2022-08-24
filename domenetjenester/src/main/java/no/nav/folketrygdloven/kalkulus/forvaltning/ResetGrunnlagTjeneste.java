package no.nav.folketrygdloven.kalkulus.forvaltning;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.KopierBeregningRequest;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;

@ApplicationScoped
public class ResetGrunnlagTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingTjeneste koblingTjeneste;
    private KoblingRepository koblingRepository;

    public ResetGrunnlagTjeneste() {
    }

    @Inject
    public ResetGrunnlagTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository, KoblingTjeneste koblingTjeneste, KoblingRepository koblingRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingTjeneste = koblingTjeneste;
        this.koblingRepository = koblingRepository;
    }

    public void resetGrunnlag(List<KopierBeregningRequest> kopiRequests,
                              YtelseTyperKalkulusStøtterKontrakt ytelseType, LocalDateTime originalBehandlingAvsluttetTid) {
        var originalReferanser = kopiRequests.stream().map(KopierBeregningRequest::getKopierFraReferanse)
                .map(KoblingReferanse::new)
                .collect(Collectors.toList());
        var originalKobling = finnKoblinger(ytelseType, originalReferanser);
        reaktiverForrigeFastsatt(originalBehandlingAvsluttetTid, originalKobling);
        originalKobling.forEach(k -> koblingRepository.fjernUgyldigKoblingrelasjonForId(k.getId()));
    }

    private void reaktiverForrigeFastsatt(LocalDateTime originalBehandlingAvsluttetTid, List<KoblingEntitet> originalKobling) {
        originalKobling.forEach(k -> {
            var forrigeFastsattGrunnlagOpt = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetOpprettetFør(k.getId(), originalBehandlingAvsluttetTid, BeregningsgrunnlagTilstand.FASTSATT);
            if (forrigeFastsattGrunnlagOpt.isPresent()) {
                var aktivtGrunnlagListe = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(List.of(k.getId()));
                if (!aktivtGrunnlagListe.isEmpty()) {
                    var aktivtGrunnlag = aktivtGrunnlagListe.get(0);
                    beregningsgrunnlagRepository.endreAktivOgLagre(aktivtGrunnlag, false);
                    var fastsatt = forrigeFastsattGrunnlagOpt.get();
                    beregningsgrunnlagRepository.endreAktivOgLagre(fastsatt, true);
                }
            }
        });
    }

    private List<KoblingEntitet> finnKoblinger(YtelseTyperKalkulusStøtterKontrakt ytelseType, List<KoblingReferanse> referanser) {
        var eksisterendeKoblinger = koblingTjeneste.hentKoblinger(referanser, ytelseType);
        return eksisterendeKoblinger;
    }

}
