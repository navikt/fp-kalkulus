package no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;

public class OmfordelingUtenRefusjonskravTjeneste {

    private OmfordelingUtenRefusjonskravTjeneste() {
    }

    public static BeregningsgrunnlagDto omfordel(BeregningsgrunnlagDto beregningsgrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        var bgBuilder = BeregningsgrunnlagDto.builder(beregningsgrunnlag);
        perioder.forEach(p -> {
            if (!p.getPeriode().erHelg()) {
                var periodeBuilder = bgBuilder.getPeriodeBuilderFor(p.getPeriode()).orElseThrow();
                var fordeling = finnFordeling(p, ytelsespesifiktGrunnlag);
                omfordel(periodeBuilder, fordeling);
            }
        });
        return bgBuilder.build();
    }

    private static void omfordel(BeregningsgrunnlagPeriodeDto.Builder periodeBuilder, List<NøkkelOgBeløp> fordelt) {
        fordelt.forEach(fordeltAndel -> {
            var andelBuilder = periodeBuilder.getBuilderForAndel(fordeltAndel.andelsnr).orElseThrow(() -> new IllegalStateException("Forventer å finne andelbuilder"));
            andelBuilder.medFordeltPrÅr(fordeltAndel.beløp.getVerdi());
        });
    }

    private static List<NøkkelOgBeløp> finnFordeling(BeregningsgrunnlagPeriodeDto p, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var bortfaltInntekt = getBortfaltInntektPrAndelsnøkkel(p, ytelsespesifiktGrunnlag);
        var tilkommet = getTilkommetAktivitet(p, ytelsespesifiktGrunnlag);
        var antallTilkommet = tilkommet.size();
        if (antallTilkommet > 0 && tilkommet.stream().allMatch(a -> a.utbetalingsgrad.compareTo(BigDecimal.valueOf(100)) == 0)) {
            var tilgjengeligForOmfordeling = bortfaltInntekt.stream().map(NøkkelOgBeløp::beløp).reduce(Beløp::adder).orElse(Beløp.ZERO);
            var perAndel = tilgjengeligForOmfordeling.getVerdi().divide(BigDecimal.valueOf(antallTilkommet), RoundingMode.HALF_UP);
            var fordelt = tilkommet.stream()
                    .map(a -> new NøkkelOgBeløp(a.andelsnr, new Beløp(perAndel))).collect(Collectors.toCollection(ArrayList::new));
            fordelt.addAll(bortfaltInntekt.stream().map(a -> new NøkkelOgBeløp(a.andelsnr, Beløp.ZERO)).toList());
            return fordelt;
        }
        return Collections.emptyList();
    }

    private static List<NøkkelOgBeløp> getBortfaltInntektPrAndelsnøkkel(BeregningsgrunnlagPeriodeDto p, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .filter(a -> finnUtbetalingsgradForAndel(a, p.getPeriode(), ytelsespesifiktGrunnlag).compareTo(BigDecimal.ZERO) == 0)
                .map(a -> new NøkkelOgBeløp(a.getAndelsnr(),
                        new Beløp(a.getBruttoPrÅr())))
                .toList();
    }

    private static List<NøkkelOgUtbetalingsgrad> getTilkommetAktivitet(BeregningsgrunnlagPeriodeDto p, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_PERIODISERING))
                .filter(a -> finnUtbetalingsgradForAndel(a, p.getPeriode(), ytelsespesifiktGrunnlag).compareTo(BigDecimal.ZERO) > 0)
                .map(a -> new NøkkelOgUtbetalingsgrad(
                        a.getAndelsnr(),
                        finnUtbetalingsgradForAndel(a, p.getPeriode(), ytelsespesifiktGrunnlag)))
                .toList();
    }

    private record NøkkelOgUtbetalingsgrad(Long andelsnr, BigDecimal utbetalingsgrad) {
    }

    private record NøkkelOgBeløp(Long andelsnr, Beløp beløp) {
    }

}
