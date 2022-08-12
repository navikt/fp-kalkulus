package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL.mapForSkjæringstidspunktOgStatuser;

import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapInntektsgrunnlagVLTilRegelFRISINN;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.ytelse.FrisinnGrunnlagMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.FastsettSkjæringstidspunktOgStatuser;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.AvklaringsbehovUtlederForeslåBeregning;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.RegelFastsettSkjæringstidspunktFrisinn;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn.RegelFastsettStatusVedSkjæringstidspunktFRISINN;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.FRISINN)
public class FastsettSkjæringstidspunktOgStatuserFRISINN implements FastsettSkjæringstidspunktOgStatuser {

    private final FastsettBeregningsperiodeTjenesteFRISINN fastsettBeregningsperiodeTjeneste = new FastsettBeregningsperiodeTjenesteFRISINN();

    @Override
    public BeregningsgrunnlagRegelResultat fastsett(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktivitetAggregat, List<Grunnbeløp> grunnbeløpSatser) {
        AktivitetStatusModell regelmodell = MapBGStatuserFraVLTilRegel.map(input.getInntektsmeldinger(), beregningAktivitetAggregat, new YtelseFilterDto(input.getIayGrunnlag().getAktørYtelseFraRegister()).før(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening()));
        RegelResultat regelResultatFastsettSkjæringstidspunkt = fastsettSkjæringstidspunkt(input, regelmodell);
        if (regelmodell.getSkjæringstidspunktForBeregning() == null) {
            return new BeregningsgrunnlagRegelResultat(null, AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, List.of(regelResultatFastsettSkjæringstidspunkt)));
        }
        RegelResultat regelResultatFastsettStatus = fastsettStatus(input, regelmodell);
        if (regelmodell.getBeregningsgrunnlagPrStatusListe() == null || regelmodell.getBeregningsgrunnlagPrStatusListe().isEmpty()) {
            return new BeregningsgrunnlagRegelResultat(null, List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.INGEN_AKTIVITETER)));
        }

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation)
        List<RegelResultat> regelResultater = List.of(
                regelResultatFastsettSkjæringstidspunkt,
                regelResultatFastsettStatus);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = mapForSkjæringstidspunktOgStatuser(input.getKoblingReferanse(), regelmodell, regelResultater, input.getIayGrunnlag(), grunnbeløpSatser);
        var fastsattBeregningsperiode = fastsettBeregningsperiodeTjeneste.fastsettBeregningsperiode(nyttBeregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
        return new BeregningsgrunnlagRegelResultat(fastsattBeregningsperiode, Collections.emptyList());

    }

    private RegelResultat fastsettSkjæringstidspunkt(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        var inntektsgrunnlagMapper = new MapInntektsgrunnlagVLTilRegelFRISINN();
        Inntektsgrunnlag inntektsgrunnlag = inntektsgrunnlagMapper.map(input, regelmodell.getSkjæringstidspunktForOpptjening());
        AktivitetStatusModellFRISINN aktivitetStatusModellFRISINN = new AktivitetStatusModellFRISINN(inntektsgrunnlag,
                regelmodell,
                FrisinnGrunnlagMapper.mapFrisinnPerioder(input));
        aktivitetStatusModellFRISINN.setInntektsgrunnlag(inntektsgrunnlag);
        String inputSkjæringstidspunkt = toJson(aktivitetStatusModellFRISINN);
        Evaluation evaluationSkjæringstidspunkt = new RegelFastsettSkjæringstidspunktFrisinn().evaluer(aktivitetStatusModellFRISINN);
        regelmodell.setSkjæringstidspunktForBeregning(aktivitetStatusModellFRISINN.getSkjæringstidspunktForBeregning());
        return lagRegelresultat(inputSkjæringstidspunkt, evaluationSkjæringstidspunkt);
    }

    private RegelResultat lagRegelresultat(String inputSkjæringstidspunkt, Evaluation evaluationSkjæringstidspunkt) {
        if (!Resultat.JA.equals(evaluationSkjæringstidspunkt.result())) {
            throw new IllegalArgumentException("Utviklerfeil: Umulig utfall");
        }
        return RegelmodellOversetter.getRegelResultat(evaluationSkjæringstidspunkt, inputSkjæringstidspunkt);
    }

    private RegelResultat fastsettStatus(BeregningsgrunnlagInput input, AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med status per beregningsgrunnlag
        var inntektsgrunnlagMapper = new MapInntektsgrunnlagVLTilRegelFRISINN();
        Inntektsgrunnlag inntektsgrunnlag = inntektsgrunnlagMapper.map(input, regelmodell.getSkjæringstidspunktForOpptjening());
        AktivitetStatusModellFRISINN aktivitetStatusModellFRISINN = new AktivitetStatusModellFRISINN(inntektsgrunnlag,
                regelmodell,
                FrisinnGrunnlagMapper.mapFrisinnPerioder(input));
        aktivitetStatusModellFRISINN.setInntektsgrunnlag(inntektsgrunnlag);
        String inputStatusFastsetting = toJson(aktivitetStatusModellFRISINN);
        Evaluation evaluationStatusFastsetting = new RegelFastsettStatusVedSkjæringstidspunktFRISINN().evaluer(aktivitetStatusModellFRISINN);
        return RegelmodellOversetter.getRegelResultat(evaluationStatusFastsetting, inputStatusFastsetting);
    }

    private static String toJson(AktivitetStatusModell grunnlag) {
        return JsonMapper.toJson(grunnlag, BeregningsgrunnlagFeil::kanIkkeSerialisereRegelinput);
    }


}
