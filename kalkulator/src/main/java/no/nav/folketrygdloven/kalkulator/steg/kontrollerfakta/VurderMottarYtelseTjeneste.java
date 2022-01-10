package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.util.Collection;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;

public class VurderMottarYtelseTjeneste {

    private VurderMottarYtelseTjeneste() {
        // Skjul
    }

    public static boolean skalVurdereMottattYtelse(BeregningsgrunnlagDto beregningsgrunnlag,
                                                   InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                   Collection<InntektsmeldingDto> inntektsmeldinger) {
        boolean erFrilanser = erFrilanser(beregningsgrunnlag);

        InntektFilterDto filter = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister());
        filter = filter.filterSammenligningsgrunnlag();

        if (erFrilanser) {
            return mottarYtelseIBeregningsperiode(beregningsgrunnlag, filter, AktivitetStatus.FRILANSER);
        }
        var arbeidstakerSomManglerInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
                .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, inntektsmeldinger);
        if (!arbeidstakerSomManglerInntektsmelding.isEmpty()) {
            return mottarYtelseIBeregningsperiode(beregningsgrunnlag, filter, AktivitetStatus.ARBEIDSTAKER);
        }
        return false;
    }

    private static boolean mottarYtelseIBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag, InntektFilterDto filter, AktivitetStatus aktivitetsStatus) {
        Intervall beregningsPeriodeForStatus = finnBeregningsperiodeForAktivitetStatus(beregningsgrunnlag, aktivitetsStatus);
        return filter.getFiltrertInntektsposter().stream().anyMatch(inntektspostDto -> {
            boolean overlapperYtelseMedBeregningsgrunnlaget = beregningsPeriodeForStatus.overlapper(inntektspostDto.getPeriode());
            return InntektspostType.YTELSE.equals(inntektspostDto.getInntektspostType()) && overlapperYtelseMedBeregningsgrunnlaget;
        });
    }

    public static boolean erFrilanser(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser());
    }

    public static Intervall finnBeregningsperiodeForAktivitetStatus(BeregningsgrunnlagDto beregningsgrunnlag, AktivitetStatus aktivitetsStatus) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> aktivitetsStatus.equals(andel.getAktivitetStatus())).map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregningsperiode).findFirst().orElseThrow(() -> new IllegalStateException("Fant ingen beregningsperiode for " + aktivitetsStatus.name()));
    }


}
