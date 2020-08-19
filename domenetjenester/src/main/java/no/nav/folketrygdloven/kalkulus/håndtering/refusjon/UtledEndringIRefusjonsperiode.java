package no.nav.folketrygdloven.kalkulus.håndtering.refusjon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.DatoEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonoverstyringEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonoverstyringPeriodeEndring;

public final class UtledEndringIRefusjonsperiode {

    private UtledEndringIRefusjonsperiode() {
        // skjul
    }

    protected static RefusjonoverstyringEndring utledRefusjonoverstyringEndring(BeregningRefusjonOverstyringerDto refusjonOverstyringaggregat, Optional<BeregningRefusjonOverstyringerDto> forrigerefusjonOverstyringaggregat) {
        List<BeregningRefusjonOverstyringDto> refusjonendringerMedOverstyrtPeriode = refusjonOverstyringaggregat.getRefusjonOverstyringer()
                .stream()
                .filter(ro -> !ro.getRefusjonPerioder().isEmpty())
                .collect(Collectors.toList());
        List<RefusjonoverstyringPeriodeEndring> endringer = new ArrayList<>();
        refusjonendringerMedOverstyrtPeriode.forEach(refusjonOverstyringHosAG -> {
            List<BeregningRefusjonPeriodeDto> nyeRefusjonperioderHosAG = refusjonOverstyringHosAG.getRefusjonPerioder();
            List<BeregningRefusjonPeriodeDto> forrigeRefusjonsperioderHosAG = finnForrigePerioderHosAG(forrigerefusjonOverstyringaggregat, refusjonOverstyringHosAG.getArbeidsgiver());
            List<RefusjonoverstyringPeriodeEndring> endringerForAG = utledEndringerIPerioder(refusjonOverstyringHosAG.getArbeidsgiver(), nyeRefusjonperioderHosAG, forrigeRefusjonsperioderHosAG);
            endringer.addAll(endringerForAG);
        });
        return new RefusjonoverstyringEndring(endringer);
    }

    private static List<RefusjonoverstyringPeriodeEndring> utledEndringerIPerioder(Arbeidsgiver arbeidsgiver, List<BeregningRefusjonPeriodeDto> nyeRefusjonperioderHosAG, List<BeregningRefusjonPeriodeDto> forrigeRefusjonsperioderHosAG) {
        List<RefusjonoverstyringPeriodeEndring> endringer = new ArrayList<>();
        nyeRefusjonperioderHosAG.forEach(periode -> {
            Optional<BeregningRefusjonPeriodeDto> matchetArbeidsforhold = forrigeRefusjonsperioderHosAG.stream()
                    .filter(p -> matcherReferanse(periode, p))
                    .findFirst();
            DatoEndring datoEndring = new DatoEndring(matchetArbeidsforhold.map(BeregningRefusjonPeriodeDto::getStartdatoRefusjon).orElse(null), periode.getStartdatoRefusjon());
            if (arbeidsgiver.getErVirksomhet()) {
                endringer.add(new RefusjonoverstyringPeriodeEndring(new Organisasjon(arbeidsgiver.getIdentifikator()), periode.getArbeidsforholdRef().getReferanse(), datoEndring));
            } else {
                endringer.add(new RefusjonoverstyringPeriodeEndring(new AktørIdPersonident(arbeidsgiver.getIdentifikator()), periode.getArbeidsforholdRef().getReferanse(), datoEndring));
            }
        });
        return endringer;
    }

    private static boolean matcherReferanse(BeregningRefusjonPeriodeDto periode, BeregningRefusjonPeriodeDto p) {
        String ref1 = p.getArbeidsforholdRef().getReferanse();
        String ref2 = periode.getArbeidsforholdRef().getReferanse();
        return Objects.equals(ref1, ref2);
    }

    private static List<BeregningRefusjonPeriodeDto> finnForrigePerioderHosAG(Optional<BeregningRefusjonOverstyringerDto> forrigerefusjonOverstyringaggregat, Arbeidsgiver ag) {
        List<BeregningRefusjonOverstyringDto> forrigeRefusjonOverstyringer = forrigerefusjonOverstyringaggregat
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList());
        return forrigeRefusjonOverstyringer
                .stream()
                .filter(refOverstyring -> refOverstyring.getArbeidsgiver().equals(ag))
                .findFirst()
                .map(BeregningRefusjonOverstyringDto::getRefusjonPerioder)
                .orElse(Collections.emptyList());
    }
}
