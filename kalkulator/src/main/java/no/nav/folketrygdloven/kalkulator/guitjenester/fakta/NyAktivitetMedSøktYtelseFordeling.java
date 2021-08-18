package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.AktivitetStatusMapper;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FordelBeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.NyPeriodeDto;

/**
 * Lager perioder for nye aktiviteter med utbetalingsgrad som ikke har gradering eller refusjon
 */
class NyAktivitetMedSøktYtelseFordeling {

    static List<NyPeriodeDto> lagPerioderForNyAktivitetMedSøktYtelse(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                     FordelingTilfelle tilfelle,
                                                                     BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                     FordelBeregningsgrunnlagArbeidsforholdDto endringAf) {
        boolean gjelderYtelseMedUtbetalingsgrad = ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag;
        boolean gjelderNyAktivitet = tilfelle.equals(FordelingTilfelle.NY_AKTIVITET);
        boolean harIkkeRefusjonEllerGradering = endringAf.getPerioderMedGraderingEllerRefusjon().isEmpty();
        if (harIkkeRefusjonEllerGradering && gjelderNyAktivitet && gjelderYtelseMedUtbetalingsgrad) {
            UtbetalingsgradGrunnlag utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag;
            return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet()
                    .stream()
                    .filter(utbAktivitet -> matcherAndelAktivitetMedUtbetalingsgrad(andel, utbAktivitet))
                    .flatMap(utbetalingsgradPrAktivitetDto -> utbetalingsgradPrAktivitetDto.getPeriodeMedUtbetalingsgrad().stream())
                    .map(NyAktivitetMedSøktYtelseFordeling::mapTilNyPeriode)
                    .collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    private static boolean matcherAndelAktivitetMedUtbetalingsgrad(BeregningsgrunnlagPrStatusOgAndelDto andel, UtbetalingsgradPrAktivitetDto utbAktivitet) {
        UtbetalingsgradArbeidsforholdDto arbeidsforhold = utbAktivitet.getUtbetalingsgradArbeidsforhold();
        UttakArbeidType uttakArbeidType = arbeidsforhold.getUttakArbeidType();
        AktivitetStatus aktivitetStatus = AktivitetStatusMapper.mapAktivitetStatus(uttakArbeidType);
        if (andel.getAktivitetStatus().equals(aktivitetStatus)) {
            if (andel.getAktivitetStatus().erArbeidstaker()) {
                return matcherArbeidSøktYtelse(andel, arbeidsforhold);
            }
            return true;
        }
        return false;
    }

    private static Boolean matcherArbeidSøktYtelse(BeregningsgrunnlagPrStatusOgAndelDto andel, UtbetalingsgradArbeidsforholdDto arbeidsforhold) {
        return andel.getBgAndelArbeidsforhold()
                .map(arb -> arb.getArbeidsgiver() != null && arbeidsforhold.getArbeidsgiver().isPresent() &&
                        arb.getArbeidsgiver().getIdentifikator().equals(arbeidsforhold.getArbeidsgiver().get().getIdentifikator()) &&
                        arb.getArbeidsforholdRef().gjelderFor(arbeidsforhold.getInternArbeidsforholdRef())).orElse(false);
    }

    private static NyPeriodeDto mapTilNyPeriode(PeriodeMedUtbetalingsgradDto p) {
        NyPeriodeDto endringIYtelsePeriode = new NyPeriodeDto(false, false, true);
        endringIYtelsePeriode.setFom(p.getPeriode().getFomDato());
        endringIYtelsePeriode.setTom(p.getPeriode().getTomDato());
        return endringIYtelsePeriode;
    }


}
