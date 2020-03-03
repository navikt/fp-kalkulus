package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import java.util.List;

public class AvklarAktiviteterDto {

    private List<AktivitetTomDatoMappingDto> aktiviteterTomDatoMapping;

    public List<AktivitetTomDatoMappingDto> getAktiviteterTomDatoMapping() {
        return aktiviteterTomDatoMapping;
    }

    public void setAktiviteterTomDatoMapping(List<AktivitetTomDatoMappingDto> aktiviteterTomDatoMapping) {
        this.aktiviteterTomDatoMapping = aktiviteterTomDatoMapping;
    }
}
