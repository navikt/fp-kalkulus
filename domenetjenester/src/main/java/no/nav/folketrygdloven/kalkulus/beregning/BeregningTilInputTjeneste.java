package no.nav.folketrygdloven.kalkulus.beregning;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@ApplicationScoped
public class BeregningTilInputTjeneste {


    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    @Inject
    public BeregningTilInputTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }


    public BeregningsgrunnlagInput finnEllerOpprett() {

        return null;
    }


    private BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        Long behandlingId = input.getBehandlingReferanse().getBehandlingId();
        Optional<BeregningsgrunnlagGrunnlagEntitet> grunnlagEntitetOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(behandlingId);

        return null;
    }
}
