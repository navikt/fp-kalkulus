package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

/**
 * For arenaytelser mapper vi alle meldekort om til inntektsperioder og bryr oss ikke om vedtakene
 */
public class MapArenaVedtakTilBesteberegningRegelmodell {
    private static final Logger LOG = LoggerFactory.getLogger(MapArenaVedtakTilBesteberegningRegelmodell.class);

    private MapArenaVedtakTilBesteberegningRegelmodell() {
        // Skjuler default
    }

    public static List<Periodeinntekt> lagInntektFraArenaYtelser(YtelseFilterDto ytelseFilter) {
        Collection<YtelseDto> arenaytelser = ytelseFilter.filter(ytelse -> ytelse.getRelatertYtelseType().erArenaytelse())
                .getFiltrertYtelser();
        loggMeldekortSomStarterFørVedtak(arenaytelser);
        List<YtelseAnvistDto> alleMeldekortFraArena = arenaytelser.stream()
                .map(YtelseDto::getYtelseAnvist)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return alleMeldekortFraArena.stream()
                .map(MapArenaVedtakTilBesteberegningRegelmodell::mapMeldekortTilInntekt)
                .collect(Collectors.toList());
    }

    private static Periodeinntekt mapMeldekortTilInntekt(YtelseAnvistDto mk) {
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP) // OBS: Utbetaling er eit eingangsbeløp og skjer ikkje daglig
                .medInntekt(mk.getBeløp().map(Beløp::getVerdi).orElse(BigDecimal.ZERO))
                .medPeriode(Periode.of(mk.getAnvistFOM(), mk.getAnvistTOM()))
                .build();
    }

    // Logger slike tilfeller for å se hvor mange av de vi har før vi bestemmer oss for å gjøre endringer
    private static void loggMeldekortSomStarterFørVedtak(Collection<YtelseDto> arenaytelser) {
        arenaytelser.forEach(vedtak -> {
            List<YtelseAnvistDto> meldekortSomStarterFørVedtak = vedtak.getYtelseAnvist().stream()
                    .filter(meldekort -> !vedtak.getPeriode().getFomDato().isBefore(meldekort.getAnvistFOM()))
                    .collect(Collectors.toList());
            meldekortSomStarterFørVedtak.forEach(mk -> {
                String msg = String.format("FT-684563: Meldekort starter før vedtak, meldekort har startdato %s mens tilhørende vedtak har startdato %s", mk.getAnvistFOM(), vedtak.getPeriode().getFomDato());
                LOG.info(msg);
            });
        });

    }

}
