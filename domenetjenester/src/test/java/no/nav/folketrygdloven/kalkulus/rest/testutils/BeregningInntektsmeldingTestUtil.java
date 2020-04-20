package no.nav.folketrygdloven.kalkulus.rest.testutils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
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
