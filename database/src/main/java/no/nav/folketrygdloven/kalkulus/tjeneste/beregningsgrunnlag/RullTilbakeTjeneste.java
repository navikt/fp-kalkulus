package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.Optional;

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

    public void deaktiverAktivtBeregningsgrunnlagOgInput(Long koblingId) {
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
        beregningsgrunnlagRepository.deaktiverKalkulatorInput(koblingId);
        regelsporingRepository.ryddRegelsporingForTilstand(koblingId, BeregningsgrunnlagTilstand.finnFørste());
    }

}
