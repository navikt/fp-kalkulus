package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;


public class VisningsnavnForAktivitetTjeneste {

    public static final String PRIVATPERSON_DEFAULT_VISNING = "Privatperson";
    public static final String NAVN_FOR_FEILET_TPS_KALL = "N/A";
    private static final int ANTALL_SIFFER_SOM_SKAL_VISES_AV_REFERANSE = 4;

    private VisningsnavnForAktivitetTjeneste() {
        // For CDI
    }

    public static Optional<String> finnArbeidsgiverNavn(Arbeidsgiver arbeidsgiver, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        Optional<String> navn = arbeidsgiverOpplysninger.stream()
                .filter(aOppl -> aOppl.getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .findFirst()
                .map(ArbeidsgiverOpplysningerDto::getNavn);
        if (erPrivatpersonMedFeilendeKallTilTPS(arbeidsgiver, arbeidsgiverOpplysninger)) {
            return Optional.of(navn.filter(n -> !n.equals(NAVN_FOR_FEILET_TPS_KALL)).orElse(PRIVATPERSON_DEFAULT_VISNING));
        }
        return navn;
    }

    public static String finnArbeidsgiverIdentifikatorForVisning(Arbeidsgiver arbeidsgiver, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        Optional<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysning = arbeidsgiverOpplysninger.stream().filter(aOppl -> aOppl.getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .findFirst();
        if (arbeidsgiverOpplysning.isEmpty() || erPrivatpersonMedFeilendeKallTilTPS(arbeidsgiver, arbeidsgiverOpplysninger)) {
            return "";
        }
        if (arbeidsgiver.getErVirksomhet()) {
            return arbeidsgiverOpplysning.get().getIdentifikator();
        }
        return arbeidsgiverOpplysning.get().getFødselsdato().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }


    public static String lagVisningsnavn(KoblingReferanse ref, InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return finnVisningsnavnForArbeidstaker(ref, iayGrunnlag, andel);
        }
        return andel.getArbeidsforholdType() == null || OpptjeningAktivitetType.UDEFINERT.equals(andel.getArbeidsforholdType()) ? andel.getAktivitetStatus().getNavn() : andel.getArbeidsforholdType().getNavn();
    }

    private static String finnVisningsnavnForArbeidstaker(KoblingReferanse ref, InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold()
                .map(bgAndelArbeidsforhold -> {
                    Arbeidsgiver arbeidsgiver = bgAndelArbeidsforhold.getArbeidsgiver();
                    String visningsnavnUtenReferanse = finnVisningsnavnUtenReferanse(arbeidsgiver, iayGrunnlag.getArbeidsgiverOpplysninger());
                    return finnVisningsnavnMedReferanseHvisFinnes(ref, arbeidsgiver, bgAndelArbeidsforhold, visningsnavnUtenReferanse, iayGrunnlag);
                }).orElse(andel.getArbeidsforholdType().getNavn());
    }

    private static String finnVisningsnavnMedReferanseHvisFinnes(KoblingReferanse ref, Arbeidsgiver arbeidsgiver, BGAndelArbeidsforholdDto bgAndelArbeidsforhold, String visningsnavnUtenReferanse, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        String referanse = bgAndelArbeidsforhold.getArbeidsforholdRef().getReferanse();
        if (referanse != null) {
            if (inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon().isEmpty()) {
                throw new IllegalStateException("Mangler arbeidsforholdinformasjon for behandlingId=" + ref.getKoblingId());
            }
            var eksternArbeidsforholdRef = inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon().get().finnEkstern(arbeidsgiver, bgAndelArbeidsforhold.getArbeidsforholdRef());
            var eksternArbeidsforholdId = eksternArbeidsforholdRef.getReferanse();
            return visningsnavnUtenReferanse + " ..." + finnSubstringAvReferanse(eksternArbeidsforholdId);
        } else if (erPrivatpersonMedFeilendeKallTilTPS(arbeidsgiver, inntektArbeidYtelseGrunnlag.getArbeidsgiverOpplysninger())) {
            return visningsnavnUtenReferanse + " ..." + finnSubstringAvReferanse(arbeidsgiver.getIdentifikator());
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
        String navn = finnArbeidsgiverNavn(arbeidsgiver, arbeidsgiverOpplysninger).orElse("");
        if (erPrivatpersonMedFeilendeKallTilTPS(arbeidsgiver, arbeidsgiverOpplysninger)) {
            return navn;
        }
        return navn + " (" + finnArbeidsgiverIdentifikatorForVisning(arbeidsgiver, arbeidsgiverOpplysninger) + ")";
    }

    private static boolean erPrivatpersonMedFeilendeKallTilTPS(Arbeidsgiver arbeidsgiver, List<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger) {
        Optional<ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysningerDto = arbeidsgiverOpplysninger.stream().filter(aOppl -> aOppl.getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .findFirst();
        if (arbeidsgiverOpplysningerDto.isEmpty()) {
            return false;
        }
        return !arbeidsgiver.getErVirksomhet() && arbeidsgiverOpplysningerDto.get().getFødselsdato() == null;
    }

}
