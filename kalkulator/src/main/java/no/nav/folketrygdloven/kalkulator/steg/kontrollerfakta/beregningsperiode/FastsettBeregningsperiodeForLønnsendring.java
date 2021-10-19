package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste.finnAktiviteterMedLønnsendringUtenInntektsmelding;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class FastsettBeregningsperiodeForLønnsendring {

    private FastsettBeregningsperiodeForLønnsendring() {}

    static BeregningsgrunnlagDto fastsettBeregningsperiodeForLønnsendring(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        Intervall beregningsperiodeATFL = new BeregningsperiodeTjeneste().fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt());
        List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringUtenInntektsmelding(beregningsgrunnlag, inntektArbeidYtelseGrunnlag, beregningsperiodeATFL);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        if (!yrkesaktiviteterMedLønnsendring.isEmpty()) {
            nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
                Map<BeregningsgrunnlagPrStatusOgAndelDto, List<YrkesaktivitetDto>> andelLønnsendringMap = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                        .filter(a -> harLønnsendring(yrkesaktiviteterMedLønnsendring, a))
                        .collect(Collectors.toMap(a -> a, a -> finnMatchendeYrkesaktiviteterMedLønnsendring(yrkesaktiviteterMedLønnsendring, a)));
                andelLønnsendringMap.forEach((andel, yrkesaktiviteter) -> {
                    LocalDate sisteLønnsendring = finnSisteLønnsendringIBeregningsperioden(yrkesaktiviteter, beregningsgrunnlag.getSkjæringstidspunkt());
                    LocalDate beregningsperiodeTom = andel.getBeregningsperiodeTom();
                    LocalDate beregningsperiodeFom = andel.getBeregningsperiodeFom();
                    LocalDate nyFom = sisteLønnsendring.isBefore(beregningsperiodeFom) ? beregningsperiodeFom : sisteLønnsendring;
                    BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel).medBeregningsperiode(nyFom, beregningsperiodeTom);
                });
            });
        }
        return nyttBeregningsgrunnlag;
    }

    private static LocalDate finnSisteLønnsendringIBeregningsperioden(List<YrkesaktivitetDto> yrkesaktiviteter, LocalDate skjæringstidspunkt) {
        return yrkesaktiviteter.stream().flatMap(y -> y.getAlleAktivitetsAvtaler().stream())
                .map(AktivitetsAvtaleDto::getSisteLønnsendringsdato)
                .filter(Objects::nonNull)
                .filter(d -> d.isBefore(skjæringstidspunkt))
                .max(Comparator.naturalOrder())
                .orElse(skjæringstidspunkt.minusMonths(4).withDayOfMonth(1));
    }

    private static boolean harLønnsendring(List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return a.getAktivitetStatus().erArbeidstaker() &&
                !finnMatchendeYrkesaktiviteterMedLønnsendring(yrkesaktiviteterMedLønnsendring, a).isEmpty();
    }

    private static List<YrkesaktivitetDto> finnMatchendeYrkesaktiviteterMedLønnsendring(List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold().isPresent() ? yrkesaktiviteterMedLønnsendring.stream().filter(y -> y.gjelderFor(andel.getBgAndelArbeidsforhold().get().getArbeidsgiver(), andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef())).collect(Collectors.toList()) :
                Collections.emptyList();
    }


}
