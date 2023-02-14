package no.nav.folketrygdloven.kalkulus.mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.GrunnbeløpInput;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSatsType;

public class GrunnbeløpMapper {

    private GrunnbeløpMapper() {
    }

    public static List<GrunnbeløpInput> mapGrunnbeløpInput(List<BeregningSats> satser) {
        List<GrunnbeløpInput> grunnbeløpListe = new ArrayList<>();
        int iår = LocalDate.now().getYear();
        for (int år = 2000; år <= iår; år++) {
            // Den vil ikke plukke opp alle grunnbeløp hvis det blir endret f.eks to ganger i året .
            LocalDate dato = LocalDate.now().withYear(år);
            var grunnbeløp = grunnbeløpInputOgSnittFor(satser, dato);
            grunnbeløp.ifPresent(grunnbeløpListe::add);
        }
        return grunnbeløpListe;
    }

    private static Optional<GrunnbeløpInput> grunnbeløpInputOgSnittFor(List<BeregningSats> satser, LocalDate dato) {
        var g = finnEksaktSats(satser, BeregningSatsType.GRUNNBELØP, dato);
        var gSnitt = g.map(BeregningSats::getPeriode).flatMap(p -> finnEksaktSats(satser, BeregningSatsType.GSNITT, p.getFomDato()));
        return g.map(val -> new GrunnbeløpInput(
                val.getPeriode().getFomDato(),
                val.getPeriode().getTomDato(),
                val.getVerdi(),
                gSnitt.orElseThrow(() -> new IllegalStateException("Forventer at snittverdi eksisterer")).getVerdi()));
    }

    private static Optional<BeregningSats> finnEksaktSats(List<BeregningSats> satser, BeregningSatsType satsType, LocalDate dato) {
        return satser.stream().filter(sats -> sats.getSatsType().equals(satsType) && sats.getPeriode().inkluderer(dato)).findFirst();
    }

}
