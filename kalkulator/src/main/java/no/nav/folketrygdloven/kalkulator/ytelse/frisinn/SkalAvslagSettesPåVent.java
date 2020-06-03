package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;

public class SkalAvslagSettesPåVent {

    private SkalAvslagSettesPåVent() {
        // skjul
    }

    public static boolean skalSettesPåVent(BeregningsgrunnlagInput input) {
        return false; // Returnerer false foreløpig.
//        boolean gjelderFrisinn = input.getFagsakYtelseType().equals(FagsakYtelseType.FRISINN);
//        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
//        LocalDate førsteMai = LocalDate.of(2020, 5, 1);
//        boolean søkerForMai = frisinnGrunnlag.getFrisinnPerioder().stream().anyMatch(p ->
//                (p.getSøkerFrilans() || p.getSøkerNæring())
//                        && p.getPeriode().getTomDato().isAfter(førsteMai));
//        return gjelderFrisinn && søkerForMai;
    }

    public static List<BeregningAksjonspunktResultat> avslagPåVent() {
        return List.of(BeregningAksjonspunktResultat.opprettMedFristFor(
                BeregningAksjonspunktDefinisjon.AUTO_VENT_FRISINN,
                BeregningVenteårsak.PERIODE_MED_AVSLAG,
                LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)));
    }


}
