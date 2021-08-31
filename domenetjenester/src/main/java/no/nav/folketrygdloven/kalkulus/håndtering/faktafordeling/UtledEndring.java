package no.nav.folketrygdloven.kalkulus.håndtering.faktafordeling;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagPeriodeEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.Endringer;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;

public class UtledEndring {

    private UtledEndring() {
        // skjul
    }

    public static Endringer utled(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto, Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlagDto = beregningsgrunnlagGrunnlagDto.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalArgumentException("Skal ha beregningsgrunnlag her"));
        Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt = forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
        BeregningsgrunnlagEndring beregningsgrunnlagEndring = utledBeregningsgrunnlagEndring(beregningsgrunnlagDto, forrigeBeregningsgrunnlagOpt);
        return new Endringer(beregningsgrunnlagEndring);
    }

    private static BeregningsgrunnlagEndring utledBeregningsgrunnlagEndring(BeregningsgrunnlagDto beregningsgrunnlagEntitet, Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt) {
        List<BeregningsgrunnlagPeriodeDto> perioder = beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> forrigePerioder = forrigeBeregningsgrunnlagOpt.map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder).orElse(Collections.emptyList());
        return new BeregningsgrunnlagEndring(utledPeriodeEndringer(perioder, forrigePerioder));
    }

    private static List<BeregningsgrunnlagPeriodeEndring> utledPeriodeEndringer(List<BeregningsgrunnlagPeriodeDto> perioder, List<BeregningsgrunnlagPeriodeDto> forrigePerioder) {
        return perioder.stream()
                    .map(p -> {
                        Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = finnPeriode(forrigePerioder, p.getBeregningsgrunnlagPeriodeFom());
                        return UtledEndringIPeriode.utled(p, forrigePeriode);
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
    }

    private static Optional<BeregningsgrunnlagPeriodeDto> finnPeriode(List<BeregningsgrunnlagPeriodeDto> forrigePerioder, LocalDate beregningsgrunnlagPeriodeFom) {
        return forrigePerioder.stream().filter(p -> p.getBeregningsgrunnlagPeriodeFom().equals(beregningsgrunnlagPeriodeFom)).findFirst();
    }
}
