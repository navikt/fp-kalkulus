package no.nav.folketrygdloven.kalkulus.domene.håndtering;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.BeregningsgrunnlagPrStatusOgAndelEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.InntektEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.InntektskategoriEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering.RefusjonEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Aktør;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.AktørIdPersonident;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Organisasjon;

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
            andelEndring = andel.getArbeidsgiver().map(a -> {
                Arbeidsgiver arbeidsgiver = andel.getArbeidsgiver().get();
                Aktør aktør = arbeidsgiver.getErVirksomhet() ? new Organisasjon(arbeidsgiver.getIdentifikator()) : new AktørIdPersonident(arbeidsgiver.getIdentifikator());
                return new BeregningsgrunnlagPrStatusOgAndelEndring(andel.getAndelsnr(), aktør, andel.getArbeidsforholdRef().map(InternArbeidsforholdRefDto::getUUIDReferanse).orElse(null));
            }).orElseGet(() -> BeregningsgrunnlagPrStatusOgAndelEndring.opprettForArbeidstakerUtenArbeidsgiver(andel.getArbeidsforholdType(), andel.getAndelsnr()));
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
                new InntektEndring(forrigeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr).map(b -> beløpTilDto(b)).orElse(null), beløpTilDto(andel.getBruttoPrÅr())) : null;
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
        return new RefusjonEndring(beløpTilDto(finnRefusjon(forrigeAndel)), beløpTilDto(initRefusjon(andel)));
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

    private static no.nav.foreldrepenger.kalkulus.kontrakt.typer.Beløp beløpTilDto(Beløp beløp) {
        return no.nav.foreldrepenger.kalkulus.kontrakt.typer.Beløp.fra(beløp != null ? beløp.verdi() : null);
    }

}
