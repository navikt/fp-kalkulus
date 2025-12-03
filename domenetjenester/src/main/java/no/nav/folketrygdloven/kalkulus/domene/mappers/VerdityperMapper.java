package no.nav.folketrygdloven.kalkulus.domene.mappers;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp;

/*
 * Mapping mellom kalklulator/modell/typer og kontrakter/felles
 */
public class VerdityperMapper {
    public static no.nav.folketrygdloven.kalkulator.modell.typer.Beløp beløpFraDao(Beløp beløp) {
        return no.nav.folketrygdloven.kalkulator.modell.typer.Beløp.fra(beløp != null ? beløp.getVerdi() : null);
    }

    public static Beløp beløpTilDao(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp beløp) {
        return beløp != null && beløp.verdi() != null ? new Beløp(beløp.verdi()) : null;
    }


}
