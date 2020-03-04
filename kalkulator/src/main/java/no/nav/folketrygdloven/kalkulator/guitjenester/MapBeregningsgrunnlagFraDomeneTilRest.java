package no.nav.folketrygdloven.kalkulator.guitjenester;


import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;

/**
 * Klasse for å mappe fra rest-dto til domene-dto. Denne mappingen vil fjerne informasjon som finnes i rest-dto, men som ikke har tilsvarende felter i domene-dto.
 */
public class MapBeregningsgrunnlagFraDomeneTilRest {


    public static ArbeidsgiverMedNavn mapArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet()) {
            return ArbeidsgiverMedNavn.virksomhet(arbeidsgiver.getOrgnr());
        }
        return ArbeidsgiverMedNavn.person(arbeidsgiver.getAktørId());
    }
}
