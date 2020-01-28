package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;

public class FinnStartdatoPermisjon {
    private FinnStartdatoPermisjon() {
        // skjul public constructor
    }

    /**
     * @param ya yrkesaktiviteten
     * @param førsteuttaksdag første ønskede dag med uttak av foreldrepenger
     * @param startdatoForArbeid       første dag i aktiviteten. Kan være før første uttaksdag,
*                        eller etter første uttaksdag dersom bruker starter i arbeidsforholdet
*                        eller er i permisjon (f.eks. PERMITTERT) ved første uttaksdag.
*                        Se {@link PermisjonsbeskrivelseType}
     * @param inntektsmeldinger inntektsmeldinger som er gyldige/aktive
     */
    public static LocalDate finnStartdatoPermisjon(YrkesaktivitetDto ya, LocalDate førsteuttaksdag, LocalDate startdatoForArbeid, Collection<InntektsmeldingDto>inntektsmeldinger) {
        return startdatoForArbeid.isBefore(førsteuttaksdag) ? førsteuttaksdag : utledStartdato(ya, startdatoForArbeid, inntektsmeldinger);
    }

    private static LocalDate utledStartdato(YrkesaktivitetDto ya, LocalDate startdatoForArbeid, Collection<InntektsmeldingDto>inntektsmeldinger) {
        Optional<InntektsmeldingDto> matchendeInntektsmelding = inntektsmeldinger.stream()
            .filter(im -> ya.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
            .findFirst();
        Optional<LocalDate> startDatoFraIM = matchendeInntektsmelding.flatMap(InntektsmeldingDto::getStartDatoPermisjon);
        return startDatoFraIM.filter(dato -> dato.isAfter(startdatoForArbeid)).orElse(startdatoForArbeid);
    }
}
