package no.nav.folketrygdloven.kalkulator.felles.inntektgradering;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnPerioderForArbeid;
import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnPerioderForStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.uttak.UttakArbeidType;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektPeriodeTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

/**
 * Foreslår et resultat av inntektgradering som, gitt at alle andre parametere er like, gir et uendret resultat.
 * <p>
 * Ved aktivering av gradering mot inntekt endret uttaksgraden for spesialhåndterte statuser seg fra å speile andre aktiviteter til å vere 100%.
 * Dette fører til at noen saker kan få økt utbetaling, noe som er uønskelig ettersom de allerede får for mye. Vi ønsker heller ikke å redusere utbetaling,
 * men å gradere slik at resultatet blir det samme.
 * <p>
 * Dersom grunnlaget og graderingsprosentene for alle andeler er lik forrige behandling (unntatt for spesialtyper) vil tilkommet inntekt som gir ingen endring kunne uttrykkes slik:
 * Tilkommet inntekt = (Gradering_ny - Gradering_forrige) * AndelsmessigFørGraderingPrÅr
 * der  Gradering_ny er ny gradering for spesialhåndterintype
 * Gradering_gammel er forrige gradering for spesialhåndterintype
 * AndelsmessigFørGraderingPrÅr (aka avkortetFørGraderingPrÅr) er andelsmessig grunnlag før gradering for spesialhåndteringstype (grunnlag avkortet mot 6G, men ikke gradert mot arbeidstid)
 */
public class ForeslåInntektGraderingForUendretResultat {

    private static final Set<UttakArbeidType> SPESIALTYPER_FRA_UTTAK = Set.of(UttakArbeidType.IKKE_YRKESAKTIV, UttakArbeidType.BRUKERS_ANDEL);
    private static final Logger LOGGER = LoggerFactory.getLogger(ForeslåInntektGraderingForUendretResultat.class);

