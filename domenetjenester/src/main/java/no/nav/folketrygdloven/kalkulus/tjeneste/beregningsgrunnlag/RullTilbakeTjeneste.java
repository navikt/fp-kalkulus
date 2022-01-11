package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulus.beregning.MapTilstandTilSteg;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelsporingRepository;

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

    public void rullTilbakeTilObligatoriskTilstandFørVedBehov(Set<Long> koblingIder, BeregningsgrunnlagTilstand tilstand) {
        List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        var rullTilbakeListe = finnGrunnlagSomSkalRullesTilbake(beregningsgrunnlagGrunnlagEntiteter, tilstand);
        if (!rullTilbakeListe.isEmpty()) {
            rullTilbakeGrunnlag(tilstand, rullTilbakeListe);
        }
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehovEtterEllerISteg(koblingIder, MapTilstandTilSteg.mapTilSteg(tilstand));
    }

    private void rullTilbakeGrunnlag(BeregningsgrunnlagTilstand tilstand, List<BeregningsgrunnlagGrunnlagEntitet> rullTilbakeListe) {
        Set<Long> rullTilbakeKoblinger = rullTilbakeListe.stream().map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId).collect(Collectors.toSet());
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntiteter(rullTilbakeListe);
        regelsporingRepository.ryddRegelsporingForTilstand(rullTilbakeKoblinger, tilstand);
        Optional<BeregningsgrunnlagTilstand> forrigeObligatoriskTilstand = tilstand.erObligatoriskTilstand() ? Optional.of(tilstand) : BeregningsgrunnlagTilstand.finnForrigeObligatoriskTilstand(tilstand);
        if (forrigeObligatoriskTilstand.isPresent()) {
            beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntiteter(rullTilbakeKoblinger, forrigeObligatoriskTilstand.get());
        } else {
            BeregningsgrunnlagTilstand første = BeregningsgrunnlagTilstand.finnFørste();
            beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntiteter(rullTilbakeKoblinger, første);
        }
    }

    private List<BeregningsgrunnlagGrunnlagEntitet> finnGrunnlagSomSkalRullesTilbake(List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet, BeregningsgrunnlagTilstand tilstand) {
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .filter(gr -> !gr.getBeregningsgrunnlagTilstand().erFør(tilstand))
                .collect(Collectors.toList());
    }

    public void deaktiverAllKoblingdata(Long koblingId) {
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
        beregningsgrunnlagRepository.deaktiverKalkulatorInput(koblingId);
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehov(koblingId);
        regelsporingRepository.ryddRegelsporingForTilstand(koblingId, BeregningsgrunnlagTilstand.finnFørste());
    }

}
