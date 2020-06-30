package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public abstract class UtbetalingsgradGrunnlag {

    private List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;

    public UtbetalingsgradGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
    }

    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        return utbetalingsgradPrAktivitet;
    }

    public List<PeriodeMedUtbetalingsgradDto> finnUtbetalingsgraderForArbeid(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto) {
        return getUtbetalingsgradPrAktivitet()
                .stream()
                .filter(akt -> matchArbeidsgiver(arbeidsgiver, akt) && matcherArbeidsforholdReferanse(arbeidsforholdRefDto, akt))
                .findFirst()
                .map(UtbetalingsgradPrAktivitetDto::getPeriodeMedUtbetalingsgrad)
                .orElse(Collections.emptyList());
    }

    private static Boolean matchArbeidsgiver(Arbeidsgiver arbeidsgiver, UtbetalingsgradPrAktivitetDto akt) {
        return akt.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().map(Arbeidsgiver::getIdentifikator)
                .map(id -> id.equals(arbeidsgiver.getIdentifikator())).orElse(false);
    }


    private static boolean matcherArbeidsforholdReferanse(InternArbeidsforholdRefDto arbeidsforholdRefDto, UtbetalingsgradPrAktivitetDto utbGrad) {
        return utbGrad.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(arbeidsforholdRefDto);
    }

}
