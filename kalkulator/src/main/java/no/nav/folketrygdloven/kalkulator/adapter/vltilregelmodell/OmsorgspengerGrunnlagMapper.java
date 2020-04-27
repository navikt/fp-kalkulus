package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class OmsorgspengerGrunnlagMapper implements YtelsesspesifikkRegelMapper {

    @Override
    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlagDto, BeregningsgrunnlagInput input) {
        List<OmsorgspengerGrunnlagPeriode> ompPerioder = beregningsgrunnlagDto.getBeregningsgrunnlagPerioder().stream()
                .map(p -> {
                    BigDecimal lavesteTotalRefusjon = MapRefusjonskravFraVLTilRegel.finnLavesteTotalRefusjonForBGPerioden(p,
                            input.getInntektsmeldinger(),
                            beregningsgrunnlagDto.getSkjæringstidspunkt(),
                            input.getYtelsespesifiktGrunnlag());
                    return new OmsorgspengerGrunnlagPeriode(Periode.of(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeFom()), lavesteTotalRefusjon);
                })
                .collect(Collectors.toList());
        return new OmsorgspengerGrunnlag(ompPerioder);
    }

}
