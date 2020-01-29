package no.nav.folketrygdloven.kalkulator;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.RegelFastsettSkjæringstidspunkt;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.status.RegelFastsettStatusVedSkjæringstidspunkt;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class FastsettSkjæringstidspunktOgStatuser {

    private FastsettSkjæringstidspunktOgStatuser() {
        // Skjul
    }

    public static BeregningsgrunnlagDto fastsett(BehandlingReferanse ref, BeregningAktivitetAggregatDto beregningAktivitetAggregat, InntektArbeidYtelseGrunnlagDto iayGrunnlag, List<Grunnbeløp> grunnbeløpSatser) {
        AktivitetStatusModell regelmodell = MapBGStatuserFraVLTilRegel.map(beregningAktivitetAggregat);

        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        String inputSkjæringstidspunkt = toJson(regelmodell);
        Evaluation evaluationSkjæringstidspunkt = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);

        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        String inputStatusFastsetting = toJson(regelmodell);
        Evaluation evaluationStatusFastsetting = new RegelFastsettStatusVedSkjæringstidspunkt().evaluer(regelmodell);

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation)
        List<RegelResultat> regelResultater = List.of(
            RegelmodellOversetter.getRegelResultat(evaluationSkjæringstidspunkt, inputSkjæringstidspunkt),
            RegelmodellOversetter.getRegelResultat(evaluationStatusFastsetting, inputStatusFastsetting));
        return MapBGSkjæringstidspunktOgStatuserFraRegelTilVL.mapForSkjæringstidspunktOgStatuser(ref, regelmodell, regelResultater, iayGrunnlag, grunnbeløpSatser);
    }

    private static String toJson(AktivitetStatusModell grunnlag) {
        return JacksonJsonConfig.toJson(grunnlag, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }
}
