package no.nav.folketrygdloven.kalkulus.mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
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
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.vedtak.konfig.Tid;

public class MapInntektsmeldingerTilKravperioder {

    public static List<KravperioderPrArbeidsforholdDto> map(InntektArbeidYtelseGrunnlagDto grunnlagDto, LocalDate skjæringstidspunkt) {
        // Henter yrkesaktiviteter for aktør, hvis ingen, returner tom liste
        var overstyrteYrkesaktiviteter = mapOverstyringerTilYrkesaktiviteter(grunnlagDto.getArbeidsforholdInformasjon());
        Set<YrkesaktivitetDto> yrkesaktiviteter = new HashSet<>(overstyrteYrkesaktiviteter);
        if (grunnlagDto.getArbeidDto() != null) {
            yrkesaktiviteter.addAll(grunnlagDto.getArbeidDto().getYrkesaktiviteter());
        }
        var arbeidsforhold = yrkesaktiviteter.stream()
            .filter(ya -> !erFrilans(ya))
            .toList();
        if (arbeidsforhold.isEmpty()) {
            return Collections.emptyList();
        }

        var sisteIMPrArbeidsforhold = finnSisteInntektsmeldingMedRefusjonPrArbeidsforhold(grunnlagDto);
        var gruppertPrArbeidsforhold = grupperInntektsmeldingerMedRefusjonPrArbeidsforhold(grunnlagDto.getAlleInntektsmeldingerPåSak());

        return gruppertPrArbeidsforhold.entrySet()
            .stream()
            .filter(kravnøkkelOgInntektsmeldinger -> sisteIMPrArbeidsforhold.containsKey(kravnøkkelOgInntektsmeldinger.getKey()))
            .map(kravnøkkelOgInntektsmeldinger -> mapTilKravPrArbeidsforhold(skjæringstidspunkt, arbeidsforhold, sisteIMPrArbeidsforhold,
                kravnøkkelOgInntektsmeldinger))
            .flatMap(Optional::stream)
            .toList();
    }

    private static boolean erFrilans(YrkesaktivitetDto ya) {
        return ya.getArbeidType().equals(ArbeidType.FRILANSER) || ya.getArbeidType().equals(ArbeidType.FRILANSER_OPPDRAGSTAKER);
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
        List<InntektsmeldingDto> aktiveInntektsmeldinger = grunnlagDto.getInntektsmeldingDto() == null ? List.of() : grunnlagDto.getInntektsmeldingDto().getInntektsmeldinger();
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
        return mapReferanse(ref1).gjelderFor(mapReferanse(ref2));
    }

    private static Optional<KravperioderPrArbeidsforholdDto> mapTilKravPrArbeidsforhold(LocalDate skjæringstidspunktOpptjening,
                                                                                     Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                     Map<Kravnøkkel, InntektsmeldingDto> sisteIMPrArbeidsforhold,
                                                                                     Map.Entry<Kravnøkkel, List<InntektsmeldingDto>> kravnøkkelOgInntektsmeldinger) {
        var alleTidligereKravPerioder = lagPerioderForAlle(skjæringstidspunktOpptjening, yrkesaktiviteter, kravnøkkelOgInntektsmeldinger.getValue());
        var sistePerioder = lagPerioderForKrav(sisteIMPrArbeidsforhold.get(kravnøkkelOgInntektsmeldinger.getKey()), skjæringstidspunktOpptjening,
            yrkesaktiviteter);
        // Her kan vi ende opp uten refusjonsperioder hvis stp har flyttet seg til å være før opphørsdato i inntektsmeldingen,
        // legger på filtrering for å ikke ta med disse da de er uinteressante
        if (alleTidligereKravPerioder.isEmpty() || sistePerioder.getPerioder().isEmpty()) {
            return Optional.empty();
        }
        var sisteIntervaller = sistePerioder.getPerioder().stream().map(RefusjonsperiodeDto::periode).toList();
        return Optional.of(new KravperioderPrArbeidsforholdDto(mapArbeidsgiver(kravnøkkelOgInntektsmeldinger.getKey().arbeidsgiver),
            mapReferanse(kravnøkkelOgInntektsmeldinger.getKey().referanse), alleTidligereKravPerioder, sisteIntervaller));
    }

    private static List<PerioderForKravDto> lagPerioderForAlle(LocalDate skjæringstidspunktOpptjening,
                                                               Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                               List<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream().map(im -> lagPerioderForKrav(im, skjæringstidspunktOpptjening, yrkesaktiviteter))
            // Her kan vi ende opp uten refusjonsperioder hvis stp har flyttet seg til å være før opphørsdato i inntektsmeldingen,
            // legger på filtrering for å ikke ta med disse da de er uinteressante
            .filter(kp -> !kp.getPerioder().isEmpty()).toList();
    }

