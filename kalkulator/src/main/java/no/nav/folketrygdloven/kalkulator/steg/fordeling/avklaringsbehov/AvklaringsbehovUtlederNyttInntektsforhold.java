package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Ved nye inntektsforhold skal beregningsgrunnlaget graderes mot inntekt.
 * <p>
 * Utleder her om det er potensielle nye inntektsforhold.
 * <p>
 * Se https://navno.sharepoint.com/sites/fag-og-ytelser-arbeid-sykefravarsoppfolging-og-sykepenger/SitePages/%C2%A7-8-13-Graderte-sykepenger.aspx
 */
public class AvklaringsbehovUtlederNyttInntektsforhold {

    public static boolean skalVurdereNyttInntektsforhold(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                         InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                         YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                         List<Intervall> forlengelseperioder) {

        if (!(ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) || !KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false)) {
            return false;
        }

        var bg = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));

        var tilVurderingTjeneste = new PerioderTilVurderingTjeneste(forlengelseperioder, bg);
        var perioderSomSkalVurderes = bg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> tilVurderingTjeneste.erTilVurdering(p.getPeriode()))
                .collect(Collectors.toList());

        return perioderSomSkalVurderes.stream().anyMatch(p -> harNyttInntektsforhold(p, bg.getSkjæringstidspunkt(), iayGrunnlag, ytelsespesifiktGrunnlag));
    }

    private static boolean harNyttInntektsforhold(BeregningsgrunnlagPeriodeDto p,
                                                  LocalDate skjæringstidspunkt,
                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                  YtelsespesifiktGrunnlag utbetalingsgradGrunnlag) {
        return harTilkommetInntektsforhold(p, skjæringstidspunkt, iayGrunnlag, utbetalingsgradGrunnlag);
    }

    private static boolean harTilkommetInntektsforhold(BeregningsgrunnlagPeriodeDto p,
                                                       LocalDate skjæringstidspunkt,
                                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                       YtelsespesifiktGrunnlag utbetalingsgradGrunnlag) {
        if (iayGrunnlag.getAktørArbeidFraRegister().isEmpty()) {
            return false;
        }
        var yrkesaktiviteter = iayGrunnlag.getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter).orElse(Collections.emptyList());
        var andeler = p.getBeregningsgrunnlagPrStatusOgAndelList();
        var periode = p.getPeriode();
        return !TilkommetInntektsforholdTjeneste.finnTilkomneInntektsforhold(skjæringstidspunkt, yrkesaktiviteter, andeler, periode, utbetalingsgradGrunnlag).isEmpty();
    }

}
