package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VurderATogFLiSammeOrganisasjonDto {

    @JsonProperty("vurderATogFLiSammeOrganisasjonAndelListe")
    @Valid
    @NotNull
    private List<VurderATogFLiSammeOrganisasjonAndelDto> vurderATogFLiSammeOrganisasjonAndelListe;

    public VurderATogFLiSammeOrganisasjonDto(@Valid @NotNull List<VurderATogFLiSammeOrganisasjonAndelDto> vurderATogFLiSammeOrganisasjonAndelListe) {
        this.vurderATogFLiSammeOrganisasjonAndelListe = vurderATogFLiSammeOrganisasjonAndelListe;
    }

    public List<VurderATogFLiSammeOrganisasjonAndelDto> getVurderATogFLiSammeOrganisasjonAndelListe() {
        return vurderATogFLiSammeOrganisasjonAndelListe;
    }

}
