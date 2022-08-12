package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeATFL.fastsettBeregningsperiodeForATFL;
import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeForLønnsendring.fastsettBeregningsperiodeForLønnsendring;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.SVANGERSKAPSPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_SYKT_BARN)
@FagsakYtelseTypeRef(FagsakYtelseType.OPPLÆRINGSPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE)
@FagsakYtelseTypeRef(FagsakYtelseType.OMSORGSPENGER)
public class FastsettBeregningsperiodeTjeneste implements FastsettBeregningsperiode {

    private final BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjeneste();

    public FastsettBeregningsperiodeTjeneste() {
    }

    public BeregningsgrunnlagDto fastsettBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                           InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                           Collection<InntektsmeldingDto> inntektsmeldinger) {
        // Fastsetter først for alle ATFL-andeler
        var fastsattForATFL = fastsettBeregningsperiodeForATFL(beregningsgrunnlag, beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt()));
        // Fastsetter for arbeidsforhold med lønnsendring innenfor siste 3 måneder før skjæringstidspunktet
        if (KonfigurasjonVerdi.get("AUTOMATISK_BEREGNE_LONNSENDRING", false)) {
            var fastsattForLønnsendring = fastsettBeregningsperiodeForLønnsendring(fastsattForATFL, inntektArbeidYtelseGrunnlag, inntektsmeldinger);
            return fastsattForLønnsendring;

        } else {
            return fastsattForATFL;
        }
    }


}
