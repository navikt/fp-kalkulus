package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.KontrollerFaktaBeregningTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


@ApplicationScoped
@FagsakYtelseTypeRef("*")
@FaktaOmBeregningTilfelleRef("FASTSETT_BG_KUN_YTELSE")
public class KunYtelseTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        if (KonfigurasjonVerdi.get("BEREGNE_FRA_YTELSE_VEDTAK", false)) {
            var harKunYtelse = KontrollerFaktaBeregningTjeneste.harAktivitetStatusKunYtelse(beregningsgrunnlag);
            var harForeldrepengerAvDagpenger = harForeldrepengerAvDagpenger(input, beregningsgrunnlag);
            var harIkkeAnvisteAndeler = harYtelseUtenAnvisteAndeler(input, beregningsgrunnlag);
            return harKunYtelse && (harIkkeAnvisteAndeler || harForeldrepengerAvDagpenger) ? Optional.of(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE) : Optional.empty();
        }
        return KontrollerFaktaBeregningTjeneste.harAktivitetStatusKunYtelse(beregningsgrunnlag) ?
                Optional.of(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE) : Optional.empty();
    }

    // Sjekker for å kunne manuelt avgjere om grunnlaget er besteberegnet
    private boolean harForeldrepengerAvDagpenger(FaktaOmBeregningInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        return input.getIayGrunnlag().getAktørYtelseFraRegister()
                .stream().flatMap(y -> y.getAlleYtelser().stream())
                .filter(y -> y.getPeriode().inkluderer(BeregningstidspunktTjeneste.finnBeregningstidspunkt(beregningsgrunnlag.getSkjæringstidspunkt())))
                .anyMatch(this::erForeldrepengerAvDagpenger);
    }

    private boolean harYtelseUtenAnvisteAndeler(FaktaOmBeregningInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        return input.getIayGrunnlag().getAktørYtelseFraRegister()
                .stream().flatMap(y -> y.getAlleYtelser().stream())
                .filter(y -> y.getPeriode().inkluderer(BeregningstidspunktTjeneste.finnBeregningstidspunkt(beregningsgrunnlag.getSkjæringstidspunkt())))
                .anyMatch(y -> y.getYtelseAnvist().stream().anyMatch(ya -> ya.getAnvisteAndeler().isEmpty()));
    }

    private boolean erForeldrepengerAvDagpenger(YtelseDto y) {
        if (y.getRelatertYtelseType().equals(FagsakYtelseType.FORELDREPENGER)) {
            return y.getYtelseAnvist().stream().anyMatch(ya -> ya.getAnvisteAndeler().stream().anyMatch(a -> a.getInntektskategori().equals(Inntektskategori.DAGPENGER)));
        }
        return false;
    }

}
