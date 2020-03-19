package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ErTidsbegrensetArbeidsforholdEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;

class UtledErTidsbegrensetArbeidsforholdEndringer {

    private UtledErTidsbegrensetArbeidsforholdEndringer() {
        // Skjul
    }

    public static List<ErTidsbegrensetArbeidsforholdEndring> utled(BeregningsgrunnlagDto beregningsgrunnlag, Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlag) {
        BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = forrigeBeregningsgrunnlag.map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder)
                .map(p -> p.get(0));
        List<BGAndelArbeidsforholdDto> arbeidMedTidsbegrensetAvklaring = finnArbeidMedAvklartTidsbegrensetArbeidsforhold(periode);
        List<BGAndelArbeidsforholdDto> forrigeAndeler = forrigePeriode.stream()
                .flatMap(a -> a.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .flatMap(a -> a.getBgAndelArbeidsforhold().stream())
                .collect(Collectors.toList());
        return arbeidMedTidsbegrensetAvklaring.stream()
                .map(arbeid -> utledErTidsbegrensetArbeidsforholdEndring(arbeid, forrigeAndeler))
                .collect(Collectors.toList());
    }

    private static List<BGAndelArbeidsforholdDto> finnArbeidMedAvklartTidsbegrensetArbeidsforhold(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .flatMap(a -> a.getBgAndelArbeidsforhold().stream())
                .filter(bgAndelArbeidsforholdDto -> bgAndelArbeidsforholdDto.getErTidsbegrensetArbeidsforhold() != null)
                .collect(Collectors.toList());
    }

    private static ErTidsbegrensetArbeidsforholdEndring utledErTidsbegrensetArbeidsforholdEndring(BGAndelArbeidsforholdDto arbeidsforhold, List<BGAndelArbeidsforholdDto> forrigeArbeidsforhold) {
        Optional<BGAndelArbeidsforholdDto> forrigeArbeid = forrigeArbeidsforhold.stream() .filter(a -> a.equals(arbeidsforhold)).findFirst();
        ToggleEndring toggleEndring = utledErTidsbegrensetEndring(arbeidsforhold, forrigeArbeid);
        Arbeidsgiver arbeidsgiver = arbeidsforhold.getArbeidsgiver();
        return new ErTidsbegrensetArbeidsforholdEndring(
                arbeidsgiver.getErVirksomhet() ? new Organisasjon(arbeidsgiver.getIdentifikator()) : new AktørIdPersonident(arbeidsgiver.getIdentifikator()),
                arbeidsforhold.getArbeidsforholdRef().getReferanse(),
                toggleEndring
        );
    }

    private static ToggleEndring utledErTidsbegrensetEndring(BGAndelArbeidsforholdDto arbeid, Optional<BGAndelArbeidsforholdDto> forrigeArbeid) {
        Boolean fraVerdi = forrigeArbeid.map(BGAndelArbeidsforholdDto::getErTidsbegrensetArbeidsforhold).orElse(null);
        Boolean tilVerdi = arbeid.getErTidsbegrensetArbeidsforhold();
        return new ToggleEndring(fraVerdi, tilVerdi);
    }


}
