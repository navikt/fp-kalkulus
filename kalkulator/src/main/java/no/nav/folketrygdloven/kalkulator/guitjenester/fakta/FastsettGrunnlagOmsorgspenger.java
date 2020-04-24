package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel;
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

        BeregningsgrunnlagPeriodeDto periode = andel.getBeregningsgrunnlagPeriode();
        Optional<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPeriode = finnTilhørendePeriode(input, periode);
        BigDecimal grenseverdi6G = input.getBeregningsgrunnlag().getGrunnbeløp().getVerdi().multiply(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getAntallGØvreGrenseverdi());
        BigDecimal maksRefusjonForPeriode = MapRefusjonskravFraVLTilRegel.finnSummertRefusjonskravForBGPerioden(periode, input.getInntektsmeldinger(), input.getSkjæringstidspunktForBeregning());

        BigDecimal totaltMaksimalRefusjon = grenseverdi6G.min(maksRefusjonForPeriode);
        BigDecimal totaltBeregningsgrunnlag = beregningsgrunnlagPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avkortetTotaltGrunnlag = grenseverdi6G.min(totaltBeregningsgrunnlag);

        if (utbetalesPengerDirekteTilBruker(totaltMaksimalRefusjon, avkortetTotaltGrunnlag)) {
            return erAvvikStørreEnn25Prosent(finnAvvikPromille(input));
        }
        return false;
    }

    private static boolean manglerBeregnetPrÅr(BeregningsgrunnlagPrStatusOgAndelDto andel){
        return andel.getBeregnetPrÅr() == null;
    }

    private static boolean utbetalesPengerDirekteTilBruker(BigDecimal maksimalRefusjon, BigDecimal avkortetTotaltGrunnlag){
        return maksimalRefusjon.compareTo(avkortetTotaltGrunnlag) < 0 ? true : false;
    }

    private static Optional<BeregningsgrunnlagPeriodeDto> finnTilhørendePeriode(BeregningsgrunnlagRestInput input, BeregningsgrunnlagPeriodeDto periode ){
        return input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getBeregningsgrunnlagPeriodeFom().equals(periode.getBeregningsgrunnlagPeriodeFom()))
                .findFirst();
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
