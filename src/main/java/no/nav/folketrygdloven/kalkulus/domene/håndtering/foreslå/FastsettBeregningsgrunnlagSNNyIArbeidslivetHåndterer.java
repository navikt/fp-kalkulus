package no.nav.folketrygdloven.kalkulus.domene.håndtering.foreslå;


import static no.nav.folketrygdloven.kalkulus.domene.håndtering.mapping.OppdatererDtoMapper.mapFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetHåndterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.HåndteringResultat;
import no.nav.folketrygdloven.kalkulus.domene.håndtering.UtledEndring;
import no.nav.foreldrepenger.kalkulus.kontrakt.request.håndtering.foreslå.FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto.class, adapter = BeregningHåndterer.class)
public class FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndterer implements BeregningHåndterer<FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto> {

    @Override
    public HåndteringResultat håndter(FastsettBeregningsgrunnlagSNNyIArbeidslivetHåndteringDto dto, HåndterBeregningsgrunnlagInput beregningsgrunnlagInput) {
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetHåndterer.oppdater(beregningsgrunnlagInput, mapFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(dto.getFastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto()));
        Optional<BeregningsgrunnlagGrunnlagDto> forrigeGrunnlag = beregningsgrunnlagInput.getForrigeGrunnlagFraHåndteringTilstand();
        BeregningsgrunnlagGrunnlagDto grunnlagFraSteg = beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag();
        var endring = UtledEndring.standard().utled(nyttGrunnlag, grunnlagFraSteg, forrigeGrunnlag, dto, beregningsgrunnlagInput.getIayGrunnlag());
        return new HåndteringResultat(nyttGrunnlag, endring);
    }

}
