package no.nav.folketrygdloven.kalkulator.endringsresultat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;

public class ErEndringIBeregning {
    private ErEndringIBeregning() {
    }

    public static boolean vurder(Optional<BeregningsgrunnlagDto> revurderingsGrunnlag, Optional<BeregningsgrunnlagDto> originaltGrunnlag) {
        if (revurderingsGrunnlag.isEmpty() && originaltGrunnlag.isEmpty()) {
            return false;
        } else if (revurderingsGrunnlag.isEmpty() || originaltGrunnlag.isEmpty()) {
            return true;
        }

        List<BeregningsgrunnlagPeriodeDto> originalePerioder = originaltGrunnlag.get().getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> revurderingsPerioder = revurderingsGrunnlag.get().getBeregningsgrunnlagPerioder();

        List<LocalDate> allePeriodeDatoer = finnAllePeriodersStartdatoer(revurderingsPerioder, originalePerioder);

        for (LocalDate dato : allePeriodeDatoer) {
            Long dagsatsRevurderingsgrunnlag = finnGjeldendeDagsatsForDenneDatoen(dato, revurderingsPerioder);
            Long dagsatsOriginaltGrunnlag = finnGjeldendeDagsatsForDenneDatoen(dato, originalePerioder);
            if (!dagsatsRevurderingsgrunnlag.equals(dagsatsOriginaltGrunnlag)) {
                return true;
            }
        }
        return false;
    }

    private static List<LocalDate> finnAllePeriodersStartdatoer(List<BeregningsgrunnlagPeriodeDto> revurderingsPerioder, List<BeregningsgrunnlagPeriodeDto> originalePerioder) {
        List<LocalDate> startDatoer = new ArrayList<>();
        for (BeregningsgrunnlagPeriodeDto periode : revurderingsPerioder) {
            startDatoer.add(periode.getBeregningsgrunnlagPeriodeFom());
        }
        for (BeregningsgrunnlagPeriodeDto periode : originalePerioder) {
            if (!startDatoer.contains(periode.getBeregningsgrunnlagPeriodeFom())) {
                startDatoer.add(periode.getBeregningsgrunnlagPeriodeFom());
            }
        }
        return startDatoer;
    }

    private static Long finnGjeldendeDagsatsForDenneDatoen(LocalDate dato, List<BeregningsgrunnlagPeriodeDto> perioder) {
        // Hvis dato er før starten på den første perioden bruker vi første periodes dagsats
        Optional<BeregningsgrunnlagPeriodeDto> førsteKronologiskePeriode = perioder.stream()
            .min(Comparator.comparing(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom));
        if (førsteKronologiskePeriode.filter(periode -> dato.isBefore(periode.getBeregningsgrunnlagPeriodeFom())).isPresent()) {
            return førsteKronologiskePeriode.get().getDagsats();
        }
        for (BeregningsgrunnlagPeriodeDto periode : perioder) {
            if (periode.getPeriode().inkluderer(dato)) {
                return periode.getDagsats();
            }
        }
        throw new IllegalStateException("Finner ikke dagsats for denne perioden");
    }

}
