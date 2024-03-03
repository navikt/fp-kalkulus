package no.nav.folketrygdloven.kalkulus.mapTilKontrakt;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.response.v1.Grunnbeløp;

public class MapBeregningSats {

    private MapBeregningSats() {
    }

    public static Grunnbeløp map(BeregningSats grunnbeløp) {
        return new Grunnbeløp(ModellTyperMapper.beløpTilDto(Beløp.fra(grunnbeløp.getVerdi())),
                new Periode(grunnbeløp.getPeriode().getFomDato(), grunnbeløp.getPeriode().getTomDato()));
    }
}
