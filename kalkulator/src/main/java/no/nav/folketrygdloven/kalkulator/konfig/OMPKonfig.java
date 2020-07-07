package no.nav.folketrygdloven.kalkulator.konfig;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;

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
        if (erPeriodeMedUtvidetFrist(datoForInnsendtRefKrav)) {
            return UTVIDET_FRIST;
        }
        return fristMånederEtterRefusjon;
    }

    @Override
    public Hjemmel getHjemmelForRefusjonfrist(LocalDate datoForInnsendtRefKrav) {
        if (erPeriodeMedUtvidetFrist(datoForInnsendtRefKrav)) {
            return Hjemmel.COV_1_5;
        }
        return Hjemmel.F_22_13_6;
    }

    /**
     *
     *
     * @param datoForInnsendtRefKrav datoForInnsendtRefusjonskrav
     * @return
     */
    public boolean erPeriodeMedUtvidetFrist(LocalDate datoForInnsendtRefKrav) {
        return PERIODE_MED_UTVIDET_FRIST.inkluderer(datoForInnsendtRefKrav);
    }

}
