package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ErMottattYtelseEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;

class UtledErMottattYtelseEndringer {

    private UtledErMottattYtelseEndringer() {
        // Skjul
    }

    static List<ErMottattYtelseEndring> utled(BeregningsgrunnlagDto beregningsgrunnlag,
                                              Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlag) {
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = forrigeBeregningsgrunnlag.map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder)
                .map(p -> p.get(0));
        List<BeregningsgrunnlagPrStatusOgAndelDto> mottarYtelseAndeler = finnAndelerMedAvklartMottattYtelse(periode);
        List<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndeler = forrigePeriode.map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList).orElse(Collections.emptyList());
        return mottarYtelseAndeler.stream()
                .map(a -> utledErMottattYtelseEndring(a, forrigeAndeler))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelerMedAvklartMottattYtelse(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> a.mottarYtelse().isPresent())
                .collect(Collectors.toList());
    }

    private static ErMottattYtelseEndring utledErMottattYtelseEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, List<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndeler) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel = finnAndel(forrigeAndeler, andel);
        ToggleEndring toggleEndring = utledErMottattYtelseEndring(andel, forrigeAndel);
        if (toggleEndring != null) {
            if (andel.getArbeidsgiver().isPresent()) {
                Arbeidsgiver arbeidsgiver = andel.getArbeidsgiver().get();
                return ErMottattYtelseEndring.lagErMottattYtelseEndringForArbeid(toggleEndring, mapArbeidsgiver(arbeidsgiver), andel.getArbeidsforholdRef().map(InternArbeidsforholdRefDto::getReferanse).orElse(null));
            } else if (andel.getAktivitetStatus().erFrilanser()) {
                return ErMottattYtelseEndring.lagErMottattYtelseEndringForFrilans(toggleEndring);
            }
        }
        return null;
    }

    private static Aktør mapArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        return arbeidsgiver.getErVirksomhet() ? new Organisasjon(arbeidsgiver.getIdentifikator()) : new AktørIdPersonident(arbeidsgiver.getIdentifikator());
    }

    private static ToggleEndring utledErMottattYtelseEndring(BeregningsgrunnlagPrStatusOgAndelDto andel, Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel) {
        return andel.mottarYtelse()
                .filter(mottarYtelse -> harEndringIMottarYtelse(forrigeAndel, mottarYtelse))
                .map(mottarYtelse -> initMottarYtelseEndring(forrigeAndel, mottarYtelse))
                .orElse(null);
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

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnAndel(List<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndeler, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return forrigeAndeler.stream().filter(a -> a.equals(andel)).findFirst();
        }
        return forrigeAndeler.stream()
                .filter(a -> a.getAktivitetStatus().equals(andel.getAktivitetStatus())).findFirst();
    }



}
