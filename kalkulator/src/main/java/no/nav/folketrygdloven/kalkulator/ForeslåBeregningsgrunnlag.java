package no.nav.folketrygdloven.kalkulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
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
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class ForeslåBeregningsgrunnlag {
    protected MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;

    public ForeslåBeregningsgrunnlag() {
        // CDI
    }

    @Inject
    public ForeslåBeregningsgrunnlag(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        this.mapBeregningsgrunnlagFraVLTilRegel = mapBeregningsgrunnlagFraVLTilRegel;
    }

    public BeregningsgrunnlagRegelResultat foreslåBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDto grunnlag = input.getBeregningsgrunnlagGrunnlag();

        // Oversetter initielt Beregningsgrunnlag -> regelmodell
        Beregningsgrunnlag regelmodellBeregningsgrunnlag = mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));
        splittPerioder(input, regelmodellBeregningsgrunnlag, beregningsgrunnlag);
        String jsonInput = toJson(regelmodellBeregningsgrunnlag);
        List<RegelResultat> regelResultater = kjørRegelForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, jsonInput);

        // Oversett endelig resultat av regelmodell til foreslått Beregningsgrunnlag  (+ spore input -> evaluation)
        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = MapBeregningsgrunnlagFraRegelTilVL.mapForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, regelResultater, beregningsgrunnlag);
        List<BeregningAksjonspunktResultat> aksjonspunkter = utledAksjonspunkter(input, regelResultater);
        BeregningsgrunnlagVerifiserer.verifiserForeslåttBeregningsgrunnlag(foreslåttBeregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(foreslåttBeregningsgrunnlag, aksjonspunkter);
    }


    protected void splittPerioder(BeregningsgrunnlagInput input,  Beregningsgrunnlag regelmodellBeregningsgrunnlag, BeregningsgrunnlagDto beregningsgrunnlag) {
        opprettPerioderForKortvarigeArbeidsforhold(input.getAktørId(),
                regelmodellBeregningsgrunnlag,
                beregningsgrunnlag, input.getIayGrunnlag());
    }

    protected List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input, List<RegelResultat> regelResultater) {
        return AksjonspunktUtlederForeslåBeregning.utledAksjonspunkter(input, regelResultater);
    }

    protected List<RegelResultat> kjørRegelForeslåBeregningsgrunnlag(Beregningsgrunnlag regelmodellBeregningsgrunnlag, String jsonInput) {
        // Evaluerer hver BeregningsgrunnlagPeriode fra initielt Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Evaluation evaluation = new RegelForeslåBeregningsgrunnlag(periode).evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }
        return regelResultater;
    }

    private void opprettPerioderForKortvarigeArbeidsforhold(AktørId aktørId,
                                                            no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag regelBeregningsgrunnlag,
                                                            BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                            InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var filter = getYrkesaktivitetFilter(aktørId, iayGrunnlag);
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarigeAktiviteter = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(aktørId, vlBeregningsgrunnlag, iayGrunnlag);
        kortvarigeAktiviteter.entrySet().stream()
            .filter(entry -> entry.getKey().getBgAndelArbeidsforhold()
                .filter(a -> Boolean.TRUE.equals(a.getErTidsbegrensetArbeidsforhold())).isPresent())
            .map(Map.Entry::getValue)
            .forEach(ya -> SplittBGPerioder.splitt(regelBeregningsgrunnlag, filter.getAnsettelsesPerioder(ya)));
    }

    private YrkesaktivitetFilterDto getYrkesaktivitetFilter(AktørId aktørId, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return  new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(aktørId));
    }

    protected String toJson(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel) {
        return JsonMapper.toJson(beregningsgrunnlagRegel, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }


}