    private static PerioderForKravDto lagPerioderForKrav(InntektsmeldingDto im,
                                                      LocalDate skjæringstidspunktOpptjening,
                                                      Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        var startRefusjon = finnStartdatoRefusjon(im, skjæringstidspunktOpptjening, yrkesaktiviteter);
        return new PerioderForKravDto(im.getInnsendingsdato(), mapRefusjonsperioder(im, startRefusjon));
    }

    private static LocalDate finnStartdatoRefusjon(InntektsmeldingDto im,
                                                   LocalDate skjæringstidspunkt,
                                                   Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        var startDatoArbeid = yrkesaktiviteter.stream()
            .filter(y -> y.getArbeidsgiver().getIdent().equals(im.getArbeidsgiver().getIdent()) && gjelderFor(y.getAbakusReferanse(), im.getArbeidsforholdRef()))
            .flatMap(y -> y.getAktivitetsAvtaler().stream())
            .filter(aa -> aa.getStillingsprosent() == null && aa.getSisteLønnsendringsdato() == null)
            .map(AktivitetsAvtaleDto::getPeriode)
            .filter(periode -> !periode.getTom().isBefore(skjæringstidspunkt))
            .map(Periode::getFom)
            .min(Comparator.naturalOrder())
            .orElse(skjæringstidspunkt);

        if (startDatoArbeid.isAfter(skjæringstidspunkt)) {
            var startDatoPermisjon = im.getStartDatoPermisjon();
            if (startDatoPermisjon != null && !startDatoArbeid.isAfter(startDatoPermisjon)) {
                return startDatoPermisjon;
            }
            return startDatoArbeid;
        } else {
            return skjæringstidspunkt;
        }
    }

    private static List<RefusjonsperiodeDto> mapRefusjonsperioder(InntektsmeldingDto im, LocalDate startdatoRefusjon) {
        if (opphørerRefusjonFørStartdato(im, startdatoRefusjon)) {
            return Collections.emptyList();
        }

        var refusjonTidslinje = new LocalDateTimeline<>(opprettRefusjonSegmenter(im, startdatoRefusjon),
            (interval, lhs, rhs) -> lhs.getFom().isBefore(rhs.getFom()) ? new LocalDateSegment<>(interval, rhs.getValue()) : new LocalDateSegment<>(
                interval, lhs.getValue()));

        return refusjonTidslinje.stream()
            .map(segment -> new RefusjonsperiodeDto(Intervall.fraOgMedTilOgMed(segment.getFom(), segment.getTom()), no.nav.folketrygdloven.kalkulator.modell.typer.Beløp.fra(segment.getValue())))
            .toList();
    }

    private static boolean opphørerRefusjonFørStartdato(InntektsmeldingDto im, LocalDate startdatoRefusjon) {
        return im.getRefusjonOpphører() != null && im.getRefusjonOpphører().isBefore(startdatoRefusjon);
    }

    private static ArrayList<LocalDateSegment<BigDecimal>> opprettRefusjonSegmenter(InntektsmeldingDto im, LocalDate startdatoRefusjon) {
        var segmenter = new ArrayList<LocalDateSegment<BigDecimal>>();
        if (erRefusjonsbeløpUliktNull(im)) {
            leggTilSegment(segmenter, startdatoRefusjon, im.getRefusjonBeløpPerMnd().verdi());
        }

        im.getEndringerRefusjon().forEach(endring -> leggTilSegment(segmenter, endring.getFom(), endring.getRefusjonsbeløpMnd().verdi()));

        if (harRefusjonOpphørsdato(im)) {
            leggTilSegment(segmenter, im.getRefusjonOpphører().plusDays(1), BigDecimal.ZERO);
        }
        return segmenter;
    }

    private static boolean erRefusjonsbeløpUliktNull(InntektsmeldingDto im) {
        return !(im.getRefusjonBeløpPerMnd() == null || im.getRefusjonBeløpPerMnd().verdi().compareTo(BigDecimal.ZERO) == 0);
    }

    private static boolean harRefusjonOpphørsdato(InntektsmeldingDto im) {
        return im.getRefusjonOpphører() != null && !im.getRefusjonOpphører().equals(Tid.TIDENES_ENDE);
    }

    private static void leggTilSegment(ArrayList<LocalDateSegment<BigDecimal>> segmenter, LocalDate fom, BigDecimal verdi) {
        segmenter.add(new LocalDateSegment<>(fom, Tid.TIDENES_ENDE, verdi));
    }

    private static Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
        return arbeidsgiver.getErPerson() ? Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent())) : Arbeidsgiver.virksomhet(arbeidsgiver.getIdent());
    }

    private static no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto mapReferanse(InternArbeidsforholdRefDto arbeidsforholdRef) {
        return arbeidsforholdRef == null || arbeidsforholdRef.getAbakusReferanse() == null
            ? no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto.nullRef()
            : no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto.ref(arbeidsforholdRef.getAbakusReferanse());
    }

    private record Kravnøkkel(Aktør arbeidsgiver, InternArbeidsforholdRefDto referanse) {
    }
}
