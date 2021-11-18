package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;

public class MapPerioderForGraderingOgUtbetalingsgrad extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegel {

    public MapPerioderForGraderingOgUtbetalingsgrad() {
        // For CDI
    }

    @Override
    protected PeriodeModell mapPeriodeModell(BeregningsgrunnlagInput input,
                                             BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                             YrkesaktivitetFilterDto filter,
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
}
