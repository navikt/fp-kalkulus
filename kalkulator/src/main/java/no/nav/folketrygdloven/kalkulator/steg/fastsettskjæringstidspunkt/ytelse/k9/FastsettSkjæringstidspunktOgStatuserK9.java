package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k9;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBGSkjæringstidspunktOgStatuserFraRegelTilVL.mapForSkjæringstidspunktOgStatuser;
import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagFeil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.JsonMapper;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBGStatuserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.FastsettSkjæringstidspunktOgStatuser;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.FastsettBeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse.k9.RegelFastsettSkjæringstidspunktK9;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellK9;
import no.nav.folketrygdloven.skjæringstidspunkt.status.RegelFastsettStatusVedSkjæringstidspunkt;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("PSB")
@FagsakYtelseTypeRef("PPN")
@FagsakYtelseTypeRef("OMP")
public class FastsettSkjæringstidspunktOgStatuserK9 implements FastsettSkjæringstidspunktOgStatuser {

    private final FastsettBeregningsperiodeTjeneste fastsettBeregningsperiodeTjeneste = new FastsettBeregningsperiodeTjeneste();

    @Override
    public BeregningsgrunnlagRegelResultat fastsett(BeregningsgrunnlagInput input, BeregningAktivitetAggregatDto beregningAktivitetAggregat, InntektArbeidYtelseGrunnlagDto iayGrunnlag, List<Grunnbeløp> grunnbeløpSatser) {
        var ytelseFilter = new YtelseFilterDto(input.getIayGrunnlag().getAktørYtelseFraRegister()).før(beregningAktivitetAggregat.getSkjæringstidspunktOpptjening());
        AktivitetStatusModell regelmodell = MapBGStatuserFraVLTilRegel.map(input.getInntektsmeldinger(),
                beregningAktivitetAggregat, ytelseFilter);

        no.nav.folketrygdloven.kalkulus.opptjening.v1.MidlertidigInaktivType midlerTidigInaktivInput = input.getOpptjeningAktiviteter().getMidlertidigInaktivType();
        MidlertidigInaktivType midlertidigInaktivType = null;
        if (midlerTidigInaktivInput != null) {
            if (midlerTidigInaktivInput.name().equals(MidlertidigInaktivType.A.name())) {
                List<AktivPeriode> aktiviteterPåStp = regelmodell.getAktivePerioder().stream().filter(aktivPeriode -> aktivPeriode.getPeriode().inneholder(input.getSkjæringstidspunktOpptjening())).collect(Collectors.toList());
                if (!aktiviteterPåStp.isEmpty()) {
                    throw new IllegalArgumentException("Skjæringstidspunktet kan ikke overlappe med aktive perioder for midlertidig inaktiv 8-47-A for: " + aktiviteterPåStp);
                }
            }
            midlertidigInaktivType = MidlertidigInaktivType.valueOf(midlerTidigInaktivInput.name());
        }

        AktivitetStatusModellK9 k9Modell = new AktivitetStatusModellK9(midlertidigInaktivType, regelmodell);

        RegelResultat regelResultatFastsettSkjæringstidspunkt = fastsettSkjæringstidspunkt(k9Modell);
        RegelResultat regelResultatFastsettStatus = fastsettStatus(k9Modell);

        // Oversett endelig resultat av regelmodell (+ spore input -> evaluation)
        List<RegelResultat> regelResultater = List.of(
                regelResultatFastsettSkjæringstidspunkt,
                regelResultatFastsettStatus);
        var nyttBeregningsgrunnlag = mapForSkjæringstidspunktOgStatuser(input.getKoblingReferanse(), k9Modell, regelResultater, iayGrunnlag, grunnbeløpSatser);
        var fastsattBeregningsperiode = fastsettBeregningsperiodeTjeneste.fastsettBeregningsperiode(nyttBeregningsgrunnlag, iayGrunnlag, input.getInntektsmeldinger());

        return new BeregningsgrunnlagRegelResultat(fastsattBeregningsperiode,
                new RegelSporingAggregat(
                        mapRegelSporingGrunnlag(regelResultatFastsettSkjæringstidspunkt, BeregningsgrunnlagRegelType.SKJÆRINGSTIDSPUNKT),
                        mapRegelSporingGrunnlag(regelResultatFastsettStatus, BeregningsgrunnlagRegelType.BRUKERS_STATUS)));
    }

    private RegelResultat fastsettSkjæringstidspunkt(AktivitetStatusModell regelmodell) {
        // Tar sporingssnapshot av regelmodell, deretter oppdateres modell med fastsatt skjæringstidspunkt for Beregning
        String inputSkjæringstidspunkt = toJson(regelmodell);
        Evaluation evaluationSkjæringstidspunkt = new RegelFastsettSkjæringstidspunktK9().evaluer(regelmodell);
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
