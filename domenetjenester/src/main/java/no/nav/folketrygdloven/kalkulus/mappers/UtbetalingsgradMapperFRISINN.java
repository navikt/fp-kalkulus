package no.nav.folketrygdloven.kalkulus.mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.EffektivÅrsinntektTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

public class UtbetalingsgradMapperFRISINN {

    /**
     * Finnner utbetalingsgrader for FRISINN
     *
     * @param iayGrunnlag InntektArbeidYtelseGrunnlag
     * @param beregningsgrunnlagGrunnlagEntitet Aktivt beregningsgrunnlag
     * @param frisinnPerioder
     * @return Liste med utbetalingsgrader for FRISINN
     */
    public static List<UtbetalingsgradPrAktivitetDto> map(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                          Optional<BeregningsgrunnlagGrunnlagEntitet> beregningsgrunnlagGrunnlagEntitet,
                                                          List<FrisinnPeriode> frisinnPerioder) {
        Optional<BeregningsgrunnlagEntitet> bgOpt = beregningsgrunnlagGrunnlagEntitet.flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
        if (bgOpt.isEmpty() || frisinnPerioder.isEmpty()) {
            return Collections.emptyList();
        }
        List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetListe = new ArrayList<>();
        utbetalingsgradPrAktivitetListe.add(mapUtbetalingsgraderForNæring(iayGrunnlag, bgOpt.get(), frisinnPerioder));
        utbetalingsgradPrAktivitetListe.add(mapUtbetalingsgraderForFrilans(iayGrunnlag, bgOpt.get(), frisinnPerioder));
        return utbetalingsgradPrAktivitetListe;
    }


    private static UtbetalingsgradPrAktivitetDto mapUtbetalingsgraderForNæring(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                               BeregningsgrunnlagEntitet bg,
                                                                               List<FrisinnPeriode> frisinnPerioder) {
        UtbetalingsgradArbeidsforholdDto snAktivitet = new UtbetalingsgradArbeidsforholdDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        List<PeriodeMedUtbetalingsgradDto> perioderMedUtbetalingsgrad = finnNæringsInntekter(iayGrunnlag).stream()
                .filter(pi -> erSøktFrisinnIPerioden(pi, frisinnPerioder, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .map(inntekt -> mapTilPeriodeMedUtbetalingsgrad(inntekt, bg, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .collect(Collectors.toList());
        return new UtbetalingsgradPrAktivitetDto(snAktivitet, perioderMedUtbetalingsgrad);
    }

    private static UtbetalingsgradPrAktivitetDto mapUtbetalingsgraderForFrilans(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                BeregningsgrunnlagEntitet bg,
                                                                                List<FrisinnPeriode> frisinnPerioder) {
        UtbetalingsgradArbeidsforholdDto frilansAktivitet = new UtbetalingsgradArbeidsforholdDto(null,
                InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.FRILANS);
        List<PeriodeMedUtbetalingsgradDto> perioderMedUtbetalingsgrad = finnFrilansInntekter(iayGrunnlag).stream()
                .filter(pi -> erSøktFrisinnIPerioden(pi, frisinnPerioder, AktivitetStatus.FRILANSER))
                .map(inntekt -> mapTilPeriodeMedUtbetalingsgrad(inntekt, bg, AktivitetStatus.FRILANSER))
                .collect(Collectors.toList());
        return new UtbetalingsgradPrAktivitetDto(frilansAktivitet, perioderMedUtbetalingsgrad);
    }

    private static boolean erSøktFrisinnIPerioden(OppgittPeriodeInntekt periodeinntekt,
                                                  List<FrisinnPeriode> frisinnPerioder,
                                                  AktivitetStatus status) {
        return frisinnPerioder.stream()
                .filter(fp -> fp.getPeriode().overlapper(periodeinntekt.getPeriode()))
                .anyMatch(fp -> AktivitetStatus.FRILANSER.equals(status) ? fp.getSøkerFrilans() : fp.getSøkerNæring());
    }

    private static List<OppgittPeriodeInntekt> finnFrilansInntekter(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (iayGrunnlag.getOppgittOpptjening().isEmpty() || iayGrunnlag.getOppgittOpptjening().get().getFrilans().isEmpty()) {
            return Collections.emptyList();
        }
        return iayGrunnlag.getOppgittOpptjening().get().getFrilans().get().getOppgittFrilansInntekt()
                .stream().map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }

    private static List<OppgittPeriodeInntekt> finnNæringsInntekter(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (iayGrunnlag.getOppgittOpptjening().isEmpty()) {
            return Collections.emptyList();
    }
        return iayGrunnlag.getOppgittOpptjening().get().getEgenNæring()
                .stream().map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }

    private static PeriodeMedUtbetalingsgradDto mapTilPeriodeMedUtbetalingsgrad(OppgittPeriodeInntekt oppgittPeriodeInntekt,
                                                                                BeregningsgrunnlagEntitet bg,
                                                                                AktivitetStatus aktivitetStatus) {
        BigDecimal løpendeÅrsinntekt = EffektivÅrsinntektTjenesteFRISINN.finnEffektivÅrsinntektForLøpenedeInntekt(oppgittPeriodeInntekt);
        BigDecimal totalInntektIPeriode = bg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getPeriode().inkluderer(oppgittPeriodeInntekt.getPeriode().getFomDato()))
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .filter(a -> a.getAktivitetStatus().equals(aktivitetStatus))
                .map(BeregningsgrunnlagPrStatusOgAndel::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        BigDecimal bortfaltInntekt = totalInntektIPeriode.subtract(løpendeÅrsinntekt).max(BigDecimal.ZERO);
        BigDecimal utbetalingsgrad = totalInntektIPeriode.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : bortfaltInntekt.divide(totalInntektIPeriode, 10, RoundingMode.HALF_EVEN);
        return new PeriodeMedUtbetalingsgradDto(oppgittPeriodeInntekt.getPeriode(), utbetalingsgrad.multiply(BigDecimal.valueOf(100)));
    }
}
