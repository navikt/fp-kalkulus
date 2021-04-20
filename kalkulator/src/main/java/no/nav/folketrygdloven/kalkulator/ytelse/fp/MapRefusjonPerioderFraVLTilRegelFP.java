package no.nav.folketrygdloven.kalkulator.ytelse.fp;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelFP extends MapRefusjonPerioderFraVLTilRegel {

    @Override
    protected List<Gradering> mapUtbetalingsgrader(InntektsmeldingDto im, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return Collections.emptyList();
    }

    // For CDI (for håndtere at annotation propageres til subklasser)

}
