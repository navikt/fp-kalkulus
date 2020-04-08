package no.nav.folketrygdloven.kalkulator;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;


@ApplicationScoped
public class OpprettBeregningsgrunnlagTjeneste {

    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;

    protected OpprettBeregningsgrunnlagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public OpprettBeregningsgrunnlagTjeneste(FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste,
                                             FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser) {
        this.fastsettBeregningsgrunnlagPerioderTjeneste = fastsettBeregningsgrunnlagPerioderTjeneste;
        this.fastsettSkjæringstidspunktOgStatuser = fastsettSkjæringstidspunktOgStatuser;
    }

    /**
     * Henter inn grunnlagsdata om nødvendig
     * Oppretter og bygger beregningsgrunnlag for behandlingen
     * Oppretter perioder og andeler på beregningsgrunnlag
     * Setter inntektskategori på andeler
     * Splitter perioder basert på refusjon, gradering og naturalytelse.
     *
     * @param input en {@link BeregningsgrunnlagInput}
     */
    BeregningsgrunnlagDto opprettOgLagreBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var ref = input.getBehandlingReferanse();
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        BeregningAktivitetAggregatDto beregningAktiviteter = grunnlag.getGjeldendeAktiviteter();

        BeregningsgrunnlagDto bgMedAndeler = fastsettSkjæringstidspunktOgStatuser.fastsett(ref, beregningAktiviteter, input.getIayGrunnlag(), input.getGrunnbeløpsatser());
        BehandlingReferanse refMedSkjæringstidspunkt = ref
            .medSkjæringstidspunkt(oppdaterSkjæringstidspunktForBeregning(ref, beregningAktiviteter, bgMedAndeler));
        FastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(bgMedAndeler, input.getIayGrunnlag());
        BeregningsgrunnlagInput newInput = input.medBehandlingReferanse(refMedSkjæringstidspunkt);
        BeregningsgrunnlagDto bgMedPerioder = fastsettBeregningsgrunnlagPerioderTjeneste.fastsettPerioderForNaturalytelse(newInput, bgMedAndeler);
        BeregningsgrunnlagVerifiserer.verifiserOppdatertBeregningsgrunnlag(bgMedPerioder);
        return bgMedPerioder;
    }

    private Skjæringstidspunkt oppdaterSkjæringstidspunktForBeregning(BehandlingReferanse ref,
                                                                      BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                      BeregningsgrunnlagDto beregningsgrunnlag) {
        return Skjæringstidspunkt.builder()
            .medSkjæringstidspunktOpptjening(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening())
            .medSkjæringstidspunktBeregning(beregningsgrunnlag.getSkjæringstidspunkt()).build();
    }

    BeregningsgrunnlagDto fastsettSkjæringstidspunktOgStatuser(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return fastsettSkjæringstidspunktOgStatuser.fastsett(input.getBehandlingReferanse(), beregningAktiviteter, iayGrunnlag, input.getGrunnbeløpsatser());
    }

}
