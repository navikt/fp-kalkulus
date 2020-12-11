package no.nav.folketrygdloven.kalkulator.testutilities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.vedtak.konfig.Tid;


@ApplicationScoped
public class BeregningInntektsmeldingTestUtil {


    public BeregningInntektsmeldingTestUtil() {
        // for CDI
    }

    public static InntektsmeldingDto opprettInntektsmelding(String orgNummer, LocalDate skjæringstidspunkt, BigDecimal refusjonskrav,
                                                            BigDecimal inntekt) {
        return opprettInntektsmelding(orgNummer, null, skjæringstidspunkt, refusjonskrav, inntekt, Tid.TIDENES_ENDE,
            Collections.emptyList(), Collections.emptyList());
    }

    public static InntektsmeldingDto opprettInntektsmelding(String orgNummer, LocalDate skjæringstidspunkt, BigDecimal refusjonskrav,
                                                            BigDecimal inntekt, LocalDate refusjonOpphørerFom) {
        return opprettInntektsmelding(orgNummer, null, skjæringstidspunkt, refusjonskrav, inntekt, refusjonOpphørerFom,
            Collections.emptyList(), Collections.emptyList());
    }

    public static InntektsmeldingDto opprettInntektsmelding(String orgnr, InternArbeidsforholdRefDto arbId, LocalDate skjæringstidspunktOpptjening) {
        return opprettInntektsmelding(orgnr, arbId, skjæringstidspunktOpptjening, 0, 10);
    }

    public static InntektsmeldingDto opprettInntektsmelding(String orgnr, InternArbeidsforholdRefDto arbId, LocalDate skjæringstidspunktOpptjening,
                                                            Integer refusjon) { // NOSONAR - brukes bare
                                                                                                                                       // til test
        BigDecimal refusjonEllerNull = refusjon != null ? BigDecimal.valueOf(refusjon) : null;
        return opprettInntektsmelding(orgnr, arbId, skjæringstidspunktOpptjening, refusjonEllerNull, BigDecimal.TEN, Tid.TIDENES_ENDE,
            Collections.emptyList(), Collections.emptyList());
    }

    public static InntektsmeldingDto opprettInntektsmelding(String orgnr, InternArbeidsforholdRefDto arbId, LocalDate skjæringstidspunktOpptjening,
                                                            Integer refusjon, Integer inntekt) { // NOSONAR
                                                                                                                                                        // -
                                                                                                                                                        // brukes
                                                                                                                                                        // bare
                                                                                                                                                        // til
                                                                                                                                                        // test
        BigDecimal refusjonEllerNull = refusjon != null ? BigDecimal.valueOf(refusjon) : null;
        return opprettInntektsmelding(orgnr, arbId, skjæringstidspunktOpptjening, refusjonEllerNull, BigDecimal.valueOf(inntekt),
            Tid.TIDENES_ENDE, Collections.emptyList(), Collections.emptyList());
    }

    public static InntektsmeldingDto opprettInntektsmelding(String orgnr, InternArbeidsforholdRefDto arbId, LocalDate skjæringstidspunktOpptjening,
                                                            Integer refusjon, LocalDate opphørsdatoRefusjon, LocalDateTime innsendingstidspunkt) { // NOSONAR - brukes
                                                                                                                                         // bare til test
        BigDecimal refusjonEllerNull = refusjon != null ? BigDecimal.valueOf(refusjon) : null;
        return opprettInntektsmelding(orgnr, arbId, skjæringstidspunktOpptjening, refusjonEllerNull, BigDecimal.valueOf(7_000),
            opphørsdatoRefusjon, Collections.emptyList(), Collections.emptyList());
    }

    public static InntektsmeldingDto opprettInntektsmeldingMedNaturalYtelser(String orgnr, // NOSONAR - brukes bare til test
                                                                             LocalDate skjæringstidspunkt,
                                                                             BigDecimal inntektBeløp,
                                                                             BigDecimal refusjonskrav,
                                                                             LocalDate refusjonOpphørerDato,
                                                                             NaturalYtelseDto... naturalYtelser) {
        return opprettInntektsmelding(orgnr, null, skjæringstidspunkt, refusjonskrav, inntektBeløp, refusjonOpphørerDato,
            Arrays.asList(naturalYtelser), Collections.emptyList());
    }

    public static InntektsmeldingDto opprettInntektsmeldingMedEndringerIRefusjon(String orgnr, InternArbeidsforholdRefDto arbId,
                                                                                 LocalDate skjæringstidspunkt, BigDecimal inntektBeløp, // NOSONAR - brukes bare til test
                                                                                 BigDecimal refusjonskrav, LocalDate refusjonOpphørerDato, List<RefusjonDto> endringRefusjon) {
        return opprettInntektsmelding(orgnr, arbId, skjæringstidspunkt, refusjonskrav, inntektBeløp, refusjonOpphørerDato,
            Collections.emptyList(), endringRefusjon);
    }

    private static InntektsmeldingDto opprettInntektsmelding(String orgnr, InternArbeidsforholdRefDto internReferanse,
                                                             LocalDate skjæringstidspunktOpptjening,  // NOSONAR - brukes bare til test
                                                             BigDecimal refusjon, BigDecimal inntekt, LocalDate opphørsdatoRefusjon, List<NaturalYtelseDto> naturalYtelser,
                                                             List<RefusjonDto> endringRefusjon) {

        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        return opprettInntektsmelding(arbeidsgiver, internReferanse, skjæringstidspunktOpptjening, refusjon, inntekt,
            opphørsdatoRefusjon, naturalYtelser, endringRefusjon);
    }

    public static InntektsmeldingDto opprettInntektsmelding(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internReferanse,
                                                            LocalDate skjæringstidspunktOpptjening,  // NOSONAR - brukes bare til test
                                                            BigDecimal refusjon, BigDecimal inntekt,
                                                            LocalDate opphørsdatoRefusjon,
                                                            List<NaturalYtelseDto> naturalYtelser,
                                                            List<RefusjonDto> endringRefusjon) {

        InntektsmeldingDtoBuilder inntektsmeldingBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingBuilder.medStartDatoPermisjon(skjæringstidspunktOpptjening);
        inntektsmeldingBuilder.medBeløp(inntekt);
        if (refusjon != null) {
            inntektsmeldingBuilder.medRefusjon(refusjon, opphørsdatoRefusjon);
        }
        inntektsmeldingBuilder.medArbeidsgiver(arbeidsgiver);
        inntektsmeldingBuilder.medArbeidsforholdId(internReferanse);
        naturalYtelser.forEach(inntektsmeldingBuilder::leggTil);
        endringRefusjon.forEach(inntektsmeldingBuilder::leggTil);

        return inntektsmeldingBuilder.build(); // gir samme resultat for hvert kall til build.
    }
}
