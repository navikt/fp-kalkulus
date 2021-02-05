package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;

class AktivitetDto {

    private YrkesaktivitetDto yrkesaktivitetDto;
    private List<InntektspostDto> inntekter;

    public AktivitetDto(YrkesaktivitetDto yrkesaktivitetDto, List<InntektspostDto> inntekter) {
        this.yrkesaktivitetDto = yrkesaktivitetDto;
        this.inntekter = inntekter;
    }

    public YrkesaktivitetDto getYrkesaktivitetDto() {
        return yrkesaktivitetDto;
    }

    public List<InntektspostDto> getInntekter() {
        return inntekter;
    }
}
