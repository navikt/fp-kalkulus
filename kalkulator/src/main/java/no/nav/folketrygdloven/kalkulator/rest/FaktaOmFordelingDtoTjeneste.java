package no.nav.folketrygdloven.kalkulator.rest;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.rest.dto.FordelingDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

public class FaktaOmFordelingDtoTjeneste {

    private FaktaOmFordelingDtoTjeneste() {
        // Skjul
    }

    public static Optional<FordelingDto> lagDto(BeregningsgrunnlagRestInput input) {
        BeregningsgrunnlagTilstand tilstandForAktivtGrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand();
        if (!tilstandForAktivtGrunnlag.erFør(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING)) {
            FordelingDto dto = new FordelingDto();
            FordelBeregningsgrunnlagDtoTjeneste.lagDto(input, dto);
            return Optional.of(dto);
        }
        return Optional.empty();
    }

}
