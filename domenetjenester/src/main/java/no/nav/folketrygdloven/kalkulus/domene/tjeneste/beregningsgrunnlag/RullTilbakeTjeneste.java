package no.nav.folketrygdloven.kalkulus.domene.tjeneste.beregningsgrunnlag;

import java.util.Collections;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulus.domene.beregning.MapTilstandTilSteg;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelsporingRepository;

@ApplicationScoped
public class RullTilbakeTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private RegelsporingRepository regelsporingRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;

    public RullTilbakeTjeneste() {
        // CDI
    }

    @Inject
    public RullTilbakeTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                               RegelsporingRepository regelsporingRepository,
                               AvklaringsbehovTjeneste avklaringsbehovTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.regelsporingRepository = regelsporingRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }

    public void rullTilbakeTilForrigeTilstandVedBehov(Long koblingId, BeregningsgrunnlagTilstand tilstand, boolean skalKjøreSteget) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        var grunnlagSomMåTilbakestilles = finnGrunnlagSomSkalRullesTilbake(beregningsgrunnlagGrunnlagEntiteter, tilstand, skalKjøreSteget);
        grunnlagSomMåTilbakestilles.ifPresent(beregningsgrunnlagGrunnlagEntitet -> rullTilbakeGrunnlag(tilstand, beregningsgrunnlagGrunnlagEntitet));
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehovEtterEllerISteg(koblingId, MapTilstandTilSteg.mapTilSteg(tilstand), skalKjøreSteget);
    }

    private void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, BeregningsgrunnlagGrunnlagEntitet grunnlagSomTilbakestilles) {
        var koblingId = grunnlagSomTilbakestilles.getKoblingId();
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntiteter(Collections.singletonList(grunnlagSomTilbakestilles));
        regelsporingRepository.ryddRegelsporingForTilstand(koblingId, tilstand);
        var rullTilbake = finnRullTilbakeBeregningsgrunnlagTjeneste(tilstand);
        rullTilbake.rullTilbakeGrunnlag(tilstand, koblingId);
    }

    private RullTilbakeBeregningsgrunnlag finnRullTilbakeBeregningsgrunnlagTjeneste(BeregningsgrunnlagTilstand tilstand) {
        if (tilstand.equals(BeregningsgrunnlagTilstand.FASTSATT_INN)) {
            return new RullTilbakeTilFastsattInn(beregningsgrunnlagRepository, avklaringsbehovTjeneste);
        } else {
            return new RullTilbakeBeregningsgrunnlagFelles(beregningsgrunnlagRepository);
        }
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnGrunnlagSomSkalRullesTilbake(Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet,
                                                                                     BeregningsgrunnlagTilstand tilstand,
                                                                                     boolean skalKjøreSteget) {
        return beregningsgrunnlagGrunnlagEntitet
                .filter(gr -> {
                    if (skalKjøreSteget) {
                        return !gr.getBeregningsgrunnlagTilstand().erFør(tilstand);
                    } else {
                        return gr.getBeregningsgrunnlagTilstand().erEtter(tilstand);
                    }
                });
    }

    public void deaktiverAllKoblingdata(Long koblingId) {
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehov(koblingId);
        regelsporingRepository.ryddRegelsporingForTilstand(koblingId, BeregningsgrunnlagTilstand.finnFørste());
    }

}
