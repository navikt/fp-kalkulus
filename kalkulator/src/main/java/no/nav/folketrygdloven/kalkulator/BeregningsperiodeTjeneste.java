package no.nav.folketrygdloven.kalkulator;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.BevegeligeHelligdagerUtil;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
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

    public static Optional<LocalDate> skalVentePåInnrapporteringAvInntekt(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag, List<Arbeidsgiver> arbeidsgivere, LocalDate dagensDato) {
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        if (!harAktivitetStatuserSomKanSettesPåVent(beregningsgrunnlag)) {
            return Optional.empty();
        }
        LocalDate beregningsperiodeTom = hentBeregningsperiodeTomForATFL(beregningsgrunnlag);
        LocalDate originalFrist = beregningsperiodeTom.plusDays((Integer) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO));
        LocalDate fristMedHelligdagerInkl = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(originalFrist);
        if (dagensDato.isAfter(fristMedHelligdagerInkl)) {
            return Optional.empty();
        }

        return harMottattInntektsmeldingForAlleArbeidsforhold(beregningsgrunnlag, arbeidsgivere)
                ? Optional.empty()
                : Optional.of(utledBehandlingPåVentFrist(input, beregningsgrunnlag));
    }

    private static boolean harAktivitetStatuserSomKanSettesPåVent(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getAktivitetStatus)
            .anyMatch(status -> status.erArbeidstaker() || status.erFrilanser());
    }

    private static LocalDate utledBehandlingPåVentFrist(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        LocalDate beregningsperiodeTom = hentBeregningsperiodeTomForATFL(beregningsgrunnlag);
        LocalDate frist = beregningsperiodeTom.plusDays((Integer) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO));
        return BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(frist).plusDays(1);
    }

    private static boolean harMottattInntektsmeldingForAlleArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag, List<Arbeidsgiver> arbeidsgivere) {
        boolean erFrilanser = beregningsgrunnlag.getAktivitetStatuser().stream()
            .map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus).anyMatch(AktivitetStatus::erFrilanser);
        if (erFrilanser) {
            return false;
        }
        return alleArbeidsforholdHarInntektsmelding(beregningsgrunnlag, arbeidsgivere);
    }

    private static LocalDate hentBeregningsperiodeTomForATFL(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getAktivitetStatus().erArbeidstaker() || andel.getAktivitetStatus().erFrilanser())
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregningsperiodeTom)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Beregningsperiode skal være satt for arbeidstaker- og frilansandeler"));
    }

    private static boolean alleArbeidsforholdHarInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag, List<Arbeidsgiver> arbeidsgivere) {
        return hentAlleArbeidsgiverePåGrunnlaget(beregningsgrunnlag)
            .filter(arbeidsgiver -> !OrgNummer.erKunstig(arbeidsgiver.getOrgnr())) //Arbeidsforhold er ikke lagt til av saksbehandler
            .allMatch(arbeidsgiver -> arbeidsgivere
                .stream()
                .anyMatch(v -> v.equals(arbeidsgiver)));
    }

    private static Stream<Arbeidsgiver> hentAlleArbeidsgiverePåGrunnlaget(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(BGAndelArbeidsforholdDto::getArbeidsgiver);
    }


}
