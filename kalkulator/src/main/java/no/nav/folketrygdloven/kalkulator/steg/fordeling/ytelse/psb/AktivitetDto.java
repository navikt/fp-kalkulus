package no.nav.folketrygdloven.kalkulator.steg.fordeling.ytelse.psb;

import java.util.Collection;

import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

class AktivitetDto {

    private Collection<YrkesaktivitetDto> yrkesaktivitetDto;
    private Arbeidsgiver arbeidsgiver;
    private Beløp månedsinntekt;


    public AktivitetDto(Collection<YrkesaktivitetDto> yrkesaktivitetDto, Beløp månedsinntekt) {
        var count = yrkesaktivitetDto.stream().map(YrkesaktivitetDto::getArbeidsgiver).distinct().count();
        if (count > 1) {
            throw new IllegalStateException("Kan ikke legge til månedsinntekt for ulike arbeidsgivere");
        }
        this.arbeidsgiver = yrkesaktivitetDto.stream().map(YrkesaktivitetDto::getArbeidsgiver).findFirst().orElseThrow();
        this.yrkesaktivitetDto = yrkesaktivitetDto;
        this.månedsinntekt = månedsinntekt;
    }

    public Collection<YrkesaktivitetDto> getYrkesaktivitetDto() {
        return yrkesaktivitetDto;
    }

    public Beløp getMånedsinntekt() {
        return månedsinntekt;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }
}
