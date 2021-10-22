package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;

import java.math.BigDecimal;
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
        var næringAndel = finnNæring(bgPeriode)
                .orElseThrow(() -> new IllegalStateException("Forventer å finne andel for næring ved vurdering av varig endret næring."));
        var forrigeNæring = forrigePeriode.flatMap(UtledVarigEndringEllerNyoppstartetSNVurderinger::finnNæring);
        boolean tilVerdi = næringAndel.getOverstyrtPrÅr() != null;
        Boolean fraVerdi = forrigeNæring.isPresent() && forrigeNæring.get().getOverstyrtPrÅr() != null ? true : null;
        return new VarigEndretNæringEndring(new ToggleEndring(fraVerdi, tilVerdi));
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnNæring(BeregningsgrunnlagPeriodeDto bgPeriode) {
        return bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                .findFirst();
    }

}
