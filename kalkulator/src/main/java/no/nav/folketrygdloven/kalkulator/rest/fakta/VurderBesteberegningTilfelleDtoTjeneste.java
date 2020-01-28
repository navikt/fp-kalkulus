package no.nav.folketrygdloven.kalkulator.rest.fakta;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.VurderBesteberegningDto;

@ApplicationScoped
public class VurderBesteberegningTilfelleDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {


    VurderBesteberegningTilfelleDtoTjeneste() {
        // For CDI
    }

    @Override
    public void lagDto(BeregningsgrunnlagRestInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        if (!harBgTilfelle(input.getBeregningsgrunnlag())) {
            return;
        }
        BeregningsgrunnlagTilstand aktivTilstand = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand();
        settVerdier(input.getBeregningsgrunnlag(), aktivTilstand, faktaOmBeregningDto);
    }

    private void settVerdier(BeregningsgrunnlagRestDto bg, BeregningsgrunnlagTilstand aktivTilstand, FaktaOmBeregningDto faktaOmBeregningDto) {
        VurderBesteberegningDto vurderBesteberegning = new VurderBesteberegningDto();
        vurderBesteberegning.setSkalHaBesteberegning(harBesteberegning(bg, aktivTilstand));
        faktaOmBeregningDto.setVurderBesteberegning(vurderBesteberegning);
    }

    private boolean harBgTilfelle(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        return beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING)
            || beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE);
    }

    private Boolean harBesteberegning(BeregningsgrunnlagRestDto beregningsgrunnlag, BeregningsgrunnlagTilstand aktivTilstand) {
        if (aktivTilstand.erFør(BeregningsgrunnlagTilstand.KOFAKBER_UT)) {
            return null;
        }
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()).anyMatch(andel -> andel.getBesteberegningPrÅr() != null);
    }
}
