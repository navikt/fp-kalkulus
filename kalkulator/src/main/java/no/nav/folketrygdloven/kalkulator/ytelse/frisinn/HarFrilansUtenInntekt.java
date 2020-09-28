package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;

public class HarFrilansUtenInntekt {

    private HarFrilansUtenInntekt() {
        // hide
    }

    public static boolean harKunFrilansUtenInntekt(FrisinnGrunnlag frisinnGrunnlag, LocalDate bgPeriodeFom, BeregningsgrunnlagPeriodeDto førstePeriode) {
        return frisinnGrunnlag.getSøkerYtelseForFrilans(bgPeriodeFom)
                && !frisinnGrunnlag.getSøkerYtelseForNæring(bgPeriodeFom)
                && førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .anyMatch(a -> a.getAktivitetStatus().erFrilanser() && a.getBeregnetPrÅr().compareTo(BigDecimal.ZERO) == 0);
    }

}
