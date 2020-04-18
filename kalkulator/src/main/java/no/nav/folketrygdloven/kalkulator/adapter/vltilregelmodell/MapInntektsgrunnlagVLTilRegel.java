package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;

public abstract class MapInntektsgrunnlagVLTilRegel {

    public MapInntektsgrunnlagVLTilRegel() {
        // CDI
    }

    public abstract Inntektsgrunnlag map(BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning);
}
