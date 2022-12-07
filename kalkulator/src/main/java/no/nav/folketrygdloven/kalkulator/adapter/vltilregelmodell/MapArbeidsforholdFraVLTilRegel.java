package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class MapArbeidsforholdFraVLTilRegel {
    private MapArbeidsforholdFraVLTilRegel() {
        // skjul public constructor
    }

    public static Arbeidsforhold arbeidsforholdFor(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        var arbeidsgiver = vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver);
        var arbeidsforholdType = vlBGPStatus.getArbeidsforholdType();
        var arbeidsforholdRef = arbeidsforholdRefFor(vlBGPStatus);
        return arbeidsforholdFor(vlBGPStatus.getAktivitetStatus(), arbeidsgiver, arbeidsforholdType, arbeidsforholdRef);
    }


    public static Arbeidsforhold arbeidsforholdFor(AktivitetStatus aktivitetStatus,
                                                   Optional<Arbeidsgiver> arbeidsgiver,
                                                   OpptjeningAktivitetType arbeidsforholdType,
                                                   String arbeidsforholdRef) {
        if (erFrilanser(aktivitetStatus)) {
            return Arbeidsforhold.frilansArbeidsforhold();
        }
        if (arbeidsgiver.isPresent()) {
            return lagArbeidsforholdHosArbeidsgiver(arbeidsgiver.get(), arbeidsforholdRef);
        } else {
            return Arbeidsforhold.anonymtArbeidsforhold(MapOpptjeningAktivitetTypeFraVLTilRegel.map(arbeidsforholdType));
        }
    }

    private static boolean erFrilanser(AktivitetStatus aktivitetStatus) {
        return AktivitetStatus.FRILANSER.equals(aktivitetStatus);
    }

    private static Arbeidsforhold lagArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver, String arbeidsforholdRef) {
        String arbRef = arbeidsforholdRef;
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getOrgnr(), arbRef);
        }
        if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getAktørId().getId(), arbRef);
        }
        throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet, men var: " + arbeidsgiver);
    }

    private static String arbeidsforholdRefFor(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        return vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).map(InternArbeidsforholdRefDto::getReferanse).orElse(null);
    }

    public static Arbeidsforhold mapArbeidsforhold(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getOrgnr(), arbeidsforholdRef.getReferanse());
        }
        if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getAktørId().getId(), arbeidsforholdRef.getReferanse());
        }
        throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet, men var: " + arbeidsgiver);
    }

    static Arbeidsforhold mapForInntektsmelding(InntektsmeldingDto im) {
        return mapArbeidsforhold(im.getArbeidsgiver(), im.getArbeidsforholdRef());
    }
}
