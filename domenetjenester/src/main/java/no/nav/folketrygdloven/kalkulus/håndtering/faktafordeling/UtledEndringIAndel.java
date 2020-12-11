package no.nav.folketrygdloven.kalkulus.håndtering.faktafordeling;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagPrStatusOgAndelEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.InntektEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.InntektskategoriEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonEndring;

class UtledEndringIAndel {

    private UtledEndringIAndel() {
        // skjul
    }

    public static Optional<BeregningsgrunnlagPrStatusOgAndelEndring> utled(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        BeregningsgrunnlagPrStatusOgAndelEndring andelEndring = initialiserAndelEndring(andel);
        andelEndring.setInntektEndring(lagInntektEndring(andel, forrigeAndel));
        andelEndring.setInntektskategoriEndring(utledInntektskategoriEndring(andel, forrigeAndel));
        andelEndring.setRefusjonEndring(utledRefusjonsendring(andel, forrigeAndel));
        if (harEndringIAndel(andelEndring)) {
            return Optional.of(andelEndring);
        }
        return Optional.empty();
    }

    private static RefusjonEndring utledRefusjonsendring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return harEndringIRefusjon(andel, forrigeAndel) ? initRefusjonEndring(andel, forrigeAndel) : null;
    }

    private static RefusjonEndring initRefusjonEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return new RefusjonEndring(finnRefusjon(forrigeAndel), initRefusjon(andel));
    }

    private static BigDecimal initRefusjon(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(BigDecimal.ZERO);
    }

    private static boolean harEndringIRefusjon(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        Optional<BigDecimal> forrigeRefusjonskrav = forrigeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold).map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr);
        Optional<BigDecimal> nyttRefusjonskrav = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr);

        if (forrigeRefusjonskrav.isEmpty() || nyttRefusjonskrav.isEmpty()) {
            // Hvis en mangler må begge mangle, ellers er det endring i refusjon
            return !(forrigeRefusjonskrav.isEmpty() && nyttRefusjonskrav.isEmpty());
        }

        return forrigeRefusjonskrav.get().compareTo(nyttRefusjonskrav.get()) != 0;
    }

    private static BeregningsgrunnlagPrStatusOgAndelEndring initialiserAndelEndring(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        BeregningsgrunnlagPrStatusOgAndelEndring andelEndring;
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            if (andel.getArbeidsgiver().isPresent()) {
                Arbeidsgiver arbeidsgiver = andel.getArbeidsgiver().get();
                Aktør aktør = arbeidsgiver.getErVirksomhet() ? new Organisasjon(arbeidsgiver.getIdentifikator()) : new AktørIdPersonident(arbeidsgiver.getIdentifikator());
                andelEndring = new BeregningsgrunnlagPrStatusOgAndelEndring(aktør, andel.getArbeidsforholdRef().map(InternArbeidsforholdRefDto::getReferanse).orElse(null));
            } else {
                andelEndring = BeregningsgrunnlagPrStatusOgAndelEndring.opprettForArbeidstakerUtenArbeidsgiver(OpptjeningAktivitetType.fraKode(andel.getArbeidsforholdType().getKode()));
            }
        } else {
            andelEndring = new BeregningsgrunnlagPrStatusOgAndelEndring(AktivitetStatus.fraKode(andel.getAktivitetStatus().getKode()));
        }
        return andelEndring;
    }

    private static boolean harEndringIAndel(BeregningsgrunnlagPrStatusOgAndelEndring a) {
        return a.getInntektEndring() != null || a.getInntektskategoriEndring() != null || a.getRefusjonEndring() != null;
    }

    private static InntektEndring lagInntektEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return andel.getFordeltPrÅr() != null ?
                new InntektEndring(forrigeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getFordeltPrÅr).orElse(null), andel.getFordeltPrÅr()) : null;
    }

    private static InntektskategoriEndring utledInntektskategoriEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return harEndringIInntektskategori(andel, forrigeAndel) ? initInntektskategoriEndring(andel, forrigeAndel) : null;
    }

    private static InntektskategoriEndring initInntektskategoriEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return new InntektskategoriEndring(finnInntektskategori(forrigeAndel), andel.getInntektskategori());
    }

    private static Boolean harEndringIInntektskategori(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return forrigeAndel.map(a -> !a.getInntektskategori().equals(andel.getInntektskategori())).orElse(true);
    }

    private static no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori finnInntektskategori(Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return forrigeAndel
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getInntektskategori)
                .orElse(null);
    }

    private static BigDecimal finnRefusjon(Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return forrigeAndel
                .flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                .map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr)
                .orElse(null);
    }

}
