package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.regelmodell.grunnlag.inntekt.ReferanseType;

class MapArbeidsforholdFraRegelTilVL {
    private MapArbeidsforholdFraRegelTilVL() {
        // skjul private constructor
    }

    static Arbeidsgiver map(Arbeidsforhold af) {
        if (ReferanseType.AKTØR_ID.equals(af.getReferanseType())) {
            return Arbeidsgiver.person(new AktørId(af.getAktørId()));
        } else if (ReferanseType.ORG_NR.equals(af.getReferanseType())) {
            return Arbeidsgiver.virksomhet(af.getOrgnr());
        }
        return null;
    }
}
