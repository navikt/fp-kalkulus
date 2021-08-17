package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.folketrygdloven.kalkulator.steg.foreslå.SplittBGPerioder.splitBeregningsgrunnlagPeriode;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn.RegelForeslåBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.steg.foreslå.ForeslåBeregningsgrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class ForeslåBeregningsgrunnlagFRISINN extends ForeslåBeregningsgrunnlag {

    public ForeslåBeregningsgrunnlagFRISINN() {
        // CDI
    }

    @Inject
    public ForeslåBeregningsgrunnlagFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

    @Override
    protected List<BeregningAksjonspunktResultat> utledAksjonspunkter(BeregningsgrunnlagInput input, List<RegelResultat> regelResultater) {
        return Collections.emptyList();
    }

    @Override
    protected List<RegelResultat> kjørRegelForeslåBeregningsgrunnlag(Beregningsgrunnlag regelmodellBeregningsgrunnlag, String jsonInput) {
        // Evaluerer hver BeregningsgrunnlagPeriode fra initielt Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(periode).evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }
        return regelResultater;
    }

    @Override
    protected void splittPerioder(BeregningsgrunnlagInput input, Beregningsgrunnlag regelmodellBeregningsgrunnlag, BeregningsgrunnlagDto beregningsgrunnlag, Optional<FaktaAggregatDto> faktaAggregat) {
        List<OppgittPeriodeInntekt> inntektListe = input.getIayGrunnlag().getOppgittOpptjening().stream().flatMap(oo -> oo.getEgenNæring().stream()).collect(Collectors.toList());
        List<OppgittPeriodeInntekt> oppgittFLInntekt = input.getIayGrunnlag().getOppgittOpptjening().stream().flatMap(ofl -> ofl.getFrilans().stream())
                .flatMap(fl -> fl.getOppgittFrilansInntekt().stream()).collect(Collectors.toList());
        inntektListe.addAll(oppgittFLInntekt);
        List<OppgittPeriodeInntekt> inntekterSomSkalFøreTilSPlitt = inntektListe.stream()
                .filter(inntekt -> !inntekt.getPeriode().getFomDato().isBefore(input.getSkjæringstidspunktOpptjening()))
                .collect(Collectors.toList());
        inntekterSomSkalFøreTilSPlitt.forEach(oppgittPeriodeInntekt -> splittForOppgittPeriode(oppgittPeriodeInntekt, regelmodellBeregningsgrunnlag));
    }


    @Override
    protected void verifiserBeregningsgrunnlag(BeregningsgrunnlagDto foreslåttBeregningsgrunnlag) {
        BeregningsgrunnlagVerifisererFRISINN.verifiserForeslåttBeregningsgrunnlag(foreslåttBeregningsgrunnlag);
    }

    private void splittForOppgittPeriode(OppgittPeriodeInntekt oppgittPeriodeInntekt, Beregningsgrunnlag regelmodellBeregningsgrunnlag) {
        LocalDate fom = oppgittPeriodeInntekt.getPeriode().getFomDato();
        LocalDate tom = oppgittPeriodeInntekt.getPeriode().getTomDato();
        splittVedFom(regelmodellBeregningsgrunnlag, fom);
        splittVedTom(regelmodellBeregningsgrunnlag, tom);
    }


    private void splittVedTom(Beregningsgrunnlag regelmodellBeregningsgrunnlag, LocalDate tom) {
        Optional<BeregningsgrunnlagPeriode> periodeSomInneholderTomOpt = regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getBeregningsgrunnlagPeriode().inneholder(tom))
                .findFirst();
        if (periodeSomInneholderTomOpt.isPresent()) {
            BeregningsgrunnlagPeriode periode = periodeSomInneholderTomOpt.get();
            if (periode.getBeregningsgrunnlagPeriode().getTom().isEqual(tom) && !tom.equals(TIDENES_ENDE)) {
                BeregningsgrunnlagPeriode nestePeriode = regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                        .filter(p -> p.getBeregningsgrunnlagPeriode().getFom().equals(tom.plusDays(1)))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Forventer å finne neste periode"));
                if (!nestePeriode.getPeriodeÅrsaker().contains(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR)) {
                    BeregningsgrunnlagPeriode.builder(nestePeriode).leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
                }
            } else {
                splitBeregningsgrunnlagPeriode(periode, tom, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
            }
        }
    }

    private void splittVedFom(Beregningsgrunnlag regelmodellBeregningsgrunnlag, LocalDate fom) {
        Optional<BeregningsgrunnlagPeriode> periodeSomInneholderFomOpt = regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getBeregningsgrunnlagPeriode().inneholder(fom))
                .findFirst();

        if (periodeSomInneholderFomOpt.isPresent()) {
            BeregningsgrunnlagPeriode periode = periodeSomInneholderFomOpt.get();
            if (periode.getBeregningsgrunnlagPeriode().getFom().isEqual(fom)) {
                if (!periode.getPeriodeÅrsaker().contains(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR)) {
                    BeregningsgrunnlagPeriode.builder(periode).leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
                }
            } else {
                LocalDate nyPeriodeTom = fom.minusDays(1);
                splitBeregningsgrunnlagPeriode(periode, nyPeriodeTom, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
            }
        }
    }

}
