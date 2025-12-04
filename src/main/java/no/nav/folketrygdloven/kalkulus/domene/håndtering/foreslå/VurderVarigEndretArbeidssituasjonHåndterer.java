package no.nav.folketrygdloven.kalkulus.domene.håndtering.foreslå;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.VurderVarigEndretEllerNyoppstartetHåndterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.UtledEndring;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.VurderVarigEndretArbeidssituasjonHåndteringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderVarigEndretArbeidssituasjonHåndteringDto.class, adapter = BeregningHåndterer.class)
class VurderVarigEndretArbeidssituasjonHåndterer implements BeregningHåndterer<VurderVarigEndretArbeidssituasjonHåndteringDto> {

    @Override
    public HåndteringResultat håndter(VurderVarigEndretArbeidssituasjonHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = VurderVarigEndretEllerNyoppstartetHåndterer.håndter(beregningsgrunnlagInput, dto.getBruttoBeregningsgrunnlag(), AktivitetStatus.BRUKERS_ANDEL);
        Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag = beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand();
        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.standard().utled(nyttGrunnlag, grunnlagFraSteg, forrigeGrunnlag, dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
