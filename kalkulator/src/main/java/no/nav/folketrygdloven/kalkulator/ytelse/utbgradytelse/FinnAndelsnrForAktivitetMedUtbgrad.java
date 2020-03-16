package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.AktivitetStatusMapper.mapAktivitetStatus;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

class FinnAndelsnrForAktivitetMedUtbgrad {

    static Optional<Long> finnAndelsnrIFørstePeriode(BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                     UtbetalingsgradArbeidsforholdDto utbetalingsgradArbeidsforhold) {
        Arbeidsgiver tilretteleggingArbeidsgiver = utbetalingsgradArbeidsforhold.getArbeidsgiver().orElse(null);
        InternArbeidsforholdRefDto tilretteleggingArbeidsforholdRef = utbetalingsgradArbeidsforhold.getInternArbeidsforholdRef();
        AktivitetStatus tilretteleggingAktivitetStatus = mapAktivitetStatus(utbetalingsgradArbeidsforhold.getUttakArbeidType());
        return vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()
            .stream().filter(a -> a.getAktivitetStatus().equals(tilretteleggingAktivitetStatus) &&
                a.getBgAndelArbeidsforhold().map(bgAndelArbeidsforhold -> bgAndelArbeidsforhold.getArbeidsgiver().equals(tilretteleggingArbeidsgiver) &&
                    bgAndelArbeidsforhold.getArbeidsforholdRef().gjelderFor(tilretteleggingArbeidsforholdRef)).orElse(true))
            .findFirst().map(BeregningsgrunnlagPrStatusOgAndelDto::getAndelsnr);
    }

}
