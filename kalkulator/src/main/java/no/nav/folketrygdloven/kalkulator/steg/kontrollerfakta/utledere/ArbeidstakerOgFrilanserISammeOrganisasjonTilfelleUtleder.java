package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


@ApplicationScoped
@FagsakYtelseTypeRef()
@FaktaOmBeregningTilfelleRef("VURDER_AT_OG_FL_I_SAMME_ORGANISASJON")
public class ArbeidstakerOgFrilanserISammeOrganisasjonTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        boolean erATFLISammeOrg = KontrollerFaktaBeregningFrilanserTjeneste.erBrukerArbeidstakerOgFrilanserISammeOrganisasjon(
                beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null), input.getIayGrunnlag());
        return erATFLISammeOrg ? Optional.of(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON) : Optional.empty();
    }

}
