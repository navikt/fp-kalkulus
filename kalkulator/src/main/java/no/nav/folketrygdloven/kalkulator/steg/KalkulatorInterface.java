package no.nav.folketrygdloven.kalkulator.steg;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;

/**
 * Interface ut fra kalkulus. Metoder i dette interfacet tas i bruk av BeregningStegTjeneste i kalkulus og fp-sak.
 *
 * Hver metode i interfacet korresponderer til et steg i beregning
 *
 */
public interface KalkulatorInterface {

    /** Steg 1: Fastsetter beregningsaktiviteter og forslår skjæringstidspunkt
     *
     * @param input Input til steget
     * @return Beregningresultat med aksjonspunkter og eventuelt avslag på vilkår.
     */
    BeregningResultatAggregat fastsettBeregningsaktiviteter(FastsettBeregningsaktiviteterInput input);

    /** Steg 2: Kontroller fakta for beregning
     *
     * @param input Input til steget
     * @return Resultat med aksjonspunkter og nytt beregningsgrunnlag
     */
    BeregningResultatAggregat kontrollerFaktaBeregningsgrunnlag(FaktaOmBeregningInput input);

    /** Steg 3: Foreslå beregningsgrunnlag
     *
     *  Utfører beregning i henhold til kap 8 i folketrygdloven
     *
     * @param input Input til steget
     * @return Resultat med aksjonspunkter og nytt beregningsgrunnlag
     */
    BeregningResultatAggregat foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input);

    /** Steg 3.5: Foreslår besteberegning
     *
     * @param input Input til steget
     * @return Nytt beregningsgrunnlag
     */
    BeregningResultatAggregat foreslåBesteberegning(ForeslåBesteberegningInput input);

    /** Steg 4: Vurder refusjonskrav
     *
     * Vurderer beregningsgrunnlagvilkåret
     *
     * @param input Input til steget
     * @return Vurdering av refusjonskrav
     */
    BeregningResultatAggregat vurderRefusjonskravForBeregninggrunnlag(StegProsesseringInput input);

    /** Steg 5: Fordel beregningsgrunnlag
     *
     * @param input Input til steget
     * @return Nytt beregningsgrunnlag og aksjonspunkter
     */
    BeregningResultatAggregat fordelBeregningsgrunnlag(FordelBeregningsgrunnlagInput input);

    /** Steg 5: Fordel beregningsgrunnlag
     *
     * @param input Input til steget
     * @return Nytt beregningsgrunnlag og aksjonspunkter
     */
    // TODO TSF-1315 rename denne metoden til #fordelBeregningsgrunnlag og slett den eksisterende #fordelBeregningsgrunnlag metoden
    @SuppressWarnings("unused")
    BeregningResultatAggregat fordelBeregningsgrunnlagUtenPeriodisering(StegProsesseringInput input);

    /** Steg 6: Fastsett beregningsgrunnlag
     *
     * @param input Input til steget
     * @return Fastsatt beregningsgrunnlag
     */
    BeregningResultatAggregat fastsettBeregningsgrunnlag(StegProsesseringInput input);
}
