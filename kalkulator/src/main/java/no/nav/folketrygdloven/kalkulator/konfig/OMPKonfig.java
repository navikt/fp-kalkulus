package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class OMPKonfig extends Konfigverdier {
    private static final Intervall PERIODE_MED_UTVIDET_FRIST = Intervall.fraOgMedTilOgMed(LocalDate.of(2020,3,16), LocalDate.of(2020, 12, 31));
    private static final BigDecimal ANTALL_G_MS_HAR_KRAV_PÅ = BigDecimal.valueOf(2);
    private static final int UTVIDET_FRIST = 9;

    public OMPKonfig() {
        super(ANTALL_G_MS_HAR_KRAV_PÅ);
    }

    /**
     *
     * @param datoForInnsendtRefKrav
     * @return antall måneder + 1 som er fristen for innsending av rekrav.
     * For OMP er det utvidet frist i en viss periode som følge av korona
     */
    @Override
    public int getFristMånederEtterRefusjon(LocalDate datoForInnsendtRefKrav) {
        if (PERIODE_MED_UTVIDET_FRIST.inkluderer(datoForInnsendtRefKrav)) {
            return UTVIDET_FRIST;
        }
        return fristMånederEtterRefusjon;
    }

}
