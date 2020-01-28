package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderNyoppstartetFLDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_NYOPPSTARTET_FL")
public class VurderNyoppstartetFLOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderNyoppstartetFLDto nyoppstartetDto = dto.getVurderNyoppstartetFL();
        List<BeregningsgrunnlagPeriodeDto> perioder = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriodeDto bgPeriode : perioder) {
            BeregningsgrunnlagPrStatusOgAndelDto bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bpsa -> AktivitetStatus.FRILANSER.equals(bpsa.getAktivitetStatus()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Kunne ikke finne BeregningsgrunnlagPrStatusOgAndel for FRILANSER"));
            BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                .medNyoppstartet(nyoppstartetDto.erErNyoppstartetFL(), bgAndel.getAktivitetStatus())
                .build(bgPeriode);
        }
    }

}
