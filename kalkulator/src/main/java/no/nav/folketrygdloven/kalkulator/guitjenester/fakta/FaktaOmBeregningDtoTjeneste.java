package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger.SaksopplysningerTjeneste.lagSaksopplysninger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

@ApplicationScoped
public class FaktaOmBeregningDtoTjeneste {

    private List<FaktaOmBeregningTilfelleDtoTjeneste> dtoTjenester;

    public FaktaOmBeregningDtoTjeneste() {
        // CDI
    }

    @Inject
    public FaktaOmBeregningDtoTjeneste(@Any Instance<FaktaOmBeregningTilfelleDtoTjeneste> dtoTjenesteInstance) {
        this.dtoTjenester = dtoTjenesteInstance.stream().collect(Collectors.toList());
    }

    // TODO (Denne burde splittes i ein del som krever bg og ein del som ikkje krever det)
    public Optional<FaktaOmBeregningDto> lagDto(BeregningsgrunnlagGUIInput input) {
        FaktaOmBeregningDto faktaOmBeregningDto = new FaktaOmBeregningDto();
        var grunnlagEntitet = input.getBeregningsgrunnlagGrunnlag();
        BeregningAktivitetAggregatDto registerAktivitetAggregat = Optional.ofNullable(grunnlagEntitet.getRegisterAktiviteter())
                .orElse(grunnlagEntitet.getGjeldendeAktiviteter());
        Optional<BeregningAktivitetAggregatDto> saksbehandletAktivitetAggregat = grunnlagEntitet.getOverstyrteEllerSaksbehandletAktiviteter();
        Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon = input.getIayGrunnlag().getArbeidsforholdInformasjon();
        AvklarAktiviteterDtoTjeneste.lagAvklarAktiviteterDto(registerAktivitetAggregat,
                saksbehandletAktivitetAggregat, arbeidsforholdInformasjon, faktaOmBeregningDto);

        // Denne delen krever Beregningsgrunnlag
        if (grunnlagEntitet.getBeregningsgrunnlag().isPresent() && !grunnlagEntitet.getBeregningsgrunnlag().get().getBeregningsgrunnlagPerioder().isEmpty()) {
            faktaOmBeregningDto.setAndelerForFaktaOmBeregning(AndelerForFaktaOmBeregningTjeneste.lagAndelerForFaktaOmBeregning(input));
            faktaOmBeregningDto.setSaksopplysninger(lagSaksopplysninger(input));
            BeregningsgrunnlagDto beregningsgrunnlag = grunnlagEntitet.getBeregningsgrunnlag().orElseThrow();
            if (skalVurdereFaktaForATFL(beregningsgrunnlag)) {
                List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
                faktaOmBeregningDto.setFaktaOmBeregningTilfeller(tilfeller.stream().collect(Collectors.toList()));
                utledDtoerForTilfeller(input, faktaOmBeregningDto);
            }
        }
        return Optional.of(faktaOmBeregningDto);
    }

    private boolean skalVurdereFaktaForATFL(BeregningsgrunnlagDto beregningsgrunnlag) {
        return !beregningsgrunnlag.getFaktaOmBeregningTilfeller().isEmpty();
    }

    private void utledDtoerForTilfeller(BeregningsgrunnlagGUIInput input,
                                        FaktaOmBeregningDto faktaOmBeregningDto) {
        dtoTjenester.forEach(dtoTjeneste -> dtoTjeneste.lagDto(input, faktaOmBeregningDto));
    }
}
