package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;

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


}
