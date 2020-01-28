package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SvpTilretteleggingerDto {

    private List<SvpTilretteleggingDto> tilretteleggingListe = new ArrayList<>();

    List<SvpTilretteleggingDto> getTilretteleggingListe() {
        return Collections.unmodifiableList(tilretteleggingListe);
    }

    public static class Builder {

        private List<SvpTilretteleggingDto> tilretteleggingListe = new ArrayList<>();

        public SvpTilretteleggingerDto build() {
            SvpTilretteleggingerDto entitet = new SvpTilretteleggingerDto();
            for (SvpTilretteleggingDto tilrettelegging : this.tilretteleggingListe) {
                SvpTilretteleggingDto svpTilrettelegging = new SvpTilretteleggingDto(tilrettelegging);
                entitet.tilretteleggingListe.add(svpTilrettelegging);
            }
            return entitet;
        }

        public Builder medTilretteleggingListe(List<SvpTilretteleggingDto> tilretteleggingListe) {
            this.tilretteleggingListe = tilretteleggingListe;
            return this;
        }
    }
}
