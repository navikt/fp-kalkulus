package no.nav.folketrygdloven.kalkulator.guitjenester;


import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.ForeslåInntektGraderingForUendretResultat;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
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

        return lagVurderNyttInntektsforholdDto(beregningsgrunnlag, iayGrunnlag, ytelsespesifiktGrunnlag, input.getFagsakYtelseType(), input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling());
    }

    public static VurderNyttInntektsforholdDto lagVurderNyttInntektsforholdDto(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, FagsakYtelseType fagsakYtelseType, List<BeregningsgrunnlagGrunnlagDto> beregningsgrunnlagGrunnlagFraForrigeBehandling) {
        var tidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(beregningsgrunnlag.getSkjæringstidspunkt(),
                5, beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList(),
                (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag,
                iayGrunnlag,
                fagsakYtelseType
        );

        return getVurderNyttInntektsforholdDto(beregningsgrunnlag, iayGrunnlag, tidslinje, beregningsgrunnlagGrunnlagFraForrigeBehandling, ytelsespesifiktGrunnlag);
    }

    public static VurderNyttInntektsforholdDto getVurderNyttInntektsforholdDto(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDateTimeline<Set<StatusOgArbeidsgiver>> tidslinje, List<BeregningsgrunnlagGrunnlagDto> beregningsgrunnlagGrunnlagFraForrigeBehandling, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var bgPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();

        var periodeListe = bgPerioder.stream()
                .filter(it -> !it.getTilkomneInntekter().isEmpty())
                .map(it -> mapPeriode(iayGrunnlag, it, tidslinje, finnPeriodeFraForrigeBehandling(it, beregningsgrunnlagGrunnlagFraForrigeBehandling), ytelsespesifiktGrunnlag))
                .collect(Collectors.toList());

        if (!periodeListe.isEmpty()) {
            return new VurderNyttInntektsforholdDto(periodeListe);
        }

        return null;
    }

    private static Optional<BeregningsgrunnlagPeriodeDto> finnPeriodeFraForrigeBehandling(BeregningsgrunnlagPeriodeDto periode, List<BeregningsgrunnlagGrunnlagDto> beregningsgrunnlagGrunnlagFraForrigeBehandling) {
        var stpOverlappendePeriodeMap = beregningsgrunnlagGrunnlagFraForrigeBehandling.stream().filter(gr -> gr.getBeregningsgrunnlag().isPresent()).collect(
                Collectors.toMap(gr -> gr.getBeregningsgrunnlag().get().getSkjæringstidspunkt(),
                        gr -> gr.getBeregningsgrunnlag()
                                .stream()
                                .flatMap(b -> b.getBeregningsgrunnlagPerioder().stream())
                                .filter(p -> !p.getBeregningsgrunnlagPeriodeTom().equals(TIDENES_ENDE))
                                .filter(p -> p.getPeriode().overlapper(periode.getPeriode()))
                                .findFirst()));

        return stpOverlappendePeriodeMap.entrySet().stream()
                .filter(e -> e.getValue().isPresent())
                .max(Map.Entry.comparingByKey())
                .flatMap(Map.Entry::getValue);
    }

    private static VurderInntektsforholdPeriodeDto mapPeriode(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                              BeregningsgrunnlagPeriodeDto periode,
                                                              LocalDateTimeline<Set<StatusOgArbeidsgiver>> inntektsforholdTidslinje, Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var innteksforholdListe = mapInntektforholdDtoListe(iayGrunnlag, periode, inntektsforholdTidslinje, forrigePeriode, ytelsespesifiktGrunnlag);
        return new VurderInntektsforholdPeriodeDto(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), innteksforholdListe.stream().toList());
    }

    private static Set<InntektsforholdDto> mapInntektforholdDtoListe(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                     BeregningsgrunnlagPeriodeDto periode, LocalDateTimeline<Set<StatusOgArbeidsgiver>> inntektsforholdTidslinje,
                                                                     Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode,
                                                                     YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {

        return finnTilkomneInntekterForVisning(periode, forrigePeriode, ytelsespesifiktGrunnlag)
                .stream()
                .map(a -> mapTilInntektsforhold(a, iayGrunnlag, finnSegmenterSomInneholderInntektsforhold(inntektsforholdTidslinje, a).toList()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static List<TilkommetInntektDto> finnTilkomneInntekterForVisning(BeregningsgrunnlagPeriodeDto periode, Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var erVurdert = periode.getTilkomneInntekter().stream().anyMatch(t -> t.skalRedusereUtbetaling() != null);
        if (erVurdert || forrigePeriode.isEmpty()) {
            return periode.getTilkomneInntekter();

        }
        return ForeslåInntektGraderingForUendretResultat.foreslå(periode, forrigePeriode.get(), ytelsespesifiktGrunnlag);
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
