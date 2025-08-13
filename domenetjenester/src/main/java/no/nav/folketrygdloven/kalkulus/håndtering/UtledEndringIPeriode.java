package no.nav.folketrygdloven.kalkulus.håndtering;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagPeriodeEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagPrStatusOgAndelEndring;

class UtledEndringIPeriode {

    private UtledEndringIPeriode() {
        // skjul
    }

    public static Optional<BeregningsgrunnlagPeriodeEndring> utled(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagPeriodeDto periodeFraSteg, Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerFraSteg = periodeFraSteg.getBeregningsgrunnlagPrStatusOgAndelList();

        List<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndeler = forrigePeriode.map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList).orElse(Collections.emptyList());
        BeregningsgrunnlagPeriodeEndring periodeEndring = new BeregningsgrunnlagPeriodeEndring(
                utledAndelEndringer(andeler, andelerFraSteg, forrigeAndeler),
                List.of(),
                new Periode(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom())
                );
        if (periodeEndring.getBeregningsgrunnlagPrStatusOgAndelEndringer().isEmpty() && periodeEndring.getNyttInntektsforholdEndringer().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(periodeEndring);
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelEndring> utledAndelEndringer(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler,  List<BeregningsgrunnlagPrStatusOgAndelDto> andelerFraSteg, List<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndeler) {
        return andeler.stream()
                .map(a -> {
                    Optional<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndel = finnAndel(forrigeAndeler, a);
                    Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelFraSteg = finnAndel(andelerFraSteg, a);
                    return UtledEndringIAndel.utled(a, andelFraSteg, forrigeAndel);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnAndel(List<BeregningsgrunnlagPrStatusOgAndelDto> forrigeAndeler, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return forrigeAndeler.stream().filter(a -> a.equals(andel)).findFirst();
        }
        return forrigeAndeler.stream()
                .filter(a -> a.getAktivitetStatus().equals(andel.getAktivitetStatus())).findFirst();
    }

}
