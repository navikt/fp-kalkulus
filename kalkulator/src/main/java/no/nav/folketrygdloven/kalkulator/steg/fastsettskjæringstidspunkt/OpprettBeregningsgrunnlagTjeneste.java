package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;


import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagVerifiserer;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.FastsettInntektskategoriTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiode;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFakta;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering.FastsettBeregningsgrunnlagPerioderTjeneste;


@ApplicationScoped
public class OpprettBeregningsgrunnlagTjeneste {

    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;
    private Instance<FastsettSkjæringstidspunktOgStatuser> fastsettSkjæringstidspunktOgStatuser;
    private Instance<FastsettBeregningsperiode> fastsettBeregningsperiodeTjeneste;
    private Instance<FastsettFakta> fastsettFaktaTjeneste;

    protected OpprettBeregningsgrunnlagTjeneste() {
        // for CDI proxy
    }

    @Inject
    public OpprettBeregningsgrunnlagTjeneste(FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPerioderTjeneste,
                                             @Any Instance<FastsettSkjæringstidspunktOgStatuser> fastsettSkjæringstidspunktOgStatuser,
                                             @Any Instance<FastsettBeregningsperiode> fastsettBeregningsperiodeTjeneste,
                                             @Any Instance<FastsettFakta> fastsettFaktaTjeneste) {
        this.fastsettBeregningsgrunnlagPerioderTjeneste = fastsettBeregningsgrunnlagPerioderTjeneste;
        this.fastsettSkjæringstidspunktOgStatuser = fastsettSkjæringstidspunktOgStatuser;
        this.fastsettBeregningsperiodeTjeneste = fastsettBeregningsperiodeTjeneste;
        this.fastsettFaktaTjeneste = fastsettFaktaTjeneste;
    }

    /**
     * Henter inn grunnlagsdata om nødvendig
     * Oppretter og bygger beregningsgrunnlag for behandlingen
     * Oppretter perioder og andeler på beregningsgrunnlag
     * Setter inntektskategori på andeler
     * Splitter perioder basert på naturalytelse.
     *
     * @param input en {@link BeregningsgrunnlagInput}
     */
    public BeregningsgrunnlagRegelResultat opprettOgLagreBeregningsgrunnlag(FaktaOmBeregningInput input) {
        var ref = input.getKoblingReferanse();
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();
        BeregningAktivitetAggregatDto beregningAktiviteter = grunnlag.getGjeldendeAktiviteter();

        var resultatMedAndeler = FagsakYtelseTypeRef.Lookup.find(fastsettSkjæringstidspunktOgStatuser, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke FastsettSkjæringstidspunktOgStatuser for ytelsetype " + input.getFagsakYtelseType().getKode()))
                .fastsett(input, beregningAktiviteter, input.getIayGrunnlag(), input.getGrunnbeløpsatser());

        KoblingReferanse refMedSkjæringstidspunkt = ref
            .medSkjæringstidspunkt(oppdaterSkjæringstidspunktForBeregning(beregningAktiviteter, resultatMedAndeler.getBeregningsgrunnlag()));

        // Fastsett inntektskategorier
        FastsettInntektskategoriTjeneste.fastsettInntektskategori(resultatMedAndeler.getBeregningsgrunnlag(), input.getIayGrunnlag());

        // Fastsett beregningsperiode
        var medFastsattBeregningsperiode = FagsakYtelseTypeRef.Lookup.find(fastsettBeregningsperiodeTjeneste, input.getFagsakYtelseType()).orElseThrow()
                .fastsettBeregningsperiode(resultatMedAndeler.getBeregningsgrunnlag(), input.getIayGrunnlag());

        // Fastsett fakta
        Optional<FaktaAggregatDto> faktaAggregatDto = FagsakYtelseTypeRef.Lookup.find(fastsettFaktaTjeneste, input.getFagsakYtelseType())
                .flatMap(t -> t.fastsettFakta(medFastsattBeregningsperiode, input.getIayGrunnlag()));

        BeregningsgrunnlagInput newInput = input.medBehandlingReferanse(refMedSkjæringstidspunkt);
        var resultatMedNaturalytelse = fastsettBeregningsgrunnlagPerioderTjeneste.fastsettPerioderForNaturalytelse(newInput, medFastsattBeregningsperiode);
        BeregningsgrunnlagVerifiserer.verifiserOppdatertBeregningsgrunnlag(resultatMedNaturalytelse.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(resultatMedNaturalytelse.getBeregningsgrunnlag(),
                faktaAggregatDto.orElse(null),
                RegelSporingAggregat.konkatiner(resultatMedAndeler.getRegelsporinger().orElse(null),
                        resultatMedNaturalytelse.getRegelsporinger().orElse(null)));
    }



    private Skjæringstidspunkt oppdaterSkjæringstidspunktForBeregning(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                      BeregningsgrunnlagDto beregningsgrunnlag) {
        return Skjæringstidspunkt.builder()
            .medSkjæringstidspunktOpptjening(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening())
            .medSkjæringstidspunktBeregning(beregningsgrunnlag.getSkjæringstidspunkt()).build();
    }

    public BeregningsgrunnlagRegelResultat fastsettSkjæringstidspunktOgStatuser(FastsettBeregningsaktiviteterInput input, BeregningAktivitetAggregatDto beregningAktiviteter, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return FagsakYtelseTypeRef.Lookup.find(fastsettSkjæringstidspunktOgStatuser, input.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Fant ikke FastsettSkjæringstidspunktOgStatuser for ytelsetype " + input.getFagsakYtelseType().getKode()))
                .fastsett(input, beregningAktiviteter, iayGrunnlag, input.getGrunnbeløpsatser());
    }

}
