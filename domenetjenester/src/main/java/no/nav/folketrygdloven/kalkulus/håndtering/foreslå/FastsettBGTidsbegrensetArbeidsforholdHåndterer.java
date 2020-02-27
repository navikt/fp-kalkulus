package no.nav.folketrygdloven.kalkulus.håndtering.foreslå;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.håndtering.BeregningHåndterer;
import no.nav.folketrygdloven.kalkulus.håndtering.DtoTilServiceAdapter;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter.AvklarAktiviteterHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå.FastsettBGTidsbegrensetArbeidsforholdHåndteringDto;
import no.nav.folketrygdloven.kalkulus.mappers.OppdatererDtoMapper;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FastsettBGTidsbegrensetArbeidsforholdHåndteringDto.class, adapter = BeregningHåndterer.class)
class FastsettBGTidsbegrensetArbeidsforholdHåndterer implements BeregningHåndterer<FastsettBGTidsbegrensetArbeidsforholdHåndteringDto> {

    @Override
    public BeregningsgrunnlagGrunnlagDto håndter(FastsettBGTidsbegrensetArbeidsforholdHåndteringDto dto, BeregningsgrunnlagInput input) {
        return no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBGTidsbegrensetArbeidsforholdHåndterer.håndter(input, OppdatererDtoMapper.mapFastsettBGTidsbegrensetArbeidsforholdDto(dto.getFastsettBGTidsbegrensetArbeidsforholdDto()));
    }

}
