package no.nav.folketrygdloven.kalkulator;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.ErFjernetIOverstyrt;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;

@ApplicationScoped
public class FinnYrkesaktiviteterForBeregningTjeneste {


    private FinnYrkesaktiviteterForBeregningTjeneste() {
        // Skjul
    }

    public static Collection<YrkesaktivitetDto> finnYrkesaktiviteter(BehandlingReferanse behandlingReferanse,
                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                     BeregningsgrunnlagGrunnlagDto grunnlag) {
        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(behandlingReferanse.getAktørId()));
        Collection<YrkesaktivitetDto> yrkesaktiviteterForBeregning = filter.getYrkesaktiviteterForBeregning();
        LocalDate skjæringstidspunktBeregning = behandlingReferanse.getSkjæringstidspunktBeregning();
        BeregningAktivitetAggregatDto overstyrtEllerRegisterAktiviteter = grunnlag.getOverstyrteEllerRegisterAktiviteter();
        return yrkesaktiviteterForBeregning.stream()
            .filter(yrkesaktivitet ->
                !ErFjernetIOverstyrt.erFjernetIOverstyrt(filter,
                        yrkesaktivitet,
                        overstyrtEllerRegisterAktiviteter,
                        skjæringstidspunktBeregning,
                        behandlingReferanse.getFagsakYtelseType()))
            .filter(ya -> FinnAnsettelsesPeriode.finnMinMaksPeriode(behandlingReferanse.getFagsakYtelseType(), filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning).isPresent())
            .collect(Collectors.toList());
    }

    /**
     * Henter ut yrkesaktiviteter for beregning inkludert korresponderende yrkesaktivitet for fjernede aktiviteter i beregning
     *
     * Skal kun brukes i tilfeller der man også skal hente ut fjernede aktiviteter eller der disse ikke er relevante (f.eks om man kun ser på aktiviteter etter stp)
     *
     * @param behandlingReferanse Referanse
     * @param filter Yrkesaktivitetfilter
     * @return Yrkesaktiviteter inkludert fjernede i overstyring av beregningaktiviteter
     */
    public static Collection<YrkesaktivitetDto> finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(BehandlingReferanse behandlingReferanse,
                                                                                                  YrkesaktivitetFilterDto filter) {
        Collection<YrkesaktivitetDto> yrkesaktiviteterForBeregning = filter.getYrkesaktiviteterForBeregning();
        LocalDate skjæringstidspunktBeregning = behandlingReferanse.getSkjæringstidspunktBeregning();
        return yrkesaktiviteterForBeregning.stream()
            .filter(ya -> FinnAnsettelsesPeriode.finnMinMaksPeriode(behandlingReferanse.getFagsakYtelseType(), filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning).isPresent())
            .collect(Collectors.toList());
    }

}
