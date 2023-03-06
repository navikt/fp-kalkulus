package no.nav.folketrygdloven.kalkulator.guitjenester;


import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.StatusOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.InntektsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderInntektsforholdPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderNyttInntektsforholdDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class VurderNyeInntektsforholdDtoTjeneste {

    public static VurderNyttInntektsforholdDto lagDto(BeregningsgrunnlagGUIInput input) {

        if (input.getAvklaringsbehov().stream().noneMatch(a -> a.getDefinisjon().equals(AvklaringsbehovDefinisjon.VURDER_NYTT_INNTKTSFRHLD))) {
            return null;
        }
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var iayGrunnlag = input.getIayGrunnlag();
        var ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();

        return lagVurderNyttInntektsforholdDto(beregningsgrunnlag, iayGrunnlag, ytelsespesifiktGrunnlag, input.getFagsakYtelseType());
    }

    public static VurderNyttInntektsforholdDto lagVurderNyttInntektsforholdDto(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, FagsakYtelseType fagsakYtelseType) {
        var tidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(beregningsgrunnlag.getSkjæringstidspunkt(),
                 5, beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList(),
                (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag,
                iayGrunnlag,
                fagsakYtelseType
        );

        return getVurderNyttInntektsforholdDto(beregningsgrunnlag, iayGrunnlag, tidslinje);
    }

    public static VurderNyttInntektsforholdDto getVurderNyttInntektsforholdDto(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDateTimeline<Set<StatusOgArbeidsgiver>> tidslinje) {
        var bgPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        var periodeListe = bgPerioder.stream()
                .filter(it -> !it.getTilkomneInntekter().isEmpty())
                .map(it -> mapPeriode(iayGrunnlag, it, tidslinje))
                .collect(Collectors.toList());

        if (!periodeListe.isEmpty()) {
            return new VurderNyttInntektsforholdDto(periodeListe);
        }

        return null;
    }

    private static VurderInntektsforholdPeriodeDto mapPeriode(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                              BeregningsgrunnlagPeriodeDto periode,
                                                              LocalDateTimeline<Set<StatusOgArbeidsgiver>> inntektsforholdTidslinje) {
        var innteksforholdListe = mapInntektforholdDtoListe(iayGrunnlag, periode, inntektsforholdTidslinje);
        return new VurderInntektsforholdPeriodeDto(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), innteksforholdListe.stream().toList());
    }

    private static Set<InntektsforholdDto> mapInntektforholdDtoListe(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                     BeregningsgrunnlagPeriodeDto periode, LocalDateTimeline<Set<StatusOgArbeidsgiver>> inntektsforholdTidslinje) {
        return periode.getTilkomneInntekter()
                .stream()
                .map(a -> mapTilInntektsforhold(a, iayGrunnlag, finnSegmenterSomInneholderInntektsforhold(inntektsforholdTidslinje, a).toList()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static Stream<LocalDateSegment<Set<StatusOgArbeidsgiver>>> finnSegmenterSomInneholderInntektsforhold(LocalDateTimeline<Set<StatusOgArbeidsgiver>> inntektsforholdTidslinje, TilkommetInntektDto a) {
        return inntektsforholdTidslinje.stream().filter(it -> it.getValue()
                .contains(new StatusOgArbeidsgiver(a.getAktivitetStatus(), a.getArbeidsgiver().orElse(null))));
    }


    private static InntektsforholdDto mapTilInntektsforhold(TilkommetInntektDto tilkommetInntektDto, InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                            List<LocalDateSegment<Set<StatusOgArbeidsgiver>>> segmenterMedInntektsforhold) {

        Periode periode = utledPeriode(segmenterMedInntektsforhold);

        var inntektsmelding = iayGrunnlag.getInntektsmeldinger().stream()
                .flatMap(it -> it.getAlleInntektsmeldinger().stream())
                .filter(im -> Objects.equals(im.getArbeidsgiver(), tilkommetInntektDto.getArbeidsgiver().orElse(null))
                        && tilkommetInntektDto.getArbeidsforholdRef().gjelderFor(im.getArbeidsforholdRef()))
                .findFirst();

        return new InntektsforholdDto(
                tilkommetInntektDto.getAktivitetStatus(),
                tilkommetInntektDto.getArbeidsgiver().map(Arbeidsgiver::getIdentifikator).orElse(null),
                tilkommetInntektDto.getArbeidsforholdRef().getReferanse(),
                finnEksternArbeidsforholdId(tilkommetInntektDto.getArbeidsgiver(), tilkommetInntektDto.getArbeidsforholdRef(), iayGrunnlag).map(EksternArbeidsforholdRef::getReferanse).orElse(null),
                periode,
                inntektsmelding.map(im -> im.getInntektBeløp().multipliser(12)).map(Beløp::getVerdi).map(BigDecimal::intValue).orElse(null),
                tilkommetInntektDto.getBruttoInntektPrÅr() != null ? tilkommetInntektDto.getBruttoInntektPrÅr().intValue() : null,
                tilkommetInntektDto.skalRedusereUtbetaling()
        );
    }

    private static <V> Periode utledPeriode(List<LocalDateSegment<V>> segmenter) {
        var fom = segmenter.stream().map(LocalDateSegment::getFom).min(Comparator.naturalOrder()).orElseThrow();
        var tom = segmenter.stream().map(LocalDateSegment::getTom).max(Comparator.naturalOrder()).orElseThrow();
        return new Periode(fom, tom);
    }

    private static Optional<EksternArbeidsforholdRef> finnEksternArbeidsforholdId(Optional<Arbeidsgiver> arbeidsgiver,
                                                                                  InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (arbeidsgiver.isEmpty() || arbeidsforholdRef.getReferanse() == null) {
            return Optional.empty();
        }
        return iayGrunnlag.getArbeidsforholdInformasjon()
                .map(d -> d.finnEkstern(arbeidsgiver.get(), arbeidsforholdRef));
    }

}
