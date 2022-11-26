package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;

/**
 * Ved nye inntektsforhold skal beregningsgrunnlaget graderes mot inntekt.
 *
 * Utleder her om det er potensielle nye inntektsforhold.
 *
 * Se https://navno.sharepoint.com/sites/fag-og-ytelser-arbeid-sykefravarsoppfolging-og-sykepenger/SitePages/%C2%A7-8-13-Graderte-sykepenger.aspx
 *
 */
class AvklaringsbehovUtlederNyttInntektsforhold {

    public static boolean skalVurdereNyttInntektsforhold(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                         InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                         YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                         List<Intervall> forlengelseperioder) {

        if (!(ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) || !KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false)) {
            return false;
        }

        var bg = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));

        var tilVurderingTjeneste = new PerioderTilVurderingTjeneste(forlengelseperioder, bg);
        var perioderSomSkalVurderes = bg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> tilVurderingTjeneste.erTilVurdering(p.getPeriode()))
                .collect(Collectors.toList());

        return perioderSomSkalVurderes.stream().anyMatch(p -> harNyttInntektsforhold(p, bg.getSkjæringstidspunkt(), iayGrunnlag, ytelsespesifiktGrunnlag));
    }

    private static boolean harNyttInntektsforhold(BeregningsgrunnlagPeriodeDto p,
                                                  LocalDate skjæringstidspunkt,
                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                  YtelsespesifiktGrunnlag utbetalingsgradGrunnlag) {
        var inntektsforholdSomErTilkommet = finnTilkomneInntektsforhold(p, skjæringstidspunkt, iayGrunnlag, utbetalingsgradGrunnlag);
        return !inntektsforholdSomErTilkommet.isEmpty();
    }

    private static Collection<StatusOgArbeidsgiver> finnTilkomneInntektsforhold(BeregningsgrunnlagPeriodeDto p,
                                                                                LocalDate skjæringstidspunkt,
                                                                                InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                YtelsespesifiktGrunnlag utbetalingsgradGrunnlag) {
        if (iayGrunnlag.getAktørArbeidFraRegister().isEmpty()) {
            return Collections.emptyList();
        }
        var yrkesaktiviteter = iayGrunnlag.getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter).orElse(Collections.emptyList());
        var andeler = p.getBeregningsgrunnlagPrStatusOgAndelList();
        var periode = p.getPeriode();
        return finnTilkomneInntektsforhold(skjæringstidspunkt, yrkesaktiviteter, andeler, periode, utbetalingsgradGrunnlag);
    }

    /** Bestemmer hvilke statuser/arbeidsgivere som skal regnes som nytt
     *
     * Dersom en inntekt/aktivitet regnes som nytt skal beregningsgrunnlaget graderes mot inntekt i denne perioden. Dette betyr at inntekt i tillegg til ytelsen kan føre til nedjustering av utbetalt ytelse.
     *
     * Et inntektsforhold regnes som nytt dersom:
     * - Den fører til at bruker har en ekstra inntekt i tillegg til det hen ville ha hatt om hen ikke mottok ytelse
     * - Inntekten ikke erstatter inntekt i et arbeidsforhold som er avsluttet
     * - Det ikke er fullt fravær i arbeidsforholdet/aktiviteten (har opprettholdt noe arbeid og dermed sannsynligvis inntekt)
     *
     * Vi antar bruker ville opprettholdt arbeid hos arbeidsgivere der bruker fortsatt er innregistrert i aareg, og at dette regner som en løpende aktivitet.
     * Dersom antall løpende aktiviteter øker, skal saksbehandler vurdere om de tilkomne aktivitetene skal føre til reduksjon i utbetaling.
     *
     *
     * @param skjæringstidspunkt skjæringstidspunkt
     * @param yrkesaktiviteter  yrkesaktiviteter
     * @param andeler Andeler
     * @param periode Beregningsgrunnlagperiode
     * @param utbetalingsgradGrunnlag Utbetalingsgradgrunnlag
     * @return Statuser/arbeidsgivere som skal regnes som tilkommet
     */
    protected static HashSet<StatusOgArbeidsgiver> finnTilkomneInntektsforhold(LocalDate skjæringstidspunkt,
                                                                               Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                               List<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                               Intervall periode,
                                                                               YtelsespesifiktGrunnlag utbetalingsgradGrunnlag) {

        var aktiviteterVedStart = andeler.stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .map(a -> new StatusOgArbeidsgiver(a.getAktivitetStatus(), a.getArbeidsgiver().orElse(null)))
                .collect(Collectors.toSet());
        var antallGodkjenteInntektsforhold = aktiviteterVedStart.size();

        var eksisterendeInntektsforhold = new HashSet<StatusOgArbeidsgiver>();
        var tilkommetInntektsforhold = new HashSet<StatusOgArbeidsgiver>();

        var sortertAndelsliste = andeler
                .stream()
                .sorted(ikkeTilkomneFørst(yrkesaktiviteter, skjæringstidspunkt)).collect(Collectors.toList());


        for (var andel : sortertAndelsliste) {
            var utbetalingsgrad = UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel(andel, periode, utbetalingsgradGrunnlag, true);
            var statusOgArbeidsgiver = new StatusOgArbeidsgiver(andel.getAktivitetStatus(), andel.getArbeidsgiver().orElse(null));
            if (andel.getArbeidsgiver().isPresent()) {
                if (erAnsattIPeriode(yrkesaktiviteter, andel, periode)) {
                    // Dersom vi har dekket opp "godkjente" inntektsforhold legges den til i lista av tilkomne
                    if (!harDekketOppEksisterendeInntektsforhold(antallGodkjenteInntektsforhold, eksisterendeInntektsforhold)) {
                        eksisterendeInntektsforhold.add(statusOgArbeidsgiver);
                    } else if (!eksisterendeInntektsforhold.contains(statusOgArbeidsgiver) && harIkkeFulltFravær(utbetalingsgrad)) {
                        tilkommetInntektsforhold.add(statusOgArbeidsgiver);
                    }
                }
            } else {
                if (!harDekketOppEksisterendeInntektsforhold(antallGodkjenteInntektsforhold, eksisterendeInntektsforhold)) {
                    eksisterendeInntektsforhold.add(statusOgArbeidsgiver);
                } else if (!eksisterendeInntektsforhold.contains(statusOgArbeidsgiver) && harIkkeFulltFravær(utbetalingsgrad)) {
                    tilkommetInntektsforhold.add(statusOgArbeidsgiver);
                }
            }
        }
        return tilkommetInntektsforhold;
    }

    private static boolean harDekketOppEksisterendeInntektsforhold(int antallAktiviteterSomIkkeSkalRegnesSomTilkommet, HashSet<StatusOgArbeidsgiver> aktiviteterSomIkkeSkalRegnesSomTilkommet) {
        return antallAktiviteterSomIkkeSkalRegnesSomTilkommet == aktiviteterSomIkkeSkalRegnesSomTilkommet.size();
    }

    private static boolean harIkkeFulltFravær(BigDecimal utbetalingsgrad) {
        return utbetalingsgrad.compareTo(BigDecimal.valueOf(100)) < 0;
    }

    private static boolean erAnsattIPeriode(Collection<YrkesaktivitetDto> yrkesaktiviteter, BeregningsgrunnlagPrStatusOgAndelDto andel, Intervall periode) {
        var ansettelsesPerioderHosArbeidsgiver = finnAnsattperiodeHosSammeArbeidsgiver(yrkesaktiviteter, andel);
        return ansettelsesPerioderHosArbeidsgiver.stream().anyMatch(ap -> ap.getPeriode().overlapper(periode));
    }

    private static List<AktivitetsAvtaleDto> finnAnsattperiodeHosSammeArbeidsgiver(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                   BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (andel.getArbeidsgiver().isEmpty()) {
            return Collections.emptyList();
        }
        return yrkesaktiviteter.stream()
                .filter(ya -> ya.getArbeidsgiver().equals(andel.getArbeidsgiver().get()))
                .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
                .toList();
    }

    private static Comparator<BeregningsgrunnlagPrStatusOgAndelDto> ikkeTilkomneFørst(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                      LocalDate stp) {
        return (a1, a2) -> {
            if (a1.getKilde().equals(AndelKilde.PROSESS_START)) {
                return a2.getKilde().equals(AndelKilde.PROSESS_START) ? 0 : -1;
            }
            if (a2.getKilde().equals(AndelKilde.PROSESS_START)) {
                return 1;
            }
            var ansattperioder1 = finnAnsattperiodeHosSammeArbeidsgiver(yrkesaktiviteter, a1);
            var ansattperioder2 = finnAnsattperiodeHosSammeArbeidsgiver(yrkesaktiviteter, a2);
            var førsteAnsattdato1 = finnFørsteAnsattdatoEtterStp(stp, ansattperioder1);
            var førsteAnsattdato2 = finnFørsteAnsattdatoEtterStp(stp, ansattperioder2);
            return førsteAnsattdato1.compareTo(førsteAnsattdato2);
        };
    }

    private static LocalDate finnFørsteAnsattdatoEtterStp(LocalDate stp, List<AktivitetsAvtaleDto> ansattperioder1) {
        return ansattperioder1.stream().map(AktivitetsAvtaleDto::getPeriode)
                .map(Intervall::getFomDato)
                .filter(fomDato -> fomDato.isAfter(stp))
                .min(Comparator.naturalOrder())
                .orElse(stp);
    }


    public record StatusOgArbeidsgiver(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StatusOgArbeidsgiver that = (StatusOgArbeidsgiver) o;
            return aktivitetStatus == that.aktivitetStatus && Objects.equals(arbeidsgiver, that.arbeidsgiver);
        }

        @Override
        public int hashCode() {
            return Objects.hash(aktivitetStatus, arbeidsgiver);
        }
    }

}
