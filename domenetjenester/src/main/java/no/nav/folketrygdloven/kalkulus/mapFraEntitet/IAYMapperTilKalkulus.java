package no.nav.folketrygdloven.kalkulus.mapFraEntitet;


import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class IAYMapperTilKalkulus {

    public static InternArbeidsforholdRefDto mapArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
        return InternArbeidsforholdRefDto.ref(arbeidsforholdRef.getReferanse());
    }

    public static Arbeidsgiver mapArbeidsgiver(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver arbeidsgiver) {
        return arbeidsgiver.getErVirksomhet() ? Arbeidsgiver.virksomhet(arbeidsgiver.getOrgnr()) :
            Arbeidsgiver.fra(new AktørId(arbeidsgiver.getAktørId().getId()));
    }
}
