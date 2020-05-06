package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

@ApplicationScoped
@FagsakYtelseTypeRef("OMP")
public class OmsorgspengerGrunnlagMapper implements YtelsesspesifikkRegelMapper {

    @Override
    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagDto beregningsgrunnlagDto, BeregningsgrunnlagInput input) {
        BigDecimal gradertRefusjonVedSkjæringstidspunkt = finnGradertRefusjonskravPåSkjæringstidspunktet(input.getInntektsmeldinger(), beregningsgrunnlagDto.getSkjæringstidspunkt(), input.getYtelsespesifiktGrunnlag());
        return new OmsorgspengerGrunnlag(gradertRefusjonVedSkjæringstidspunkt);
    }

}
