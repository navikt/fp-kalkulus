package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;

class FinnTilkommetInntektTjeneste {

    List<AktivitetDto> finnAktiviteterMedTilkommetInntekt(BeregningsgrunnlagDto beregningsgrunnlagDto,
                                                          InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                          List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        var skjæringstidspunkt = beregningsgrunnlagDto.getSkjæringstidspunkt();
        var sisteSøkteDato = finnSisteSøkteDato(utbetalingsgradPrAktivitet);
        var beregningsperiodeOpt = new BeregningsperiodeTjeneste().finnFullstendigBeregningsperiodeForArbeidIGrunnlag(beregningsgrunnlagDto);
        var resultat = new ArrayList<AktivitetDto>();
        if (beregningsperiodeOpt.isEmpty()) {
            return resultat;
        }
        var yrkesaktivitetFilterDto = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        var aktiviteterFraAOrdningen = finnAktiviteterFraAOrdningen(beregningsgrunnlagDto, iayGrunnlag, skjæringstidspunkt, sisteSøkteDato, beregningsperiodeOpt, yrkesaktivitetFilterDto);
        resultat.addAll(aktiviteterFraAOrdningen);
        var aktiviteterFraInntektsmelding = finnAktiviteterFraInntektsmelding(beregningsgrunnlagDto, iayGrunnlag, skjæringstidspunkt, yrkesaktivitetFilterDto);
        resultat.addAll(aktiviteterFraInntektsmelding);
        return resultat;
    }

    private List<AktivitetDto> finnAktiviteterFraAOrdningen(BeregningsgrunnlagDto beregningsgrunnlagDto, InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunkt, LocalDate sisteSøkteDato, Optional<Intervall> beregningsperiodeOpt, YrkesaktivitetFilterDto yrkesaktivitetFilterDto) {
        var inntektsposterIBeregningsperioden = finnInntektposterIBeregningsperioden(iayGrunnlag, beregningsperiodeOpt.get());
        var inntektsposterEtter = finnInntektsposterMellom(iayGrunnlag, skjæringstidspunkt, sisteSøkteDato);
        var alleInntektsmeldinger = iayGrunnlag.getInntektsmeldinger().map(InntektsmeldingAggregatDto::getAlleInntektsmeldinger).orElse(Collections.emptyList());
        var tilkomneInntekterFraInntektregister = finnTilkomneInntekterFraAOrdningen(
                beregningsgrunnlagDto,
                inntektsposterIBeregningsperioden,
                inntektsposterEtter,
                yrkesaktivitetFilterDto,
                alleInntektsmeldinger);
        return mapTilAktiviteter(tilkomneInntekterFraInntektregister);
    }

