package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BesteberegningFødendeKvinneDto {

    @JsonProperty("besteberegningAndelListe")
    @Valid
    @NotNull
    private List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe;

    @JsonProperty("nyDagpengeAndel")
    @Valid
    private DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel;

    public BesteberegningFødendeKvinneDto(@Valid @NotNull List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe, @Valid DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel) {
        this.besteberegningAndelListe = besteberegningAndelListe;
        this.nyDagpengeAndel = nyDagpengeAndel;
    }

    public List<BesteberegningFødendeKvinneAndelDto> getBesteberegningAndelListe() {
        return besteberegningAndelListe;
    }

    public void setBesteberegningAndelListe(List<BesteberegningFødendeKvinneAndelDto> besteberegningAndelListe) {
        this.besteberegningAndelListe = besteberegningAndelListe;
    }

    public DagpengeAndelLagtTilBesteberegningDto getNyDagpengeAndel() {
        return nyDagpengeAndel;
    }
}
