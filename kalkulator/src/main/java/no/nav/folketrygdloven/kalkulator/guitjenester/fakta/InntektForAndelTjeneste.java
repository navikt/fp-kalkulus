package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;

class InntektForAndelTjeneste {

    private static final int MND_I_1_ÅR = 12;

    private InntektForAndelTjeneste() {
        // Hide pulbic constructor
    }

    static BigDecimal finnSnittinntektForArbeidstakerIBeregningsperioden(InntektFilterDto filter, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        LocalDate fraDato = andel.getBeregningsperiodeFom();
        LocalDate tilDato = andel.getBeregningsperiodeTom();
        long beregningsperiodeLengdeIMnd = ChronoUnit.MONTHS.between(fraDato, tilDato.plusDays(1));
        BigDecimal totalBeløp = finnTotalbeløpIBeregningsperioden(filter, andel, tilDato, beregningsperiodeLengdeIMnd);
        return totalBeløp.divide(BigDecimal.valueOf(beregningsperiodeLengdeIMnd), 10, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal finnTotalbeløpIBeregningsperioden(InntektFilterDto filter, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel, LocalDate tilDato,
                                                                Long beregningsperiodeLengdeIMnd) {
        if (filter.isEmpty()) {
            return BigDecimal.ZERO;
        }
        var inntekter = finnInntekterForAndel(andel, filter);

        AtomicReference<BigDecimal> totalBeløp = new AtomicReference<>(BigDecimal.ZERO);
        inntekter.forFilter((inntekt, inntektsposter) -> totalBeløp
            .set(totalBeløp.get().add(summerInntekterIBeregningsperioden(tilDato, inntektsposter, beregningsperiodeLengdeIMnd))));

        return totalBeløp.get();
    }

    static BigDecimal finnSnittinntektPrÅrForArbeidstakerIBeregningsperioden(InntektFilterDto filter, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (filter.isEmpty()) {
            return BigDecimal.ZERO;
        }
        LocalDate fraDato = andel.getBeregningsperiodeFom();
        LocalDate tilDato = andel.getBeregningsperiodeTom();
        Long beregningsperiodeLengdeIMnd = ChronoUnit.MONTHS.between(fraDato, tilDato.plusDays(1));
        BigDecimal totalBeløp = finnTotalbeløpIBeregningsperioden(filter, andel, tilDato, beregningsperiodeLengdeIMnd);
        BigDecimal faktor = BigDecimal.valueOf(MND_I_1_ÅR).divide(BigDecimal.valueOf(beregningsperiodeLengdeIMnd), 10, RoundingMode.HALF_EVEN);
        return totalBeløp.multiply(faktor);
    }

    private static InntektFilterDto finnInntekterForAndel(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel, InntektFilterDto filter) {
        Optional<Arbeidsgiver> arbeidsgiver = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver);
        if (arbeidsgiver.isEmpty()) {
            return InntektFilterDto.EMPTY;
        }
        return filter.filterBeregningsgrunnlag()
            .filter(arbeidsgiver.get());
    }

    private static BigDecimal summerInntekterIBeregningsperioden(LocalDate tilDato, Collection<InntektspostDto> inntektsposter, Long beregningsperiodeLengdeIMnd) {
        BigDecimal totalBeløp = BigDecimal.ZERO;
        for (int måned = 0; måned < beregningsperiodeLengdeIMnd; måned++) {
            LocalDate dato = tilDato.minusMonths(måned);
            Beløp beløp = finnMånedsinntekt(inntektsposter, dato);
            totalBeløp = totalBeløp.add(beløp.getVerdi());
        }
        return totalBeløp;
    }

    private static Beløp finnMånedsinntekt(Collection<InntektspostDto> inntektsposter, LocalDate dato) {
        return inntektsposter.stream()
            .filter(inntektspost -> inntektspost.getPeriode().inkluderer(dato))
            .findFirst().map(InntektspostDto::getBeløp).orElse(Beløp.ZERO);
    }

    static Optional<BigDecimal> finnSnittAvFrilansinntektIBeregningsperioden(AktørId aktørId, InntektArbeidYtelseGrunnlagDto grunnlag,
                                                                             no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto frilansAndel, LocalDate skjæringstidspunkt) {
        var filter = new InntektFilterDto(grunnlag.getAktørInntektFraRegister(aktørId)).før(skjæringstidspunkt);
        if (!filter.isEmpty()) {
            LocalDate fraDato = frilansAndel.getBeregningsperiodeFom();
            LocalDate tilDato = frilansAndel.getBeregningsperiodeTom();
            long beregningsperiodeLengdeIMnd = ChronoUnit.MONTHS.between(fraDato, tilDato.plusDays(1));
            List<YrkesaktivitetDto> yrkesaktiviteter = finnYrkesaktiviteter(aktørId, grunnlag, skjæringstidspunkt);
            boolean erFrilanser = yrkesaktiviteter.stream().anyMatch(ya -> ArbeidType.FRILANSER.equals(ya.getArbeidType()));

            var frilansInntekter = filter.filterBeregningsgrunnlag().filter(inntekt -> {
                var arbeidTyper = getArbeidTyper(yrkesaktiviteter, inntekt.getArbeidsgiver());
                return erFrilansInntekt(arbeidTyper, erFrilanser);
            });

            if (frilansInntekter.isEmpty()) {
                return Optional.empty();
            }
            AtomicReference<BigDecimal> totalBeløp = new AtomicReference<>(BigDecimal.ZERO);
            frilansInntekter.forFilter((inntekt, inntektsposter) -> totalBeløp
                .set(totalBeløp.get().add(summerInntekterIBeregningsperioden(tilDato, inntektsposter, beregningsperiodeLengdeIMnd))));
            return Optional.of(totalBeløp.get().divide(BigDecimal.valueOf(beregningsperiodeLengdeIMnd), 10, RoundingMode.HALF_EVEN));

        }
        return Optional.empty();
    }

    private static List<YrkesaktivitetDto> finnYrkesaktiviteter(AktørId aktørId, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                LocalDate skjæringstidspunkt) {
        List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>();

        var aktørArbeid = inntektArbeidYtelseGrunnlag.getAktørArbeidFraRegister(aktørId);

        var filterRegister = new YrkesaktivitetFilterDto(inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid).før(skjæringstidspunkt);
        yrkesaktiviteter.addAll(filterRegister.getYrkesaktiviteterForBeregning());
        yrkesaktiviteter.addAll(filterRegister.getFrilansOppdrag());

        var bekreftetAnnenOpptjening = inntektArbeidYtelseGrunnlag.getBekreftetAnnenOpptjening(aktørId);
        var filterSaksbehandlet = new YrkesaktivitetFilterDto(inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon(), bekreftetAnnenOpptjening);
        yrkesaktiviteter.addAll(filterSaksbehandlet.getYrkesaktiviteterForBeregning());

        return yrkesaktiviteter;
    }

    private static Collection<ArbeidType> getArbeidTyper(Collection<YrkesaktivitetDto> yrkesaktiviteter, Arbeidsgiver arbeidsgiver) {
        return yrkesaktiviteter
            .stream()
            .filter(it -> it.getArbeidsgiver() != null)
            .filter(it -> it.getArbeidsgiver().getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
            .map(YrkesaktivitetDto::getArbeidType)
            .distinct()
            .collect(Collectors.toList());
    }

    private static boolean erFrilansInntekt(Collection<ArbeidType> arbeidTyper, boolean erFrilanser) {
        return (arbeidTyper.isEmpty() && erFrilanser) || arbeidTyper.contains(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
    }

}
