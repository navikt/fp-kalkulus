package no.nav.folketrygdloven.kalkulator.rest.fakta;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.VurderMilitærDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

@ApplicationScoped
public class VurderMilitærDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    VurderMilitærDtoTjeneste() {
        // For CDI
    }

    @Override
    public void lagDto(BeregningsgrunnlagRestInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        List<BeregningsgrunnlagAktivitetStatusDto> aktivitetStatuser = input.getBeregningsgrunnlag().getAktivitetStatuser();
        BeregningsgrunnlagTilstand aktivTilstand = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand();
        VurderMilitærDto dto;
        if (aktivTilstand.erFør(BeregningsgrunnlagTilstand.KOFAKBER_UT)) {
            dto = new VurderMilitærDto(null);
        } else {
            dto = new VurderMilitærDto(aktivitetStatuser.stream().anyMatch(status -> status.getAktivitetStatus().equals(AktivitetStatus.MILITÆR_ELLER_SIVIL)));
        }
        faktaOmBeregningDto.setVurderMilitaer(dto);
    }
}
