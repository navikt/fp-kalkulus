package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.SvpTilretteleggingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingMedUtbelingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class AutopunktUtlederSvangerskapspengerTjeneste {

    private AutopunktUtlederSvangerskapspengerTjeneste() {
        // skjul
    }

    static boolean harSøktForskjelligeStartdatoerForPermisjon(List<TilretteleggingMedUtbelingsgradDto> tilretteleggingMedUtbelingsgradList) {
        Map<TilretteleggingArbeidsforholdDto, Optional<LocalDate>> arbeidsgiverPermisjonsstartdatoMap = tilretteleggingMedUtbelingsgradList
            .stream()
            .collect(Collectors.toMap(
                TilretteleggingMedUtbelingsgradDto::getTilretteleggingArbeidsforhold,
                a -> a.getPeriodeMedUtbetalingsgrad().stream()
                    .filter(p -> p.getUtbetalingsgrad().compareTo(BigDecimal.ZERO) > 0)
                    .map(p -> p.getPeriode().getFomDato())
                    .min(Comparator.naturalOrder())
            ));
        long antallDistinktePermisjonsStartdatoer = arbeidsgiverPermisjonsstartdatoMap
            .values()
            .stream()
            .flatMap(Optional::stream)
            .distinct()
            .count();
        return antallDistinktePermisjonsStartdatoer > 1;
    }

    static boolean harSøktDelvisSVPOgHarRefusjonskrav(List<SvpTilretteleggingDto> aktuelleTilrettelegginger, Collection<InntektsmeldingDto>inntektsmeldinger) {
        boolean erSøktOmDelvisTilrettelegging = aktuelleTilrettelegginger.stream()
            .anyMatch(SvpTilretteleggingDto::getHarSøktDelvisTilrettelegging);
        return erSøktOmDelvisTilrettelegging && finnesRefusjonskravForTilrettelegging(aktuelleTilrettelegginger, inntektsmeldinger);
    }

    private static boolean finnesRefusjonskravForTilrettelegging(List<SvpTilretteleggingDto> aktuelleTilrettelegginger, Collection<InntektsmeldingDto>inntektsmeldinger) {
        Set<Arbeidsgiver> arbeidsgivereMedRefusjon = inntektsmeldinger.stream()
            .filter(im -> im.getRefusjonBeløpPerMnd() != null)
            .filter(im -> !im.getRefusjonBeløpPerMnd().erNullEllerNulltall())
            .map(InntektsmeldingDto::getArbeidsgiver)
            .collect(Collectors.toSet());

        return aktuelleTilrettelegginger.stream()
            .map(SvpTilretteleggingDto::getArbeidsgiver)
            .flatMap(Optional::stream)
            .anyMatch(arbeidsgivereMedRefusjon::contains);
    }
}
