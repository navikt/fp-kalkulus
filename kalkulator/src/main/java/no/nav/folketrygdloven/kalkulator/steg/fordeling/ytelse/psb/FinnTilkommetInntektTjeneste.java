package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;

class FinnTilkommetInntektTjeneste {

    private final BeregningsperiodeTjeneste beregningsperiodeTjeneste = new BeregningsperiodeTjeneste();

    List<AktivitetDto> finnAktiviteterMedTilkommetInntekt(BeregningsgrunnlagDto beregningsgrunnlagDto,
                                                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var skjæringstidspunkt = beregningsgrunnlagDto.getSkjæringstidspunkt();
        var inntektsposterIBeregningsperioden = finnInntektposterIBeregningsperioden(iayGrunnlag, skjæringstidspunkt);
        var inntektsposterEtter = finnInntektsposterEtter(iayGrunnlag, skjæringstidspunkt);
        var yrkesaktivitetFilterDto = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        var tilkomneInntekterFraInntektregister = finnTilkomneInntekter(
                beregningsgrunnlagDto,
                inntektsposterIBeregningsperioden,
                inntektsposterEtter,
                yrkesaktivitetFilterDto);
        return mapTilAktiviteter(tilkomneInntekterFraInntektregister);
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

    private Collection<InntektspostDto> finnInntektsposterEtter(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunkt) {
        var inntektEtterStp = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister())
                .etter(skjæringstidspunkt)
                .filterBeregningsgrunnlag();
        return inntektEtterStp.getFiltrertInntektsposter();
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
