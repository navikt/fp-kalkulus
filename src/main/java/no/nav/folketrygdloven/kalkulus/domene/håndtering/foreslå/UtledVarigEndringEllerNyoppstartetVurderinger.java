package no.nav.folketrygdloven.kalkulus.domene.håndtering.foreslå;

import java.util.Optional;
import java.util.function.Function;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.VarigEndretEllerNyoppstartetNæringEndring;

public class UtledVarigEndringEllerNyoppstartetVurderinger {

    private UtledVarigEndringEllerNyoppstartetVurderinger() {
        // Skjul
    }

    public static VarigEndretEllerNyoppstartetNæringEndring utledForVarigEndretEllerNyoppstartetNæring(BeregningsgrunnlagDto nyttBeregningsgrunnlag,
                                                                                                       Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt,
                                                                                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        ToggleEndring endring = utledEndring(nyttBeregningsgrunnlag, forrigeBeregningsgrunnlagOpt, UtledVarigEndringEllerNyoppstartetVurderinger::finnNæring);
        boolean harOppgittVarigEndring = iayGrunnlag.getOppgittOpptjening().stream()
                .flatMap(o -> o.getEgenNæring().stream())
                .anyMatch(OppgittEgenNæringDto::getVarigEndring);
        boolean harOppgittNyoppstartet = iayGrunnlag.getOppgittOpptjening().stream()
                .flatMap(o -> o.getEgenNæring().stream())
                .anyMatch(OppgittEgenNæringDto::getNyoppstartet);
        return new VarigEndretEllerNyoppstartetNæringEndring(
                harOppgittVarigEndring ? endring : null,
                harOppgittNyoppstartet ? endring : null);
    }

    public static ToggleEndring utledEndring(BeregningsgrunnlagDto nyttBeregningsgrunnlag, Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt, Function<BeregningsgrunnlagPeriodeDto, BeregningsgrunnlagPrStatusOgAndelDto> finnAndel) {
        var bgPeriode = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = forrigeBeregningsgrunnlagOpt.map(bg -> bg.getBeregningsgrunnlagPerioder().get(0));
        var næringAndel = finnAndel.apply(bgPeriode);
        var forrigeNæring = forrigePeriode.map(finnAndel);
        boolean tilVerdi = næringAndel.getOverstyrtPrÅr() != null;
        Boolean fraVerdi = forrigeNæring.isPresent() && forrigeNæring.get().getOverstyrtPrÅr() != null ? true : null;
        return new ToggleEndring(fraVerdi, tilVerdi);
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto finnNæring(BeregningsgrunnlagPeriodeDto bgPeriode) {
        return bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Forventet å finne næring"));
    }

}
