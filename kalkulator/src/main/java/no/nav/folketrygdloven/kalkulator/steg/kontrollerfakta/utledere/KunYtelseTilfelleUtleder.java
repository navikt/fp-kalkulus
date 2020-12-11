package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.KontrollerFaktaBeregningTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


@ApplicationScoped
@FagsakYtelseTypeRef("*")
@FaktaOmBeregningTilfelleRef("FASTSETT_BG_KUN_YTELSE")
public class KunYtelseTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        return KontrollerFaktaBeregningTjeneste.harAktivitetStatusKunYtelse(beregningsgrunnlag) ?
            Optional.of(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE) : Optional.empty();
    }

}
