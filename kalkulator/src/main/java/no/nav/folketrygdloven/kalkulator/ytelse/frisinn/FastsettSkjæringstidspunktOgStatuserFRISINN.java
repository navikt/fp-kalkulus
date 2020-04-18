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
    protected RegelResultat fastsettSkjæringstidspunkt(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        String inputSkjæringstidspunkt = toJson(regelmodell);
        Evaluation evaluationSkjæringstidspunkt = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(regelmodell);
        return RegelmodellOversetter.getRegelResultat(evaluationSkjæringstidspunkt, inputSkjæringstidspunkt);
    }

    @Override
    protected RegelResultat fastsettStatus(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        var inntektsgrunnlagMapper = new MapInntektsgrunnlagVLTilRegelFRISINN();
        Inntektsgrunnlag inntektsgrunnlag = inntektsgrunnlagMapper.map(input, regelmodell.getSkjæringstidspunktForBeregning());
        AktivitetStatusModellFRISINN aktivitetStatusModellFRISINN = new AktivitetStatusModellFRISINN(inntektsgrunnlag, regelmodell);
        aktivitetStatusModellFRISINN.setInntektsgrunnlag(inntektsgrunnlag);
        String inputStatusFastsetting = toJson(aktivitetStatusModellFRISINN);
        Evaluation evaluationStatusFastsetting = new RegelFastsettStatusVedSkjæringstidspunktFRISINN().evaluer(aktivitetStatusModellFRISINN);
        return RegelmodellOversetter.getRegelResultat(evaluationStatusFastsetting, inputStatusFastsetting);
    }


}
