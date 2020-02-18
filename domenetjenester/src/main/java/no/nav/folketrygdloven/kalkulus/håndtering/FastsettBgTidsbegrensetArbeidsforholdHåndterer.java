package no.nav.folketrygdloven.kalkulus.håndtering;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBGTidsbegrensetArbeidsforholdHåndterer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.FastsettBGTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBGTidsbegrensetArbeidsforholdDto.class, adapter = BeregningHåndterer.class)
public class FastsettBgTidsbegrensetArbeidsforholdHåndterer implements BeregningHåndterer<FastsettBGTidsbegrensetArbeidsforholdDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(FastsettBGTidsbegrensetArbeidsforholdDto dto, BeregningsgrunnlagInput beregningsgrunnlagInput) {
        return FastsettBGTidsbegrensetArbeidsforholdHåndterer.håndter(beregningsgrunnlagInput, OppdatererDtoMapper.mapFastsettBGTidsbegrensetArbeidsforholdDto(dto));
    }


}
