package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FASTSATT;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@ApplicationScoped
public class BeregningTjeneste {

    private final BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste;
    private final BeregningsgrunnlagRepository repository;

    @Inject
    public BeregningTjeneste(BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste, BeregningsgrunnlagRepository repository) {
        this.beregningsgrunnlagTjeneste = beregningsgrunnlagTjeneste;
        this.repository = repository;
    }

    public void fastsettBeregningsaktiviteter(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.fastsettBeregningsgrunnlag(input);
        Long behandlingId = input.getBehandlingReferanse().getBehandlingId();
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag = beregningResultatAggregat.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag();

        if (beregningsgrunnlag.isPresent()) {
            BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningsgrunnlag.get());
            repository.lagre(behandlingId, beregningsgrunnlagEntitet, FASTSATT);
        }
    }
}
