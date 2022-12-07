package no.nav.folketrygdloven.kalkulator.guitjenester;


import static no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektsforholdTjeneste.finnTilkomneInntektsforhold;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.InntektsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderInntektsforholdPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderNyttInntektsforholdDto;

class VurderNyeInntektsforholdDtoTjeneste {

    public static VurderNyttInntektsforholdDto lagDto(BeregningsgrunnlagGUIInput input) {
        var ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        var iayGrunnlag = input.getIayGrunnlag();
        var yrkesaktiviteter = iayGrunnlag.getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter).orElse(Collections.emptyList());
        var skjæringstidspunktForBeregning = input.getSkjæringstidspunktForBeregning();

        var bgPerioder = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();

        var nyeArbeidsforholdPrPeriode = utledNyeInntektsforholdPrPeriode(
                bgPerioder, ytelsespesifiktGrunnlag,
                yrkesaktiviteter,
                skjæringstidspunktForBeregning
        );

        var periodeListe = mapTilPerioderForNyeInntektsforhold(iayGrunnlag, nyeArbeidsforholdPrPeriode);

        if (!periodeListe.isEmpty()) {
            return new VurderNyttInntektsforholdDto(periodeListe);
        }

        return null;
    }

    private static List<VurderInntektsforholdPeriodeDto> mapTilPerioderForNyeInntektsforhold(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                             Map<BeregningsgrunnlagPeriodeDto, Set<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver>> nyeArbeidsforholdPrPeriode) {
        return nyeArbeidsforholdPrPeriode.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> mapPeriode(iayGrunnlag, e))
                .collect(Collectors.toList());
    }

    private static Map<BeregningsgrunnlagPeriodeDto, Set<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver>> utledNyeInntektsforholdPrPeriode(List<BeregningsgrunnlagPeriodeDto> bgPerioder,
                                                                                                                                                  YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                                                                                                  Collection<YrkesaktivitetDto> yrkesaktiviteter, LocalDate skjæringstidspunktForBeregning) {
        return bgPerioder.stream().collect(
                Collectors.toMap(Function.identity(), p -> finnInntektsforholdForPeriode(ytelsespesifiktGrunnlag, yrkesaktiviteter, skjæringstidspunktForBeregning, p)));
    }

    private static Set<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> finnInntektsforholdForPeriode(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                                                            Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                                            LocalDate skjæringstidspunktForBeregning,
                                                                                                            BeregningsgrunnlagPeriodeDto p) {
        return finnTilkomneInntektsforhold(skjæringstidspunktForBeregning,
                yrkesaktiviteter,
                p.getBeregningsgrunnlagPrStatusOgAndelList(),
                p.getPeriode(),
                ytelsespesifiktGrunnlag);
    }

    private static VurderInntektsforholdPeriodeDto mapPeriode(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                              Map.Entry<BeregningsgrunnlagPeriodeDto, Set<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver>> e) {
        var periode = e.getKey();
        var nyeInntektsforholdListe = new LinkedHashSet<>(e.getValue());
        var innteksforholdListe = new ArrayList<InntektsforholdDto>();
        innteksforholdListe.addAll(mapAndelerTilInntektsforholdDto(iayGrunnlag, periode, nyeInntektsforholdListe));
        innteksforholdListe.addAll(mapResterendeUtenAndel(nyeInntektsforholdListe, innteksforholdListe));
        var tilkomneInntekter = periode.getTilkomneInntekter();
        setFelterForBekreftetVerdi(tilkomneInntekter, innteksforholdListe);
        setInnteksmeldingFelter(iayGrunnlag, innteksforholdListe);
        return new VurderInntektsforholdPeriodeDto(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), innteksforholdListe.stream().toList());
    }


    private static Set<InntektsforholdDto> mapResterendeUtenAndel(LinkedHashSet<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> nyeInntektsforholdListe, Collection<InntektsforholdDto> tilkomneInntektsforhold) {
        var tilkomneInntekterUtenAndel = finnTilkomneUtenAndel(nyeInntektsforholdListe, tilkomneInntektsforhold);
        return tilkomneInntekterUtenAndel.stream().map(it -> new InntektsforholdDto(it.aktivitetStatus(), it.arbeidsgiver() != null ? it.arbeidsgiver().getIdentifikator() : null, null, null)).collect(Collectors.toSet());
    }

    private static List<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> finnTilkomneUtenAndel(LinkedHashSet<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> nyeInntektsforholdListe, Collection<InntektsforholdDto> tilkomneInntektsforhold) {
        return nyeInntektsforholdListe.stream()
                .filter(it -> tilkomneInntektsforhold.stream().noneMatch(ti -> ti.getAktivitetStatus().equals(it.aktivitetStatus()) &&
                        Objects.equals(ti.getArbeidsgiverIdentifikator(), finnArbeidsgiverIdentifikator(it))))
                .toList();
    }

    private static String finnArbeidsgiverIdentifikator(TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver it) {
        return it.arbeidsgiver() != null ? it.arbeidsgiver().getIdentifikator() : null;
    }

    private static void setInnteksmeldingFelter(InntektArbeidYtelseGrunnlagDto iayGrunnlag, Collection<InntektsforholdDto> tilkomneInntektsforhold) {
        tilkomneInntektsforhold.forEach(ti -> {
            var inntektsmelding = iayGrunnlag.getInntektsmeldinger().stream()
                    .flatMap(it -> it.getAlleInntektsmeldinger().stream())
                    .filter(im -> Objects.equals(im.getArbeidsgiver().getIdentifikator(), ti.getArbeidsgiverIdentifikator())
                            && InternArbeidsforholdRefDto.ref(ti.getArbeidsforholdId()).gjelderFor(im.getArbeidsforholdRef()))
                    .findFirst();
            inntektsmelding.map(im -> im.getInntektBeløp().getVerdi().multiply(BigDecimal.valueOf(12)))
                    .map(BigDecimal::intValue)
                    .ifPresent(ti::setBruttoInntektPrÅr);
            ti.setHarInntektsmelding(inntektsmelding.isPresent());
        });
    }

    private static void setFelterForBekreftetVerdi(List<TilkommetInntektDto> tilkomneInntekter, Collection<InntektsforholdDto> tilkomneInntektsforhold) {
        tilkomneInntektsforhold.forEach(inntektsforholdDto -> settBekreftetVerdiHvisFinnes(tilkomneInntekter, inntektsforholdDto));
    }

    private static void settBekreftetVerdiHvisFinnes(List<TilkommetInntektDto> tilkomneInntekter, InntektsforholdDto inntektsforholdDto) {
        var gjeldendeVerdi = finnGjeldendeVurdering(tilkomneInntekter, inntektsforholdDto);
        gjeldendeVerdi.map(TilkommetInntektDto::getBruttoInntektPrÅr)
                .map(BigDecimal::intValue).ifPresent(inntektsforholdDto::setBruttoInntektPrÅr);
        gjeldendeVerdi.map(TilkommetInntektDto::skalRedusereUtbetaling).ifPresent(inntektsforholdDto::setSkalRedusereUtbetaling);
    }

    private static Set<InntektsforholdDto> mapAndelerTilInntektsforholdDto(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                           BeregningsgrunnlagPeriodeDto periode,
                                                                           Set<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> nyeInntektsforholdListe) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> erNyttArbeidsforhold(nyeInntektsforholdListe, a))
                .map(a -> mapTilInntektsforhold(a, iayGrunnlag))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static boolean erNyttArbeidsforhold(Set<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> nyeInntektsforholdListe, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return finnMatchendeNyttInntektsforhold(nyeInntektsforholdListe, a).isPresent();
    }

    private static Optional<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> finnMatchendeNyttInntektsforhold(Set<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> nyeInntektsforholdListe, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return nyeInntektsforholdListe.stream().filter(sa -> sa.aktivitetStatus().equals(a.getAktivitetStatus()) &&
                Objects.equals(sa.arbeidsgiver(), a.getArbeidsgiver().orElse(null))).findFirst();
    }

    private static InntektsforholdDto mapTilInntektsforhold(BeregningsgrunnlagPrStatusOgAndelDto a, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return new InntektsforholdDto(
                a.getAktivitetStatus(),
                a.getArbeidsgiver().map(Arbeidsgiver::getIdentifikator).orElse(null),
                a.getArbeidsforholdRef().map(InternArbeidsforholdRefDto::getReferanse).orElse(null),
                finnEksternArbeidsforholdId(a.getArbeidsgiver(), a.getArbeidsforholdRef(), iayGrunnlag).map(EksternArbeidsforholdRef::getReferanse).orElse(null));
    }

    private static Optional<EksternArbeidsforholdRef> finnEksternArbeidsforholdId(Optional<Arbeidsgiver> arbeidsgiver,
                                                                                  Optional<InternArbeidsforholdRefDto> arbeidsforholdRef,
                                                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (arbeidsgiver.isEmpty() || arbeidsforholdRef.isEmpty()) {
            return Optional.empty();
        }
        return iayGrunnlag.getArbeidsforholdInformasjon()
                .map(d -> d.finnEkstern(arbeidsgiver.get(), arbeidsforholdRef.get()));
    }

    private static Optional<TilkommetInntektDto> finnGjeldendeVurdering(List<TilkommetInntektDto> tilkomneInntekter, InntektsforholdDto inntektsforholdDto) {
        return tilkomneInntekter.stream().filter(ti -> ti.getAktivitetStatus().equals(inntektsforholdDto.getAktivitetStatus()) &&
                        Objects.equals(ti.getArbeidsgiver().map(Arbeidsgiver::getIdentifikator)
                                .orElse(null), inntektsforholdDto.getArbeidsgiverIdentifikator()) &&
                        ti.getArbeidsforholdRef().gjelderFor(InternArbeidsforholdRefDto.ref(inntektsforholdDto.getArbeidsforholdId())))
                .findFirst();
    }

}
