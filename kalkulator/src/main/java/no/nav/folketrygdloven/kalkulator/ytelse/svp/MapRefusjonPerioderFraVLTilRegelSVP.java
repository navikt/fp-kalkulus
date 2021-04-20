package no.nav.folketrygdloven.kalkulator.ytelse.svp;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapRefusjonPerioderFraVLTilRegelUtbgrad;

@FagsakYtelseTypeRef("SVP")
@ApplicationScoped
public class MapRefusjonPerioderFraVLTilRegelSVP extends MapRefusjonPerioderFraVLTilRegelUtbgrad {

    @Override
    protected List<Gradering> mapUtbetalingsgrader(InntektsmeldingDto im, BeregningsgrunnlagInput input) {
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet = ((UtbetalingsgradGrunnlag) input.getYtelsespesifiktGrunnlag()).getUtbetalingsgradPrAktivitet();
        List<PeriodeMedUtbetalingsgradDto> utbetalinger = utbetalingsgradPrAktivitet.stream()
                .filter(utb -> matcherArbeidsforhold(im, utb))
                .findFirst().map(UtbetalingsgradPrAktivitetDto::getPeriodeMedUtbetalingsgrad)
                .orElse(Collections.emptyList());
        return utbetalinger.stream()
                .map(utb -> new Gradering(Periode.of(utb.getPeriode().getFomDato(), utb.getPeriode().getTomDato()), utb.getUtbetalingsgrad()))
                .collect(Collectors.toList());
    }

    private boolean matcherArbeidsforhold(InntektsmeldingDto im, UtbetalingsgradPrAktivitetDto utb) {
        Arbeidsgiver utbAG = utb.getUtbetalingsgradArbeidsforhold().getArbeidsgiver().orElse(null);
        InternArbeidsforholdRefDto utbRef = utb.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef() != null
                ? utb.getUtbetalingsgradArbeidsforhold().getInternArbeidsforholdRef()
                : InternArbeidsforholdRefDto.nullRef();
        boolean gjelderSammeAG = Objects.equals(im.getArbeidsgiver(), utbAG);
        return gjelderSammeAG && im.getArbeidsforholdRef().gjelderFor(utbRef);
    }

    // Fordi annotasjoner propageres til subklasser
    // Burde vurdere å gjere om til interface

}
