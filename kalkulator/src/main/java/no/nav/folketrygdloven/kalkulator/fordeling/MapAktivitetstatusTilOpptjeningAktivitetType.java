package no.nav.folketrygdloven.kalkulator.fordeling;

import java.util.Map;

import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

class MapAktivitetstatusTilOpptjeningAktivitetType {

    private static final Map<AktivitetStatus, OpptjeningAktivitetType> MAP_OPPTJENING_AKTIVITET_TYPE = Map.of(
            AktivitetStatus.ARBEIDSTAKER, OpptjeningAktivitetType.ARBEID,
            AktivitetStatus.FRILANSER, OpptjeningAktivitetType.FRILANS,
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, OpptjeningAktivitetType.NÆRING,
            AktivitetStatus.MILITÆR_ELLER_SIVIL, OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
            AktivitetStatus.DAGPENGER, OpptjeningAktivitetType.DAGPENGER,
            AktivitetStatus.ARBEIDSAVKLARINGSPENGER, OpptjeningAktivitetType.ARBEIDSAVKLARING
            );



    static OpptjeningAktivitetType map(AktivitetStatus aktivitetStatus) {
        return MAP_OPPTJENING_AKTIVITET_TYPE.getOrDefault(aktivitetStatus, OpptjeningAktivitetType.UDEFINERT);
    }


}
