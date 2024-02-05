package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.ytelse.k14.fp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

/**
 * Tjeneste som brukes i beregning av foreldrepenger for å se om søker har aktiviteter som må avklares manuelt av saksbehandler
 * Ikke relevant for beregning av andre ytelser da kun foreldrepenger tar AAP med i beregningen
 */
public class AvklarAktiviteterTjeneste {

    private AvklarAktiviteterTjeneste() {
        // Skjul meg
    }

    public static boolean skalAvklareAktiviteter(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                 Optional<AktørYtelseDto> aktørYtelse) {
        return harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat)
                || harFullAAPITilleggTilAnnenAktivitet(beregningAktivitetAggregat, aktørYtelse);
    }

    public static boolean harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        List<BeregningAktivitetDto> relevanteAktiviteter = beregningAktivitetAggregat.getBeregningAktiviteter();
        LocalDate skjæringstidspunkt = beregningAktivitetAggregat.getSkjæringstidspunktOpptjening();
        List<BeregningAktivitetDto> aktiviteterPåStp = relevanteAktiviteter.stream()
                .filter(opptjeningsperiode -> opptjeningsperiode.getPeriode().getFomDato().isBefore(skjæringstidspunkt))
                .filter(opptjeningsperiode -> !opptjeningsperiode.getPeriode().getTomDato().isBefore(skjæringstidspunkt))
                .toList();
        return aktiviteterPåStp.stream()
                .anyMatch(aktivitet -> aktivitet.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.VENTELØNN_VARTPENGER));
    }

    public static boolean harFullAAPITilleggTilAnnenAktivitet(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                              Optional<AktørYtelseDto> aktørYtelse) {
        LocalDate skjæringstidspunkt = beregningAktivitetAggregat.getSkjæringstidspunktOpptjening();
        List<OpptjeningAktivitetType> opptjeningsaktivitetTyper = beregningAktivitetAggregat.getAktiviteterPåDato(skjæringstidspunkt).stream()
                .map(BeregningAktivitetDto::getOpptjeningAktivitetType).collect(Collectors.toList());
        if (opptjeningsaktivitetTyper.stream().noneMatch(type -> type.equals(OpptjeningAktivitetType.ARBEIDSAVKLARING))) {
            return false;
        }
        if (beregningAktivitetAggregat.getAktiviteterPåDato(skjæringstidspunkt).size() <= 1) {
            return false;
        }
        return hentUtbetalingsprosent(aktørYtelse, skjæringstidspunkt, YtelseType.ARBEIDSAVKLARINGSPENGER)
                .filter(verdi -> verdi.compareTo(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG) == 0)
                .isPresent();
    }

    private static Optional<BigDecimal> hentUtbetalingsprosent(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt, YtelseType ytelseTypeForMeldekort) {
        var ytelseFilter = new YtelseFilterDto(aktørYtelse).før(skjæringstidspunkt);

        Optional<YtelseDto> nyligsteVedtak = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(ytelseTypeForMeldekort));
        if (nyligsteVedtak.isEmpty()) {
            return Optional.empty();
        }
        if (KonfigurasjonVerdi.get("MELDEKORT_DELVIS_PERIODE", false)) {
            var meldekort = MeldekortUtils.finnSisteHeleMeldekortFørStpMedJustertPeriode(ytelseFilter,
                    skjæringstidspunkt,
                    Set.of(ytelseTypeForMeldekort)
            );
            return meldekort.map(MeldekortUtils.Meldekort::utbetalingsfaktor).map(f -> f.multiply(BigDecimal.valueOf(200)));
        } else {
            Optional<YtelseAnvistDto> nyligsteMeldekort = MeldekortUtils.sisteHeleMeldekortFørStp(ytelseFilter, nyligsteVedtak.get(), skjæringstidspunkt, Set.of(ytelseTypeForMeldekort), FagsakYtelseType.FORELDREPENGER);
            return Optional.of(nyligsteMeldekort.flatMap(YtelseAnvistDto::getUtbetalingsgradProsent).map(Stillingsprosent::getVerdi).orElse(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG));
        }
    }

}
