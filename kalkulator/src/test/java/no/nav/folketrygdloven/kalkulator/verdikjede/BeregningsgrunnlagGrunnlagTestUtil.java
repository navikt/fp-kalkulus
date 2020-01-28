package no.nav.folketrygdloven.kalkulator.verdikjede;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.BeregningsgrunnlagTilstand;

class BeregningsgrunnlagGrunnlagTestUtil {
    static BeregningsgrunnlagGrunnlagDto nyttGrunnlag(BeregningsgrunnlagGrunnlagDto grunnlag, BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand tilstand) {
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.of(grunnlag))
            .medBeregningsgrunnlag(beregningsgrunnlag)
            .build(tilstand);
    }
}
