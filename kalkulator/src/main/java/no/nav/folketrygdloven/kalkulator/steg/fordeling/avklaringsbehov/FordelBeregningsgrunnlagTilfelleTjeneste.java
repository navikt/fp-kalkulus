package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

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

        boolean andelHarRefusjonIPerioden = harInnvilgetRefusjon(andel);
        boolean harGraderingIBGPeriode = FordelingGraderingTjeneste.harGraderingForAndelIPeriode(andel, input.getAktivitetGradering(), periode.getPeriode());
        if (!harGraderingIBGPeriode && !andelHarRefusjonIPerioden) {
            return Optional.empty();
        }

        if (FordelingGraderingTjeneste.skalGraderePåAndelUtenBeregningsgrunnlag(andel, harGraderingIBGPeriode)) {
            return Optional.of(FordelingTilfelle.FORESLÅTT_BG_PÅ_GRADERT_ANDEL_ER_0);
        }

        if (harGraderingIBGPeriode && !andelHarRefusjonIPerioden) {
            Beløp grunnbeløp = input.getBeregningsgrunnlag().getGrunnbeløp();
            if (FordelingGraderingTjeneste.gradertAndelVilleBlittAvkortet(andel, grunnbeløp, periode)) {
                return Optional.of(FordelingTilfelle.GRADERT_ANDEL_SOM_VILLE_HA_BLITT_AVKORTET_TIL_0);
            }
            boolean refusjonForPeriodeOverstiger6G = grunnbeløp.multipliser(6).getVerdi().compareTo(finnTotalRefusjonPrÅr(periode)) <= 0;
            if (refusjonForPeriodeOverstiger6G) {
                return Optional.of(FordelingTilfelle.TOTALT_REFUSJONSKRAV_STØRRE_ENN_6G);
            }
        }
        return Optional.empty();
    }

    private static Boolean harInnvilgetRefusjon(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
                .map(r -> r.compareTo(BigDecimal.ZERO) > 0).orElse(false);
    }

    private static BigDecimal finnTotalRefusjonPrÅr(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().flatMap(a -> a.getBgAndelArbeidsforhold().stream())
                .map(BGAndelArbeidsforholdDto::getRefusjonskravPrÅr)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static boolean erAutomatiskFordelt(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return erInntektskategoriSatt(andel) && andel.getFordeltPrÅr() != null;
    }

    private static boolean erInntektskategoriSatt(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getInntektskategori() != null && !andel.getInntektskategori().equals(Inntektskategori.UDEFINERT);
    }
}
