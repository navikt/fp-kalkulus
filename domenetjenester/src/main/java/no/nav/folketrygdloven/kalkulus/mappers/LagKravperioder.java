package no.nav.folketrygdloven.kalkulus.mappers;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.beregning.v1.KravperioderPrArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.beregning.v1.PerioderForKrav;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

class LagKravperioder {

    @Deprecated
    public static List<KravperioderPrArbeidsforhold> lagKravperioderPrArbeidsforhold(List<RefusjonskravDatoDto> refusjonskravDatoer,
                                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                     LocalDate skjæringstidspunktBeregning) {
        List<KravperioderPrArbeidsforhold> perioderPrArbeidsgiver = new ArrayList<>();
        if (iayGrunnlag.getInntektsmeldingDto() == null) {
            return Collections.emptyList();
        }
        iayGrunnlag.getInntektsmeldingDto().getInntektsmeldinger()
                .stream()
                .filter(im -> (im.getRefusjonBeløpPerMnd() != null &&
                        im.getRefusjonBeløpPerMnd().getVerdi().compareTo(BigDecimal.ZERO) > 0) ||
                        !im.getEndringerRefusjon().isEmpty())
                .forEach(im -> {
                    PerioderForKrav perioderForKravDto = lagPerioderForKrav(im,
                            finnInnsendsingsdato(refusjonskravDatoer, im.getArbeidsgiver(), skjæringstidspunktBeregning),
                            skjæringstidspunktBeregning,
                            iayGrunnlag.getArbeidDto());
                    lagNyEllerLeggTilEksisterende(perioderPrArbeidsgiver, im, perioderForKravDto);
                });
        return perioderPrArbeidsgiver;
    }

    private static LocalDate finnInnsendsingsdato(List<RefusjonskravDatoDto> refusjonskravDatoer, Aktør arbeidsgiver, LocalDate skjæringstidspunktBeregning) {
        if (refusjonskravDatoer == null) {
            return skjæringstidspunktBeregning;
        }
        return refusjonskravDatoer.stream().filter(d -> d.getArbeidsgiver().getIdent().equals(arbeidsgiver.getIdent()))
                .findFirst()
                .map(RefusjonskravDatoDto::getFørsteInnsendingAvRefusjonskrav)
                .orElse(skjæringstidspunktBeregning);
    }

    private static void lagNyEllerLeggTilEksisterende(List<KravperioderPrArbeidsforhold> perioderPrArbeidsgiver, InntektsmeldingDto im, PerioderForKrav perioderForKravDto) {
        Optional<KravperioderPrArbeidsforhold> eksisterende = perioderPrArbeidsgiver.stream()
                .filter(k -> k.getArbeidsgiver().getIdent().equals(im.getArbeidsgiver().getIdent()) &&
                        (im.getArbeidsforholdRef() == null || im.getArbeidsforholdRef().getAbakusReferanse() == null ||
                                k.getInternreferanse() == null || im.getArbeidsforholdRef().getAbakusReferanse().equals(k.getInternreferanse().getAbakusReferanse())))
                .findFirst();
        eksisterende.ifPresentOrElse(e -> e.getAlleSøktePerioder().add(perioderForKravDto),
                () ->
                    perioderPrArbeidsgiver.add(new KravperioderPrArbeidsforhold(im.getArbeidsgiver(),
                            im.getArbeidsforholdRef(),
                            new ArrayList<>(List.of(perioderForKravDto)),
                            perioderForKravDto))
                );
    }

    private static PerioderForKrav lagPerioderForKrav(InntektsmeldingDto im, LocalDate innsendingsdato,
                                                      LocalDate skjæringstidspunktBeregning,
                                                      ArbeidDto arbeidDto) {
        LocalDate startRefusjon = finnStartdatoRefusjon(im, skjæringstidspunktBeregning, arbeidDto);
        PerioderForKrav perioderForKravDto = new PerioderForKrav(innsendingsdato, lagPerioder(im, startRefusjon));
        return perioderForKravDto;
    }

    private static LocalDate finnStartdatoRefusjon(InntektsmeldingDto im, LocalDate skjæringstidspunktBeregning, ArbeidDto arbeidDto) {
        LocalDate startRefusjon;
        if (arbeidDto != null) {
            LocalDate startDatoArbeid = arbeidDto.getYrkesaktiviteter().stream()
                    .filter(y -> y.getArbeidsgiver().getIdent().equals(im.getArbeidsgiver().getIdent()) &&
                            matcherReferanse(y.getAbakusReferanse(), im.getArbeidsforholdRef()))
                    .flatMap(y -> y.getAktivitetsAvtaler().stream())
                    .filter(a -> a.getStillingsprosent() == null)
                    .filter(a -> a.getPeriode().getFom().isAfter(skjæringstidspunktBeregning))
                    .map(AktivitetsAvtaleDto::getPeriode)
                    .map(Periode::getFom)
                    .min(Comparator.naturalOrder())
                    .orElse(skjæringstidspunktBeregning);
            if (startDatoArbeid.isAfter(skjæringstidspunktBeregning)) {
                if (im.getStartDatoPermisjon() == null) {
                    startRefusjon = startDatoArbeid;
                } else {
                    startRefusjon = startDatoArbeid.isAfter(im.getStartDatoPermisjon()) ? startDatoArbeid : im.getStartDatoPermisjon();
                }
            } else {
                startRefusjon = skjæringstidspunktBeregning;
            }
        } else {
            startRefusjon = skjæringstidspunktBeregning;
        }
        return startRefusjon;
    }

    private static boolean matcherReferanse(InternArbeidsforholdRefDto abakusReferanse, InternArbeidsforholdRefDto abakusReferanse2) {
        return abakusReferanse == null || abakusReferanse.getAbakusReferanse() == null || abakusReferanse2 == null || abakusReferanse2.getAbakusReferanse() == null ||
                Objects.equals(abakusReferanse.getAbakusReferanse(), abakusReferanse2.getAbakusReferanse());
    }

    private static List<Refusjonsperiode> lagPerioder(InntektsmeldingDto im, LocalDate startdatoRefusjon) {
        ArrayList<LocalDateSegment<BigDecimal>> alleSegmenter = new ArrayList<>();
        if (!(im.getRefusjonBeløpPerMnd() == null || im.getRefusjonBeløpPerMnd().getVerdi().compareTo(BigDecimal.ZERO) ==0)) {
            alleSegmenter.add(new LocalDateSegment<>(startdatoRefusjon, TIDENES_ENDE, im.getRefusjonBeløpPerMnd().getVerdi()));
        }

        alleSegmenter.addAll(im.getEndringerRefusjon().stream().map(e ->
                new LocalDateSegment<>(e.getFom(), TIDENES_ENDE, e.getRefusjonsbeløpMnd().getVerdi())
        ).collect(Collectors.toList()));

        if (im.getRefusjonOpphører() != null && !im.getRefusjonOpphører().equals(TIDENES_ENDE)) {
            alleSegmenter.add(new LocalDateSegment<>(im.getRefusjonOpphører().plusDays(1), TIDENES_ENDE, BigDecimal.ZERO));
        }

        var refusjonTidslinje = new LocalDateTimeline<>(alleSegmenter, (interval, lhs, rhs) -> {
            if (lhs.getFom().isBefore(rhs.getFom())) {
                return new LocalDateSegment<>(interval, rhs.getValue());
            }
            return new LocalDateSegment<>(interval, lhs.getValue());
        });


        return refusjonTidslinje.stream()
                .map(r -> new Refusjonsperiode(new Periode(r.getFom(), r.getTom()), r.getValue()))
                .collect(Collectors.toList());

    }

}
