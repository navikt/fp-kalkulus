package no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.LønnsendringTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;

@ApplicationScoped
public class VurderLønnsendringTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        var ref = input.getBehandlingReferanse();
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        return LønnsendringTjeneste.brukerHarHattLønnsendringOgManglerInntektsmelding(ref.getAktørId(), beregningsgrunnlag, input.getIayGrunnlag()) ?
            Optional.of(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING) : Optional.empty();
    }

}
