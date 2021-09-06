package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k14;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.FastsettSkjæringstidspunktOgStatuser;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.RegelFastsettSkjæringstidspunkt;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.status.RegelFastsettStatusVedSkjæringstidspunkt;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
@FagsakYtelseTypeRef("SVP")
public class FastsettSkjæringstidspunktOgStatuserK14 implements FastsettSkjæringstidspunktOgStatuser {

    protected MapBGSkjæringstidspunktOgStatuserFraRegelTilVL mapFraRegel;

    public FastsettSkjæringstidspunktOgStatuserK14() {
        // CDI
    }

    @Inject
    public FastsettSkjæringstidspunktOgStatuserK14(MapBGSkjæringstidspunktOgStatuserFraRegelTilVL mapFraRegel) {
        this.mapFraRegel = mapFraRegel;
    }

    @Override
    public BeregningsgrunnlagRegelResultat fastsett(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktivitetAggregat, InntektArbeidYtelseGrunnlagDto iayGrunnlag, List<Grunnbeløp> grunnbeløpSatser) {
        AktivitetStatusModell regelmodell = MapBGStatuserFraVLTilRegel.map(input.getInntektsmeldinger(), beregningAktivitetAggregat);
        RegelResultat regelResultatFastsettSkjæringstidspunkt = fastsettSkjæringstidspunkt(regelmodell);
        RegelResultat regelResultatFastsettStatus = fastsettStatus(regelmodell);

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation)
        List<RegelResultat> regelResultater = List.of(
                regelResultatFastsettSkjæringstidspunkt,
                regelResultatFastsettStatus);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = mapFraRegel.mapForSkjæringstidspunktOgStatuser(input.getKoblingReferanse(), regelmodell, regelResultater, iayGrunnlag, grunnbeløpSatser);
        return new BeregningsgrunnlagRegelResultat(nyttBeregningsgrunnlag,
                new RegelSporingAggregat(
                        mapRegelSporingGrunnlag(regelResultatFastsettSkjæringstidspunkt, BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT),
                        mapRegelSporingGrunnlag(regelResultatFastsettStatus, BeregningsgrunnlagRegelType.BRUKERS_STATUS)));
    }

    private RegelResultat fastsettSkjæringstidspunkt(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        String inputSkjæringstidspunkt = toJson(regelmodell);
        Evaluation evaluationSkjæringstidspunkt = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        return RegelmodellOversetter.getRegelResultat(evaluationSkjæringstidspunkt, inputSkjæringstidspunkt);
    }

    private RegelResultat fastsettStatus(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        String inputStatusFastsetting = toJson(regelmodell);
        Evaluation evaluationStatusFastsetting = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);
        return RegelmodellOversetter.getRegelResultat(evaluationStatusFastsetting, inputStatusFastsetting);
    }

    private static String toJson(AktivitetStatusModell grunnlag) {
        return JsonMapper.toJson(grunnlag, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }
}
