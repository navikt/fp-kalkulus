package no.nav.folketrygdloven.kalkulator.felles;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.konfig.Konfigverdier;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt.FordelTilkommetArbeidsforholdTjeneste;

public class HarYrkesaktivitetInnsendtRefusjonForSent {

    /**
     * Vurderer om refusjonskrav for en aktivitet isolert sett har komt inn for sent. Tar ikke hensyn til at andre aktiviteter for samme arbeidsgiver har refusjonskrav som har kommet inn i tide.
     *
     * Regelen for gyldig refusjonskrav sier at for et refusjonskrav som gjelder tilbake i tid er første gyldige dato for refusjon den første i måneden som ligger 3 måneder tilbake i tid.
     *
     * Eksempel:
     * Refusjonkrav kommer inn 15.01.2020 og kravet ber om refusjon fom 01.09.2019. Dette refusjonskravet er ugylig fordi første gyldige dato for dette kravet er 01.10.2019.
     *
     *
     * @param refusjonsdato Sammensatt objekt for refusjonskravdatoer
     * @param yrkesaktivitet aktuell yrkesaktivitet
     * @param gjeldendeAktiviteter gjeldende aktiviteter på skjæringstidspunktet
     * @param skjæringstidspunktBeregning skjæringstidspunkt for beregning
     * @return Om refusjonskrav for aktivitet har komt inn for sent eller ikke
     */
    public static boolean vurder(RefusjonskravDatoDto refusjonsdato, YrkesaktivitetDto yrkesaktivitet, BeregningAktivitetAggregatDto gjeldendeAktiviteter, LocalDate skjæringstidspunktBeregning) {
        LocalDate førsteLovligeDatoForRefusjon = finnFørsteGyldigeDatoMedRefusjon(refusjonsdato);
        LocalDate førsteDagMedRefusjon = finnFørsteDagMedSøktRefusjon(refusjonsdato, gjeldendeAktiviteter, skjæringstidspunktBeregning, yrkesaktivitet.getArbeidsforholdRef());
        return førsteLovligeDatoForRefusjon.isAfter(førsteDagMedRefusjon);
    }

    /**
     * Finner første dato det er søkt refusjon for f
     *
     * @param refusjonsdato Dto for datoer for innsendte krav
     * @param gjeldendeAktiviteter Alle gjeldende aktiviteter i beregning
     * @param skjæringstidspunktBeregning Skjæringstidspunkt for beregning
     * @param arbeidsforholdRef Arbeidsforholdreferanse
     * @return Første dag med søkt refusjon
     */
    public static LocalDate finnFørsteDagMedSøktRefusjon(RefusjonskravDatoDto refusjonsdato,
                                                         BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                         LocalDate skjæringstidspunktBeregning,
                                                         InternArbeidsforholdRefDto arbeidsforholdRef) {
        LocalDate førsteDagMedRefusjon = refusjonsdato.getFørsteDagMedRefusjonskrav().orElse(skjæringstidspunktBeregning);
        boolean erNyttArbeidsforhold = erTilkommetEtterBeregningstidspunkt(
                refusjonsdato.getArbeidsgiver(),
                arbeidsforholdRef,
                gjeldendeAktiviteter, skjæringstidspunktBeregning);
        boolean harRefusjonFraStart = refusjonsdato.harRefusjonFraStart();
        if (!erNyttArbeidsforhold && harRefusjonFraStart) {
            førsteDagMedRefusjon = skjæringstidspunktBeregning;
        }
        return førsteDagMedRefusjon;
    }

    /**
     * Finner første gyldige dato med refusjon.
     *
     * @param refusjonsdato Dto for datoer for innsendte krav
     * @return Første lovlige dato med refusjon på grunnlag av opplysninger tilgjengelig i register
     */
    public static LocalDate finnFørsteGyldigeDatoMedRefusjon(RefusjonskravDatoDto refusjonsdato) {
        int senesteGyldigeInnsendigsdatoForRefusjonskrav = Konfigverdier.FRIST_MÅNEDER_ETTER_REFUSJON + 1;
        return refusjonsdato.getFørsteInnsendingAvRefusjonskrav().minusMonths(senesteGyldigeInnsendigsdatoForRefusjonskrav - 1).withDayOfMonth(1);
    }

    private static boolean erTilkommetEtterBeregningstidspunkt(Arbeidsgiver arbeidsgiver,
                                                               InternArbeidsforholdRefDto arbeidsforholdRef,
                                                               BeregningAktivitetAggregatDto aktivitetAggregat,
                                                               LocalDate skjæringstidspunkt) {
        var beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
        return beregningAktiviteter.stream()
                .filter(beregningAktivitet -> erAktivPåBeregningstidspunkt(skjæringstidspunkt, beregningAktivitet))
                .noneMatch(beregningAktivitet -> arbeidsforholdRef.gjelderFor(beregningAktivitet.getArbeidsforholdRef()) && matcherArbeidsgiver(arbeidsgiver, beregningAktivitet));
    }

    private static boolean erAktivPåBeregningstidspunkt(LocalDate skjæringstidspunkt, BeregningAktivitetDto beregningAktivitet) {
        LocalDate beregningstidspunkt = BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunkt);
        return beregningAktivitet.getPeriode().inkluderer(beregningstidspunkt);
    }

    private static boolean matcherArbeidsgiver(Arbeidsgiver arbeidsgiver, BeregningAktivitetDto beregningAktivitet) {
        return Objects.equals(arbeidsgiver, beregningAktivitet.getArbeidsgiver());
    }

}
