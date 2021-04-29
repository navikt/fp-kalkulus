package no.nav.folketrygdloven.kalkulator.ytelse.k9;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapRefusjonPerioderFraVLTilRegelUtbgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@FagsakYtelseTypeRef("OMP")
@FagsakYtelseTypeRef("PSB")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelK9 extends MapRefusjonPerioderFraVLTilRegelUtbgrad {

    /** Finner gyldige perioder for refusjon basert på perioder med utbetalingsgrad
     *
     *
     * @param startdatoPermisjon Startdato for permisjonen for ytelse søkt for
     * @param ytelsespesifiktGrunnlag Ytelsesspesifikt grunnlag
     * @param inntektsmelding                      inntektsmelding
     * @param beregningsgrunnlag
     * @return Gyldige perioder for refusjon
     */
    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon,
                                                          YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                          InntektsmeldingDto inntektsmelding, BeregningsgrunnlagDto beregningsgrunnlag) {
        if (erMidlertidigInaktiv(beregningsgrunnlag)) {
            return Collections.emptyList();
        }
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            var utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag;
            var perioderMedUtbetaling = utbetalingsgradGrunnlag.finnUtbetalingsgraderForArbeid(inntektsmelding.getArbeidsgiver(), inntektsmelding.getArbeidsforholdRef())
                    .stream()
                    .filter(p -> p.getUtbetalingsgrad() != null && p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                    .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                    .collect(Collectors.toList());
            return perioderMedUtbetaling;
        }
        return List.of();
    }

    private boolean erMidlertidigInaktiv(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().stream().anyMatch(a -> a.getAktivitetStatus().equals(AktivitetStatus.MIDLERTIDIG_INAKTIV));
    }


    @Override
    protected List<Gradering> mapUtbetalingsgrader(InntektsmeldingDto im, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return Collections.emptyList();
    }

    @Override
    protected void mapFristData(BeregningsgrunnlagInput input, InntektsmeldingDto inntektsmelding, ArbeidsforholdOgInntektsmelding.Builder builder) {
        // Skal ikkje vurdere frist for k9
    }

}
