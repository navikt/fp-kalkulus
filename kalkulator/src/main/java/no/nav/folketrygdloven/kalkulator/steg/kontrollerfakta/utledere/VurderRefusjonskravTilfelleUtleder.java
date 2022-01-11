package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


@ApplicationScoped
@FagsakYtelseTypeRef("FP")
@FagsakYtelseTypeRef("SVP")
@FagsakYtelseTypeRef("PSB")
@FagsakYtelseTypeRef("PPN")
@FaktaOmBeregningTilfelleRef("VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT")
public class VurderRefusjonskravTilfelleUtleder implements TilfelleUtleder {

    private InntektsmeldingMedRefusjonTjeneste inntektsmeldingMedRefusjonTjeneste;

    public VurderRefusjonskravTilfelleUtleder() {
    }

    @Inject
    public VurderRefusjonskravTilfelleUtleder(InntektsmeldingMedRefusjonTjeneste inntektsmeldingMedRefusjonTjeneste) {
        this.inntektsmeldingMedRefusjonTjeneste = inntektsmeldingMedRefusjonTjeneste;
    }

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        if (!inntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                input.getKoblingReferanse(),
                input.getIayGrunnlag(),
                beregningsgrunnlagGrunnlag,
                input.getKravPrArbeidsgiver(),
                input.getFagsakYtelseType()).isEmpty()) {
            return Optional.of(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT);
        }
        return Optional.empty();
    }
}
