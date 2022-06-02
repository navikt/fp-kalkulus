package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.MidlertidigInaktivType;

public abstract class UtbetalingsgradGrunnlag {

    private final List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;

    public UtbetalingsgradGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
    }

    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        return utbetalingsgradPrAktivitet;
    }

    public List<PeriodeMedUtbetalingsgradDto> finnUtbetalingsgraderForArbeid(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto) {
        return getUtbetalingsgradPrAktivitet()
                .stream()
                .filter(akt -> akt.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(UttakArbeidType.MIDL_INAKTIV) || (matchArbeidsgiver(arbeidsgiver, akt) && matcherArbeidsforholdReferanse(arbeidsforholdRefDto, akt)))
                .flatMap(akt -> akt.getPeriodeMedUtbetalingsgrad().stream())
                .collect(Collectors.toList());
    }

    private static Boolean matchArbeidsgiver(Arbeidsgiver arbeidsgiver, UtbetalingsgradPrAktivitetDto akt) {
        return akt.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().map(Arbeidsgiver::getIdentifikator)
                .map(id -> id.equals(arbeidsgiver.getIdentifikator())).orElse(false);
    }


    private static boolean matcherArbeidsforholdReferanse(InternArbeidsforholdRefDto arbeidsforholdRefDto, UtbetalingsgradPrAktivitetDto utbGrad) {
        return utbGrad.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef().gjelderFor(arbeidsforholdRefDto);
    }

    protected boolean erMidlertidigInaktivTypeA(BeregningsgrunnlagDto bg, OpptjeningAktiviteterDto opptjeningAktiviteterDto) {
        return bg.getAktivitetStatuser().stream().anyMatch(a -> AktivitetStatus.MIDLERTIDIG_INAKTIV.equals(a.getAktivitetStatus()))
                && opptjeningAktiviteterDto != null
                && MidlertidigInaktivType.A.equals(opptjeningAktiviteterDto.getMidlertidigInaktivType());
    }

}
