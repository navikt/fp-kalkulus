package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;

public final class GrunnbeløpMapper {
    private GrunnbeløpMapper() {
    }

    public static List<Grunnbeløp> mapGrunnbeløpInput(List<Grunnbeløp> grunnbeløpene, List<GrunnbeløpInput> grunnbeløpInput) {
        if (grunnbeløpInput != null && !grunnbeløpInput.isEmpty()) {
            return grunnbeløpInput.stream().map(g -> new Grunnbeløp(g.fom(), g.tom(), g.gVerdi(), g.gSnitt())).collect(Collectors.toList());
        }
        return grunnbeløpene;
    }

    public static List<GrunnbeløpInput> mapTilGrunnbeløpInput(List<Grunnbeløp> grunnbeløpene, List<GrunnbeløpInput> grunnbeløpInput) {
        if (grunnbeløpInput != null && !grunnbeløpInput.isEmpty()) {
            return grunnbeløpInput;
        }
        return grunnbeløpene.stream().map(g -> new GrunnbeløpInput(g.getFom(), g.getTom(), g.getGVerdi(), g.getGSnitt())).collect(Collectors.toList());
    }
}
