package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import static no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType.ARBEIDSAVKLARINGSPENGER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType.DAGPENGER;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

/**
 * Vi kan få vedtak fra arena som overlapper med hverandre, slike skal vi slå sammen og
 * deres perioder og meldekort kombineres.
 * Vi kan også få meldekort som overskrider periode på vedtaket, i slike tilfeller
 * skal vedtaksperioden avgrense meldekortsperioden.
 */
public class MapArenaVedtakTilBesteberegningRegelmodell {

    private MapArenaVedtakTilBesteberegningRegelmodell() {
        // Skjuler default
    }

    public static List<Periodeinntekt> lagInntektFraArenaYtelser(YtelseFilterDto ytelseFilter) {
        List<Periodeinntekt> inntekter = slåSammenVedOverlapp(ytelseFilter.getAlleYtelser(), DAGPENGER).stream()
                .map(MapArenaVedtakTilBesteberegningRegelmodell::mapYtelseTilPeriodeinntekt)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        inntekter.addAll(slåSammenVedOverlapp(ytelseFilter.getAlleYtelser(), ARBEIDSAVKLARINGSPENGER).stream()
                .map(MapArenaVedtakTilBesteberegningRegelmodell::mapYtelseTilPeriodeinntekt)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
        return inntekter;
    }

    private static List<YtelseVedtak> slåSammenVedOverlapp(List<YtelseDto> ytelsevedtak, FagsakYtelseType ytelse) {
        List<YtelseVedtak> kombinertVedOverlapp = slåSammenOverlappendeVedtak(ytelsevedtak, ytelse);
        List<LocalDateSegment<YtelseVedtak>> segmenterUtenOverlapp = kombinertVedOverlapp.stream()
                .map(vedtak -> new LocalDateSegment<>(vedtak.periode().getFomDato(), vedtak.periode().getTomDato(), vedtak))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(segmenterUtenOverlapp).compress((v1, v2) -> true, MapArenaVedtakTilBesteberegningRegelmodell::slåSammenPåfølgendeVedtak)
                .toSegments()
                .stream()
                .map(LocalDateSegment::getValue)
                .collect(Collectors.toList());
    }

    private static List<YtelseVedtak> slåSammenOverlappendeVedtak(List<YtelseDto> ytelsevedtak, FagsakYtelseType ytelse) {
        List<YtelseDto> relevanteVedtak = ytelsevedtak.stream()
                .filter(yt -> ytelse.equals(yt.getRelatertYtelseType()))
                .collect(Collectors.toList());
        Set<LocalDate> alleVedtakFOM = relevanteVedtak.stream()
                .filter(yt -> ytelse.equals(yt.getRelatertYtelseType()))
                .sorted(Comparator.comparing(YtelseDto::getPeriode))
                .map(vedtak -> vedtak.getPeriode().getFomDato())
                .collect(Collectors.toSet());
        return slåSammenVedOverlapp(ytelse, relevanteVedtak, alleVedtakFOM);
    }

    private static LocalDateSegment<YtelseVedtak> slåSammenPåfølgendeVedtak(LocalDateInterval i,
                                                                            LocalDateSegment<YtelseVedtak> lhs,
                                                                            LocalDateSegment<YtelseVedtak> rhs) {
        Intervall periode = Intervall.fraOgMedTilOgMed(lhs.getValue().periode().getFomDato(), rhs.getValue().periode().getTomDato());
        ArrayList<YtelseAnvistDto> meldekort = new ArrayList<>(lhs.getValue().meldekort());
        meldekort.addAll(rhs.getValue().meldekort());
        return new LocalDateSegment<>(i, new YtelseVedtak(periode, lhs.getValue().ytelse(), meldekort));
    }

    private static List<YtelseVedtak> slåSammenVedOverlapp(FagsakYtelseType ytelse, List<YtelseDto> relevanteVedtak, Set<LocalDate> alleVedtakFOM) {
        // For hver FOM, finn alle vedtak som overlapper med denne dagen og slå de sammen
        return alleVedtakFOM.stream().map(fom -> slåSammenVedtakSomInkludererDato(ytelse, relevanteVedtak, fom)).collect(Collectors.toList());
    }

    private static YtelseVedtak slåSammenVedtakSomInkludererDato(FagsakYtelseType ytelse,
                                                                 List<YtelseDto> relevanteVedtak,
                                                                 LocalDate fom) {
        List<YtelseDto> alleOverlappendeVedtak = relevanteVedtak.stream()
                .filter(yv -> yv.getPeriode().inkluderer(fom))
                .collect(Collectors.toList());
        LocalDate sisteTOM = finnSisteTom(alleOverlappendeVedtak);
        List<YtelseAnvistDto> alleMeldekort = finnAlleMeldekort(alleOverlappendeVedtak);
        return new YtelseVedtak(Intervall.fraOgMedTilOgMed(fom, sisteTOM), ytelse, alleMeldekort);
    }

    private static List<YtelseAnvistDto> finnAlleMeldekort(List<YtelseDto> alleOverlappendeVedtak) {
        return alleOverlappendeVedtak.stream()
                .map(YtelseDto::getYtelseAnvist)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static LocalDate finnSisteTom(List<YtelseDto> alleOverlappendeVedtak) {
        return alleOverlappendeVedtak.stream()
                .map(yv -> yv.getPeriode().getTomDato())
                .max(Comparator.naturalOrder())
                .stream()
                .findFirst()
                .orElseThrow();
    }

    private static List<Periodeinntekt> mapYtelseTilPeriodeinntekt(YtelseVedtak yt) {
        validerMeldekort(yt);

        // Mapper alle ytelser til DP sidan man ikkje kan ha både AAP og DP på skjæringstidspunktet
        return yt.meldekort().stream()
                .map(meldekort -> Periodeinntekt.builder()
                        .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP) // OBS: Utbetaling er eit eingangsbeløp og skjer ikkje daglig
                        .medInntekt(meldekort.getBeløp().map(Beløp::getVerdi).orElse(BigDecimal.ZERO))
                        .medPeriode(utledMeldekortperiode(meldekort, yt.periode()))
                        .build())
                .collect(Collectors.toList());
    }

    private static Periode utledMeldekortperiode(YtelseAnvistDto meldekort, Intervall vedtaksperiode) {
        LocalDate fom = meldekort.getAnvistFOM().isBefore(vedtaksperiode.getFomDato()) ? vedtaksperiode.getFomDato() : meldekort.getAnvistFOM();
        LocalDate tom = meldekort.getAnvistTOM().isAfter(vedtaksperiode.getTomDato()) ? vedtaksperiode.getTomDato() : meldekort.getAnvistTOM();
        return Periode.of(fom, tom);
    }


    private static void validerMeldekort(YtelseVedtak yt) {
        var ugyldigMeldekort = yt.meldekort().stream()
                .filter(mk -> mk.getAnvistFOM().isAfter(yt.periode.getTomDato()))
                .findFirst();
        if (ugyldigMeldekort.isPresent()) {
            throw new IllegalStateException("Finnes meldekort med startdato " +
                    ugyldigMeldekort.get().getAnvistFOM() + " som er etter vedtaksperiodens sluttdato " + yt.periode().getTomDato());
        }

    }

    private static record YtelseVedtak(Intervall periode, FagsakYtelseType ytelse, Collection<YtelseAnvistDto> meldekort) {}
}
