package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.SvangerskapspengerGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;

@ApplicationScoped
@FagsakYtelseTypeRef("SVP")
public class YtelsespesifiktGrunnlagTjenesteSVP implements YtelsespesifiktGrunnlagTjeneste {

    @Override
    public Optional<YtelsespesifiktGrunnlagDto> map(BeregningsgrunnlagGUIInput input) {
        return Optional.of(new SvangerskapspengerGrunnlagDto());
    }
}
