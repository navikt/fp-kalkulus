package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand;
import no.nav.folketrygdloven.kalkulus.beregning.MapTilstandTilSteg;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.forlengelse.ForlengelseTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelsporingRepository;

@ApplicationScoped
public class RullTilbakeTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private RegelsporingRepository regelsporingRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private ForlengelseTjeneste forlengelseTjeneste;

    public RullTilbakeTjeneste() {
        // CDI
    }

    @Inject
    public RullTilbakeTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                               RegelsporingRepository regelsporingRepository,
                               AvklaringsbehovTjeneste avklaringsbehovTjeneste, ForlengelseTjeneste forlengelseTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.regelsporingRepository = regelsporingRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
        this.forlengelseTjeneste = forlengelseTjeneste;
    }

    public void rullTilbakeTilForrigeTilstandVedBehov(Set<Long> koblingIder, BeregningsgrunnlagTilstand tilstand, boolean skalKjøreSteget) {
        List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        var rullTilbakeListe = finnGrunnlagSomSkalRullesTilbake(beregningsgrunnlagGrunnlagEntiteter, tilstand, skalKjøreSteget);
        if (!rullTilbakeListe.isEmpty()) {
            rullTilbakeGrunnlag(tilstand, rullTilbakeListe, skalKjøreSteget);
        }
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehovEtterEllerISteg(koblingIder, MapTilstandTilSteg.mapTilSteg(tilstand), skalKjøreSteget);
        forlengelseTjeneste.deaktiverVedTilbakerulling(koblingIder, tilstand);

    }

    private void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, List<BeregningsgrunnlagGrunnlagEntitet> rullTilbakeListe, boolean skalKjøreSteget) {
        Set<Long> rullTilbakeKoblinger = rullTilbakeListe.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet());
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntiteter(rullTilbakeListe);
        regelsporingRepository.ryddRegelsporingForTilstand(rullTilbakeKoblinger, tilstand);
        if (BeregningsgrunnlagTilstand.finnFørste().erFør(tilstand)) {
            if (skalKjøreSteget) {
                beregningsgrunnlagRepository.reaktiverForrigeGrunnlagForKoblinger(rullTilbakeKoblinger, tilstand);
            } else {
                var koblingerMedAvklaringsbehovIStegUt = finnKoblingerMedAvklaringsbehovIStegUt(tilstand, rullTilbakeKoblinger);
                beregningsgrunnlagRepository.reaktiverSisteMedTilstand(tilstand, koblingerMedAvklaringsbehovIStegUt);

                var koblingerUtenAvklaringsbehovIStegUt = rullTilbakeKoblinger.stream().filter(k -> !koblingerMedAvklaringsbehovIStegUt.contains(k)).collect(Collectors.toSet());
                beregningsgrunnlagRepository.reaktiverForrigeGrunnlagForKoblinger(koblingerUtenAvklaringsbehovIStegUt, tilstand);
            }
        }
    }

    private Set<Long> finnKoblingerMedAvklaringsbehovIStegUt(BeregningsgrunnlagTilstand tilstand, Set<Long> rullTilbakeKoblinger) {
        return avklaringsbehovTjeneste.hentAlleAvklaringsbehovForKoblinger(rullTilbakeKoblinger).stream()
                .filter(a -> MapStegTilTilstand.mapTilStegUtTilstand(a.getStegFunnet()).map(tilstand::equals).orElse(false))
                .map(AvklaringsbehovEntitet::getKoblingId)
                .collect(Collectors.toSet());
    }

    private List<BeregningsgrunnlagGrunnlagEntitet> finnGrunnlagSomSkalRullesTilbake(List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet,
                                                                                     BeregningsgrunnlagTilstand tilstand,
                                                                                     boolean skalKjøreSteget) {
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .filter(gr -> {
                    if (skalKjøreSteget) {
                        return !gr.getBeregningsgrunnlagTilstand().erFør(tilstand);
                    } else {
                        return gr.getBeregningsgrunnlagTilstand().erEtter(tilstand);
                    }
                })
                .collect(Collectors.toList());
    }

    public void deaktiverAllKoblingdata(Long koblingId) {
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
        beregningsgrunnlagRepository.deaktiverKalkulatorInput(koblingId);
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehov(koblingId);
        regelsporingRepository.ryddRegelsporingForTilstand(koblingId, BeregningsgrunnlagTilstand.finnFørste());
    }

}
