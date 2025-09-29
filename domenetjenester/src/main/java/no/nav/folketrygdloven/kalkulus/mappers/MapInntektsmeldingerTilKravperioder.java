package no.nav.folketrygdloven.kalkulus.mappers;

import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.KravperioderPrArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MapInntektsmeldingerTilKravperioder {

    public static List<KravperioderPrArbeidsforholdDto> map(InntektArbeidYtelseGrunnlagDto grunnlagDto, LocalDate skjæringstidspunkt) {
        // Henter yrkesaktiviteter for aktør, hvis ingen, returner tom liste
        var overstyrteYrkesaktiviteter = mapOverstyringerTilYrkesaktiviteter(grunnlagDto.getArbeidsforholdInformasjon());
        Set<YrkesaktivitetDto> yrkesaktiviteter = new HashSet<>(overstyrteYrkesaktiviteter);
        if (grunnlagDto.getArbeidDto() != null) {
            yrkesaktiviteter.addAll(grunnlagDto.getArbeidDto().getYrkesaktiviteter());
        }
        if (yrkesaktiviteter.isEmpty()) {
            return Collections.emptyList();
        }

        var sisteIMPrArbeidsforhold = finnSisteInntektsmeldingMedRefusjonPrArbeidsforhold(grunnlagDto);
        var gruppertPrArbeidsforhold = grupperInntektsmeldingerMedRefusjonPrArbeidsforhold(grunnlagDto.getAlleInntektsmeldingerPåSak());

        return gruppertPrArbeidsforhold.entrySet()
            .stream()
            .filter(kravnøkkelOgInntektsmeldinger -> sisteIMPrArbeidsforhold.containsKey(kravnøkkelOgInntektsmeldinger.getKey()))
            .map(kravnøkkelOgInntektsmeldinger -> mapTilKravPrArbeidsforhold(skjæringstidspunkt, yrkesaktiviteter, sisteIMPrArbeidsforhold,
                kravnøkkelOgInntektsmeldinger))
            .flatMap(Optional::stream)
            .toList();
    }

    private static List<YrkesaktivitetDto> mapOverstyringerTilYrkesaktiviteter(ArbeidsforholdInformasjonDto arbeidsforholdInformasjon) {
        if (arbeidsforholdInformasjon == null || arbeidsforholdInformasjon.getOverstyringer() == null) {
            return Collections.emptyList();
        }
        var overstyringer = arbeidsforholdInformasjon.getOverstyringer()
            .stream()
            .filter(os -> os.getStillingsprosent() != null && os.getStillingsprosent().verdi() != null)
            .toList();
        return overstyringer.stream()
            .map(os -> new YrkesaktivitetDto(os.getArbeidsgiver(), os.getArbeidsforholdRefDto(), ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
                mapPerioder(os.getArbeidsforholdOverstyrtePerioder())))
            .toList();
    }

    private static List<AktivitetsAvtaleDto> mapPerioder(List<Periode> arbeidsforholdOverstyrtePerioder) {
        return arbeidsforholdOverstyrtePerioder.stream().map(os -> new AktivitetsAvtaleDto(new Periode(os.getFom(), os.getTom()), null, null)).toList();
    }

    private static Map<Kravnøkkel, InntektsmeldingDto> finnSisteInntektsmeldingMedRefusjonPrArbeidsforhold(InntektArbeidYtelseGrunnlagDto grunnlagDto) {
        var aktiveInntektsmeldinger = grunnlagDto.getInntektsmeldingDto() == null ? List.of() : grunnlagDto.getInntektsmeldingDto().getInntektsmeldinger();
        var inntektsmeldingerMedRefusjonskrav = filtrerKunRefusjon(aktiveInntektsmeldinger);
        return inntektsmeldingerMedRefusjonskrav.stream()
            .collect(Collectors.toMap(im -> new Kravnøkkel(im.getArbeidsgiver(), im.getArbeidsforholdRef()), im -> im));
    }

    private static Map<Kravnøkkel, List<InntektsmeldingDto>> grupperInntektsmeldingerMedRefusjonPrArbeidsforhold(Collection<InntektsmeldingDto> inntektsmeldinger) {
        var inntektsmeldingerMedRefusjonskrav = filtrerKunRefusjon(inntektsmeldinger);
        var grupperteInntektsmeldinger = lagKravnøklerForInntektsmeldinger(inntektsmeldingerMedRefusjonskrav);
        inntektsmeldingerMedRefusjonskrav.forEach(im -> finnKeysSomSkalHaInntektsmelding(grupperteInntektsmeldinger, im).forEach(
            kravnøkkel -> grupperteInntektsmeldinger.get(kravnøkkel).add(im)));
        return grupperteInntektsmeldinger;
    }

    private static List<InntektsmeldingDto> filtrerKunRefusjon(Collection<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream().filter(MapInntektsmeldingerTilKravperioder::harRefusjonsbeløpUliktNull).toList();
    }

    private static boolean harRefusjonsbeløpUliktNull(InntektsmeldingDto im) {
        var erRefusjonNull = erNullEllerNulltall(im.getRefusjonBeløpPerMnd());
        return !erRefusjonNull || im.getEndringerRefusjon()
            .stream()
            .anyMatch(e -> !erNullEllerNulltall(e.getRefusjonsbeløpMnd()));
    }

    private static boolean erNullEllerNulltall(Beløp beløp) {
        return beløp == null || beløp.compareTo(Beløp.ZERO) == 0;
    }

    private static Map<Kravnøkkel, List<InntektsmeldingDto>> lagKravnøklerForInntektsmeldinger(List<InntektsmeldingDto> inntektsmeldingerMedRefusjonskrav) {
        return inntektsmeldingerMedRefusjonskrav.stream()
            .map(im -> new Kravnøkkel(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
            .distinct()
            .collect(Collectors.toMap(n -> n, n -> new ArrayList<>()));
    }

    private static Set<Kravnøkkel> finnKeysSomSkalHaInntektsmelding(Map<Kravnøkkel, List<InntektsmeldingDto>> kravnøklerOgInntektsmeldinger,
                                                                    InntektsmeldingDto im) {
        return kravnøklerOgInntektsmeldinger.keySet()
            .stream()
            .filter(nøkkel -> nøkkel.arbeidsgiver.equals(im.getArbeidsgiver()) && gjelderFor(nøkkel.referanse, im.getArbeidsforholdRef()))
            .collect(Collectors.toSet());
    }

    private static boolean gjelderFor(InternArbeidsforholdRefDto ref1, InternArbeidsforholdRefDto ref2) {
        var erNullRef = ref1.getAbakusReferanse() == null || ref2.getAbakusReferanse() == null;
        return erNullRef || Objects.equals(ref1.getAbakusReferanse(), ref2.getAbakusReferanse());
    }

    private static Optional<KravperioderPrArbeidsforhold> mapTilKravPrArbeidsforhold(LocalDate skjæringstidspunktOpptjening,
                                                                                     Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                     Map<Kravnøkkel, InntektsmeldingDto> sisteIMPrArbeidsforhold,
                                                                                     Map.Entry<Kravnøkkel, List<InntektsmeldingDto>> kravnøkkelOgInntektsmeldinger) {
        var alleTidligereKravPerioder = lagPerioderForAlle(skjæringstidspunktOpptjening, yrkesaktiviteter, kravnøkkelOgInntektsmeldinger.getValue());
        var sistePerioder = lagPerioderForKrav(sisteIMPrArbeidsforhold.get(kravnøkkelOgInntektsmeldinger.getKey()), skjæringstidspunktOpptjening,
            yrkesaktiviteter);
        // Her kan vi ende opp uten refusjonsperioder hvis stp har flyttet seg til å være før opphørsdato i inntektsmeldingen,
        // legger på filtrering for å ikke ta med disse da de er uinteressante
        if (alleTidligereKravPerioder.isEmpty() || sistePerioder.getRefusjonsperioder().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new KravperioderPrArbeidsforhold(mapTilAktør(kravnøkkelOgInntektsmeldinger.getKey().arbeidsgiver),
            mapReferanse(kravnøkkelOgInntektsmeldinger.getKey().referanse), alleTidligereKravPerioder, sistePerioder));
    }

    private static List<PerioderForKravDto> lagPerioderForAlle(LocalDate skjæringstidspunktOpptjening,
                                                               Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                               List<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream().map(im -> lagPerioderForKrav(im, skjæringstidspunktOpptjening, yrkesaktiviteter))
            // Her kan vi ende opp uten refusjonsperioder hvis stp har flyttet seg til å være før opphørsdato i inntektsmeldingen,
            // legger på filtrering for å ikke ta med disse da de er uinteressante
            .filter(kp -> !kp.getRefusjonsperioder().isEmpty()).toList();
    }

    private static PerioderForKrav lagPerioderForKrav(Inntektsmelding im,
                                                      LocalDate skjæringstidspunktOpptjening,
                                                      Collection<Yrkesaktivitet> yrkesaktiviteter) {
        var startRefusjon = finnStartdatoRefusjon(im, skjæringstidspunktOpptjening, yrkesaktiviteter);
        return new PerioderForKrav(im.getInnsendingstidspunkt().toLocalDate(), mapRefusjonsperioder(im, startRefusjon));
    }

    private static LocalDate finnStartdatoRefusjon(Inntektsmelding im,
                                                   LocalDate skjæringstidspunktOpptjening,
                                                   Collection<Yrkesaktivitet> yrkesaktiviteter) {
        var startDatoArbeid = yrkesaktiviteter.stream()
            .filter(y -> y.getArbeidsgiver().getIdentifikator().equals(im.getArbeidsgiver().getIdentifikator()) && y.getArbeidsforholdRef()
                .gjelderFor(im.getArbeidsforholdRef()))
            .flatMap(y -> y.getAlleAktivitetsAvtaler().stream())
            .filter(AktivitetsAvtale::erAnsettelsesPeriode)
            .map(AktivitetsAvtale::getPeriode)
            .filter(periode -> !periode.getTomDato().isBefore(skjæringstidspunktOpptjening))
            .map(DatoIntervallEntitet::getFomDato)
            .min(Comparator.naturalOrder())
            .orElse(skjæringstidspunktOpptjening);

        return startDatoArbeid.isAfter(skjæringstidspunktOpptjening) ? im.getStartDatoPermisjon()
            .filter(startDatoPermisjon -> !startDatoArbeid.isAfter(startDatoPermisjon))
            .orElse(startDatoArbeid) : skjæringstidspunktOpptjening;
    }

    private static List<Refusjonsperiode> mapRefusjonsperioder(Inntektsmelding im, LocalDate startdatoRefusjon) {
        if (opphørerRefusjonFørStartdato(im, startdatoRefusjon)) {
            return Collections.emptyList();
        }

        var refusjonTidslinje = new LocalDateTimeline<>(opprettRefusjonSegmenter(im, startdatoRefusjon),
            (interval, lhs, rhs) -> lhs.getFom().isBefore(rhs.getFom()) ? new LocalDateSegment<>(interval, rhs.getValue()) : new LocalDateSegment<>(
                interval, lhs.getValue()));

        return refusjonTidslinje.stream()
            .map(segment -> new Refusjonsperiode(new Periode(segment.getFom(), segment.getTom()), Beløp.fra(segment.getValue())))
            .toList();
    }

    private static boolean opphørerRefusjonFørStartdato(Inntektsmelding im, LocalDate startdatoRefusjon) {
        return im.getRefusjonOpphører() != null && im.getRefusjonOpphører().isBefore(startdatoRefusjon);
    }

    private static ArrayList<LocalDateSegment<BigDecimal>> opprettRefusjonSegmenter(Inntektsmelding im, LocalDate startdatoRefusjon) {
        var segmenter = new ArrayList<LocalDateSegment<BigDecimal>>();
        if (erRefusjonsbeløpUliktNull(im)) {
            leggTilSegment(segmenter, startdatoRefusjon, im.getRefusjonBeløpPerMnd().getVerdi());
        }

        im.getEndringerRefusjon().forEach(endring -> leggTilSegment(segmenter, endring.getFom(), endring.getRefusjonsbeløp().getVerdi()));

        if (harRefusjonOpphørsdato(im)) {
            leggTilSegment(segmenter, im.getRefusjonOpphører().plusDays(1), BigDecimal.ZERO);
        }
        return segmenter;
    }

    private static boolean erRefusjonsbeløpUliktNull(Inntektsmelding im) {
        return !(im.getRefusjonBeløpPerMnd() == null || im.getRefusjonBeløpPerMnd().getVerdi().compareTo(BigDecimal.ZERO) == 0);
    }

    private static boolean harRefusjonOpphørsdato(Inntektsmelding im) {
        return im.getRefusjonOpphører() != null && !im.getRefusjonOpphører().equals(Tid.TIDENES_ENDE);
    }

    private static void leggTilSegment(ArrayList<LocalDateSegment<BigDecimal>> segmenter, LocalDate fom, BigDecimal verdi) {
        segmenter.add(new LocalDateSegment<>(fom, Tid.TIDENES_ENDE, verdi));
    }

    private static Aktør mapTilAktør(Arbeidsgiver arbeidsgiver) {
        return arbeidsgiver.getErVirksomhet() ? new Organisasjon(arbeidsgiver.getIdentifikator()) : new AktørIdPersonident(
            arbeidsgiver.getIdentifikator());
    }

    private static no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto mapReferanse(InternArbeidsforholdRef arbeidsforholdRef) {
        return arbeidsforholdRef.getReferanse() == null ? null : new no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto(
            arbeidsforholdRef.getReferanse());
    }

    public record Kravnøkkel(Aktør arbeidsgiver, InternArbeidsforholdRefDto referanse) {
    }
}
