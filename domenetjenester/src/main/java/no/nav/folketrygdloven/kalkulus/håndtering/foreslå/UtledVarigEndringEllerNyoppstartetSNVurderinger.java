package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.VarigEndretNæringEndring;

public class UtledVarigEndringEllerNyoppstartetSNVurderinger {

    private UtledVarigEndringEllerNyoppstartetSNVurderinger() {
        // Skjul
    }

    public static VarigEndretNæringEndring utled(BeregningsgrunnlagDto nyttBeregningsgrunnlag, Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt) {
        var bgPeriode = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = forrigeBeregningsgrunnlagOpt.map(bg -> bg.getBeregningsgrunnlagPerioder().get(0));
        BigDecimal fastsattBeløp = finnFastsattVerdiForNæring(bgPeriode)
                .orElseThrow(() -> new IllegalStateException("Forventer å finne andel for næring ved vurdering av varig endret næring."));
        Optional<BigDecimal> forrigeFastsattBeløp = forrigePeriode.flatMap(UtledVarigEndringEllerNyoppstartetSNVurderinger::finnFastsattVerdiForNæring);
        boolean tilVerdi = fastsattBeløp != null;
        Boolean fraVerdi = forrigeFastsattBeløp.map(obj -> true).orElse(null);
        return new VarigEndretNæringEndring(new ToggleEndring(fraVerdi, tilVerdi));
    }

    private static Optional<BigDecimal> finnFastsattVerdiForNæring(BeregningsgrunnlagPeriodeDto bgPeriode) {
        return bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                .findFirst()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getOverstyrtPrÅr);
    }

}
