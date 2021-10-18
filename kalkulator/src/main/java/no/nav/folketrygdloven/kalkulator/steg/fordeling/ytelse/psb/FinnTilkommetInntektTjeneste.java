package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class FinnTilkommetInntektTjeneste {

    private final BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjeneste();

    List<AktivitetDto> finnAktiviteterMedTilkommetInntekt(BeregningsgrunnlagDto beregningsgrunnlagDto,
                                                          InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                          List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        var skjæringstidspunkt = beregningsgrunnlagDto.getSkjæringstidspunkt();
        var sisteSøkteDato = finnSisteSøkteDato(utbetalingsgradPrAktivitet);
        var inntektsposterIBeregningsperioden = finnInntektposterIBeregningsperioden(iayGrunnlag, skjæringstidspunkt);
        var inntektsposterEtter = finnInntektsposterMellom(iayGrunnlag, skjæringstidspunkt, sisteSøkteDato);
        var yrkesaktivitetFilterDto = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        var tilkomneInntekterFraInntektregister = finnTilkomneInntekter(
                beregningsgrunnlagDto,
                inntektsposterIBeregningsperioden,
                inntektsposterEtter,
                yrkesaktivitetFilterDto);
        return mapTilAktiviteter(tilkomneInntekterFraInntektregister);
    }

    private LocalDate finnSisteSøkteDato(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        LocalDate sisteSøkteDato = utbetalingsgradPrAktivitet.stream()
                .flatMap(utbetalingsgradPrAktivitetDto -> utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(Intervall::getTomDato)
                .max(Comparator.naturalOrder()).orElse(TIDENES_ENDE);
        return sisteSøkteDato;
    }

    private List<AktivitetDto> mapTilAktiviteter(Map<YrkesaktivitetDto, List<InntektspostDto>> tilkomneInntekter) {
        return tilkomneInntekter.entrySet().stream()
                .map(e -> new AktivitetDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private Map<YrkesaktivitetDto, List<InntektspostDto>> finnTilkomneInntekter(BeregningsgrunnlagDto beregningsgrunnlagDto,
                                                                                Collection<InntektspostDto> inntektsposterIBeregningsperioden,
                                                                                Collection<InntektspostDto> inntektsposterEtter,
                                                                                YrkesaktivitetFilterDto yrkesaktivitetFilterDto) {
        return inntektsposterEtter.stream()
                .filter(p -> p.getInntekt().getArbeidsgiver() != null)
                .filter(post -> ingenInntektIBeregningsperioden(post, inntektsposterIBeregningsperioden))
                .filter(post -> finnesIkkeIBeregningsgrunnlag(beregningsgrunnlagDto, post))
                .filter(post -> finnYrkesaktivitet(post, yrkesaktivitetFilterDto.getYrkesaktiviteter()).isPresent())
                .collect(Collectors.groupingBy(i -> finnYrkesaktivitet(i, yrkesaktivitetFilterDto.getYrkesaktiviteter()).get()));
    }

    private Collection<InntektspostDto> finnInntektsposterMellom(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                 LocalDate skjæringstidspunkt,
                                                                 LocalDate sisteSøkteDato) {
        var inntektEtterStp = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister())
                .etter(skjæringstidspunkt)
                .filterBeregningsgrunnlag();
        return inntektEtterStp.getFiltrertInntektsposter().stream()
                .filter(inntektspostDto -> inntektspostDto.getPeriode().getFomDato().isBefore(sisteSøkteDato.plusDays(1)))
                .collect(Collectors.toList());
    }

    private Collection<InntektspostDto> finnInntektposterIBeregningsperioden(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunkt) {
        var inntektFørStp = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister())
                .før(skjæringstidspunkt)
                .filterBeregningsgrunnlag();
        var beregningsperiodenArbeidstaker = beregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunkt);
        return inntektFørStp.getFiltrertInntektsposter()
                .stream()
                .filter(p -> beregningsperiodenArbeidstaker.overlapper(p.getPeriode()))
                .collect(Collectors.toList());
    }

    private static boolean finnesIkkeIBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlagDto, InntektspostDto post) {
        return beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().stream().flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .flatMap(a -> a.getArbeidsgiver().stream())
                .noneMatch(a -> a.getIdentifikator().equals(post.getInntekt().getArbeidsgiver().getIdentifikator()));
    }

    private static Optional<YrkesaktivitetDto> finnYrkesaktivitet(InntektspostDto i, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return yrkesaktiviteter.stream()
                .filter(ya -> ya.getArbeidsgiver() != null && ya.getArbeidsgiver().equals(i.getInntekt().getArbeidsgiver()))
                .findFirst();
    }

    private boolean ingenInntektIBeregningsperioden(InntektspostDto postEtter,
                                                    Collection<InntektspostDto> inntektsposterIBeregningsperioden) {
        return inntektsposterIBeregningsperioden.stream()
                .noneMatch(p -> Objects.equals(p.getInntekt().getArbeidsgiver(), postEtter.getInntekt().getArbeidsgiver()));
    }

}
