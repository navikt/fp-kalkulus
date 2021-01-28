package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.felles.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


@ApplicationScoped
@FagsakYtelseTypeRef("FP")
@FagsakYtelseTypeRef("SVP")
@FagsakYtelseTypeRef("PSB")
@FaktaOmBeregningTilfelleRef("VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT")
public class VurderRefusjonskravTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        if (!InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
            input.getKoblingReferanse(),
            input.getIayGrunnlag(),
            beregningsgrunnlagGrunnlag,
            input.getRefusjonskravDatoer()).isEmpty()) {
            return Optional.of(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT);
        }
        return Optional.empty();
    }
}
