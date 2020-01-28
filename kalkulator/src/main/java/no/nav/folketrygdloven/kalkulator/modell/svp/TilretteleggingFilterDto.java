package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Brukt til å filtrere tilrettelegginger for SVP.
 *
 * Benytter seg av de overstyrte tilretteleggingene hvor mulig.
 *
 */
public class TilretteleggingFilterDto {

    private SvpTilretteleggingerDto opprinneligeTilrettelegginger;
    private SvpTilretteleggingerDto overstyrteTilrettelegginger;

    public TilretteleggingFilterDto(SvpGrunnlagDto svpGrunnlag) {
        this.opprinneligeTilrettelegginger = svpGrunnlag.getOpprinneligeTilrettelegginger();
        this.overstyrteTilrettelegginger = svpGrunnlag.getOverstyrteTilrettelegginger();
    }

    /**
     * Tar _IKKE_ hensyn til hvorvidt tilretteleggingen skal brukes eller ikke.
     *
     * Bruk {@link #getAktuelleTilretteleggingerFiltrert} hvis du kun skal benytte de tilretteleggingene
     * som saksbehandler har valgt å bruke
     *
     * @return Ufiltrert liste av tilrettelegginger
     */
    private List<SvpTilretteleggingDto> getAktuelleTilretteleggingerUfiltrert() {
        if (overstyrteTilrettelegginger != null ) {
            return overstyrteTilrettelegginger.getTilretteleggingListe();
        }
        if (opprinneligeTilrettelegginger != null) {
            return opprinneligeTilrettelegginger.getTilretteleggingListe();
        }
        return Collections.emptyList();
    }

    /**
     * Tar hensyn til valget saksbehandler har gjort om hvorvidt tilretteleggingen skal brukes eller ikke.
     *
     * @return En filtrert liste av tilrettelegginger som skal brukes.
     */
    public List<SvpTilretteleggingDto> getAktuelleTilretteleggingerFiltrert() {
        return getAktuelleTilretteleggingerUfiltrert().stream()
            .filter(SvpTilretteleggingDto::getSkalBrukes)
            .collect(Collectors.toList());
    }

}
