package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag.YtelsespesifiktGrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;

public class YtelsespesifiktGrunnlagTjenesteMock implements YtelsespesifiktGrunnlagTjeneste {

    public YtelsespesifiktGrunnlagTjenesteMock() {
        // CDI
    }

    @Override
    public Optional<YtelsespesifiktGrunnlagDto> map(BeregningsgrunnlagRestInput input) {
        return Optional.empty();
    }
}
