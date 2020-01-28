package no.nav.folketrygdloven.kalkulator.kontrollerfakta;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;

public class KontrollerFaktaBeregningFrilanserTjeneste {

    private KontrollerFaktaBeregningFrilanserTjeneste() {
        // Skjul
    }

    public static boolean erNyoppstartetFrilanser(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        boolean erFrilanser = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser());

        return erFrilanser
            && iayGrunnlag.getOppgittOpptjening()
            .flatMap(OppgittOpptjeningDto::getFrilans)
            .map(OppgittFrilansDto::getErNyoppstartet)
            .orElse(false);
    }

    public static boolean erBrukerArbeidstakerOgFrilanserISammeOrganisasjon(AktørId aktørId, BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return !brukerErArbeidstakerOgFrilanserISammeOrganisasjon(aktørId, beregningsgrunnlag, iayGrunnlag).isEmpty();
    }

    public static Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon(AktørId aktørId, BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return arbeidsgivereSomHarFrilansforholdOgArbeidsforholdMedBruker(iayGrunnlag, beregningsgrunnlag, aktørId);
    }

    private static Set<Arbeidsgiver> arbeidsgivereSomHarFrilansforholdOgArbeidsforholdMedBruker(InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagDto beregningsgrunnlag, AktørId aktørId) {

        // Sjekk om statusliste inneholder AT og FL.

        if (beregningsgrunnlag.getBeregningsgrunnlagPerioder().isEmpty() ||
            !harFrilanserOgArbeidstakerAndeler(beregningsgrunnlag)) {
            return Collections.emptySet();
        }

        // Sjekk om samme orgnr finnes både som arbeidsgiver og frilansoppdragsgiver

        final Set<Arbeidsgiver> arbeidsforholdArbeidsgivere = finnArbeidsgivere(beregningsgrunnlag);
        if (arbeidsforholdArbeidsgivere.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<Arbeidsgiver> frilansOppdragsgivere = finnFrilansOppdragsgivere(aktørId, beregningsgrunnlag, iayGrunnlag);
        if (frilansOppdragsgivere.isEmpty()) {
            return Collections.emptySet();
        }
        return finnMatchendeArbeidsgiver(arbeidsforholdArbeidsgivere, frilansOppdragsgivere);
    }

    private static boolean harFrilanserOgArbeidstakerAndeler(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
        .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser()) &&
            beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .anyMatch(andel -> andel.getAktivitetStatus().erArbeidstaker());
    }

    private static Set<Arbeidsgiver> finnMatchendeArbeidsgiver(final Set<Arbeidsgiver> virksomheterForArbeidsforhold, final Set<Arbeidsgiver> frilansOppdragsgivere) {
        Set<Arbeidsgiver> intersection = new HashSet<>(virksomheterForArbeidsforhold);
        intersection.retainAll(frilansOppdragsgivere);
        return intersection;
    }

    private static Set<Arbeidsgiver> finnFrilansOppdragsgivere(AktørId aktørId, BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        boolean erFrilanser = beregningsgrunnlag.getAktivitetStatuser().stream()
            .map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus)
            .anyMatch(AktivitetStatus::erFrilanser);
        if (!erFrilanser) {
            return Collections.emptySet();
        }
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(aktørId)).før(beregningsgrunnlag.getSkjæringstidspunkt());

        return filter.getFrilansOppdrag()
            .stream()
            .map(YrkesaktivitetDto::getArbeidsgiver)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private static Set<Arbeidsgiver> finnArbeidsgivere(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bpsa -> AktivitetStatus.ARBEIDSTAKER.equals(bpsa.getAktivitetStatus()))
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(BGAndelArbeidsforholdDto::getArbeidsgiver)
            .collect(Collectors.toSet());
    }
}
