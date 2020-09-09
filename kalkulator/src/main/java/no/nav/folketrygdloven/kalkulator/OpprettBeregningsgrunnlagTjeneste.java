package no.nav.folketrygdloven.kalkulator;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;


@ApplicationScoped
public class OpprettBeregningsgrunnlagTjeneste {

    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;
    private Instance<FastsettSkjæringstidspunktOgStatuser> fastsettSkjæringstidspunktOgStatuser;

    protected OpprettBeregningsgrunnlagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public OpprettBeregningsgrunnlagTjeneste(FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste,
                                             @Any Instance<FastsettSkjæringstidspunktOgStatuser> fastsettSkjæringstidspunktOgStatuser) {
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
        var ref = input.getKoblingReferanse();
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        BeregningAktivitetAggregatDto beregningAktiviteter = grunnlag.getGjeldendeAktiviteter();

        BeregningsgrunnlagDto bgMedAndeler = FagsakYtelseTypeRef.Lookup.find(fastsettSkjæringstidspunktOgStatuser, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke FastsettSkjæringstidspunktOgStatuser for ytelsetype " + input.getFagsakYtelseType().getKode()))
                .fastsett(input, beregningAktiviteter, input.getIayGrunnlag(), input.getGrunnbeløpsatser())
                .getBeregningsgrunnlag();

        KoblingReferanse refMedSkjæringstidspunkt = ref
            .medSkjæringstidspunkt(oppdaterSkjæringstidspunktForBeregning(beregningAktiviteter, bgMedAndeler));
        FastsettInntektskategoriFraSøknadTjeneste.fastsettInntektskategori(bgMedAndeler, input.getIayGrunnlag());
        BeregningsgrunnlagInput newInput = input.medBehandlingReferanse(refMedSkjæringstidspunkt);
        BeregningsgrunnlagDto bgMedPerioder = fastsettBeregningsgrunnlagPerioderTjeneste.fastsettPerioderForNaturalytelse(newInput, bgMedAndeler);
        BeregningsgrunnlagVerifiserer.verifiserOppdatertBeregningsgrunnlag(bgMedPerioder);
        return bgMedPerioder;
    }

    private Skjæringstidspunkt oppdaterSkjæringstidspunktForBeregning(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                      BeregningsgrunnlagDto beregningsgrunnlag) {
        return Skjæringstidspunkt.builder()
            .medSkjæringstidspunktOpptjening(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening())
            .medSkjæringstidspunktBeregning(beregningsgrunnlag.getSkjæringstidspunkt()).build();
    }

    BeregningsgrunnlagRegelResultat fastsettSkjæringstidspunktOgStatuser(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return FagsakYtelseTypeRef.Lookup.find(fastsettSkjæringstidspunktOgStatuser, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke FastsettSkjæringstidspunktOgStatuser for ytelsetype " + input.getFagsakYtelseType().getKode()))
                .fastsett(input, beregningAktiviteter, iayGrunnlag, input.getGrunnbeløpsatser());
    }

}
