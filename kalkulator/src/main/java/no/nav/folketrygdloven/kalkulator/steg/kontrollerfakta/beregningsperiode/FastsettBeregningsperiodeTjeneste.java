package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeATFL.fastsettBeregningsperiodeForATFL;
import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeForLønnsendring.fastsettBeregningsperiodeForLønnsendring;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
@FagsakYtelseTypeRef("SVP")
@FagsakYtelseTypeRef("PSB")
@FagsakYtelseTypeRef("OMP")
public class FastsettBeregningsperiodeTjeneste implements FastsettBeregningsperiode {

    private final BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjeneste();

    public FastsettBeregningsperiodeTjeneste() {}

    public BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        // Fastsetter først for alle ATFL-andeler
        var fastsattForATFL = fastsettBeregningsperiodeForATFL(beregningsgrunnlag, beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt()));
        // Fastsetter for arbeidsforhold med lønnsendring innenfor siste 3 måneder før skjæringstidspunktet
        if (KonfigurasjonVerdi.get("AUTOMATISK_BERGNE_LØNNENDRING", false)) {
            var fastsattForLønnsendring = fastsettBeregningsperiodeForLønnsendring(fastsattForATFL, inntektArbeidYtelseGrunnlag);
            return fastsattForLønnsendring;

        } else {
            return fastsattForATFL;
        }
    }



}
