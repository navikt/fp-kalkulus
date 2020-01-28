package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.svp.SvpTilretteleggingDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.TilretteleggingMedUtbelingsgradDto;

public class SvangerskapspengerGrunnlag implements YtelsespesifiktGrunnlag {

    private int dekningsgrad = 100;
    private List<TilretteleggingMedUtbelingsgradDto> tilretteleggingMedUtbelingsgrad;
    private List<SvpTilretteleggingDto> aktuelleTilrettelegginger;
    private Integer grunnbeløpMilitærHarKravPå;


    public SvangerskapspengerGrunnlag(List<TilretteleggingMedUtbelingsgradDto> tilretteleggingMedUtbelingsgrad,
                                      List<SvpTilretteleggingDto> aktuelleTilrettelegginger) {
        this.tilretteleggingMedUtbelingsgrad = tilretteleggingMedUtbelingsgrad;
        this.aktuelleTilrettelegginger = aktuelleTilrettelegginger;
    }

    @Override
    public int getDekningsgrad() {
        // egentlig ikke relevant, eller alt kan sees på som alltid 100% dekning
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

    public List<TilretteleggingMedUtbelingsgradDto> getTilretteleggingMedUtbelingsgrad() {
        return tilretteleggingMedUtbelingsgrad;
    }

    public List<SvpTilretteleggingDto> getAktuelleTilrettelegginger() {
        return aktuelleTilrettelegginger;
    }
}
