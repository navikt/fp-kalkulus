package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderNyoppstartetFLDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;


@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_NYOPPSTARTET_FL")
public class VurderNyoppstartetFLOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderNyoppstartetFLDto nyoppstartetDto = dto.getVurderNyoppstartetFL();
        FaktaAggregatDto.Builder faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        FaktaAktørDto.Builder faktaAktørBuilder = faktaAggregatBuilder.getFaktaAktørBuilder();
        faktaAktørBuilder.medErNyoppstartetFLFastsattAvSaksbehandler(nyoppstartetDto.erErNyoppstartetFL());
        faktaAggregatBuilder.medFaktaAktør(faktaAktørBuilder.build());
        grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
    }

}
