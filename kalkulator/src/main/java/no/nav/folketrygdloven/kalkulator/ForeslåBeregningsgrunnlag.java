package no.nav.folketrygdloven.kalkulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
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
public class ForeslåBeregningsgrunnlag {
    private MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel;

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
        var ref = input.getBehandlingReferanse();
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag regelmodellBeregningsgrunnlag = mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
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

    private void opprettPerioderForKortvarigeArbeidsforhold(AktørId aktørId, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag regelBeregningsgrunnlag, BeregningsgrunnlagDto vlBeregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var filter = getYrkesaktivitetFilter(aktørId, iayGrunnlag);
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarigeAktiviteter = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(aktørId, vlBeregningsgrunnlag, iayGrunnlag);
        kortvarigeAktiviteter.entrySet().stream()
            .filter(entry -> entry.getKey().getBgAndelArbeidsforhold()
                .filter(a -> Boolean.TRUE.equals(a.getErTidsbegrensetArbeidsforhold())).isPresent())
            .map(Map.Entry::getValue)
            .forEach(ya -> SplittBGPerioderMedAvsluttetArbeidsforhold.splitt(regelBeregningsgrunnlag, filter.getAnsettelsesPerioder(ya)));
    }

    private YrkesaktivitetFilterDto getYrkesaktivitetFilter(AktørId aktørId, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return  new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(aktørId));
    }

    private String toJson(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag beregningsgrunnlagRegel) {
        return JsonMapper.toJson(beregningsgrunnlagRegel, BeregningsgrunnlagFeil.FEILFACTORY::kanIkkeSerialisereRegelinput);
    }

}
