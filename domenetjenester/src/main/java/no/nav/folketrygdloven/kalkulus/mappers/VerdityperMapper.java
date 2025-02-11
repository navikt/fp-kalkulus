package no.nav.folketrygdloven.kalkulus.mappers;

/*
 * Mapping mellom kalklulator/modell/typer og kontrakter/felles
 */
public class VerdityperMapper {
    public static no.nav.folketrygdloven.kalkulator.modell.typer.Beløp beløpFraDao(no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp beløp) {
        return no.nav.folketrygdloven.kalkulator.modell.typer.Beløp.fra(beløp != null ? beløp.getVerdi() : null);
    }

    public static no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp beløpTilDao(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp beløp) {
        return beløp != null && beløp.verdi() != null ? new no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Beløp(beløp.verdi()) : null;
    }


}
