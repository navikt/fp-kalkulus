package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.gradering.MapAndelGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;

public abstract class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel {

    protected MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel() {
    }

    public PeriodeModell map(BeregningsgrunnlagInput input,
                             BeregningsgrunnlagDto beregningsgrunnlag) {
        precondition(beregningsgrunnlag);
        var iayGrunnlag = input.getIayGrunnlag();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(MapSplittetPeriodeFraVLTilRegel::map).collect(Collectors.toList());
        var regelAndelGraderinger = finnGraderinger(input).stream()
                .map(andelGradering -> MapAndelGradering.mapGradering(andelGradering,
                        beregningsgrunnlag,
                        input.getInntektsmeldinger(),
                        filter,
                        skjæringstidspunkt))
                .collect(Collectors.toList());

        return mapPeriodeModell(input,
                beregningsgrunnlag,
                filter,
                skjæringstidspunkt,
                eksisterendePerioder,
                List.copyOf(regelAndelGraderinger));
    }

    protected Set<AndelGradering> finnGraderinger(BeregningsgrunnlagInput input) {
        return input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag ?
                ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).getAktivitetGradering().getAndelGradering()
                : new HashSet<>();
    }

    protected abstract PeriodeModell mapPeriodeModell(BeregningsgrunnlagInput input,
                                                      BeregningsgrunnlagDto beregningsgrunnlag,
                                                      YrkesaktivitetFilterDto filter,
                                                      LocalDate skjæringstidspunkt,
                                                      List<SplittetPeriode> eksisterendePerioder,
                                                      List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering> regelAndelGraderinger);


    protected void precondition(@SuppressWarnings("unused") BeregningsgrunnlagDto beregningsgrunnlag) {
        // template method
    }



    public static class Input {
        private final BeregningsgrunnlagInput beregningsgrunnlagInput;
        private final List<BeregningsgrunnlagPrStatusOgAndelDto> andeler;

        public Input(BeregningsgrunnlagInput input, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
            this.beregningsgrunnlagInput = input;
            this.andeler = Collections.unmodifiableList(andeler);
        }

        public Collection<InntektsmeldingDto> getInntektsmeldinger() {
            return beregningsgrunnlagInput.getInntektsmeldinger();
        }

        public BeregningsgrunnlagInput getBeregningsgrunnlagInput() {
            return beregningsgrunnlagInput;
        }

        public List<BeregningsgrunnlagPrStatusOgAndelDto> getAndeler() {
            return andeler;
        }
    }

}
