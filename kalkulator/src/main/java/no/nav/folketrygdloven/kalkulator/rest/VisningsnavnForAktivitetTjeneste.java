package no.nav.folketrygdloven.kalkulator.rest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;


public class VisningsnavnForAktivitetTjeneste {

    private static final int ANTALL_SIFFER_SOM_SKAL_VISES_AV_REFERANSE = 4;

    private VisningsnavnForAktivitetTjeneste() {
        // For CDI
    }

    public static Optional<String> finnArbeidsgiverNavn(Arbeidsgiver arbeidsgiver, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        return arbeidsgiverOpplysninger.stream().filter(aOppl -> aOppl.getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .findFirst().map(ArbeidsgiverOpplysningerDto::getNavn);
    }

    public static String finnArbeidsgiverIdentifikatorForVisning(Arbeidsgiver arbeidsgiver, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        Optional<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysning = arbeidsgiverOpplysninger.stream().filter(aOppl -> aOppl.getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .findFirst();
        if (arbeidsgiverOpplysning.isEmpty()) {
            return "";
        }
        if (arbeidsgiver.getErVirksomhet()) {
            return arbeidsgiverOpplysning.get().getIdentifikator();
        }
        return arbeidsgiverOpplysning.get().getFødselsdato().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }


    public static String lagVisningsnavn(BehandlingReferanse ref, InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return finnVisningsnavnForArbeidstaker(ref, iayGrunnlag, andel);
        }
        return andel.getArbeidsforholdType() == null || OpptjeningAktivitetType.UDEFINERT.equals(andel.getArbeidsforholdType()) ? andel.getAktivitetStatus().getNavn() : andel.getArbeidsforholdType().getNavn();
    }

    private static String finnVisningsnavnForArbeidstaker(BehandlingReferanse ref, InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold()
            .map(bgAndelArbeidsforhold -> {
                Arbeidsgiver arbeidsgiver = bgAndelArbeidsforhold.getArbeidsgiver();
                String visningsnavnUtenReferanse = finnVisningsnavnUtenReferanse(arbeidsgiver, iayGrunnlag.getArbeidsgiverOpplysninger());
                return finnVisningsnavnMedReferanseHvisFinnes(ref, arbeidsgiver, bgAndelArbeidsforhold, visningsnavnUtenReferanse, iayGrunnlag);
            }).orElse(andel.getArbeidsforholdType().getNavn());
    }

    private static String finnVisningsnavnMedReferanseHvisFinnes(BehandlingReferanse ref, Arbeidsgiver arbeidsgiver, BGAndelArbeidsforholdDto bgAndelArbeidsforhold, String visningsnavnUtenReferanse, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        String referanse = bgAndelArbeidsforhold.getArbeidsforholdRef().getReferanse();
        if (referanse != null) {
            if (inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon().isEmpty()) {
                throw new IllegalStateException("Mangler arbeidsforholdinformasjon for behandlingId=" + ref.getBehandlingId());
            }
            var eksternArbeidsforholdRef = inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon().get().finnEkstern(arbeidsgiver, bgAndelArbeidsforhold.getArbeidsforholdRef());
            var eksternArbeidsforholdId = eksternArbeidsforholdRef.getReferanse();
            return visningsnavnUtenReferanse + " ..." + finnSubstringAvReferanse(eksternArbeidsforholdId);
        }
        return visningsnavnUtenReferanse;
    }

    private static String finnSubstringAvReferanse(String eksternArbeidsforholdId) {
        if (eksternArbeidsforholdId == null) {
            return "";
        }
        if (eksternArbeidsforholdId.length() <= ANTALL_SIFFER_SOM_SKAL_VISES_AV_REFERANSE) {
            return eksternArbeidsforholdId;
        }
        return eksternArbeidsforholdId.substring(eksternArbeidsforholdId.length() - ANTALL_SIFFER_SOM_SKAL_VISES_AV_REFERANSE);
    }

    private static String finnVisningsnavnUtenReferanse(Arbeidsgiver arbeidsgiver, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        return finnArbeidsgiverNavn(arbeidsgiver, arbeidsgiverOpplysninger).orElse("") + " (" + finnArbeidsgiverIdentifikatorForVisning(arbeidsgiver, arbeidsgiverOpplysninger) + ")";
    }
}
