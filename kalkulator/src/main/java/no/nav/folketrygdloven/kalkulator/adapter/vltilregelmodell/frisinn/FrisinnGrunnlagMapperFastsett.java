package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.FrisinnPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.YtelsesspesifikkRegelMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.FRISINN)
public class FrisinnGrunnlagMapperFastsett implements YtelsesspesifikkRegelMapper {

    @Override
    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlagDto, BeregningsgrunnlagInput input) {
        if (!(input.getYtelsespesifiktGrunnlag() instanceof no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag)) {
            throw new IllegalStateException("Mangler frisinngrunnlag for frisinnberegning");
        }
        List<FrisinnPeriode> regelPerioder = mapFrisinnPerioder(input);
        return new FrisinnGrunnlag(regelPerioder);
    }

    public static List<FrisinnPeriode> mapFrisinnPerioder(BeregningsgrunnlagInput input) {
        no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        return frisinnGrunnlag.getFrisinnPerioder().stream()
                .map(fg -> new FrisinnPeriode(mapPeriode(fg.getPeriode()), fg.getSøkerFrilans(), fg.getSøkerNæring()))
                .collect(Collectors.toList());
    }

    private static Periode mapPeriode(Intervall periode) {
        return new Periode(periode.getFomDato(), periode.getTomDato());
    }
}
