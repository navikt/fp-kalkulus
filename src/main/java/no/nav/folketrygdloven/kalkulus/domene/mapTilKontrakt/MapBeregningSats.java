package no.nav.folketrygdloven.kalkulus.domene.mapTilKontrakt;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.foreldrepenger.kalkulus.kontrakt.response.grunnbeløp.Grunnbeløp;
import no.nav.foreldrepenger.kalkulus.kontrakt.typer.Periode;

public class MapBeregningSats {

    private MapBeregningSats() {
    }

    public static Grunnbeløp map(BeregningSats grunnbeløp) {
        return new Grunnbeløp(beløpTilDto(Beløp.fra(grunnbeløp.getVerdi())),
                new Periode(grunnbeløp.getPeriode().getFomDato(), grunnbeløp.getPeriode().getTomDato()));
   }
    private static no.nav.foreldrepenger.kalkulus.kontrakt.typer.Beløp beløpTilDto(Beløp beløp) {
        return no.nav.foreldrepenger.kalkulus.kontrakt.typer.Beløp.fra(beløp != null ? beløp.verdi() : null);
    }

}
