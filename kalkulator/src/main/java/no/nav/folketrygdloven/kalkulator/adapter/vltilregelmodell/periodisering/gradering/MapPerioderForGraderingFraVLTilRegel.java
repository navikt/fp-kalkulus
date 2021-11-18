package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.gradering;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapPeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapSplittetPeriodeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;


public class MapPerioderForGraderingFraVLTilRegel {

    public static PeriodeModell map(BeregningsgrunnlagInput input,
                             BeregningsgrunnlagDto beregningsgrunnlag) {
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

        return mapPeriodeModell(
                beregningsgrunnlag,
                skjæringstidspunkt,
                eksisterendePerioder,
                List.copyOf(regelAndelGraderinger));
    }

    private static PeriodeModell mapPeriodeModell(BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                  LocalDate skjæringstidspunkt,
                                                  List<SplittetPeriode> eksisterendePerioder,
                                                  List<AndelGradering> regelAndelGraderinger) {
        List<PeriodisertBruttoBeregningsgrunnlag> periodiseringBruttoBg = MapPeriodisertBruttoBeregningsgrunnlag.map(vlBeregningsgrunnlag);

        return PeriodeModell.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medGrunnbeløp(vlBeregningsgrunnlag.getGrunnbeløp().getVerdi())
                .medAndelGraderinger(regelAndelGraderinger)
                .medEndringISøktYtelse(Collections.emptyList())
                .medEksisterendePerioder(eksisterendePerioder)
                .medPeriodisertBruttoBeregningsgrunnlag(periodiseringBruttoBg)
                .build();
    }

    protected static Set<no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering> finnGraderinger(BeregningsgrunnlagInput input) {
        return input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag ?
                ((ForeldrepengerGrunnlag) input.getYtelsespesifiktGrunnlag()).getAktivitetGradering().getAndelGradering()
                : new HashSet<>();
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
