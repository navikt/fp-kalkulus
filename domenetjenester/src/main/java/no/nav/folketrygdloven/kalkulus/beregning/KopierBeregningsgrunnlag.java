package no.nav.folketrygdloven.kalkulus.beregning;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningAktivitetAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;

@ApplicationScoped
class KopierBeregningsgrunnlag {

    private KopierBeregningsgrunnlag() {
        // For CDI
    }

    /**
     * Sjekker om det er mulig å kopiere beregningsgrunnlagGrunnlaget som ble bekreftet ved forrige saksbehandling om det har oppstått aksjonspunkter.
     * @param aksjonspunkter Utledede aksjonspunkter for nytt beregningsgrunnlag
     * @param nyttGrunnlag Nytt beregningsgrunnlagGrunnlag
     * @param forrigeGrunnlag Forrige grunnlag som lagres i beregningsteget
     * @param forrigeBekreftetGrunnlag Forrige grunnlag som ble lagret etter saksbehandlers vurdering i steget
     */
    static boolean kanKopiereFraForrigeBekreftetGrunnlag(List<BeregningAksjonspunktResultat> aksjonspunkter,
                                                         BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                         Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag,
                                                         Optional<BeregningsgrunnlagGrunnlagDto> forrigeBekreftetGrunnlag) {
        boolean kanKopiereFraBekreftet = kanKopiereAktiviteter(aksjonspunkter, nyttGrunnlag, forrigeGrunnlag, forrigeBekreftetGrunnlag);
        if (kanKopiereFraBekreftet) {
            return forrigeBekreftetGrunnlag.isPresent();
        } else {
            return false;
        }
    }

    /**
     * Sjekker om det er mulig å kopiere beregningsgrunnlaget som ble bekreftet ved forrige saksbehandling om det har oppstått aksjonspunkter.
     * @param aksjonspunkter Utledede aksjonspunkter for nytt beregningsgrunnlag
     * @param nyttBg Nytt beregningsgrunnlag
     * @param forrigeBeregningsgrunnlag Forrige beregningsgrunnlag som lagres i beregningsteget
     * @param forrigeBekreftetBeregningsgrunnlag Forrige beregningsgrunnlag som ble lagret etter saksbehandlers vurdering i steget
     */
    static boolean kanKopiereFraForrigeBekreftetGrunnlag(List<BeregningAksjonspunktResultat> aksjonspunkter,
                                                         BeregningsgrunnlagDto nyttBg,
                                                         Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlag,
                                                         Optional<BeregningsgrunnlagDto> forrigeBekreftetBeregningsgrunnlag) {
        boolean kanKopiereFraBekreftet = kanKopiereBeregningsgrunnlag(
            aksjonspunkter,
            nyttBg,
            forrigeBeregningsgrunnlag);
        if (kanKopiereFraBekreftet) {
            return forrigeBekreftetBeregningsgrunnlag.isPresent();
        } else {
            return false;
        }
    }

    private static boolean kanKopiereAktiviteter(List<BeregningAksjonspunktResultat> aksjonspunkter,
                                                 BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                 Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag,
                                                 Optional<BeregningsgrunnlagGrunnlagDto> forrigeBekreftetGrunnlag) {
        BeregningAktivitetAggregatDto nyttRegister = nyttGrunnlag.getRegisterAktiviteter();
        Optional<BeregningAktivitetAggregatDto> forrigeRegister = forrigeGrunnlag.map(BeregningsgrunnlagGrunnlagDto::getRegisterAktiviteter);
        return forrigeRegister.map(aktivitetAggregatEntitet -> !BeregningsgrunnlagDiffSjekker.harSignifikantDiffIAktiviteter(nyttRegister, aktivitetAggregatEntitet)).orElse(false)
            && (!aksjonspunkter.isEmpty() || forrigeBekreftetGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getOverstyring).isPresent());
    }


    private static boolean kanKopiereBeregningsgrunnlag(List<BeregningAksjonspunktResultat> aksjonspunkter, BeregningsgrunnlagDto nyttBg, Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlag) {
        return forrigeBeregningsgrunnlag.map(bg -> !BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(nyttBg, bg)).orElse(false) && !aksjonspunkter.isEmpty();
    }

}
