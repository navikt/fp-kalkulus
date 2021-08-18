package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class VurderTilbaketrekkDto {

    private Boolean hindreTilbaketrekk;

    public VurderTilbaketrekkDto(boolean hindreTilbaketrekk) {
        this.hindreTilbaketrekk = hindreTilbaketrekk;
    }

    public void setHindreTilbaketrekk(Boolean hindreTilbaketrekk) {
        this.hindreTilbaketrekk = hindreTilbaketrekk;
    }


    public boolean skalHindreTilbaketrekk() {
        return hindreTilbaketrekk;
    }
}