    private List<AktivitetDto> finnAktiviteterFraInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlagDto, InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunkt, YrkesaktivitetFilterDto yrkesaktivitetFilterDto) {
        var alleInntektsmeldinger = iayGrunnlag.getInntektsmeldinger().map(InntektsmeldingAggregatDto::getAlleInntektsmeldinger).orElse(Collections.emptyList());
        var yrkesaktiviteter = yrkesaktivitetFilterDto.getYrkesaktiviteterForBeregning();
        return alleInntektsmeldinger.stream().filter(im -> finnesIkkeIBeregningsgrunnlag(beregningsgrunnlagDto, im.getArbeidsgiver()))
                .map(im -> new AktivitetDto(finnYrkesaktiviteter(im, skjæringstidspunkt, yrkesaktiviteter),
                        im.getInntektBeløp()))
                .filter(a -> !a.getYrkesaktivitetDto().isEmpty())
                .collect(Collectors.toList());
    }

    private List<YrkesaktivitetDto> finnYrkesaktiviteter(InntektsmeldingDto im, LocalDate skjæringstidspunkt, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        var aktiviteterForInntektsmelding = yrkesaktiviteter.stream()
                .filter(ya -> ya.gjelderFor(im))
                .collect(Collectors.toSet());
        return aktiviteterForInntektsmelding.stream()
                .filter(ya -> ya.getAlleAnsettelsesperioder().stream().anyMatch(a -> a.getPeriode().getTomDato().isAfter(skjæringstidspunkt)))
                .collect(Collectors.toList());
    }

    private LocalDate finnSisteSøkteDato(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        return utbetalingsgradPrAktivitet.stream()
                .flatMap(utbetalingsgradPrAktivitetDto -> utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(Intervall::getTomDato)
                .max(Comparator.naturalOrder()).orElse(TIDENES_ENDE);
    }

    private List<AktivitetDto> mapTilAktiviteter(Map<YrkesaktivitetDto, List<InntektspostDto>> tilkomneInntekter) {
        return tilkomneInntekter.entrySet().stream()
                .map(e -> new AktivitetDto(List.of(e.getKey()), finnMånedsinntektFraInntektsposter(e.getValue())))
                .collect(Collectors.toList());
    }

    private Beløp finnMånedsinntektFraInntektsposter(List<InntektspostDto> poster) {
        var førsteDato = poster.stream().map(InntektspostDto::getPeriode)
                .map(Intervall::getFomDato)
                .min(Comparator.naturalOrder());

        if (førsteDato.isEmpty()) {
            return Beløp.ZERO;
        }
        var sisteDato = poster.stream().map(InntektspostDto::getPeriode)
                .map(Intervall::getTomDato)
                .max(Comparator.naturalOrder()).orElseThrow();
        int månederMellom = finnMånederMellom(førsteDato.get(), sisteDato);
        if (månederMellom < 2) {
            return finnSnittFraUnder3Måneder(poster, månederMellom);
        } else {
            return finnSnittFra3FørsteMåneder(poster, førsteDato.get());
        }
    }

    private int finnMånederMellom(LocalDate førsteDato, LocalDate sisteDato) {
        var årsdifferanse = sisteDato.getYear() - førsteDato.getYear();
        var månedsdifferanse = sisteDato.getMonthValue() - førsteDato.getMonthValue();
        return månedsdifferanse + 12 * årsdifferanse;
    }

    private Beløp finnSnittFra3FørsteMåneder(List<InntektspostDto> poster, LocalDate førsteDato) {
        var sumTreMåneder = poster.stream()
                .filter(p -> p.getPeriode().getTomDato().isBefore(førsteDato.withDayOfMonth(1).plusMonths(4)))
                .map(InntektspostDto::getBeløp)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
        return new Beløp(sumTreMåneder.getVerdi().divide(BigDecimal.valueOf(3), 10, RoundingMode.HALF_UP));
    }

    private Beløp finnSnittFraUnder3Måneder(List<InntektspostDto> poster, int månederMellom) {
        var antallMåneder = månederMellom + 1;
        var sum = poster.stream().map(InntektspostDto::getBeløp)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
        return new Beløp(sum.getVerdi().divide(BigDecimal.valueOf(antallMåneder), 10, RoundingMode.HALF_UP));
    }

    private Map<YrkesaktivitetDto, List<InntektspostDto>> finnTilkomneInntekterFraAOrdningen(BeregningsgrunnlagDto beregningsgrunnlagDto,
                                                                                             Collection<InntektspostDto> inntektsposterIBeregningsperioden,
                                                                                             Collection<InntektspostDto> inntektsposterEtter,
                                                                                             YrkesaktivitetFilterDto yrkesaktivitetFilterDto,
                                                                                             List<InntektsmeldingDto> alleInntektsmeldinger) {
        return inntektsposterEtter.stream()
                .filter(p -> p.getInntekt().getArbeidsgiver() != null)
                .filter(post -> ingenInntektIBeregningsperioden(post, inntektsposterIBeregningsperioden))
                .filter(post -> ingenMottattInntektsmelding(alleInntektsmeldinger, post.getInntekt().getArbeidsgiver()))
                .filter(post -> finnesIkkeIBeregningsgrunnlag(beregningsgrunnlagDto, post.getInntekt().getArbeidsgiver()))
                .filter(post -> finnYrkesaktivitet(post, yrkesaktivitetFilterDto.getYrkesaktiviteter()).isPresent())
                .collect(Collectors.groupingBy(i -> finnYrkesaktivitet(i, yrkesaktivitetFilterDto.getYrkesaktiviteter()).get()));
    }

    private boolean ingenMottattInntektsmelding(List<InntektsmeldingDto> alleInntektsmeldinger, Arbeidsgiver arbeidsgiver) {
        return alleInntektsmeldinger.stream().noneMatch(im -> im.getArbeidsgiver().equals(arbeidsgiver));
    }

    private Collection<InntektspostDto> finnInntektsposterMellom(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                 LocalDate skjæringstidspunkt,
                                                                 LocalDate sisteSøkteDato) {
        var inntektEtterStp = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister())
                .etter(skjæringstidspunkt)
                .filterBeregningsgrunnlag();
        return inntektEtterStp.getFiltrertInntektsposter().stream()
                .filter(p -> p.getInntektspostType().equals(InntektspostType.LØNN))
                .filter(inntektspostDto -> inntektspostDto.getPeriode().getFomDato().isBefore(sisteSøkteDato.plusDays(1)))
                .collect(Collectors.toList());
    }


    private Collection<InntektspostDto> finnInntektposterIBeregningsperioden(InntektArbeidYtelseGrunnlagDto iayGrunnlag, Intervall beregningsperiodeArbeid) {
        var inntektFørStp = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister())
                .filterBeregningsgrunnlag();
        return inntektFørStp.getFiltrertInntektsposter()
                .stream()
                .filter(p -> p.getInntektspostType().equals(InntektspostType.LØNN))
                .filter(p -> beregningsperiodeArbeid.overlapper(p.getPeriode()))
                .collect(Collectors.toList());
    }

    private static boolean finnesIkkeIBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlagDto, Arbeidsgiver arbeidsgiver) {
        return beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().stream().flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .flatMap(a -> a.getArbeidsgiver().stream())
                .noneMatch(a -> a.getIdentifikator().equals(arbeidsgiver.getIdentifikator()));
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
