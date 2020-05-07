package no.nav.folketrygdloven.kalkulator;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.RegelFastsettSkjæringstidspunkt;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.status.RegelFastsettStatusVedSkjæringstidspunkt;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class FastsettSkjæringstidspunktOgStatuser {

    protected MapBGSkjæringstidspunktOgStatuserFraRegelTilVL mapFraRegel;

    public FastsettSkjæringstidspunktOgStatuser() {
        // CDI
    }

    @Inject
    public FastsettSkjæringstidspunktOgStatuser(MapBGSkjæringstidspunktOgStatuserFraRegelTilVL mapFraRegel) {
        this.mapFraRegel = mapFraRegel;
    }

    public BeregningsgrunnlagRegelResultat fastsett(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktivitetAggregat, InntektArbeidYtelseGrunnlagDto iayGrunnlag, List<Grunnbeløp> grunnbeløpSatser) {
        AktivitetStatusModell regelmodell = mapTilRegel(beregningAktivitetAggregat);
        RegelResultat regelResultatFastsettSkjæringstidspunkt = fastsettSkjæringstidspunkt(input, regelmodell);
        RegelResultat regelResultatFastsettStatus = fastsettStatus(regelmodell);

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation)
        List<RegelResultat> regelResultater = List.of(
                regelResultatFastsettSkjæringstidspunkt,
                regelResultatFastsettStatus);
        return new BeregningsgrunnlagRegelResultat(mapFraRegel.mapForSkjæringstidspunktOgStatuser(input.getBehandlingReferanse(), regelmodell, regelResultater, iayGrunnlag, grunnbeløpSatser), Collections.emptyList());
    }

    protected AktivitetStatusModell mapTilRegel(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        return MapBGStatuserFraVLTilRegel.map(beregningAktivitetAggregat);
    }

    protected RegelResultat fastsettSkjæringstidspunkt(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        String inputSkjæringstidspunkt = toJson(regelmodell);
        Evaluation evaluationSkjæringstidspunkt = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        return RegelmodellOversetter.getRegelResultat(evaluationSkjæringstidspunkt, inputSkjæringstidspunkt);
    }

    protected RegelResultat fastsettStatus(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        String inputStatusFastsetting = toJson(regelmodell);
        Evaluation evaluationStatusFastsetting = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);
        return RegelmodellOversetter.getRegelResultat(evaluationStatusFastsetting, inputStatusFastsetting);
    }

    protected static String toJson(AktivitetStatusModell grunnlag) {
        return JsonMapper.toJson(grunnlag, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }
}
