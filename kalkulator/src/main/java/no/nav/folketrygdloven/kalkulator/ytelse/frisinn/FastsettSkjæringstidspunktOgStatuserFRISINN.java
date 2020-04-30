package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.AksjonspunktUtlederForeslåBeregning;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FastsettSkjæringstidspunktOgStatuser;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFRISINN;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
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
    public BeregningsgrunnlagRegelResultat fastsett(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktivitetAggregat, InntektArbeidYtelseGrunnlagDto iayGrunnlag, List<Grunnbeløp> grunnbeløpSatser) {
        AktivitetStatusModell regelmodell = MapBGStatuserFraVLTilRegel.map(beregningAktivitetAggregat);
        RegelResultat regelResultatFastsettSkjæringstidspunkt = fastsettSkjæringstidspunkt(input, regelmodell);
        if (regelmodell.getSkjæringstidspunktForBeregning() == null) {
            return new BeregningsgrunnlagRegelResultat(null, AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(input, List.of(regelResultatFastsettSkjæringstidspunkt)));
        }
        RegelResultat regelResultatFastsettStatus = fastsettStatus(input, regelmodell);
        if (regelmodell.getBeregningsgrunnlagPrStatusListe() == null || regelmodell.getBeregningsgrunnlagPrStatusListe().isEmpty()) {
           return new BeregningsgrunnlagRegelResultat(null, List.of(BeregningAksjonspunktResultat.opprettFor(BeregningAksjonspunktDefinisjon.INGEN_AKTIVITETER)));
        }

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation)
        List<RegelResultat> regelResultater = List.of(
                regelResultatFastsettSkjæringstidspunkt,
                regelResultatFastsettStatus);
        return new BeregningsgrunnlagRegelResultat(mapFraRegel.mapForSkjæringstidspunktOgStatuser(input.getBehandlingReferanse(), regelmodell, regelResultater, iayGrunnlag, grunnbeløpSatser), Collections.emptyList());

    }

    private RegelResultat fastsettSkjæringstidspunkt(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
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

    private RegelResultat fastsettStatus(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        var inntektsgrunnlagMapper = new MapInntektsgrunnlagVLTilRegelFRISINN();
        Inntektsgrunnlag inntektsgrunnlag = inntektsgrunnlagMapper.map(input, regelmodell.getSkjæringstidspunktForOpptjening());
        AktivitetStatusModellFRISINN aktivitetStatusModellFRISINN = new AktivitetStatusModellFRISINN(inntektsgrunnlag, regelmodell);
        aktivitetStatusModellFRISINN.setInntektsgrunnlag(inntektsgrunnlag);
        String inputStatusFastsetting = toJson(aktivitetStatusModellFRISINN);
        Evaluation evaluationStatusFastsetting = new RegelFastsettStatusVedSkjæringstidspunktFRISINN().evaluer(aktivitetStatusModellFRISINN);
        return RegelmodellOversetter.getRegelResultat(evaluationStatusFastsetting, inputStatusFastsetting);
    }


}
