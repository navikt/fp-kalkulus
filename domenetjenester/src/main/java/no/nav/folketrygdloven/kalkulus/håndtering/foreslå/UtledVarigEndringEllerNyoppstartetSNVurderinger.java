package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.VarigEndretEllerNyoppstartetNæringEndring;

public class UtledVarigEndringEllerNyoppstartetSNVurderinger {

    private UtledVarigEndringEllerNyoppstartetSNVurderinger() {
        // Skjul
    }

    public static VarigEndretEllerNyoppstartetNæringEndring utled(BeregningsgrunnlagDto nyttBeregningsgrunnlag,
                                                                  Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt,
                                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var bgPeriode = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = forrigeBeregningsgrunnlagOpt.map(bg -> bg.getBeregningsgrunnlagPerioder().get(0));
        var næringAndel = finnNæring(bgPeriode)
                .orElseThrow(() -> new IllegalStateException("Forventer å finne andel for næring ved vurdering av varig endret næring."));
        var forrigeNæring = forrigePeriode.flatMap(UtledVarigEndringEllerNyoppstartetSNVurderinger::finnNæring);
        boolean tilVerdi = næringAndel.getOverstyrtPrÅr() != null;
        Boolean fraVerdi = forrigeNæring.isPresent() && forrigeNæring.get().getOverstyrtPrÅr() != null ? true : null;
        ToggleEndring endring = new ToggleEndring(fraVerdi, tilVerdi);
        boolean harOppgittVarigEndring = iayGrunnlag.getOppgittOpptjening().stream()
                .flatMap(o -> o.getEgenNæring().stream())
                .anyMatch(OppgittEgenNæringDto::getVarigEndring);
        return harOppgittVarigEndring ? VarigEndretEllerNyoppstartetNæringEndring.forVarigEndretNæring(endring)
                : VarigEndretEllerNyoppstartetNæringEndring.forNyoppstartetNæring(endring);
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnNæring(BeregningsgrunnlagPeriodeDto bgPeriode) {
        return bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                .findFirst();
    }

}
