package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnGradertBruttoForAndel;
import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet;

import java.math.BigDecimal;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

@FagsakYtelseTypeRef("OMP")
@ApplicationScoped
public class FastsettGrunnlagOmsorgspenger extends FastsettGrunnlagGenerell {

    @Override
    public boolean skalGrunnlagFastsettes(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagPrStatusOgAndelDto andel){
        if(erBrukerKunArbeidstaker(input)){
            if(girDirekteUtbetalingTilBruker(input, input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0))){
                return erAvvikStørreEnn25Prosent(finnAvvikPromille(input));
            }
            return false;
        }
        return super.skalGrunnlagFastsettes(input, andel);
    }

    public static boolean girDirekteUtbetalingTilBruker(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagPeriodeDto periode) {
        if(!harForeslåttBeregning(input.getBeregningsgrunnlagGrunnlag())){
            return false;
        }
        Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer();
        BigDecimal grenseverdi6G = input.getBeregningsgrunnlag().getGrunnbeløp().getVerdi().multiply(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getAntallGØvreGrenseverdi());
        BigDecimal gradertRefusjonVedSkjæringstidspunkt = finnGradertRefusjonskravPåSkjæringstidspunktet(input.getInntektsmeldinger(), input.getSkjæringstidspunktForBeregning(), input.getYtelsespesifiktGrunnlag(), refusjonOverstyringer);
        BigDecimal lavesteGrenseRefusjon = grenseverdi6G.min(gradertRefusjonVedSkjæringstidspunkt);
        BigDecimal totaltBeregningsgrunnlag = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .map(a -> finnGradertBruttoForAndel(a, periode.getPeriode(), input.getYtelsespesifiktGrunnlag()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = grenseverdi6G.min(totaltBeregningsgrunnlag);

        return lavesteGrenseRefusjon.compareTo(avkortetTotaltGrunnlag) < 0;
    }

    private static boolean erBrukerKunArbeidstaker(BeregningsgrunnlagGUIInput input){
        return input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .allMatch(a -> AktivitetStatus.ARBEIDSTAKER.equals(a.getAktivitetStatus()));
    }

    private static boolean harForeslåttBeregning(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto){
        return !beregningsgrunnlagGrunnlagDto.getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private static boolean erAvvikStørreEnn25Prosent(BigDecimal avvikPromille){
        return avvikPromille.compareTo(BigDecimal.valueOf(250)) > 0;
    }

    private static BigDecimal finnAvvikPromille(BeregningsgrunnlagGUIInput input){
        Optional<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatus = input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
                .filter(s -> s.getSammenligningsgrunnlagType().equals(SammenligningsgrunnlagType.SAMMENLIGNING_AT)
                        || s.getSammenligningsgrunnlagType().equals(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN))
                .findAny();

        return input.getBeregningsgrunnlag().getSammenligningsgrunnlag() != null ? input.getBeregningsgrunnlag().getSammenligningsgrunnlag().getAvvikPromilleNy() : sammenligningsgrunnlagPrStatus.get().getAvvikPromilleNy();
    }

}
