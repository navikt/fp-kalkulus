package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
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
        return getYtelseFilterKap8(input, beregningsgrunnlag)
                .filter(y -> y.getYtelseType().equals(FagsakYtelseType.FORELDREPENGER))
                .getAlleYtelser()
                .stream()
                .anyMatch(this::erBasertPåDagpenger);
    }

    private boolean harYtelseUtenAnvisteAndeler(FaktaOmBeregningInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        List<YtelseAnvistDto> sisteAnvisninger = finnSisteAnvisninger(getYtelseFilterKap8(input, beregningsgrunnlag));
        return sisteAnvisninger.isEmpty() || sisteAnvisninger.stream().anyMatch(a -> a.getAnvisteAndeler().isEmpty());
    }

    private YtelseFilterDto getYtelseFilterKap8(FaktaOmBeregningInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        return new YtelseFilterDto(input.getIayGrunnlag().getAktørYtelseFraRegister())
                .før(beregningsgrunnlag.getSkjæringstidspunkt())
                .filter(y -> !y.getYtelseType().equals(FagsakYtelseType.DAGPENGER) && !y.getYtelseType().equals(FagsakYtelseType.ARBEIDSAVKLARINGSPENGER))
                .filter(y -> y.getPeriode().getTomDato().isAfter(beregningsgrunnlag.getSkjæringstidspunkt().minusMonths(3).withDayOfMonth(1)));
    }

    private List<YtelseAnvistDto> finnSisteAnvisninger(YtelseFilterDto filter) {
        var ytelser = filter.getAlleYtelser();
        var alleAnvisninger = ytelser.stream().flatMap(y -> y.getYtelseAnvist().stream()).toList();
        var sisteDagMedAnvisning = alleAnvisninger.stream().max(Comparator.comparing(YtelseAnvistDto::getAnvistTOM)).map(YtelseAnvistDto::getAnvistTOM);
        return sisteDagMedAnvisning.isEmpty() ? Collections.emptyList() : alleAnvisninger.stream()
                .filter(a -> a.getAnvistTOM().equals(sisteDagMedAnvisning.get())).toList();
    }

    private boolean erBasertPåDagpenger(YtelseDto y) {
        return y.getYtelseAnvist().stream().anyMatch(ya -> ya.getAnvisteAndeler().stream().anyMatch(a -> a.getInntektskategori().equals(Inntektskategori.DAGPENGER)));
    }

}
