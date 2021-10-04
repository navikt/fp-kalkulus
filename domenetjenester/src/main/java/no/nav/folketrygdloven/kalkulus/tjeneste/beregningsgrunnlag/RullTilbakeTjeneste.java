package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
        if (skalRullesTilbake(beregningsgrunnlagGrunnlagEntiteter, tilstand)) {
            beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntiteter(beregningsgrunnlagGrunnlagEntiteter);
            regelsporingRepository.ryddRegelsporingerForTilstand(koblingIder, tilstand);
            Optional<BeregningsgrunnlagTilstand> forrigeObligatoriskTilstand = tilstand.erObligatoriskTilstand() ? Optional.of(tilstand) : BeregningsgrunnlagTilstand.finnForrigeObligatoriskTilstand(tilstand);
            if (forrigeObligatoriskTilstand.isPresent()) {
                beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntiteter(koblingIder, forrigeObligatoriskTilstand.get());
            } else {
                BeregningsgrunnlagTilstand første = BeregningsgrunnlagTilstand.finnFørste();
                beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntiteter(koblingIder, første);
            }
        }
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehovEtterEllerISteg(koblingIder, MapTilstandTilSteg.mapTilSteg(tilstand));
    }

    private boolean skalRullesTilbake(List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet, BeregningsgrunnlagTilstand tilstand) {
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlagTilstand)
                .anyMatch(aktivTilstand ->  !aktivTilstand.erFør(tilstand));
    }

    public void deaktiverAllKoblingdata(Long koblingId) {
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
        beregningsgrunnlagRepository.deaktiverKalkulatorInput(koblingId);
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehov(koblingId);
        regelsporingRepository.ryddRegelsporingForTilstand(koblingId, BeregningsgrunnlagTilstand.finnFørste());
    }

}
