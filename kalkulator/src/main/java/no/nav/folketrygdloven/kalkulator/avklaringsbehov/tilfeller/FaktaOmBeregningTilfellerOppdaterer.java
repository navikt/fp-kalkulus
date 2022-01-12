package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


@ApplicationScoped
public class FaktaOmBeregningTilfellerOppdaterer {

    private Instance<FaktaOmBeregningTilfelleOppdaterer> faktaOmBeregningTilfelleOppdaterer;

    FaktaOmBeregningTilfellerOppdaterer() {
        // for CDI proxy
    }

    @Inject
    FaktaOmBeregningTilfellerOppdaterer(@Any Instance<FaktaOmBeregningTilfelleOppdaterer> faktaOmBeregningTilfelleOppdaterer) {
        this.faktaOmBeregningTilfelleOppdaterer = faktaOmBeregningTilfelleOppdaterer;
    }

    public void oppdater(FaktaBeregningLagreDto faktaDto,
                         Optional<BeregningsgrunnlagDto> forrigeBg,
                         BeregningsgrunnlagInput input,
                         BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        kjørOppdateringForTilfeller(faktaDto, forrigeBg, input, grunnlagBuilder);
        List<FaktaOmBeregningTilfelle> tilfeller = faktaDto.getFaktaOmBeregningTilfeller();
        settNyeFaktaOmBeregningTilfeller(grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag(), tilfeller);
    }

    private void kjørOppdateringForTilfeller(FaktaBeregningLagreDto faktaDto,
                                             Optional<BeregningsgrunnlagDto> forrigeBg,
                                             BeregningsgrunnlagInput input,
                                             BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        faktaDto.getFaktaOmBeregningTilfeller()
            .stream()
            .map(kode -> FaktaOmBeregningTilfelleRef.Lookup.find(faktaOmBeregningTilfelleOppdaterer, kode))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(oppdaterer -> oppdaterer.oppdater(faktaDto, forrigeBg, input, grunnlagBuilder));
    }

    private void settNyeFaktaOmBeregningTilfeller(BeregningsgrunnlagDto nyttBeregningsgrunnlag, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        List<FaktaOmBeregningTilfelle> utledetTilfeller = nyttBeregningsgrunnlag.getFaktaOmBeregningTilfeller();
        List<FaktaOmBeregningTilfelle> tilfellerLagtTilManuelt = faktaOmBeregningTilfeller.stream()
            .filter(tilfelle -> !utledetTilfeller.contains(tilfelle)).collect(Collectors.toList());
        if (!tilfellerLagtTilManuelt.isEmpty()) {
            BeregningsgrunnlagDto.Builder.oppdater(Optional.of(nyttBeregningsgrunnlag)).leggTilFaktaOmBeregningTilfeller(tilfellerLagtTilManuelt);
        }
    }
}
