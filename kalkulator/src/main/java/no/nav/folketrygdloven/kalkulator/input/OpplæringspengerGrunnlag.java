package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class OpplæringspengerGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    protected final int dekningsgrad = 100;
    protected final int dekningsgrad_inaktiv = 65;

    public OpplæringspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
    }

    @Override
    public int getDekningsgrad() {
        return dekningsgrad;
    }

}
