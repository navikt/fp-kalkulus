package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.refusjonskravgyldighet;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class LagArbeidsgiverForSentRefusjonskravMap {

    public static Map<Arbeidsgiver, Boolean> lag(Map<YrkesaktivitetDto, Optional<RefusjonskravDatoDto>> yrkesaktivitetDatoMap, BeregningAktivitetAggregatDto gjeldendeAktiviteter, LocalDate skjæringstidspunktBeregning) {
        Map<Arbeidsgiver, Boolean> harSøktForSentMap = new HashMap<>();
        for (Map.Entry<YrkesaktivitetDto, Optional<RefusjonskravDatoDto>> entry : yrkesaktivitetDatoMap.entrySet()) {
            if (entry.getValue().isPresent()) {
                YrkesaktivitetDto yrkesaktivitet = entry.getKey();
                boolean arbeidsgiverHarSøktForSent = harSøktForSentMap.containsKey(yrkesaktivitet.getArbeidsgiver()) && harSøktForSentMap.get(yrkesaktivitet.getArbeidsgiver());
                boolean harSøktForSentForArbeidsforhold = HarYrkesaktivitetInnsendtRefusjonForSent.vurder(entry.getValue().get(), yrkesaktivitet, gjeldendeAktiviteter, skjæringstidspunktBeregning);
                harSøktForSentMap.put(yrkesaktivitet.getArbeidsgiver(), harSøktForSentForArbeidsforhold || arbeidsgiverHarSøktForSent);
            }
        }
        return harSøktForSentMap;
    }

}
