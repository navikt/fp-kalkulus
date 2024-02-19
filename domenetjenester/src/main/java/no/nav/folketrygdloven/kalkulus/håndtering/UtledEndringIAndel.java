package no.nav.folketrygdloven.kalkulus.håndtering;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagPrStatusOgAndelEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.InntektEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.InntektskategoriEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonEndring;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

public class UtledEndringIAndel {

    private UtledEndringIAndel() {
        // skjul
    }

    public static Optional<BeregningsgrunnlagPrStatusOgAndelEndring> utled(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelFraSteg, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        BeregningsgrunnlagPrStatusOgAndelEndring andelEndring = initialiserAndelEndring(andel);
        andelEndring.setInntektEndring(lagInntektEndring(andel, forrigeAndel));
        andelEndring.setInntektskategoriEndring(utledInntektskategoriEndring(andel, andelFraSteg, forrigeAndel));
        andelEndring.setRefusjonEndring(utledRefusjonsendring(andel, forrigeAndel));
        if (harEndringIAndel(andelEndring)) {
            return Optional.of(andelEndring);
        }
        return Optional.empty();
    }


    private static BeregningsgrunnlagPrStatusOgAndelEndring initialiserAndelEndring(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        BeregningsgrunnlagPrStatusOgAndelEndring andelEndring;
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            if (andel.getArbeidsgiver().isPresent()) {
                Arbeidsgiver arbeidsgiver = andel.getArbeidsgiver().get();
                Aktør aktør = arbeidsgiver.getErVirksomhet() ? new Organisasjon(arbeidsgiver.getIdentifikator()) : new AktørIdPersonident(arbeidsgiver.getIdentifikator());
                andelEndring = new BeregningsgrunnlagPrStatusOgAndelEndring(andel.getAndelsnr(), aktør, andel.getArbeidsforholdRef().map(InternArbeidsforholdRefDto::getReferanse).orElse(null));
            } else {
                andelEndring = BeregningsgrunnlagPrStatusOgAndelEndring.opprettForArbeidstakerUtenArbeidsgiver(andel.getArbeidsforholdType(), andel.getAndelsnr());
            }
        } else {
            andelEndring = new BeregningsgrunnlagPrStatusOgAndelEndring(andel.getAndelsnr(), andel.getAktivitetStatus());
        }
        return andelEndring;
    }

    private static boolean harEndringIAndel(BeregningsgrunnlagPrStatusOgAndelEndring a) {
        return a.getInntektEndring() != null || a.getInntektskategoriEndring() != null;
    }

    private static InntektEndring lagInntektEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return Beløp.safeVerdi(andel.getBruttoPrÅr()) != null ?
                new InntektEndring(forrigeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr).orElse(null), andel.getBruttoPrÅr()) : null;
    }

    private static InntektskategoriEndring utledInntektskategoriEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelFraSteg, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return harEndringIInntektskategori(andel, andelFraSteg, forrigeAndel) ? initInntektskategoriEndring(andel, forrigeAndel) : null;
    }

    private static InntektskategoriEndring initInntektskategoriEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return new InntektskategoriEndring(finnInntektskategori(forrigeAndel), andel.getGjeldendeInntektskategori());
    }

    private static Boolean harEndringIInntektskategori(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelFraSteg, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        if (forrigeAndel.isEmpty()) {
            return andelFraSteg.map(a -> !Objects.equals(a.getGjeldendeInntektskategori(), andel.getGjeldendeInntektskategori())).orElse(true);
        }
        return forrigeAndel.map(a -> !a.getGjeldendeInntektskategori().equals(andel.getGjeldendeInntektskategori())).orElse(true);
    }

    private static no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori finnInntektskategori(Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return forrigeAndel
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getGjeldendeInntektskategori)
                .orElse(null);
    }

    private static RefusjonEndring utledRefusjonsendring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return harEndringIRefusjon(andel, forrigeAndel) ? initRefusjonEndring(andel, forrigeAndel) : null;
    }

    private static RefusjonEndring initRefusjonEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return new RefusjonEndring(finnRefusjon(forrigeAndel), initRefusjon(andel));
    }

    private static Beløp initRefusjon(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(Beløp.ZERO);
    }

    private static boolean harEndringIRefusjon(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        var forrigeRefusjonskrav = forrigeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold).map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr);
        var nyttRefusjonskrav = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr);

        if (forrigeRefusjonskrav.isEmpty() || nyttRefusjonskrav.isEmpty()) {
            // Hvis en mangler må begge mangle, ellers er det endring i refusjon
            return !(forrigeRefusjonskrav.isEmpty() && nyttRefusjonskrav.isEmpty());
        }

        return forrigeRefusjonskrav.get().compareTo(nyttRefusjonskrav.get()) != 0;
    }

    private static Beløp finnRefusjon(Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return forrigeAndel
                .flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                .map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr)
                .orElse(null);
    }

}
