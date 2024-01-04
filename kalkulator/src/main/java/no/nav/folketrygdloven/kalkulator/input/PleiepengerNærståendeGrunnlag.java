package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class PleiepengerNærståendeGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    protected final int dekningsgrad = 100;
    protected final int dekningsgrad_inaktiv = 65;

    public PleiepengerNærståendeGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
    }

    public PleiepengerNærståendeGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad, LocalDate tilkommetInntektHensyntasFom) {
        super(tilretteleggingMedUtbelingsgrad, tilkommetInntektHensyntasFom);
    }

    @Override
    public int getDekningsgrad() {
        return dekningsgrad;
    }
}
