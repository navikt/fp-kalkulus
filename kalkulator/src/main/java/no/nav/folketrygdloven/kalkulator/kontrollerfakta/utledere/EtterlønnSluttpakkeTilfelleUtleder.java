package no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.EtterlønnSluttpakkeTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;


@ApplicationScoped
@FagsakYtelseTypeRef("*")
@FaktaOmBeregningTilfelleRef("VURDER_ETTERLØNN_SLUTTPAKKE")
public class EtterlønnSluttpakkeTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        return EtterlønnSluttpakkeTjeneste.skalVurdereOmBrukerHarEtterlønnSluttpakke(beregningsgrunnlagGrunnlag) ?
            Optional.of(FaktaOmBeregningTilfelle.VURDER_ETTERLØNN_SLUTTPAKKE) : Optional.empty();
    }
}
