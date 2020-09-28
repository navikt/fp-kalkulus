package no.nav.folketrygdloven.kalkulator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class BeregningInntektsmeldingTjeneste {

    private static final int MND_I_1_ÅR = 12;
    private static final int SEKS = 6;

    private BeregningInntektsmeldingTjeneste() {
    }

    public static boolean erTotaltRefusjonskravStørreEnnEllerLikSeksG(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Collection<InntektsmeldingDto> inntektsmeldinger, Beløp grunnbeløp) {
        Beløp seksG = grunnbeløp.multipliser(SEKS);
        Beløp totaltRefusjonskravPrÅr = new Beløp(beregnTotaltRefusjonskravPrÅrIPeriode(beregningsgrunnlagPeriode, inntektsmeldinger));
        return totaltRefusjonskravPrÅr.compareTo(seksG) >= 0;
    }


    private static BigDecimal beregnTotaltRefusjonskravPrÅrIPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Collection<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream()
            .map(im -> finnRefusjonskravIPeriode(im, beregningsgrunnlagPeriode.getPeriode()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .multiply(BigDecimal.valueOf(MND_I_1_ÅR));
    }

    private static BigDecimal finnRefusjonskravIPeriode(InntektsmeldingDto inntektsmelding, Intervall periode) {
        Optional<Beløp> refKravFraEndring = inntektsmelding.getEndringerRefusjon().stream()
            .filter(r -> periode.inkluderer(r.getFom()))
            .findFirst()
            .map(RefusjonDto::getRefusjonsbeløp);
        if (refKravFraEndring.isPresent()) {
            return refKravFraEndring.get().getVerdi();
        }
        if (inntektsmelding.getRefusjonOpphører() != null && !inntektsmelding.getRefusjonOpphører().isBefore(periode.getFomDato())) {
            return inntektsmelding.getRefusjonBeløpPerMnd().getVerdi();
        }
        return BigDecimal.ZERO;
    }

    public static Optional<BigDecimal> finnRefusjonskravPrÅrIPeriodeForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                             Intervall periode,
                                                                             Collection<InntektsmeldingDto> inntektsmeldinger) {
        return finnInntektsmeldingForAndel(andel, inntektsmeldinger)
            .map(im -> finnRefusjonskravIPeriode(im, periode))
            .map(ref -> ref.multiply(BigDecimal.valueOf(MND_I_1_ÅR)));
    }

    public static Optional<InntektsmeldingDto> finnInntektsmeldingForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                           Collection<InntektsmeldingDto> inntektsmeldinger) {
        return inntektsmeldinger.stream()
            .filter(im -> andel.gjelderInntektsmeldingFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
            .findFirst();
    }

    public static Optional<AndelGradering> finnGraderingForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering) {
        if (aktivitetGradering == null) {
            return Optional.empty();
        }
        return aktivitetGradering.getAndelGradering().stream()
            .filter(andelGradering -> andelGradering.matcher(andel))
            .findFirst();
    }
}
