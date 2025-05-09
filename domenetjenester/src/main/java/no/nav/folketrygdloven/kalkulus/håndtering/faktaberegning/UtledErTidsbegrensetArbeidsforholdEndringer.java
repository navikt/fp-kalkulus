package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ErTidsbegrensetArbeidsforholdEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;

class UtledErTidsbegrensetArbeidsforholdEndringer {

    private UtledErTidsbegrensetArbeidsforholdEndringer() {
        // Skjul
    }

    public static List<ErTidsbegrensetArbeidsforholdEndring> utled(FaktaAggregatDto fakta, Optional<FaktaAggregatDto> forrigeFakta) {

        List<FaktaArbeidsforholdDto> arbeidMedTidsbegrensetAvklaring = fakta.getFaktaArbeidsforhold().stream()
                .filter(fa -> fa.getErTidsbegrensetVurdering() != null)
                .toList();
        List<FaktaArbeidsforholdDto> forrigeArbeidFakta = forrigeFakta.map(FaktaAggregatDto::getFaktaArbeidsforhold).orElse(Collections.emptyList());
        return arbeidMedTidsbegrensetAvklaring.stream()
                .map(fa -> utledErTidsbegrensetArbeidsforholdEndring(fa, forrigeArbeidFakta))
                .toList();
    }

    private static ErTidsbegrensetArbeidsforholdEndring utledErTidsbegrensetArbeidsforholdEndring(FaktaArbeidsforholdDto faktaArbeidsforhold, List<FaktaArbeidsforholdDto> forrigeFaktaListe) {
        Optional<FaktaArbeidsforholdDto> forrigeFakta = forrigeFaktaListe.stream()
                .filter(a -> a.gjelderFor(faktaArbeidsforhold.getArbeidsgiver(), faktaArbeidsforhold.getArbeidsforholdRef()))
                .findFirst();
        ToggleEndring toggleEndring = utledErTidsbegrensetEndring(faktaArbeidsforhold, forrigeFakta);
        Arbeidsgiver arbeidsgiver = faktaArbeidsforhold.getArbeidsgiver();
        return new ErTidsbegrensetArbeidsforholdEndring(
                arbeidsgiver.getErVirksomhet() ? new Organisasjon(arbeidsgiver.getIdentifikator()) : new AktørIdPersonident(arbeidsgiver.getIdentifikator()),
                faktaArbeidsforhold.getArbeidsforholdRef().getUUIDReferanse(),
                toggleEndring
        );
    }

    private static ToggleEndring utledErTidsbegrensetEndring(FaktaArbeidsforholdDto fakta, Optional<FaktaArbeidsforholdDto> forrigeFakta) {
        Boolean fraVerdi = forrigeFakta.map(FaktaArbeidsforholdDto::getErTidsbegrensetVurdering).orElse(null);
        Boolean tilVerdi = fakta.getErTidsbegrensetVurdering();
        return new ToggleEndring(fraVerdi, tilVerdi);
    }


}
