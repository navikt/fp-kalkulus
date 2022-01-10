package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeATFL.fastsettBeregningsperiodeForATFL;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiode;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class FastsettBeregningsperiodeTjenesteFRISINN implements FastsettBeregningsperiode {

    private final BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjenesteFRISINN();

    public FastsettBeregningsperiodeTjenesteFRISINN() {
    }

    public BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger) {
        return fastsettBeregningsperiodeForATFL(beregningsgrunnlag, beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt()));
    }


}
