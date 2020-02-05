package no.nav.folketrygdloven.kalkulator.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class GraderingUtenBeregningsgrunnlagTjeneste {

    private GraderingUtenBeregningsgrunnlagTjeneste() {
        // Skjuler default konstruktør
    }

    public static List<BeregningsgrunnlagPrStatusOgAndelRestDto> finnAndelerMedGraderingUtenBG(BeregningsgrunnlagRestDto beregningsgrunnlag, AktivitetGradering aktivitetGradering) {
        List<BeregningsgrunnlagPrStatusOgAndelRestDto> graderingsandelerUtenBG = new ArrayList<>();
        aktivitetGradering.getAndelGradering().forEach(andelGradering -> {
            List<BeregningsgrunnlagPrStatusOgAndelRestDto> andeler = finnTilsvarendeAndelITilsvarendePeriode(andelGradering, beregningsgrunnlag);
            graderingsandelerUtenBG.addAll(andeler);
        });
        return graderingsandelerUtenBG;
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelRestDto> finnTilsvarendeAndelITilsvarendePeriode(AndelGradering andelGradering, BeregningsgrunnlagRestDto beregningsgrunnlag) {
        List<BeregningsgrunnlagPrStatusOgAndelRestDto> andeler = new ArrayList<>();
        andelGradering.getGraderinger().forEach(gradering ->{
            Optional<BeregningsgrunnlagPeriodeRestDto> korrektBGPeriode = finnTilsvarendeBGPeriode(gradering, beregningsgrunnlag.getBeregningsgrunnlagPerioder());
            Optional<BeregningsgrunnlagPrStatusOgAndelRestDto> korrektBGAndel = korrektBGPeriode.flatMap(p -> finnTilsvarendeAndelIPeriode(andelGradering, p));
            if (korrektBGAndel.isPresent() && harIkkeTilkjentBGEtterRedusering(korrektBGAndel.get()) && arbeidsforholdErAktivtIGraderingsperiode(gradering, korrektBGAndel.get())) {
                andeler.add(korrektBGAndel.get());
            }
        });
        return andeler;
    }

    private static boolean harIkkeTilkjentBGEtterRedusering(BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        return andel.getRedusertPrÅr() != null && andel.getRedusertPrÅr().compareTo(BigDecimal.ZERO) <= 0;
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelRestDto> finnTilsvarendeAndelIPeriode(AndelGradering andelGradering, BeregningsgrunnlagPeriodeRestDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> bgAndelMatcherGraderingAndel(andel, andelGradering)).findFirst();
    }

    private static boolean bgAndelMatcherGraderingAndel(BeregningsgrunnlagPrStatusOgAndelRestDto andel, AndelGradering andelGradering) {
        if (!andel.getAktivitetStatus().equals(andelGradering.getAktivitetStatus())) {
            return false;
        }
        if (!Objects.equals(andelGradering.getArbeidsgiver(), andel.getArbeidsgiver().orElse(null))) {
            return false;
        }
        return andelGradering.getArbeidsforholdRef().gjelderFor(andel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()));
    }

    private static Optional<BeregningsgrunnlagPeriodeRestDto> finnTilsvarendeBGPeriode(Gradering gradering, List<BeregningsgrunnlagPeriodeRestDto> beregningsgrunnlagPerioder) {
        return beregningsgrunnlagPerioder.stream().filter(p -> gradering.getPeriode().overlapper(p.getPeriode())).findFirst();
    }

    private static boolean arbeidsforholdErAktivtIGraderingsperiode(Gradering gradering, BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        Optional<Intervall> arbeidsperiode = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdRestDto::getArbeidsperiode);
        return arbeidsperiode.map(ap -> ap.overlapper(gradering.getPeriode())).orElse(true);
    }

}