    public static List<TilkommetInntektDto> foreslå(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagPeriodeDto forrigePeriode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (skalGraderesFullt(periode) || harIkkeFastsattForrigePeriode(forrigePeriode)) {
            return ingenEndring(periode);
        }

        var graderingsdataPrAndel = finnGraderingsdataPrAndel(periode, forrigePeriode, (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag);

        LOGGER.info("Graderingsdata: " + graderingsdataPrAndel);

        if (harEndringerSomPåvirkerUtbetaling(graderingsdataPrAndel)) {
            return ingenEndring(periode);
        }

        var graderingsdataForSpesialTyper = finnDataForSpesialtyper(graderingsdataPrAndel);

        var harKunTyperUtenUtbetaling = graderingsdataForSpesialTyper.stream()
                .allMatch(g -> g.forrigeUtbetalingsgrad() == null || g.forrigeAndelsmessigFørGradering() == null);

        if (harKunTyperUtenUtbetaling) {
            return lagListeUtenReduksjon(periode);
        }

        return lagListeForDelvisReduksjon(periode, ytelsespesifiktGrunnlag, graderingsdataForSpesialTyper);
    }

    private static Set<GraderingsdataPrAndel> finnDataForSpesialtyper(List<GraderingsdataPrAndel> graderingsdataPrAndel) {
        return graderingsdataPrAndel.stream()
                .filter(a -> !a.tilkommetAktivitet())
                .filter(g -> SPESIALTYPER_FRA_UTTAK.contains(g.uttakArbeidType())).collect(Collectors.toSet());
    }

    private static boolean harEndringerSomPåvirkerUtbetaling(List<GraderingsdataPrAndel> graderingsdataPrAndel) {
        return graderingsdataPrAndel.stream()
                .anyMatch(g -> !g.tilkommetAktivitet() && (g.ikkeNokData() || harRelevantEndringIGradering(g)));
    }

    private static boolean skalGraderesFullt(BeregningsgrunnlagPeriodeDto periode) {
        return !periode.getPeriode().getFomDato().isBefore(TilkommetInntektPeriodeTjeneste.FOM_DATO_GRADERING_MOT_INNTEKT);
    }

    private static boolean harIkkeFastsattForrigePeriode(BeregningsgrunnlagPeriodeDto forrigePeriode) {
        return forrigePeriode.getDagsats() == null;
    }

    private static List<TilkommetInntektDto> ingenEndring(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getTilkomneInntekter();
    }

    private static List<TilkommetInntektDto> lagListeForDelvisReduksjon(BeregningsgrunnlagPeriodeDto periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, Set<GraderingsdataPrAndel> graderingsdataForSpesialTyper) {
        var totalDifferanse = finnTotalDifferanse(graderingsdataForSpesialTyper);

        if (totalDifferanse.compareTo(BigDecimal.ZERO) > 0) {
            return ingenEndring(periode);
        }

        var tilkomneInntekter = periode.getTilkomneInntekter();
        var antallTilkomne = tilkomneInntekter.size();
        var tilkommetPrAktivitet = totalDifferanse.divide(BigDecimal.valueOf(antallTilkomne), 10, RoundingMode.HALF_UP);
        if (tilkommetPrAktivitet.compareTo(BigDecimal.ZERO) == 0) {
            return lagListeUtenReduksjon(periode);
        }
        return tilkomneInntekter.stream().map(it -> mapTilDelvisReduksjon(periode, ytelsespesifiktGrunnlag, tilkommetPrAktivitet, it)).toList();
    }

    private static BigDecimal finnTotalDifferanse(Set<GraderingsdataPrAndel> graderingsdataForSpesialTyper) {
        return graderingsdataForSpesialTyper.stream()
                .map(g -> (g.utbetalingsgrad().subtract(g.forrigeUtbetalingsgrad()))
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                        .multiply(g.forrigeAndelsmessigFørGradering()))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static TilkommetInntektDto mapTilDelvisReduksjon(BeregningsgrunnlagPeriodeDto periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BigDecimal tilkommetPrAktivitet, TilkommetInntektDto it) {
        return new TilkommetInntektDto(it.getAktivitetStatus(),
                it.getArbeidsgiver().orElse(null),
                it.getArbeidsforholdRef(),
                finnBruttoFraTilkommet(it, tilkommetPrAktivitet, periode.getPeriode(), ytelsespesifiktGrunnlag),
                tilkommetPrAktivitet,
                true
        );
    }

    private static BigDecimal finnBruttoFraTilkommet(TilkommetInntektDto it, BigDecimal tilkommetPrAktivitet, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var utbetalingsgrad = finnUtbetalingsgradForTilkommetInntekt(it, periode, ytelsespesifiktGrunnlag);
        return tilkommetPrAktivitet.divide(BigDecimal.valueOf(100).subtract(utbetalingsgrad), 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private static BigDecimal finnUtbetalingsgradForTilkommetInntekt(TilkommetInntektDto it, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (it.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)) {
            return UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(it.getArbeidsgiver().orElseThrow(), it.getArbeidsforholdRef(), periode, ytelsespesifiktGrunnlag, false);
        } else {
            return UtbetalingsgradTjeneste.finnUtbetalingsgradForStatus(it.getAktivitetStatus(), periode, ytelsespesifiktGrunnlag);
        }
    }

    private static List<TilkommetInntektDto> lagListeUtenReduksjon(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getTilkomneInntekter().stream()
                .map(it -> new TilkommetInntektDto(
                        it.getAktivitetStatus(),
                        it.getArbeidsgiver().orElse(null),
                        it.getArbeidsforholdRef(),
                        null,
                        null, false))
                .toList();
    }

    private static boolean harRelevantEndringIGradering(GraderingsdataPrAndel g) {
        if (g.uttakArbeidType() != null && SPESIALTYPER_FRA_UTTAK.contains(g.uttakArbeidType())) {
            return false;
        }
        if (g.forrigeUtbetalingsgrad() == null) {
            // Dette kan skje i tre tilfeller
            // 1. andelen avkortes til 0 før gradering
            // 2. Brutto er satt til 0 ved fordeling eller skjønnsfastsetting
            // 3. Beregnet for andelen er 0
            // Alle disse 3 er ok (antar at det fordeles likt)
            return false;
        }
        if (g.utbetalingsgrad() == null) {
            return g.forrigeUtbetalingsgrad().compareTo(BigDecimal.ZERO) == 0;
        }
        return g.utbetalingsgrad().compareTo(g.forrigeUtbetalingsgrad()) != 0;

    }

    private static List<GraderingsdataPrAndel> finnGraderingsdataPrAndel(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagPeriodeDto forrigePeriode, UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag) {
        List<GraderingsdataPrAndel> utbetalingsgraderPrAndel = new ArrayList<>();
        for (BeregningsgrunnlagPrStatusOgAndelDto a : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            var beregnetPrÅr = a.getBeregnetPrÅr();
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelFraForrige = finnAndelFraForrige(forrigePeriode, a);
            if (andelFraForrige.isEmpty()) {
                utbetalingsgraderPrAndel.add(tilkommet());
                continue;
            }

            var forrigeBeregnetPrÅr = andelFraForrige.get().getBeregnetPrÅr();
            if (forrigeBeregnetPrÅr.compareTo(beregnetPrÅr) != 0) {
                utbetalingsgraderPrAndel.add(ikkeNokData());
                continue;
            }

            var avkortetFørGraderingPrÅr = andelFraForrige.get().getAvkortetFørGraderingPrÅr();
            if (avkortetFørGraderingPrÅr == null) {
                utbetalingsgraderPrAndel.add(ikkeNokData());
                continue;
            }

            Optional<BigDecimal> forrigeUtbetalingsgrad = beregnForrigeUtbetalingsgrad(andelFraForrige.get(), avkortetFørGraderingPrÅr);
            var utbetalingsgraderMedOverlapp = finnUtbetalingsgrad(periode, a, ytelsespesifiktGrunnlag);
            utbetalingsgraderMedOverlapp.ifPresentOrElse(
                    gradering -> utbetalingsgraderPrAndel.add(utledGraderingsdataMedUtbetalingsgradForAndel(andelFraForrige.get(), forrigeUtbetalingsgrad, gradering)),
                    () -> utbetalingsgraderPrAndel.add(lagGraderingsdataUtenUtbetalingsgradForAndel(andelFraForrige.get(), forrigeUtbetalingsgrad))
            );

        }
        return utbetalingsgraderPrAndel;
    }

    private static GraderingsdataPrAndel lagGraderingsdataUtenUtbetalingsgradForAndel(BeregningsgrunnlagPrStatusOgAndelDto andelFraForrige, Optional<BigDecimal> forrigeUtbetalingsgrad) {
        return new GraderingsdataPrAndel(
                false, false,
                andelFraForrige.getAvkortetFørGraderingPrÅr(),
                null,
                null,
                forrigeUtbetalingsgrad.orElse(null));
    }

    private static GraderingsdataPrAndel utledGraderingsdataMedUtbetalingsgradForAndel(BeregningsgrunnlagPrStatusOgAndelDto andelFraForrige, Optional<BigDecimal> forrigeUtbetalingsgrad, UtbetalingsgradPrAktivitetDto gradering) {
        var uttakArbeidType = gradering.getUtbetalingsgradArbeidsforhold().getUttakArbeidType();
        var utbetalingsgrad = finnUtbetalingsgradFraPerioder(gradering);
        return new GraderingsdataPrAndel(
                false, false,
                andelFraForrige.getAvkortetFørGraderingPrÅr(),
                uttakArbeidType, utbetalingsgrad,
                forrigeUtbetalingsgrad.orElse(null));
    }

    private static BigDecimal finnUtbetalingsgradFraPerioder(UtbetalingsgradPrAktivitetDto gradering) {
        return gradering.getPeriodeMedUtbetalingsgrad()
                .stream()
                .filter(p -> p.getPeriode().overlapper(p.getPeriode()))
                .findFirst()
                .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad).orElse(BigDecimal.ZERO);
    }

    private static Optional<BigDecimal> beregnForrigeUtbetalingsgrad(BeregningsgrunnlagPrStatusOgAndelDto andelFraForrige, BigDecimal avkortetFørGraderingPrÅr) {
        return avkortetFørGraderingPrÅr.compareTo(BigDecimal.ZERO) > 0 ?
                Optional.of(andelFraForrige.getAvkortetPrÅr()
                        .divide(avkortetFørGraderingPrÅr, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))) :
                Optional.empty();
    }

    private static GraderingsdataPrAndel ikkeNokData() {
        return new GraderingsdataPrAndel(
                false, true,
                null,
                null,
                null,
                null);
    }

    private static GraderingsdataPrAndel tilkommet() {
        return new GraderingsdataPrAndel(
                true,
                false,
                null,
                null,
                null,
                null);
    }


    private static Optional<UtbetalingsgradPrAktivitetDto> finnUtbetalingsgrad(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagPrStatusOgAndelDto a, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        if (a.getBgAndelArbeidsforhold().isPresent()) {
            var utbetalingsgradForArbeid = finnPerioderForArbeid(
                    utbetalingsgradGrunnlag,
                    a.getBgAndelArbeidsforhold().get().getArbeidsgiver(),
                    a.getBgAndelArbeidsforhold().get().getArbeidsforholdRef(),
                    false);
            return finnFørsteOverlappende(periode, utbetalingsgradForArbeid);
        } else {
            var utbetalingsgradForStatus = finnPerioderForStatus(
                    a.getAktivitetStatus(),
                    utbetalingsgradGrunnlag);
            return finnEnesteOverlappende(periode, utbetalingsgradForStatus);
        }
    }

    private static Optional<UtbetalingsgradPrAktivitetDto> finnFørsteOverlappende(BeregningsgrunnlagPeriodeDto periode, List<UtbetalingsgradPrAktivitetDto> utbetalingsgradForArbeid) {
        return utbetalingsgradForArbeid.stream()
                .filter(it -> it.getPeriodeMedUtbetalingsgrad().stream()
                        .anyMatch(p -> p.getPeriode().overlapper(periode.getPeriode()))).findFirst();
    }

    private static Optional<UtbetalingsgradPrAktivitetDto> finnEnesteOverlappende(BeregningsgrunnlagPeriodeDto periode, Optional<UtbetalingsgradPrAktivitetDto> utbetalingsgradForStatus) {
        return utbetalingsgradForStatus.stream()
                .filter(it -> it.getPeriodeMedUtbetalingsgrad().stream()
                        .anyMatch(p -> p.getPeriode().overlapper(periode.getPeriode()))).findFirst();
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelFraForrige(BeregningsgrunnlagPeriodeDto forrigePeriode, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return forrigePeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(it -> a.equals(it) ||
                (a.getGjeldendeInntektskategori() == null && a.matchUtenInntektskategori(it))).findFirst();
    }

    private record GraderingsdataPrAndel(
            boolean tilkommetAktivitet,
            boolean ikkeNokData,
            BigDecimal forrigeAndelsmessigFørGradering,
            UttakArbeidType uttakArbeidType,
            BigDecimal utbetalingsgrad,
            BigDecimal forrigeUtbetalingsgrad) {

        @Override
        public String toString() {
            return "GraderingsdataPrAndel{" +
                    "tilkommetAktivitet=" + tilkommetAktivitet +
                    ", ikkeNokData=" + ikkeNokData +
                    ", forrigeAndelsmessigFørGradering=" + forrigeAndelsmessigFørGradering +
                    ", uttakArbeidType=" + uttakArbeidType +
                    ", utbetalingsgrad=" + utbetalingsgrad +
                    ", forrigeUtbetalingsgrad=" + forrigeUtbetalingsgrad +
                    '}';
        }
    }
}
