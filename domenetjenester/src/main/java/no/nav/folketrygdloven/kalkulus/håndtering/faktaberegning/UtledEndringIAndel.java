package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagPrStatusOgAndelEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.InntektEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.InntektskategoriEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;

class UtledEndringIAndel {

    private UtledEndringIAndel() {
        // skjul
    }

    public static Optional<BeregningsgrunnlagPrStatusOgAndelEndring> utled(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        BeregningsgrunnlagPrStatusOgAndelEndring andelEndring = initialiserAndelEndring(andel);
        andelEndring.setInntektEndring(lagInntektEndring(andel, forrigeAndel));
        andelEndring.setInntektskategoriEndring(utledInntektskategoriEndring(andel, forrigeAndel));
        andelEndring.setMottarYtelseEndring(lagMottarYtelseEndring(andel, forrigeAndel));
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
                andelEndring = new BeregningsgrunnlagPrStatusOgAndelEndring(aktør, andel.getArbeidsforholdRef().map(InternArbeidsforholdRefDto::getReferanse).orElse(null));
            } else {
                andelEndring = BeregningsgrunnlagPrStatusOgAndelEndring.opprettForArbeidstakerUtenArbeidsgiver(new OpptjeningAktivitetType(andel.getArbeidsforholdType().getKode()));
            }
        } else {
            andelEndring = new BeregningsgrunnlagPrStatusOgAndelEndring(new AktivitetStatus(andel.getAktivitetStatus().getKode()));
        }
        return andelEndring;
    }

    private static boolean harEndringIAndel(BeregningsgrunnlagPrStatusOgAndelEndring a) {
        return a.getInntektEndring() != null || a.getInntektskategoriEndring() != null || a.getMottarYtelseEndring() != null;
    }

    private static ToggleEndring lagMottarYtelseEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return andel.mottarYtelse()
                .filter(mottarYtelse -> harEndringIMottarYtelse(forrigeAndel, mottarYtelse))
                .map(mottarYtelse -> initMottarYtelseEndring(forrigeAndel, mottarYtelse)).orElse(null);
    }

    private static ToggleEndring initMottarYtelseEndring(Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel, Boolean mottarYtelse) {
        return new ToggleEndring(finnMottarYtelse(forrigeAndel), mottarYtelse);
    }

    private static Boolean finnMottarYtelse(Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return forrigeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::mottarYtelse).orElse(null);
    }

    private static Boolean harEndringIMottarYtelse(Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel, Boolean mottarYtelse) {
        return forrigeAndel.flatMap(BeregningsgrunnlagPrStatusOgAndelDto::mottarYtelse).map(m -> !m.equals(mottarYtelse)).orElse(true);
    }

    private static InntektEndring lagInntektEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return andel.getBeregnetPrÅr() != null ?
                new InntektEndring(forrigeAndel.map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr).orElse(null), andel.getBeregnetPrÅr()) : null;
    }

    private static InntektskategoriEndring utledInntektskategoriEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return harEndringIInntektskategori(andel, forrigeAndel) ? initInntektskategoriEndring(andel, forrigeAndel) : null;
    }

    private static InntektskategoriEndring initInntektskategoriEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return new InntektskategoriEndring(finnInntektskategori(forrigeAndel), initInntektskategori(andel));
    }

    private static no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori initInntektskategori(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return new no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori(andel.getInntektskategori().getKode());
    }

    private static Boolean harEndringIInntektskategori(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return forrigeAndel.map(a -> !a.getInntektskategori().equals(andel.getInntektskategori())).orElse(true);
    }

    private static no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori finnInntektskategori(Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return forrigeAndel
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getInntektskategori)
                .map(Inntektskategori::getKode)
                .map(no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori::new)
                .orElse(null);
    }

}
