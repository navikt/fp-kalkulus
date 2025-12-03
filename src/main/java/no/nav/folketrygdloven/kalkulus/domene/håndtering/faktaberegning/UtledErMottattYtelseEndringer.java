package no.nav.folketrygdloven.kalkulus.domene.håndtering.faktaberegning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ErMottattYtelseEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;

class UtledErMottattYtelseEndringer {

    private UtledErMottattYtelseEndringer() {
        // Skjul
    }

    static List<ErMottattYtelseEndring> utled(FaktaAggregatDto fakta,
                                              Optional<FaktaAggregatDto> forrigeFakta) {
        List<ErMottattYtelseEndring> endringer = new ArrayList<>();
        utledFLMottarYtelseEndring(fakta, forrigeFakta).ifPresent(endringer::add);
        List<FaktaArbeidsforholdDto> faktaArbeidsforhold = fakta.getFaktaArbeidsforhold();
        List<FaktaArbeidsforholdDto> forrigeFaktaArbeidsforhold = forrigeFakta.map(FaktaAggregatDto::getFaktaArbeidsforhold).orElse(Collections.emptyList());
        faktaArbeidsforhold.stream()
                .map(fa -> utledErMottattYtelseEndring(fa, forrigeFaktaArbeidsforhold))
                .filter(Objects::nonNull)
                .forEach(endringer::add);
        return endringer;
    }

    private static Optional<ErMottattYtelseEndring> utledFLMottarYtelseEndring(FaktaAggregatDto fakta, Optional<FaktaAggregatDto> forrigeFakta) {
        Boolean flMottarYtelse = fakta.getFaktaAktør().map(FaktaAktørDto::getHarFLMottattYtelseVurdering).orElse(null);
        if (flMottarYtelse != null) {
            var forrigeFlMottarYtelse = forrigeFakta.flatMap(FaktaAggregatDto::getFaktaAktør)
                    .map(FaktaAktørDto::getHarFLMottattYtelseVurdering).orElse(null);
            if (forrigeFlMottarYtelse == null || !forrigeFlMottarYtelse.equals(flMottarYtelse)) {
                return Optional.of(ErMottattYtelseEndring.lagErMottattYtelseEndringForFrilans(new ToggleEndring(forrigeFlMottarYtelse, flMottarYtelse)));
            }
        }
        return Optional.empty();
    }

    private static ErMottattYtelseEndring utledErMottattYtelseEndring(FaktaArbeidsforholdDto fakta, List<FaktaArbeidsforholdDto> forrigeFaktaListe) {
        Optional<FaktaArbeidsforholdDto> forrigeFakta = finnForrigeFakta(forrigeFaktaListe, fakta.getArbeidsgiver(), fakta.getArbeidsforholdRef());
        ToggleEndring toggleEndring = utledErMottattYtelseEndring(fakta, forrigeFakta);
        if (toggleEndring != null) {
            return ErMottattYtelseEndring.lagErMottattYtelseEndringForArbeid(toggleEndring, mapArbeidsgiver(fakta.getArbeidsgiver()), fakta.getArbeidsforholdRef().getUUIDReferanse());
        }
        return null;
    }

    private static Aktør mapArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        return arbeidsgiver.getErVirksomhet() ? new Organisasjon(arbeidsgiver.getIdentifikator()) : new AktørIdPersonident(arbeidsgiver.getIdentifikator());
    }

    private static ToggleEndring utledErMottattYtelseEndring(FaktaArbeidsforholdDto fakta, Optional<FaktaArbeidsforholdDto> forrigeFakta) {
        if (fakta.getHarMottattYtelseVurdering() != null && harEndringIMottarYtelse(forrigeFakta.map(FaktaArbeidsforholdDto::getHarMottattYtelseVurdering), fakta.getHarMottattYtelseVurdering())) {
            return initMottarYtelseEndring(forrigeFakta, fakta.getHarMottattYtelseVurdering());
        }
        return null;
    }

    private static ToggleEndring initMottarYtelseEndring(Optional<FaktaArbeidsforholdDto> forrigeFakta, Boolean mottarYtelse) {
        return new ToggleEndring(finnMottarYtelse(forrigeFakta), mottarYtelse);
    }

    private static Boolean finnMottarYtelse(Optional<FaktaArbeidsforholdDto> forrigeFakta) {
        return forrigeFakta.map(FaktaArbeidsforholdDto::getHarMottattYtelseVurdering).orElse(null);
    }

    private static Boolean harEndringIMottarYtelse(Optional<Boolean> forrigeMottarYtelse, Boolean mottarYtelse) {
        return forrigeMottarYtelse.map(m -> !m.equals(mottarYtelse)).orElse(true);
    }

    private static Optional<FaktaArbeidsforholdDto> finnForrigeFakta(List<FaktaArbeidsforholdDto> forrigeFaktaArbeidsforhold, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto) {
        return forrigeFaktaArbeidsforhold.stream().filter(fa -> fa.gjelderFor(arbeidsgiver, arbeidsforholdRefDto)).findFirst();
    }



}
