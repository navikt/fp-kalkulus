package no.nav.folketrygdloven.kalkulus.kopiering;

import java.util.Collection;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;

@ApplicationScoped
class KanKopierBeregningsgrunnlag {

    private KanKopierBeregningsgrunnlag() {
        // For CDI
    }

    /**
     * Sjekker om det er mulig å kopiere beregningsgrunnlagGrunnlaget som ble bekreftet ved forrige saksbehandling om det har oppstått avklaringsbehov.
     * @param avklaringsbehov Utledede avklaringsbehov for nytt beregningsgrunnlag
     * @param nyttGrunnlag Nytt beregningsgrunnlagGrunnlag
     * @param forrigeGrunnlag Forrige grunnlag som lagres i beregningsteget
     */
    static boolean kanKopiereForrigeGrunnlagAvklartIStegUt(Collection<BeregningAvklaringsbehovResultat> avklaringsbehov,
                                                           BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                           Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag) {
        if (avklaringsbehov.isEmpty()) {
            return false;
        }
        boolean kanKopiereAktiviteter = kanKopiereAktiviteter(nyttGrunnlag, forrigeGrunnlag);
        boolean kanKopiereBeregningsgrunnlag = kanKopiereBeregningsgrunnlag(
                nyttGrunnlag.getBeregningsgrunnlagHvisFinnes(),
                forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes));
        return kanKopiereAktiviteter && kanKopiereBeregningsgrunnlag;
    }

    private static boolean kanKopiereAktiviteter(BeregningsgrunnlagGrunnlagDto nyttGrunnlag,
                                                 Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag) {
        BeregningAktivitetAggregatDto nyttRegister = nyttGrunnlag.getRegisterAktiviteter();
        Optional<BeregningAktivitetAggregatDto> forrigeRegister = forrigeGrunnlag.map(BeregningsgrunnlagGrunnlagDto::getRegisterAktiviteter);
        return forrigeRegister.map(aktivitetAggregatEntitet -> !BeregningsgrunnlagDiffSjekker.harSignifikantDiffIAktiviteter(nyttRegister, aktivitetAggregatEntitet)).orElse(false);
    }


    private static boolean kanKopiereBeregningsgrunnlag(Optional<BeregningsgrunnlagDto> nyttBg, Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlag) {
        if (nyttBg.isEmpty()) {
            return forrigeBeregningsgrunnlag.isEmpty();
        }
        return forrigeBeregningsgrunnlag.map(bg -> !BeregningsgrunnlagDiffSjekker.harSignifikantDiffIBeregningsgrunnlag(nyttBg.get(), bg)).orElse(false);
    }

}
