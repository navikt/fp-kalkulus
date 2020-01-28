package no.nav.folketrygdloven.kalkulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class ForeslåBeregningsgrunnlag {

    private ForeslåBeregningsgrunnlag() {
        // Skjul
    }

    public static BeregningsgrunnlagRegelResultat foreslåBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDto grunnlag = input.getBeregningsgrunnlagGrunnlag();

        // Oversetter initielt Beregningsgrunnlag -> regelmodell
        var ref = input.getBehandlingReferanse();
        no.nav.folketrygdloven.kalkulator.regelmodell.resultat.Beregningsgrunnlag regelmodellBeregningsgrunnlag = MapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag().orElse(null);
        opprettPerioderForKortvarigeArbeidsforhold(ref.getAktørId(), regelmodellBeregningsgrunnlag, beregningsgrunnlag, input.getIayGrunnlag());
        String jsonInput = toJson(regelmodellBeregningsgrunnlag);

        // Evaluerer hver BeregningsgrunnlagPeriode fra initielt Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(periode).evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }

        // Oversett endelig resultat av regelmodell til foreslått Beregningsgrunnlag  (+ spore input -> evaluation)
        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = MapBeregningsgrunnlagFraRegelTilVL.mapForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, regelResultater, beregningsgrunnlag);
        List<BeregningAksjonspunktResultat> aksjonspunkter = AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(input, regelResultater);
        BeregningsgrunnlagVerifiserer.verifiserForeslåttBeregningsgrunnlag(foreslåttBeregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(foreslåttBeregningsgrunnlag, aksjonspunkter);
    }

    private static void opprettPerioderForKortvarigeArbeidsforhold(AktørId aktørId, no.nav.folketrygdloven.kalkulator.regelmodell.resultat.Beregningsgrunnlag regelBeregningsgrunnlag, BeregningsgrunnlagDto vlBeregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var filter = getYrkesaktivitetFilter(aktørId, iayGrunnlag);
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarigeAktiviteter = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(aktørId, vlBeregningsgrunnlag, iayGrunnlag);
        kortvarigeAktiviteter.entrySet().stream()
            .filter(entry -> entry.getKey().getBgAndelArbeidsforhold()
                .filter(a -> Boolean.TRUE.equals(a.getErTidsbegrensetArbeidsforhold())).isPresent())
            .map(Map.Entry::getValue)
            .forEach(ya -> SplittBGPerioderMedAvsluttetArbeidsforhold.splitt(regelBeregningsgrunnlag, filter.getAnsettelsesPerioder(ya)));
    }

    private static YrkesaktivitetFilterDto getYrkesaktivitetFilter(AktørId aktørId, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return  new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(aktørId));
    }

    private static String toJson(no.nav.folketrygdloven.kalkulator.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel) {
        return JacksonJsonConfig.toJson(beregningsgrunnlagRegel, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

}
