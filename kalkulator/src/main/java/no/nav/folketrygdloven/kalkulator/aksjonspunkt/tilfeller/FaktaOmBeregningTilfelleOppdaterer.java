package no.nav.folketrygdloven.kalkulator.aksjonspunkt.tilfeller;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;

/**
 * Interface for oppdaterere for fakta om beregning tilfeller.
 *
 */
public interface FaktaOmBeregningTilfelleOppdaterer {

    /**
     * Oppdaterer beregningsgrunnlaget med verdier fastsatt i gui og lager historikkinnslag.
     *
     * Metoder som overrider denne MÅ sjekke om dto.getFaktaOmBeregningTilfeller() inneholder aktuelt tilfelle.
     * @param dto Fakta om beregning dto sendt ned fra frontend
     * @param forrigeBg Beregningsgrunnlag fra forrige avklaring av fakta om beregning
     * @param input
     * @param grunnlagBuilder
     */
    void oppdater(FaktaBeregningLagreDto dto,
                  Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder);

}
