package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

public class AvklarAktiviteterTjeneste {

    private AvklarAktiviteterTjeneste() {
        // Skjul meg
    }

    public static boolean skalAvklareAktiviteter(BeregningAktivitetAggregatDto beregningAktivitetAggregat, Optional<AktørYtelseDto> aktørYtelse, FagsakYtelseType fagsakYtelseType) {
        return harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat)
                || harFullAAPPåStpMedAndreAktiviteter(beregningAktivitetAggregat, aktørYtelse, fagsakYtelseType)
                || harFullDPPåStpMedAndreAktiviteter(beregningAktivitetAggregat, aktørYtelse, fagsakYtelseType);
    }

    public static boolean harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        List<BeregningAktivitetDto> relevanteAktiviteter = beregningAktivitetAggregat.getBeregningAktiviteter();
        LocalDate skjæringstidspunkt = beregningAktivitetAggregat.getSkjæringstidspunktOpptjening();
        List<BeregningAktivitetDto> aktiviteterPåStp = relevanteAktiviteter.stream()
                .filter(opptjeningsperiode -> opptjeningsperiode.getPeriode().getFomDato().isBefore(skjæringstidspunkt))
                .filter(opptjeningsperiode -> !opptjeningsperiode.getPeriode().getTomDato().isBefore(skjæringstidspunkt))
                .collect(Collectors.toList());
        return aktiviteterPåStp.stream()
                .anyMatch(aktivitet -> aktivitet.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.VENTELØNN_VARTPENGER));
    }

    public static boolean harFullAAPPåStpMedAndreAktiviteter(BeregningAktivitetAggregatDto beregningAktivitetAggregat, Optional<AktørYtelseDto> aktørYtelse, FagsakYtelseType fagsakYtelseType) {
        LocalDate skjæringstidspunkt = beregningAktivitetAggregat.getSkjæringstidspunktOpptjening();
        List<OpptjeningAktivitetType> opptjeningsaktivitetTyper = beregningAktivitetAggregat.getAktiviteterPåDato(skjæringstidspunkt).stream()
                .map(BeregningAktivitetDto::getOpptjeningAktivitetType).collect(Collectors.toList());
        if (opptjeningsaktivitetTyper.stream().noneMatch(type -> type.equals(OpptjeningAktivitetType.ARBEIDSAVKLARING))) {
            return false;
        }
        if (beregningAktivitetAggregat.getAktiviteterPåDato(skjæringstidspunkt).size() <= 1) {
            return false;
        }
        return hentUtbetalingsprosent(aktørYtelse, skjæringstidspunkt, fagsakYtelseType, FagsakYtelseType.ARBEIDSAVKLARINGSPENGER)
                .filter(verdi -> verdi.compareTo(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG) == 0)
                .isPresent();
    }

    public static boolean harFullDPPåStpMedAndreAktiviteter(BeregningAktivitetAggregatDto beregningAktivitetAggregat, Optional<AktørYtelseDto> aktørYtelse, FagsakYtelseType fagsakYtelseType) {
        LocalDate skjæringstidspunkt = beregningAktivitetAggregat.getSkjæringstidspunktOpptjening();
        List<OpptjeningAktivitetType> opptjeningsaktivitetTyper = beregningAktivitetAggregat.getAktiviteterPåDato(skjæringstidspunkt).stream()
                .map(BeregningAktivitetDto::getOpptjeningAktivitetType).collect(Collectors.toList());
        if (opptjeningsaktivitetTyper.stream().noneMatch(type -> type.equals(OpptjeningAktivitetType.DAGPENGER))) {
            return false;
        }
        if (beregningAktivitetAggregat.getAktiviteterPåDato(skjæringstidspunkt).size() <= 1) {
            return false;
        }
        return hentUtbetalingsprosent(aktørYtelse, skjæringstidspunkt, fagsakYtelseType, FagsakYtelseType.DAGPENGER)
                .filter(verdi -> verdi.compareTo(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG) == 0)
                .isPresent();
    }

    private static Optional<BigDecimal> hentUtbetalingsprosent(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt, FagsakYtelseType ytelseUnderBehandling, FagsakYtelseType ytelseTypeForMeldekort) {
        var ytelseFilter = new YtelseFilterDto(aktørYtelse).før(skjæringstidspunkt);

        Optional<YtelseDto> nyligsteVedtak = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(ytelseTypeForMeldekort));
        if (nyligsteVedtak.isEmpty()) {
            return Optional.empty();
        }

        Optional<YtelseAnvistDto> nyligsteMeldekort = MeldekortUtils.sisteHeleMeldekortFørStp(ytelseFilter, nyligsteVedtak.get(), skjæringstidspunkt, Set.of(ytelseTypeForMeldekort), ytelseUnderBehandling);
        return Optional.of(nyligsteMeldekort.flatMap(YtelseAnvistDto::getUtbetalingsgradProsent).map(Stillingsprosent::getVerdi).orElse(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG));
    }

}
