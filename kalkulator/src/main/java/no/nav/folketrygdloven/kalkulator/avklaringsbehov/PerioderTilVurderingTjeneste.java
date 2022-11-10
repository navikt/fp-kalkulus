package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class PerioderTilVurderingTjeneste {

    private final List<Intervall> forlengelseperioder;
    private final List<Intervall> beregningsgrunnlagsperioder;

    public PerioderTilVurderingTjeneste(List<Intervall> forlengelseperioder, BeregningsgrunnlagDto beregningsgrunnlag) {
        this.forlengelseperioder = forlengelseperioder;
        this.beregningsgrunnlagsperioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).toList();
    }

    public boolean erTilVurdering(Intervall periode) {
        if (!KonfigurasjonVerdi.get("KOPIERING_VED_FORLENGELSE", false)) {
            return true;
        }
        return finnPerioderTilVurdering().stream().anyMatch(periode::overlapper);
    }

    private List<Intervall> finnPerioderTilVurdering() {
        return forlengelseperioder == null || forlengelseperioder.isEmpty() ? beregningsgrunnlagsperioder :
                filtrerKunForlengelse(beregningsgrunnlagsperioder, forlengelseperioder);
    }

    private List<Intervall> filtrerKunForlengelse(List<Intervall> beregningsgrunnlagsperioder, List<Intervall> forlengelseperioder) {
        return beregningsgrunnlagsperioder.stream().filter(p -> forlengelseperioder.stream().anyMatch(p::overlapper)).toList();
    }


}
