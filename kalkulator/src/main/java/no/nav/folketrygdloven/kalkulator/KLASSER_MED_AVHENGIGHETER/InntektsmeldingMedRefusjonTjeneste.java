package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class InntektsmeldingMedRefusjonTjeneste {

    public static Map<Arbeidsgiver, LocalDate> finnFørsteInntektsmeldingMedRefusjon(BeregningsgrunnlagInput input) {
        return input.getRefusjonskravDatoer().stream().collect(Collectors.toMap(RefusjonskravDatoDto::getArbeidsgiver, RefusjonskravDatoDto::getFørsteInnsendingAvRefusjonskrav));
    }

    public static Set<Arbeidsgiver> finnArbeidsgiverSomHarSøktRefusjonForSent(BehandlingReferanse behandlingReferanse,
                                                                              InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                              BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                              List<RefusjonskravDatoDto> refusjonskravDatoer
                                                                              ) {
        Collection<YrkesaktivitetDto> yrkesaktiviteterForBeregning = FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(behandlingReferanse, iayGrunnlag, grunnlag);
        Map<YrkesaktivitetDto, Optional<RefusjonskravDatoDto>> yrkesaktivitetDatoMap = map(yrkesaktiviteterForBeregning, refusjonskravDatoer);
        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlag().map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ha beregningsgrunnlag"));

        Set<Arbeidsgiver> resultat = new HashSet<>();

        for (Map.Entry<YrkesaktivitetDto, Optional<RefusjonskravDatoDto>> entry : yrkesaktivitetDatoMap.entrySet()) {
            if (entry.getValue().isPresent()) {
                YrkesaktivitetDto yrkesaktivitet = entry.getKey();

                if (harArbeidsgiverSendtInnInntektsmeldingMedUgyldigRefusjonskrav(entry.getValue().get(), skjæringstidspunktBeregning)) {
                    resultat.add(yrkesaktivitet.getArbeidsgiver());
                }
            }
        }
        return resultat;
    }

    private static boolean harArbeidsgiverSendtInnInntektsmeldingMedUgyldigRefusjonskrav(RefusjonskravDatoDto refusjonsdato, LocalDate skjæringstidspunktBeregning) {
            LocalDate førsteDagMedRefusjon = refusjonsdato.getFørsteDagMedRefusjonskrav().orElse(skjæringstidspunktBeregning);
            LocalDate førsteLovligeDatoForRefusjon = refusjonsdato.getFørsteInnsendingAvRefusjonskrav().minusMonths(3).withDayOfMonth(1);
            return førsteLovligeDatoForRefusjon.isAfter(førsteDagMedRefusjon);
    }

    public static Optional<LocalDate> finnFørsteLovligeDatoForRefusjonFørOverstyring(BeregningsgrunnlagInput input, Arbeidsgiver arbeidsgiver) {
        Map<Arbeidsgiver, LocalDate> førsteInntektsmeldingMap = finnFørsteInntektsmeldingMedRefusjon(input);
        LocalDate innsendingstidspunkt = førsteInntektsmeldingMap.get(arbeidsgiver);
        if (innsendingstidspunkt != null) {
            return Optional.of(innsendingstidspunkt.withDayOfMonth(1).minusMonths(3));
        }
        return Optional.empty();
    }

    private static Map<YrkesaktivitetDto, Optional<RefusjonskravDatoDto>> map(Collection<YrkesaktivitetDto> yrkesaktiviteterForBeregning, List<RefusjonskravDatoDto> refusjonskravDatoList) {
        return yrkesaktiviteterForBeregning.stream()
            .collect(Collectors.toMap(y -> y, finnRefusjonskravDato(refusjonskravDatoList)));
    }

    private static Function<YrkesaktivitetDto, Optional<RefusjonskravDatoDto>> finnRefusjonskravDato(List<RefusjonskravDatoDto> refusjonskravDatoList) {
        return y -> refusjonskravDatoList.stream().filter(rd -> y.getArbeidsgiver().equals(rd.getArbeidsgiver())).findFirst();
    }

}
