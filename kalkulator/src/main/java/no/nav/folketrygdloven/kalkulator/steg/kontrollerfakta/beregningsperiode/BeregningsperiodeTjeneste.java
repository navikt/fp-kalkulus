package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.beregningsperiode;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.BevegeligeHelligdagerUtil;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

@ApplicationScoped
@FagsakYtelseTypeRef("*")
public class BeregningsperiodeTjeneste {

    public static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";

    public Intervall fastsettBeregningsperiodeForATFLAndeler(LocalDate skjæringstidspunkt) {
        LocalDate fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return Intervall.fraOgMedTilOgMed(fom, tom);
    }

    public Intervall fastsettBeregningsperiodeForSNAndeler(LocalDate skjæringstidspunkt) {
        LocalDate fom = skjæringstidspunkt.minusYears(3).withDayOfMonth(1);
        LocalDate tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return Intervall.fraOgMedTilOgMed(fom, tom);
    }

    public static Optional<LocalDate> skalVentePåInnrapporteringAvInntekt(BeregningsgrunnlagInput input,
                                                                          List<Arbeidsgiver> arbeidsgivere,
                                                                          LocalDate dagensDato,
                                                                          BeregningAktivitetAggregatDto aktivitetAggregatDto) {
        if (!harAktivitetStatuserSomKanSettesPåVent(aktivitetAggregatDto, input.getSkjæringstidspunktForBeregning())) {
            return Optional.empty();
        }
        LocalDate beregningsperiodeTom = hentBeregningsperiodeTomForATFL(input.getSkjæringstidspunktForBeregning());
        LocalDate originalFrist = beregningsperiodeTom.plusDays((Integer) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO));
        LocalDate fristMedHelligdagerInkl = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(originalFrist);
        if (dagensDato.isAfter(fristMedHelligdagerInkl)) {
            return Optional.empty();
        }

        return harMottattInntektsmeldingForAlleArbeidsforhold(
                input.getSkjæringstidspunktForBeregning(),
                aktivitetAggregatDto,
                arbeidsgivere)
                ? Optional.empty()
                : Optional.of(utledBehandlingPåVentFrist(input));
    }

    private static boolean harAktivitetStatuserSomKanSettesPåVent(BeregningAktivitetAggregatDto aktivitetAggregatDto, LocalDate skjæringstidspunkt) {
        return aktivitetAggregatDto.getBeregningAktiviteter().stream()
                .filter(ba -> ba.getPeriode().inkluderer(skjæringstidspunkt))
                .map(BeregningAktivitetDto::getOpptjeningAktivitetType)
                .anyMatch(type -> type.equals(OpptjeningAktivitetType.ARBEID) || type.equals(OpptjeningAktivitetType.FRILANS));
    }

    private static LocalDate utledBehandlingPåVentFrist(BeregningsgrunnlagInput input) {
        LocalDate beregningsperiodeTom = hentBeregningsperiodeTomForATFL(input.getSkjæringstidspunktForBeregning());
        LocalDate frist = beregningsperiodeTom.plusDays((Integer) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO));
        return BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(frist).plusDays(1);
    }

    private static boolean harMottattInntektsmeldingForAlleArbeidsforhold(LocalDate skjæringstidspunkt,
                                                                          BeregningAktivitetAggregatDto aktivitetAggregatDto,
                                                                          List<Arbeidsgiver> arbeidsgivere) {
        boolean erFrilanser = aktivitetAggregatDto.getAktiviteterPåDato(skjæringstidspunkt).stream()
                .map(BeregningAktivitetDto::getOpptjeningAktivitetType)
                .anyMatch(t -> t.equals(OpptjeningAktivitetType.FRILANS));
        if (erFrilanser) {
            return false;
        }
        return alleArbeidsforholdHarInntektsmelding(aktivitetAggregatDto, arbeidsgivere, skjæringstidspunkt);
    }

    private static LocalDate hentBeregningsperiodeTomForATFL(LocalDate skjæringstidspunkt) {
        return skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    }

    private static boolean alleArbeidsforholdHarInntektsmelding(BeregningAktivitetAggregatDto aktivitetAggregatDto,
                                                                List<Arbeidsgiver> arbeidsgivere,
                                                                LocalDate skjæringstidspunktBeregning) {
        return hentAlleArbeidsgiverePåSkjæringstidspunktet(aktivitetAggregatDto, skjæringstidspunktBeregning)
                .filter(arbeidsgiver -> !OrgNummer.erKunstig(arbeidsgiver.getOrgnr())) //Arbeidsforhold er ikke lagt til av saksbehandler
                .allMatch(arbeidsgiver -> arbeidsgivere
                        .stream()
                        .anyMatch(v -> v.equals(arbeidsgiver)));
    }

    private static Stream<Arbeidsgiver> hentAlleArbeidsgiverePåSkjæringstidspunktet(BeregningAktivitetAggregatDto aktivitetAggregatDto, LocalDate skjæringstidspunktBeregning) {
        return aktivitetAggregatDto.getAktiviteterPåDato(skjæringstidspunktBeregning)
                .stream()
                .map(BeregningAktivitetDto::getArbeidsgiver)
                .filter(Objects::nonNull);
    }


}
