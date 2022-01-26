package no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class OmfordelingUtenRefusjonskravTjeneste {

    private OmfordelingUtenRefusjonskravTjeneste() {
    }

    public static BeregningsgrunnlagDto omfordel(BeregningsgrunnlagDto beregningsgrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        var bgBuilder = BeregningsgrunnlagDto.builder(beregningsgrunnlag);
        perioder.forEach(p -> {
            var periodeBuilder = bgBuilder.getPeriodeBuilderFor(p.getPeriode()).orElseThrow();
            var fordeling = finnFordeling(p, ytelsespesifiktGrunnlag);
            omfordel(periodeBuilder, fordeling);
        });
        return bgBuilder.build();
    }

    private static void omfordel(BeregningsgrunnlagPeriodeDto.Builder periodeBuilder, List<NøkkelOgBeløp> fordelt) {
        fordelt.stream()
                .collect(Collectors.groupingBy(a -> a.andelsnr))
                .forEach((andelsnr, fordelingForAndel) -> {
                    var fordelingIterator = fordelingForAndel.iterator();
                    var førsteAndel = fordelingIterator.next();
                    var andelBuilder = periodeBuilder.getBuilderForAndel(førsteAndel.andelsnr).orElseThrow(() -> new IllegalStateException("Forventer å finne andelbuilder"));
                    andelBuilder.medInntektskategoriManuellFordeling(førsteAndel.inntektskategori);
                    andelBuilder.medFordeltPrÅr(førsteAndel.beløp.getVerdi());
                    var andel = andelBuilder.build();
                    while (fordelingIterator.hasNext()) {
                        var nesteAndel = fordelingIterator.next();
                        leggTilNyAndelBasertPåEksisterende(periodeBuilder, andelBuilder, andel, nesteAndel);
                    }
                });
    }

    private static void leggTilNyAndelBasertPåEksisterende(BeregningsgrunnlagPeriodeDto.Builder periodeBuilder, BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder, BeregningsgrunnlagPrStatusOgAndelDto andel, NøkkelOgBeløp nesteAndel) {
        var nesteAndelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medKilde(AndelKilde.PROSESS_OMFORDELING)
                .medInntektskategoriManuellFordeling(nesteAndel.inntektskategori)
                .medFordeltPrÅr(nesteAndel.beløp.getVerdi())
                .medAktivitetStatus(andel.getAktivitetStatus())
                .medBeregningsperiode(andel.getBeregningsperiodeFom(), andel.getBeregningsperiodeTom())
                .medArbforholdType(andel.getArbeidsforholdType());
        andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto.Builder::kopier)
                .ifPresent(nesteAndelBuilder::medBGAndelArbeidsforhold);
        periodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(andelBuilder);
    }

    private static List<NøkkelOgBeløp> finnFordeling(BeregningsgrunnlagPeriodeDto p, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var bortfaltInntekt = getBortfaltInntektPrAndelsnøkkel(p, ytelsespesifiktGrunnlag);
        var tilkommet = getTilkommetAktivitet(p, ytelsespesifiktGrunnlag);
        List<NøkkelOgBeløp> fordelt = fordelForTilkomneAndelerMedUtbetalingsgrad(bortfaltInntekt, tilkommet);
        List<NøkkelOgBeløp> fordeltUtenUtbetaling = fordelForTilkomneAndelerUtenUtbetalingsgrad(tilkommet);
        fordelt.addAll(fordeltUtenUtbetaling);
        return fordelt;
    }

    private static ArrayList<NøkkelOgBeløp> fordelForTilkomneAndelerUtenUtbetalingsgrad(List<NøkkelOgUtbetalingsgrad> tilkommet) {
        var tilkommetUtenUtbetaling = tilkommet.stream()
                .filter(a -> a.utbetalingsgrad.compareTo(BigDecimal.ZERO) == 0)
                .toList();
        return tilkommetUtenUtbetaling.stream().map(OmfordelingUtenRefusjonskravTjeneste::ingenFordeling)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static ArrayList<NøkkelOgBeløp> fordelForTilkomneAndelerMedUtbetalingsgrad(List<NøkkelOgBeløp> bortfaltInntekt, List<NøkkelOgUtbetalingsgrad> tilkommet) {
        var fordelt =  new ArrayList<NøkkelOgBeløp>();
        var tilkommetMedUtbetaling = tilkommet.stream().filter(a -> a.utbetalingsgrad.compareTo(BigDecimal.ZERO) > 0)
                .toList();
        var antallTilkommetMedUtbetaling = tilkommetMedUtbetaling.size();
        if (antallTilkommetMedUtbetaling > 0 && tilkommetMedUtbetaling.stream().allMatch(a -> a.utbetalingsgrad.compareTo(BigDecimal.valueOf(100)) == 0)) {
            var tilgjengeligForPerInntektskategoriOgAndel = bortfaltInntekt.stream()
                    .collect(Collectors.toMap(a -> a.inntektskategori,
                            a -> a.beløp.getVerdi().divide(BigDecimal.valueOf(antallTilkommetMedUtbetaling), RoundingMode.HALF_EVEN),
                            BigDecimal::add));
            tilkommet.stream()
                    .flatMap(a -> fordelLiktPerInntektskategori(tilgjengeligForPerInntektskategoriOgAndel, a))
                    .forEach(fordelt::add);
            bortfaltInntekt.stream().map(a -> new NøkkelOgBeløp(a.andelsnr, a.inntektskategori, Beløp.ZERO))
                    .forEach(fordelt::add);
        }
        return fordelt;
    }

    private static Stream<NøkkelOgBeløp> fordelLiktPerInntektskategori(Map<Inntektskategori, BigDecimal> tilgjengeligForPerInntektskategoriOgAndel, NøkkelOgUtbetalingsgrad a) {
        if (tilgjengeligForPerInntektskategoriOgAndel.isEmpty()) {
            return Stream.of(ingenFordeling(a));
        }
        return tilgjengeligForPerInntektskategoriOgAndel.entrySet().stream()
                .map(e -> new NøkkelOgBeløp(a.andelsnr, e.getKey(), new Beløp(e.getValue())));
    }

    private static NøkkelOgBeløp ingenFordeling(NøkkelOgUtbetalingsgrad a) {
        return new NøkkelOgBeløp(a.andelsnr, a.aktivitetStatus.getInntektskategori(), Beløp.ZERO);
    }


    private static List<NøkkelOgBeløp> getBortfaltInntektPrAndelsnøkkel(BeregningsgrunnlagPeriodeDto p, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .filter(a -> finnUtbetalingsgradForAndel(a, p.getPeriode(), ytelsespesifiktGrunnlag, true).compareTo(BigDecimal.ZERO) == 0)
                .map(a -> new NøkkelOgBeløp(a.getAndelsnr(),
                        a.getGjeldendeInntektskategori(),
                        new Beløp(a.getBruttoPrÅr())))
                .toList();
    }

    private static List<NøkkelOgUtbetalingsgrad> getTilkommetAktivitet(BeregningsgrunnlagPeriodeDto p, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_PERIODISERING))
                .map(a -> new NøkkelOgUtbetalingsgrad(
                        a.getAndelsnr(),
                        a.getAktivitetStatus(),
                        finnUtbetalingsgradForAndel(a, p.getPeriode(), ytelsespesifiktGrunnlag, false)))
                .toList();
    }

    private record NøkkelOgUtbetalingsgrad(Long andelsnr, AktivitetStatus aktivitetStatus, BigDecimal utbetalingsgrad) {
    }

    private record NøkkelOgBeløp(Long andelsnr, Inntektskategori inntektskategori, Beløp beløp) {
    }

}
