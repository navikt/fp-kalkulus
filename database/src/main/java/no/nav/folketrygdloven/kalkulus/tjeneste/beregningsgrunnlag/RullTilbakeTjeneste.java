package no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag;

import java.util.Optional;

import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

public class RullTilbakeTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    public RullTilbakeTjeneste() {
        // CDI
    }

    @Inject
    public RullTilbakeTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    public void rullTilbakeTilObligatoriskTilstandFørVedBehov(Long koblingId, BeregningsgrunnlagTilstand tilstand) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(koblingId);
        if (beregningsgrunnlagGrunnlagEntitet.isPresent()) {
            BeregningsgrunnlagTilstand aktivTilstand = beregningsgrunnlagGrunnlagEntitet.get().getBeregningsgrunnlagTilstand();
            if (!aktivTilstand.erFør(tilstand)) {
                beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
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
            if (!aktivTilstand.erFør(tilstand) || aktivTilstand.equals(tilstand)) {
                beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId);
                Optional<BeregningsgrunnlagTilstand> forrigeTilstand = BeregningsgrunnlagTilstand.finnForrigeTilstand(tilstand);
                if (forrigeTilstand.isPresent()) {
                    beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId, forrigeTilstand.get());
                } else {
                    BeregningsgrunnlagTilstand første = BeregningsgrunnlagTilstand.finnFørste();
                    beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntitet(koblingId, første);
                }
            }
        }
    }


}
