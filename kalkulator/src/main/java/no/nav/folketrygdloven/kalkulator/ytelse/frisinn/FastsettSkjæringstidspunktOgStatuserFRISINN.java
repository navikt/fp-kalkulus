package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FastsettSkjæringstidspunktOgStatuser;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFRISINN;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.RegelFastsettSkjæringstidspunktFrisinn;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn.RegelFastsettStatusVedSkjæringstidspunktFRISINN;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class FastsettSkjæringstidspunktOgStatuserFRISINN extends FastsettSkjæringstidspunktOgStatuser {

    public FastsettSkjæringstidspunktOgStatuserFRISINN() {
        super();
    }

    @Inject
    public FastsettSkjæringstidspunktOgStatuserFRISINN(MapBGSkjæringstidspunktOgStatuserFraRegelTilVL mapFraRegel) {
        super(mapFraRegel);
    }

    @Override
    protected RegelResultat fastsettSkjæringstidspunkt(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        var inntektsgrunnlagMapper = new MapInntektsgrunnlagVLTilRegelFRISINN();
        Inntektsgrunnlag inntektsgrunnlag = inntektsgrunnlagMapper.map(input, regelmodell.getSkjæringstidspunktForOpptjening());
        AktivitetStatusModellFRISINN aktivitetStatusModellFRISINN = new AktivitetStatusModellFRISINN(inntektsgrunnlag, regelmodell);
        aktivitetStatusModellFRISINN.setInntektsgrunnlag(inntektsgrunnlag);
        String inputSkjæringstidspunkt = toJson(aktivitetStatusModellFRISINN);
        Evaluation evaluationSkjæringstidspunkt = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(aktivitetStatusModellFRISINN);
        regelmodell.setSkjæringstidspunktForBeregning(aktivitetStatusModellFRISINN.getSkjæringstidspunktForBeregning());
        return RegelmodellOversetter.getRegelResultat(evaluationSkjæringstidspunkt, inputSkjæringstidspunkt);
    }

    @Override
    protected RegelResultat fastsettStatus(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        String inputStatusFastsetting = toJson(regelmodell);
        Evaluation evaluationStatusFastsetting = new RegelFastsettStatusVedSkjæringstidspunktFRISINN().evaluer(regelmodell);
        return RegelmodellOversetter.getRegelResultat(evaluationStatusFastsetting, inputStatusFastsetting);
    }


}
