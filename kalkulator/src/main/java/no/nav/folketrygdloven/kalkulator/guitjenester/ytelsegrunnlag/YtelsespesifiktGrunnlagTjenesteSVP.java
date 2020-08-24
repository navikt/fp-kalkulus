package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.SvangerskapspengerGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;

@ApplicationScoped
@FagsakYtelseTypeRef("SVP")
public class YtelsespesifiktGrunnlagTjenesteSVP implements YtelsespesifiktGrunnlagTjeneste {

    @Override
    public Optional<YtelsespesifiktGrunnlagDto> map(BeregningsgrunnlagRestInput input) {
        return Optional.of(new SvangerskapspengerGrunnlagDto());
    }
}
