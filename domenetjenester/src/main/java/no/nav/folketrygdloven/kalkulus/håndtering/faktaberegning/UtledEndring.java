package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaOmBeregningHåndteringDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.BeregningsgrunnlagPeriodeEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.FaktaOmBeregningVurderinger;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;

public class UtledEndring {

    private UtledEndring() {
        // skjul
    }

    public static OppdateringRespons utled(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto, BeregningsgrunnlagGrunnlagDto grunnlagFraSteg, Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag, FaktaOmBeregningHåndteringDto dto) {
        BeregningsgrunnlagDto beregningsgrunnlagDto = beregningsgrunnlagGrunnlagDto.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalArgumentException("Skal ha beregningsgrunnlag her"));
        BeregningsgrunnlagDto bgFraSteg = grunnlagFraSteg.getBeregningsgrunnlag()
                .orElseThrow(() -> new IllegalArgumentException("Skal ha beregningsgrunnlag her"));
        Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt = forrigeGrunnlag.flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
        BeregningsgrunnlagEndring beregningsgrunnlagEndring = utledBeregningsgrunnlagEndring(beregningsgrunnlagDto, bgFraSteg, forrigeBeregningsgrunnlagOpt);
        FaktaOmBeregningVurderinger faktaOmBeregningVurderinger = UtledFaktaOmBeregningVurderinger.utled(dto, beregningsgrunnlagDto, forrigeBeregningsgrunnlagOpt);
        return new OppdateringRespons(beregningsgrunnlagEndring, faktaOmBeregningVurderinger);
    }

    private static BeregningsgrunnlagEndring utledBeregningsgrunnlagEndring(BeregningsgrunnlagDto beregningsgrunnlagEntitet, BeregningsgrunnlagDto bgFraSteg, Optional<BeregningsgrunnlagDto> forrigeBeregningsgrunnlagOpt) {
        List<BeregningsgrunnlagPeriodeDto> perioder = beregningsgrunnlagEntitet.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> perioderFraSteg = bgFraSteg.getBeregningsgrunnlagPerioder();
        List<BeregningsgrunnlagPeriodeDto> forrigePerioder = forrigeBeregningsgrunnlagOpt.map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder).orElse(Collections.emptyList());
        List<BeregningsgrunnlagPeriodeEndring> beregningsgrunnlagPeriodeEndringer = utledPeriodeEndringer(perioder, perioderFraSteg, forrigePerioder);
        return beregningsgrunnlagPeriodeEndringer.isEmpty() ? null : new BeregningsgrunnlagEndring(beregningsgrunnlagPeriodeEndringer);
    }

    private static List<BeregningsgrunnlagPeriodeEndring> utledPeriodeEndringer(List<BeregningsgrunnlagPeriodeDto> perioder, List<BeregningsgrunnlagPeriodeDto> perioderFraSteg, List<BeregningsgrunnlagPeriodeDto> forrigePerioder) {
        return perioder.stream()
                    .map(p -> {
                        BeregningsgrunnlagPeriodeDto periodeFraSteg = finnPeriode(perioderFraSteg, p.getBeregningsgrunnlagPeriodeFom())
                                .orElseThrow(() -> new IllegalStateException("Skal ikke ha endring i periode fra steg"));
                        Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = finnPeriode(forrigePerioder, p.getBeregningsgrunnlagPeriodeFom());
                        return UtledEndringIPeriode.utled(p, periodeFraSteg, forrigePeriode);
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
    }

    private static Optional<BeregningsgrunnlagPeriodeDto> finnPeriode(List<BeregningsgrunnlagPeriodeDto> forrigePerioder, LocalDate beregningsgrunnlagPeriodeFom) {
        return forrigePerioder.stream().filter(p -> p.getBeregningsgrunnlagPeriodeFom().equals(beregningsgrunnlagPeriodeFom)).findFirst();
    }
}
