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
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapRefusjonPerioderFraVLTilRegelUtbgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

@FagsakYtelseTypeRef("OMP")
@FagsakYtelseTypeRef("PSB")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelK9 extends MapRefusjonPerioderFraVLTilRegelUtbgrad {


    /** Finner gyldige perioder for refusjon basert på perioder med utbetalingsgrad og ansettelse
     *
     *
     * @param startdatoPermisjon Startdato for permisjonen for ytelse søkt for
     * @param ytelsespesifiktGrunnlag Ytelsesspesifikt grunnlag
     * @param im inntektsmelding for refusjonskrav
     * @param beregningsgrunnlag  Beregningsgrunnlag
     * @return Gyldige perioder for refusjon
     */
    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon,
                                                          YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                          InntektsmeldingDto im,
                                                          List<AktivitetsAvtaleDto> ansattperioder,
                                                          BeregningsgrunnlagDto beregningsgrunnlag) {

        if (erMidlertidigInaktiv(beregningsgrunnlag)) {
            return Collections.emptyList();
        }

        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            LocalDateTimeline<Object> utbetalingTidslinje = finnUtbetalingTidslinje((UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag, im);
            LocalDateTimeline<Object> ansettelseTidslinje = finnAnsettelseTidslinje(ansattperioder);
            return finnOverlappendeIntervaller(utbetalingTidslinje, ansettelseTidslinje);
        }
        throw new IllegalStateException("Forventet utbetalingsgrader men fant ikke UtbetalingsgradGrunnlag.");
    }

    private List<Intervall> finnOverlappendeIntervaller(LocalDateTimeline<Object> utbetalingTidslinje, LocalDateTimeline<Object> ansettelseTidslinje) {
        return utbetalingTidslinje.intersection(ansettelseTidslinje).getLocalDateIntervals()
                .stream()
                .map(i -> Intervall.fraOgMedTilOgMed(i.getFomDato(), i.getTomDato()))
                .collect(Collectors.toList());
    }

    private LocalDateTimeline<Object> finnAnsettelseTidslinje(List<AktivitetsAvtaleDto> ansattperioder) {
        List<LocalDateSegment<Object>> segmenterMedAnsettelse = ansattperioder.stream()
                .map(AktivitetsAvtaleDto::getPeriode)
                .map(p -> LocalDateSegment.emptySegment(p.getFomDato(), p.getTomDato()))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(segmenterMedAnsettelse);
    }

    private LocalDateTimeline<Object> finnUtbetalingTidslinje(UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag, InntektsmeldingDto im) {
        var segmenterMedUtbetaling = ytelsespesifiktGrunnlag.finnUtbetalingsgraderForArbeid(im.getArbeidsgiver(), im.getArbeidsforholdRef())
                .stream()
                .filter(p -> p.getUtbetalingsgrad() != null && p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(p -> LocalDateSegment.emptySegment(p.getFomDato(), p.getTomDato()))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(segmenterMedUtbetaling);
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
