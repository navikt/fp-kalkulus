package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnGradertBruttoForAndel;
import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet;

import java.math.BigDecimal;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.SammenligningsgrunnlagType;

@FagsakYtelseTypeRef("OMP")
@ApplicationScoped
public class FastsettGrunnlagOmsorgspenger implements FastsettGrunnlag {

    @Override
    public boolean skalGrunnlagFastsettes(BeregningsgrunnlagRestInput input, BeregningsgrunnlagPrStatusOgAndelDto andel){
        if(manglerBeregnetPrÅr(andel)){
            return false;
        }
        if(girDirekteUtbetalingTilBruker(input, input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0))){
            return erAvvikStørreEnn25Prosent(finnAvvikPromille(input));
        }
        return false;
    }

    public static boolean girDirekteUtbetalingTilBruker(BeregningsgrunnlagRestInput input, BeregningsgrunnlagPeriodeDto periode) {
        BigDecimal grenseverdi6G = input.getBeregningsgrunnlag().getGrunnbeløp().getVerdi().multiply(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getAntallGØvreGrenseverdi());
        BigDecimal gradertRefusjonVedSkjæringstidspunkt = finnGradertRefusjonskravPåSkjæringstidspunktet(input.getInntektsmeldinger(), input.getSkjæringstidspunktForBeregning(), input.getYtelsespesifiktGrunnlag());

        BigDecimal lavesteGrenseRefusjon = grenseverdi6G.min(gradertRefusjonVedSkjæringstidspunkt);
        BigDecimal totaltBeregningsgrunnlag = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .map(a -> finnGradertBruttoForAndel(a, periode.getPeriode(), input.getYtelsespesifiktGrunnlag()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = grenseverdi6G.min(totaltBeregningsgrunnlag);

        return lavesteGrenseRefusjon.compareTo(avkortetTotaltGrunnlag) < 0;
    }

    private static boolean manglerBeregnetPrÅr(BeregningsgrunnlagPrStatusOgAndelDto andel){
        return andel.getBeregnetPrÅr() == null;
    }

    private static boolean erAvvikStørreEnn25Prosent(BigDecimal avvikPromille){
        return avvikPromille.compareTo(BigDecimal.valueOf(250)) > 0;
    }

    private static BigDecimal finnAvvikPromille(BeregningsgrunnlagRestInput input){
        Optional<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagPrStatus = input.getBeregningsgrunnlag().getSammenligningsgrunnlagPrStatusListe().stream()
                .filter(s -> s.getSammenligningsgrunnlagType().equals(SammenligningsgrunnlagType.SAMMENLIGNING_AT)
                        || s.getSammenligningsgrunnlagType().equals(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN))
                .findAny();

        return input.getBeregningsgrunnlag().getSammenligningsgrunnlag() != null ? input.getBeregningsgrunnlag().getSammenligningsgrunnlag().getAvvikPromilleNy() : sammenligningsgrunnlagPrStatus.get().getAvvikPromilleNy();
    }

}
