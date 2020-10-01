package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AndelerMedØktRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;

@ApplicationScoped
public class VurderRefusjonDtoTjeneste {
    private AndelerMedØktRefusjonTjeneste andelerMedØktRefusjonTjeneste;

    VurderRefusjonDtoTjeneste() {
        // CDI
    }

    @Inject
    public VurderRefusjonDtoTjeneste(AndelerMedØktRefusjonTjeneste andelerMedØktRefusjonTjeneste) {
        this.andelerMedØktRefusjonTjeneste = andelerMedØktRefusjonTjeneste;
    }

    public Optional<RefusjonTilVurderingDto> lagDto(BeregningsgrunnlagGUIInput input) {
        Optional<BeregningsgrunnlagDto> orginaltBG = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);
        if (orginaltBG.isEmpty()) {
            return Optional.empty();
        }

        BeregningsgrunnlagInput bgInput = new BeregningsgrunnlagInput(input.getKoblingReferanse(),
                input.getIayGrunnlag(),
                null,
                input.getAktivitetGradering(),
                input.getRefusjonskravDatoer(),
                input.getYtelsespesifiktGrunnlag());

        BeregningsgrunnlagInput medBGSomBrukesForÅUtledeAndeler = bgInput.medBeregningsgrunnlagGrunnlag(input.getVurderRefusjonBeregningsgrunnlagGrunnlag().orElse(input.getBeregningsgrunnlagGrunnlag()));
        BeregningsgrunnlagGrunnlagDto orginaltBeregningsgrunnlagGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().get();
        BeregningsgrunnlagInput endeligInput = medBGSomBrukesForÅUtledeAndeler.medBeregningsgrunnlagGrunnlagFraForrigeBehandling(orginaltBeregningsgrunnlagGrunnlag);
        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjon = andelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(endeligInput);
        return LagVurderRefusjonDto.lagDto(andelerMedØktRefusjon, input);
    }

}
