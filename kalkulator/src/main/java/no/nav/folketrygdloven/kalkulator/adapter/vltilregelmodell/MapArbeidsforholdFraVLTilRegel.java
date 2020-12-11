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

public class MapArbeidsforholdFraVLTilRegel {
    private MapArbeidsforholdFraVLTilRegel() {
        // skjul public constructor
    }

    static Arbeidsforhold arbeidsforholdFor(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        if (erFrilanser(vlBGPStatus.getAktivitetStatus())) {
            return Arbeidsforhold.frilansArbeidsforhold();
        }
        Optional<Arbeidsgiver> arbeidsgiver = vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver);
        if (arbeidsgiver.isPresent()) {
            return lagArbeidsforholdHosArbeidsgiver(arbeidsgiver.get(), vlBGPStatus);
        } else {
            return Arbeidsforhold.anonymtArbeidsforhold(MapOpptjeningAktivitetTypeFraVLTilRegel.map(vlBGPStatus.getArbeidsforholdType()));
        }
    }

    private static boolean erFrilanser(AktivitetStatus aktivitetStatus) {
        return AktivitetStatus.FRILANSER.equals(aktivitetStatus);
    }

    private static Arbeidsforhold lagArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver, BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        String arbRef = arbeidsforholdRefFor(vlBGPStatus);
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
