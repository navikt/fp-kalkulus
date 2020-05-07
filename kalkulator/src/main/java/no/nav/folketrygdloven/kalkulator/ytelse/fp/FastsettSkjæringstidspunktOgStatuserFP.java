package no.nav.folketrygdloven.kalkulator.ytelse.fp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FastsettSkjæringstidspunktOgStatuser;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.RegelFastsettSkjæringstidspunkt;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFP;
import no.nav.folketrygdloven.skjæringstidspunkt.status.RegelFastsettStatusVedSkjæringstidspunkt;
import no.nav.folketrygdloven.skjæringstidspunkt.status.fp.RegelFastsettStatusVedSkjæringstidspunktFP;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
public class  FastsettSkjæringstidspunktOgStatuserFP extends FastsettSkjæringstidspunktOgStatuser {

    public FastsettSkjæringstidspunktOgStatuserFP() {
        // CDI
    }

    @Inject
    public FastsettSkjæringstidspunktOgStatuserFP(MapBGSkjæringstidspunktOgStatuserFraRegelTilVL mapFraRegel) {
        super(mapFraRegel);
    }

    @Override
    protected AktivitetStatusModell mapTilRegel(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        return new AktivitetStatusModellFP(MapBGStatuserFraVLTilRegel.map(beregningAktivitetAggregat));
    }

    @Override
    protected RegelResultat fastsettSkjæringstidspunkt(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        String inputSkjæringstidspunkt = toJson(regelmodell);
        Evaluation evaluationSkjæringstidspunkt = new RegelFastsettSkjæringstidspunkt().evaluer(regelmodell);
        return RegelmodellOversetter.getRegelResultat(evaluationSkjæringstidspunkt, inputSkjæringstidspunkt);
    }

    @Override
    protected RegelResultat fastsettStatus(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        String inputStatusFastsetting = toJson(regelmodell);
        Evaluation evaluationStatusFastsetting = new RegelFastsettStatusVedSkjæringstidspunktFP().evaluer(regelmodell);
        return RegelmodellOversetter.getRegelResultat(evaluationStatusFastsetting, inputStatusFastsetting);
    }

}
