package no.nav.folketrygdloven.kalkulator.ytelse.k9;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.RefusjonskravFrist;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingUtbgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

@FagsakYtelseTypeRef("OMP")
@FagsakYtelseTypeRef("PSB")
@ApplicationScoped
public class MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingK9 extends MapFastsettBeregningsgrunnlagPerioderFraVLTilRegelRefusjonOgGraderingUtbgrad {


    /** Finner gyldige perioder for refusjon basert på perioder med utbetalingsgrad og ansettelse
     *
     *
     * @param startdatoPermisjon Startdato for permisjonen for ytelse søkt for
     * @param ytelsespesifiktGrunnlag Ytelsesspesifikt grunnlag
     * @param ya                      Yrkesaktivitet
     * @return Gyldige perioder for refusjon
     */
    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, YrkesaktivitetDto ya) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            LocalDateTimeline<Object> utbetalingTidslinje = finnUtbetalingTidslinje((UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag, ya);
            LocalDateTimeline<Object> ansettelseTidslinje = finnAnsettelseTidslinje(ya);
            return finnOverlappendeIntervaller(utbetalingTidslinje, ansettelseTidslinje);
        }
        return ya.getAlleAktivitetsAvtaler().stream().filter(AktivitetsAvtaleDto::erAnsettelsesPeriode).map(AktivitetsAvtaleDto::getPeriode).collect(Collectors.toList());
    }

    private List<Intervall> finnOverlappendeIntervaller(LocalDateTimeline<Object> utbetalingTidslinje, LocalDateTimeline<Object> ansettelseTidslinje) {
        return utbetalingTidslinje.intersection(ansettelseTidslinje).getLocalDateIntervals()
                .stream()
                .map(i -> Intervall.fraOgMedTilOgMed(i.getFomDato(), i.getTomDato()))
                .collect(Collectors.toList());
    }

    private LocalDateTimeline<Object> finnAnsettelseTidslinje(YrkesaktivitetDto ya) {
        List<LocalDateSegment<Object>> segmenterMedAnsettelse = ya.getAlleAktivitetsAvtaler().stream()
                .filter(AktivitetsAvtaleDto::erAnsettelsesPeriode)
                .map(AktivitetsAvtaleDto::getPeriode)
                .map(p -> LocalDateSegment.emptySegment(p.getFomDato(), p.getTomDato()))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(segmenterMedAnsettelse);
    }

    private LocalDateTimeline<Object> finnUtbetalingTidslinje(UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag, YrkesaktivitetDto ya) {
        var segmenterMedUtbetaling = ytelsespesifiktGrunnlag.finnUtbetalingsgraderForArbeid(ya.getArbeidsgiver(), ya.getArbeidsforholdRef())
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



    /**
     * Skal ikkje vurdere refusjonskravfrist i kalkulus for k9-ytelser. Dette vurderes felles for søknadsfrist.
     *
     */
    @Override
    protected Optional<RefusjonskravFrist> mapRefusjonskravFrist() {
        return Optional.empty();
    }

    /**
     * Returerer ingen dato fordi dette ikke er relevant for k9. Søknadsfrist vurderes utenfor og alle krav skal godkjennes.
     *
     * @param ya Yrkesaktiviet
     * @param refusjonOverstyringer Refusjonsoverstyringer
     * @return Første gyldige dato med refusjon
     */
    @Override
    protected Optional<LocalDate> mapFørsteGyldigeDatoForRefusjon(YrkesaktivitetDto ya, Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer) {
        return Optional.empty();
    }


}
