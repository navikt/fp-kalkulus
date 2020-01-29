package no.nav.folketrygdloven.kalkulator.rest.fakta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;

@ApplicationScoped
public class FaktaOmBeregningDtoTjeneste {

    private List<FaktaOmBeregningTilfelleDtoTjeneste> dtoTjenester;
    private AndelerForFaktaOmBeregningTjeneste andelerForFaktaOmBeregningTjeneste;


    @Inject
    public FaktaOmBeregningDtoTjeneste(@Any Instance<FaktaOmBeregningTilfelleDtoTjeneste> dtoTjenesteInstance,
                                       AndelerForFaktaOmBeregningTjeneste andelerForFaktaOmBeregningTjeneste) {
        this.dtoTjenester = dtoTjenesteInstance.stream().collect(Collectors.toList());
        this.andelerForFaktaOmBeregningTjeneste = andelerForFaktaOmBeregningTjeneste;
    }

    public Optional<FaktaOmBeregningDto> lagDto(BeregningsgrunnlagRestInput input) {
        FaktaOmBeregningDto faktaOmBeregningDto = new FaktaOmBeregningDto();
        var grunnlagEntitet = input.getBeregningsgrunnlagGrunnlag();
        BeregningAktivitetAggregatRestDto registerAktivitetAggregat = Optional.ofNullable(grunnlagEntitet.getRegisterAktiviteter())
            .orElse(grunnlagEntitet.getGjeldendeAktiviteter());
        Optional<BeregningAktivitetAggregatRestDto> saksbehandletAktivitetAggregat = grunnlagEntitet.getOverstyrteEllerSaksbehandletAktiviteter();

        faktaOmBeregningDto.setAndelerForFaktaOmBeregning(andelerForFaktaOmBeregningTjeneste.lagAndelerForFaktaOmBeregning(input));

        Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon = input.getIayGrunnlag().getArbeidsforholdInformasjon();
        AvklarAktiviteterDtoTjeneste.lagAvklarAktiviteterDto(input.getSkjæringstidspunktForBeregning(), registerAktivitetAggregat,
            saksbehandletAktivitetAggregat, arbeidsforholdInformasjon, faktaOmBeregningDto);
        BeregningsgrunnlagRestDto beregningsgrunnlag = grunnlagEntitet.getBeregningsgrunnlag().orElseThrow();
        if (skalVurdereFaktaForATFL(beregningsgrunnlag)) {
            List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
            faktaOmBeregningDto.setFaktaOmBeregningTilfeller(tilfeller);
            utledDtoerForTilfeller(input, faktaOmBeregningDto);
        }
        return Optional.of(faktaOmBeregningDto);
    }

    private boolean skalVurdereFaktaForATFL(BeregningsgrunnlagRestDto beregningsgrunnlag) {
        return !beregningsgrunnlag.getFaktaOmBeregningTilfeller().isEmpty();
    }

    private void utledDtoerForTilfeller(BeregningsgrunnlagRestInput input,
                                        FaktaOmBeregningDto faktaOmBeregningDto) {
        dtoTjenester.forEach(dtoTjeneste -> dtoTjeneste.lagDto(input, faktaOmBeregningDto));
    }
}
