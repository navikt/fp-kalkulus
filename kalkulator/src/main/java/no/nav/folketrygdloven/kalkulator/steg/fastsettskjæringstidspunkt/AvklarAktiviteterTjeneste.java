package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.BeregningUtils;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

public class AvklarAktiviteterTjeneste {

    private AvklarAktiviteterTjeneste() {
        // Skjul meg
    }

    public static boolean skalAvklareAktiviteter(BeregningsgrunnlagDto beregningsgrunnlag, BeregningAktivitetAggregatDto beregningAktivitetAggregat, Optional<AktørYtelseDto> aktørYtelse, FagsakYtelseType fagsakYtelseType) {
        return harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat)
                || harFullAAPPåStpMedAndreAktiviteter(beregningsgrunnlag, aktørYtelse, fagsakYtelseType)
                || harFullDPPåStpMedAndreAktiviteter(beregningsgrunnlag, aktørYtelse, fagsakYtelseType);
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

    public static boolean harFullAAPPåStpMedAndreAktiviteter(BeregningsgrunnlagDto beregningsgrunnlag, Optional<AktørYtelseDto> aktørYtelse, FagsakYtelseType fagsakYtelseType) {
        List<BeregningsgrunnlagAktivitetStatusDto> aktivitetStatuser = beregningsgrunnlag.getAktivitetStatuser();
        if (aktivitetStatuser.stream().noneMatch(as -> as.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSAVKLARINGSPENGER))) {
            return false;
        }
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
        if (aktivitetStatuser.size() <= 1) {
            return false;
        }
        return hentUtbetalingsprosent(aktørYtelse, skjæringstidspunkt, fagsakYtelseType, FagsakYtelseType.ARBEIDSAVKLARINGSPENGER)
                .filter(verdi -> verdi.compareTo(BeregningUtils.MAX_UTBETALING_PROSENT_AAP_DAG) == 0)
                .isPresent();
    }

    public static boolean harFullDPPåStpMedAndreAktiviteter(BeregningsgrunnlagDto beregningsgrunnlag, Optional<AktørYtelseDto> aktørYtelse, FagsakYtelseType fagsakYtelseType) {
        List<BeregningsgrunnlagAktivitetStatusDto> aktivitetStatuser = beregningsgrunnlag.getAktivitetStatuser();
        if (aktivitetStatuser.stream().noneMatch(as -> as.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER))) {
            return false;
        }
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
        if (aktivitetStatuser.size() <= 1) {
            return false;
        }
        return hentUtbetalingsprosent(aktørYtelse, skjæringstidspunkt, fagsakYtelseType, FagsakYtelseType.DAGPENGER)
                .filter(verdi -> verdi.compareTo(BeregningUtils.MAX_UTBETALING_PROSENT_AAP_DAG) == 0)
                .isPresent();
    }

    private static Optional<BigDecimal> hentUtbetalingsprosent(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt, FagsakYtelseType ytelseUnderBehandling, FagsakYtelseType ytelseTypeForMeldekort) {
        var ytelseFilter = new YtelseFilterDto(aktørYtelse).før(skjæringstidspunkt);

        Optional<YtelseDto> nyligsteVedtak = BeregningUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(ytelseTypeForMeldekort));
        if (nyligsteVedtak.isEmpty()) {
            return Optional.empty();
        }

        Optional<YtelseAnvistDto> nyligsteMeldekort = BeregningUtils.sisteHeleMeldekortFørStp(ytelseFilter, nyligsteVedtak.get(), skjæringstidspunkt, Set.of(ytelseTypeForMeldekort), ytelseUnderBehandling);
        return Optional.of(nyligsteMeldekort.flatMap(YtelseAnvistDto::getUtbetalingsgradProsent).map(Stillingsprosent::getVerdi).orElse(BeregningUtils.MAX_UTBETALING_PROSENT_AAP_DAG));
    }

}
