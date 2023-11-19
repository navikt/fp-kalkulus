package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeATFL.fastsettBeregningsperiodeForATFL;
import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeForLønnsendring.fastsettBeregningsperiodeForLønnsendring;

import java.util.Collection;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

public class FastsettBeregningsperiodeTjeneste {

    public BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                           Collection<InntektsmeldingDto> inntektsmeldinger) {
        // Fastsetter først for alle ATFL-andeler
        var fastsattForATFL = fastsettBeregningsperiodeForATFL(beregningsgrunnlag, new BeregningsperiodeTjeneste().fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt()));
        // Fastsetter for arbeidsforhold med lønnsendring innenfor siste 3 måneder før skjæringstidspunktet
        if (KonfigurasjonVerdi.get("AUTOMATISK_BEREGNE_LONNSENDRING", false) || KonfigurasjonVerdi.get("AUTOMATISK_BEREGNE_LONNSENDRING_V2", false)) {
            var fastsattForLønnsendring = fastsettBeregningsperiodeForLønnsendring(fastsattForATFL, inntektArbeidYtelseGrunnlag, inntektsmeldinger);
            return fastsattForLønnsendring;

        } else {
            return fastsattForATFL;
        }
    }


}
