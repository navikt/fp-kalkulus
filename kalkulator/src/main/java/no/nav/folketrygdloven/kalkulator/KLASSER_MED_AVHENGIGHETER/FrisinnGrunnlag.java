package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class FrisinnGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private final int dekningsgrad = 100;

    private Integer grunnbeløpMilitærHarKravPå = 2;
    private final boolean søkerYtelseForFrilans;
    private final boolean søkerYtelseForNæring;


    public FrisinnGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, boolean søkerYtelseForFrilans, boolean søkerYtelseForNæring) {
        super(utbetalingsgradPrAktivitet);
        this.søkerYtelseForFrilans = søkerYtelseForFrilans;
        this.søkerYtelseForNæring = søkerYtelseForNæring;
    }


    @Override
    public int getDekningsgrad() {
        return dekningsgrad;
    }

    @Override
    public int getGrunnbeløpMilitærHarKravPå() {
        return grunnbeløpMilitærHarKravPå;
    }

    @Override
    public void setGrunnbeløpMilitærHarKravPå(int grunnbeløpMilitærHarKravPå) {
        this.grunnbeløpMilitærHarKravPå = grunnbeløpMilitærHarKravPå;
    }

    public boolean getSøkerYtelseForFrilans() {
        return søkerYtelseForFrilans;
    }

    public boolean getSøkerYtelseForNæring() {
        return søkerYtelseForNæring;
    }
}
