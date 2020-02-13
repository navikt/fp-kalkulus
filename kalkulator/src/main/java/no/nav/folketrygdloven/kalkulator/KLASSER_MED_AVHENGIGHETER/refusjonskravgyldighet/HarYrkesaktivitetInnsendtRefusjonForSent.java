package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.refusjonskravgyldighet;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.fordeling.FordelTilkommetArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;

class HarYrkesaktivitetInnsendtRefusjonForSent {

    public static final int MAKS_FRIST_MND_ETTERSENDT_REFUSJON = 4;

    /**
     * Vurderer om refusjonskrav for en aktivitet isolert sett har komt inn for sent. Tar ikke hensyn til at andre aktiviteter for samme arbeidsgiver har refusjonskrav som har kommet inn i tide.
     *
     * Regelen for gyldig refusjonskrav sier at for et refusjonskrav som gjelder tilbake i tid er første gyldige dato for refusjon den første i måneden som ligger 3 måneder tilbake i tid.
     *
     * Eksempel:
     * Refusjonkrav kommer inn 15.01.2020 og kravet ber om refusjon fom 01.09.2019. Dette refusjonskravet er ugylig fordi første gyldige dato for dette kravet er 01.10.2019.
     *
     * @param refusjonsdato Sammensatt objekt for refusjonskravdatoer
     * @param yrkesaktivitet aktuell yrkesaktivitet
     * @param gjeldendeAktiviteter gjeldende aktiviteter på skjæringstidspunktet
     * @param skjæringstidspunktBeregning skjæringstidspunkt for beregning
     * @return Om refusjonskrav for aktivitet har komt inn for sent eller ikke
     */
    static boolean vurder(RefusjonskravDatoDto refusjonsdato, YrkesaktivitetDto yrkesaktivitet, BeregningAktivitetAggregatDto gjeldendeAktiviteter, LocalDate skjæringstidspunktBeregning) {
        boolean erNyttArbeidsforhold = FordelTilkommetArbeidsforholdTjeneste.erNyttArbeidsforhold(yrkesaktivitet, gjeldendeAktiviteter, skjæringstidspunktBeregning);
        LocalDate førsteDagMedRefusjon = refusjonsdato.getFørsteDagMedRefusjonskrav().orElse(skjæringstidspunktBeregning);
        boolean harRefusjonFraStart = refusjonsdato.harRefusjonFraStart();
        if (!erNyttArbeidsforhold && harRefusjonFraStart) {
            førsteDagMedRefusjon = skjæringstidspunktBeregning;
        }
        LocalDate førsteLovligeDatoForRefusjon = refusjonsdato.getFørsteInnsendingAvRefusjonskrav().minusMonths(MAKS_FRIST_MND_ETTERSENDT_REFUSJON - 1).withDayOfMonth(1);
        return førsteLovligeDatoForRefusjon.isAfter(førsteDagMedRefusjon);
    }

}
