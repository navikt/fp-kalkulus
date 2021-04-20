package no.nav.folketrygdloven.kalkulator.ytelse.k9;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapRefusjonPerioderFraVLTilRegelUtbgrad;

@FagsakYtelseTypeRef("OMP")
@FagsakYtelseTypeRef("PSB")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelK9 extends MapRefusjonPerioderFraVLTilRegelUtbgrad {

    @Override
    protected List<Gradering> mapUtbetalingsgrader(InntektsmeldingDto im, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return Collections.emptyList();
    }

    @Override
    protected void mapFristData(BeregningsgrunnlagInput input, InntektsmeldingDto inntektsmelding, ArbeidsforholdOgInntektsmelding.Builder builder) {
        // Skal ikkje vurdere frist for k9
    }

}
