package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulus.beregning.MapTilstandTilSteg;
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
            rullTilbakeGrunnlag(tilstand, rullTilbakeListe);
        }
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehovEtterEllerISteg(koblingIder, MapTilstandTilSteg.mapTilSteg(tilstand), skalKjøreSteget);
        forlengelseTjeneste.deaktiverVedTilbakerulling(koblingIder, tilstand);

    }

    private void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, List<BeregningsgrunnlagGrunnlagEntitet> rullTilbakeListe) {
        Set<Long> rullTilbakeKoblinger = rullTilbakeListe.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet());
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntiteter(rullTilbakeListe);
        regelsporingRepository.ryddRegelsporingForTilstand(rullTilbakeKoblinger, tilstand);
        var rullTilbake = finnRullTilbakeBeregningsgrunnlagTjeneste(tilstand);
        rullTilbake.rullTilbakeGrunnlag(tilstand, rullTilbakeKoblinger);
    }

    private RullTilbakeBeregningsgrunnlag finnRullTilbakeBeregningsgrunnlagTjeneste(BeregningsgrunnlagTilstand tilstand) {
        if (tilstand.equals(BeregningsgrunnlagTilstand.FASTSATT_INN)) {
            return new RullTilbakeTilFastsattInn(beregningsgrunnlagRepository, avklaringsbehovTjeneste);
        } else {
            return new RullTilbakeBeregningsgrunnlagFelles(beregningsgrunnlagRepository);
        }
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
