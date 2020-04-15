package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;

public abstract class MapInntektsgrunnlagVLTilRegel {

    public MapInntektsgrunnlagVLTilRegel() {
        // CDI
    }

    abstract Inntektsgrunnlag map(BehandlingReferanse referanse, BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning);
}
