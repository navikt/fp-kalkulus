package no.nav.folketrygdloven.kalkulus.mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSatsType;

public class GrunnbeløpMapper {

    private List<BeregningSats> satser;

    @Inject
    public GrunnbeløpMapper(List<BeregningSats> satser) {
        this.satser = satser;
    }

    public List<Grunnbeløp> mapGrunnbeløpSatser() {
        List<Grunnbeløp> grunnbeløpListe = new ArrayList<>();
        int iår = LocalDate.now().getYear();
        for (int år = 2000; år <= iår; år++) {
            // Den vil ikke plukke opp alle grunnbeløp hvis det blir endret f.eks to ganger i året .
            LocalDate dato = LocalDate.now().withYear(år);
            var grunnbeløp = grunnbeløpOgSnittFor(dato);
            grunnbeløp.ifPresent(grunnbeløpListe::add);
        }
        return grunnbeløpListe;
    }

    private Optional<Grunnbeløp> grunnbeløpOgSnittFor(LocalDate dato) {
        var g = finnEksaktSats(BeregningSatsType.GRUNNBELØP, dato);
        var gSnitt = g.map(BeregningSats::getPeriode).flatMap(p -> finnEksaktSats(BeregningSatsType.GSNITT, p.getFomDato()));
        return g.map(val -> new Grunnbeløp(
                val.getPeriode().getFomDato(),
                val.getPeriode().getTomDato(),
                val.getVerdi(),
                gSnitt.orElseThrow(() -> new IllegalStateException("Forventer at snittverdi eksisterer")).getVerdi()));
    }

    private Optional<BeregningSats> finnEksaktSats(BeregningSatsType satsType, LocalDate dato) {
        return satser.stream().filter(sats -> sats.getSatsType().equals(satsType) && sats.getPeriode().inkluderer(dato)).findFirst();
    }

}
