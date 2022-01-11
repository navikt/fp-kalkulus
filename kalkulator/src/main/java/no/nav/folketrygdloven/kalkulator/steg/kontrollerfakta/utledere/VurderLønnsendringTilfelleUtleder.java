package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


@ApplicationScoped
@FagsakYtelseTypeRef()
@FaktaOmBeregningTilfelleRef("VURDER_LØNNSENDRING")
public class VurderLønnsendringTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
            BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        if (KonfigurasjonVerdi.get("AUTOMATISK_BEREGNE_LONNSENDRING",false)) {
            return LønnsendringTjeneste.brukerHarHattLønnsendringISisteMånedOgManglerInntektsmelding(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger()) ?
                    Optional.of(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING) : Optional.empty();
        } else {
            return LønnsendringTjeneste.brukerHarHattLønnsendringIHeleBeregningsperiodenOgManglerInntektsmelding(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger()) ?
                    Optional.of(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING) : Optional.empty();
        }
    }

}
