package no.nav.folketrygdloven.fpkalkulus.kontrakt.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BeregningRefusjonOverstyringerMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private List<BeregningRefusjonOverstyringMigreringDto> overstyringer;

    public BeregningRefusjonOverstyringerMigreringDto(List<BeregningRefusjonOverstyringMigreringDto> overstyringer) {
        this.overstyringer = overstyringer;
    }

    public List<BeregningRefusjonOverstyringMigreringDto> getOverstyringer() {
        return overstyringer;
    }
}
