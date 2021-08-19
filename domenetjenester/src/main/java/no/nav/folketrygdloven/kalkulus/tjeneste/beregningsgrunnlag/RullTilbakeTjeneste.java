package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelsporingRepository;

@ApplicationScoped
public class RullTilbakeTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private RegelsporingRepository regelsporingRepository;

    public RullTilbakeTjeneste() {
        // CDI
    }

    @Inject
    public RullTilbakeTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository, RegelsporingRepository regelsporingRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.regelsporingRepository = regelsporingRepository;
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
    }

    public void rullTilbakeTilObligatoriskTilstandFørVedBehov(Long koblingId, BeregningsgrunnlagTilstand tilstand) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        if (beregningsgrunnlagGrunnlagEntitet.isPresent()) {
            BeregningsgrunnlagTilstand aktivTilstand = beregningsgrunnlagGrunnlagEntitet.get().getBeregningsgrunnlagTilstand();
            if (!aktivTilstand.erFør(tilstand)) {
                beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
                regelsporingRepository.ryddRegelsporingForTilstand(koblingId, tilstand);
                Optional<BeregningsgrunnlagTilstand> forrigeObligatoriskTilstand = tilstand.erObligatoriskTilstand() ? Optional.of(tilstand) : BeregningsgrunnlagTilstand.finnForrigeObligatoriskTilstand(tilstand);
                if (forrigeObligatoriskTilstand.isPresent()) {
                    beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId, forrigeObligatoriskTilstand.get());
                } else {
                    BeregningsgrunnlagTilstand første = BeregningsgrunnlagTilstand.finnFørste();
                    beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId, første);
                }
            }
        }
    }

    public void rullTilbakeTilTilstandFørVedBehov(Long koblingId, BeregningsgrunnlagTilstand tilstand) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        if (beregningsgrunnlagGrunnlagEntitet.isPresent()) {
            BeregningsgrunnlagTilstand aktivTilstand = beregningsgrunnlagGrunnlagEntitet.get().getBeregningsgrunnlagTilstand();
            if (!aktivTilstand.erFør(tilstand)) {
                beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
                regelsporingRepository.ryddRegelsporingForTilstand(koblingId, tilstand);
                beregningsgrunnlagRepository.reaktiverSisteBeregningsgrunnlagGrunnlagEntitetFørTilstand(koblingId, aktivTilstand);
            }
        }
    }

    private boolean skalRullesTilbake(List<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet, BeregningsgrunnlagTilstand tilstand) {
        return beregningsgrunnlagGrunnlagEntitet.stream()
                .map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlagTilstand)
                .anyMatch(aktivTilstand ->  !aktivTilstand.erFør(tilstand));
    }

    public void deaktiverAktivtBeregningsgrunnlagOgInput(Long koblingId) {
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
        beregningsgrunnlagRepository.deaktiverKalkulatorInput(koblingId);
        regelsporingRepository.ryddRegelsporingForTilstand(koblingId, BeregningsgrunnlagTilstand.finnFørste());
    }

}
