package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class OmsorgspengerGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    private final int dekningsgrad = 100;
    private Integer grunnbeløpMilitærHarKravPå;

    private List<Intervall> brukerSøkerPerioder;

    public OmsorgspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet, List<Intervall> brukerSøkerPerioder) {
        super(utbetalingsgradPrAktivitet);
        this.brukerSøkerPerioder = brukerSøkerPerioder;
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

    public Optional<List<Intervall>> getBrukerSøkerPerioder() {
        return Optional.ofNullable(brukerSøkerPerioder);
    }
}
