package no.nav.folketrygdloven.kalkulator.rest.dto;

import java.util.ArrayList;
import java.util.List;

public class KunYtelseDto {

    private List<AndelMedBeløpDto> andeler = new ArrayList<>();
    private boolean fodendeKvinneMedDP;
    private Boolean erBesteberegning = null;

    public List<AndelMedBeløpDto> getAndeler() {
        return andeler;
    }

    public void setAndeler(List<AndelMedBeløpDto> andeler) {
        this.andeler = andeler;
    }

    public void leggTilAndel(AndelMedBeløpDto andel) {
        andeler.add(andel);
    }

    public boolean isFodendeKvinneMedDP() {
        return fodendeKvinneMedDP;
    }

    public void setFodendeKvinneMedDP(boolean fodendeKvinneMedDP) {
        this.fodendeKvinneMedDP = fodendeKvinneMedDP;
    }

    public Boolean getErBesteberegning() {
        return erBesteberegning;
    }

    public void setErBesteberegning(Boolean erBesteberegning) {
        this.erBesteberegning = erBesteberegning;
    }
}
