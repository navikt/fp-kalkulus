package no.nav.foreldrepenger.kalkulus.kontrakt.response.håndtering;

import java.util.List;
import jakarta.validation.Valid;

public class OverstyrtRefusjonEndringDto {

    private List<@Valid RefusjonOverstyrtAndelEndringDto> refusjonOverstyrtAndelEndringer;

    public OverstyrtRefusjonEndringDto() {
    }

    public OverstyrtRefusjonEndringDto(List<@Valid RefusjonOverstyrtAndelEndringDto> refusjonOverstyrtAndelEndringer) {
        this.refusjonOverstyrtAndelEndringer = refusjonOverstyrtAndelEndringer;
    }

    public List<RefusjonOverstyrtAndelEndringDto> getRefusjonOverstyrtAndelEndringer() {
        return refusjonOverstyrtAndelEndringer;
    }
}
