package no.nav.folketrygdloven.kalkulator.kontrollerfakta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere.TilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;

@ApplicationScoped
public class FaktaOmBeregningTilfelleTjeneste {
    private List<TilfelleUtleder> utledere;

    public FaktaOmBeregningTilfelleTjeneste() {
        // For CDI
    }

    @Inject
    public FaktaOmBeregningTilfelleTjeneste(@Any Instance<TilfelleUtleder> tilfelleUtledere) {
        this.utledere = tilfelleUtledere.stream().collect(Collectors.toList());
    }

    public List<FaktaOmBeregningTilfelle> finnTilfellerForFellesAksjonspunkt(BeregningsgrunnlagInput input,
                                                                             BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        List<FaktaOmBeregningTilfelle> tilfeller = new ArrayList<>();
        for (TilfelleUtleder utleder : utledere) {
            utleder.utled(input, beregningsgrunnlagGrunnlag).ifPresent(tilfeller::add);
        }
        return tilfeller;
    }

}
