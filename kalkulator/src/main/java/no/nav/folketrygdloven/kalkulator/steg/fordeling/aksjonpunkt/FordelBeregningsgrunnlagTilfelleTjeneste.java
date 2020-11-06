package no.nav.folketrygdloven.kalkulator.steg.fordeling.aksjonpunkt;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public final class FordelBeregningsgrunnlagTilfelleTjeneste {

    private FordelBeregningsgrunnlagTilfelleTjeneste() {
        // Skjuler default konstruktør
    }

    public static boolean harTilfelleForFordeling(FordelBeregningsgrunnlagTilfelleInput input) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> andelTilfelleMap = vurderManuellBehandling(input);
        return !andelTilfelleMap.isEmpty();

    }

    public static Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> vurderManuellBehandling(FordelBeregningsgrunnlagTilfelleInput input) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        for (BeregningsgrunnlagPeriodeDto periode : beregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> tilfelleMap = vurderManuellBehandlingForPeriode(periode, input);
            if (!tilfelleMap.isEmpty())
                return tilfelleMap;
        }
        return Collections.emptyMap();
    }

    public static Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> vurderManuellBehandlingForPeriode(BeregningsgrunnlagPeriodeDto periode,
                                                                                                                 FordelBeregningsgrunnlagTilfelleInput input) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> andelTilfelleMap = new HashMap<>();
        for (BeregningsgrunnlagPrStatusOgAndelDto andel : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            Optional<FordelingTilfelle> tilfelle = utledTilfelleForAndel(periode, input, andel);
            tilfelle.ifPresent(fordelingTilfelle -> andelTilfelleMap.put(andel, fordelingTilfelle));
        }
        return andelTilfelleMap;
    }

    private static Optional<FordelingTilfelle> utledTilfelleForAndel(BeregningsgrunnlagPeriodeDto periode, FordelBeregningsgrunnlagTilfelleInput input, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (FordelTilkommetArbeidsforholdTjeneste.erAktivitetLagtTilIPeriodisering(andel) && !erAutomatiskFordelt(andel)) {
            return Optional.of(FordelingTilfelle.NY_AKTIVITET);
        }

        BigDecimal refusjonForAndelIPeriode = BeregningInntektsmeldingTjeneste.finnRefusjonskravPrÅrIPeriodeForAndel(andel, periode.getPeriode(), input.getInntektsmeldinger()).orElse(BigDecimal.ZERO);
        boolean andelHarRefusjonIPerioden = refusjonForAndelIPeriode.compareTo(BigDecimal.ZERO) > 0;
        boolean harGraderingIBGPeriode = FordelingGraderingTjeneste.harGraderingForAndelIPeriode(andel, input.getAktivitetGradering(), periode.getPeriode());
        if (!harGraderingIBGPeriode && !andelHarRefusjonIPerioden) {
            return Optional.empty();
        }

        if (FordelingGraderingTjeneste.skalGraderePåAndelUtenBeregningsgrunnlag(andel, harGraderingIBGPeriode)) {
            return Optional.of(FordelingTilfelle.FORESLÅTT_BG_PÅ_GRADERT_ANDEL_ER_0);
        }

        if (harGraderingIBGPeriode && !andelHarRefusjonIPerioden) {
            Beløp grunnbeløp = input.getBeregningsgrunnlag().getGrunnbeløp();
            boolean refusjonForPeriodeOverstiger6G = BeregningInntektsmeldingTjeneste.erTotaltRefusjonskravStørreEnnEllerLikSeksG(periode, input.getInntektsmeldinger(), grunnbeløp);
            if (refusjonForPeriodeOverstiger6G) {
                return Optional.of(FordelingTilfelle.TOTALT_REFUSJONSKRAV_STØRRE_ENN_6G);
            }
            if (FordelingGraderingTjeneste.gradertAndelVilleBlittAvkortet(andel, grunnbeløp, periode)) {
                return Optional.of(FordelingTilfelle.GRADERT_ANDEL_SOM_VILLE_HA_BLITT_AVKORTET_TIL_0);
            }
        }
        return Optional.empty();
    }

    private static boolean erAutomatiskFordelt(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getInntektskategori() != null && andel.getFordeltPrÅr() != null;
    }
}
