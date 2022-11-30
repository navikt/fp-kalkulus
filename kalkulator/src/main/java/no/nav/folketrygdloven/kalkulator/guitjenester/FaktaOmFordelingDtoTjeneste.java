package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelingDto;

public class FaktaOmFordelingDtoTjeneste {

    private FaktaOmFordelingDtoTjeneste() {
        // Skjul
    }

    public static Optional<FordelingDto> lagDto(BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagTilstand tilstandForAktivtGrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand();
        if (!tilstandForAktivtGrunnlag.erFør(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING)) {
            FordelingDto dto = new FordelingDto();
            FordelBeregningsgrunnlagDtoTjeneste.lagDto(input, dto);
            VurderStortingsperiodeDtoTjeneste.lagDto(input).ifPresent(dto::setVurderRepresentererStortinget);
            return Optional.of(dto);
        }
        return Optional.empty();
    }

}
