package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.FastsettBeregningVerdierTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BesteberegningFødendeKvinneAndelDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.BesteberegningFødendeKvinneDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.DagpengeAndelLagtTilBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierForBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;


@ApplicationScoped
@FaktaOmBeregningTilfelleRef("FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE")
public class FastsettBesteberegningFødendeKvinneOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto,
                         Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        BesteberegningFødendeKvinneDto besteberegningDto = dto.getBesteberegningAndeler();
        List<BesteberegningFødendeKvinneAndelDto> andelListe = besteberegningDto.getBesteberegningAndelListe();
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = grunnlagBuilder.getBeregningsgrunnlagBuilder();
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = beregningsgrunnlagBuilder.getBeregningsgrunnlag();
        for (var periode : nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = forrigeBg
                .flatMap(beregningsgrunnlag -> beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                    .filter(periode1 -> periode1.getPeriode().overlapper(periode.getPeriode())).findFirst());
            andelListe.forEach(dtoAndel -> FastsettBeregningVerdierTjeneste.fastsettVerdierForAndel(mapTilRedigerbarAndel(dtoAndel), mapTilFastsatteVerdier(dtoAndel), periode, forrigePeriode));
            if (besteberegningDto.getNyDagpengeAndel() != null) {
                FastsettBeregningVerdierTjeneste.fastsettVerdierForAndel(lagRedigerbarAndelDtoForDagpenger(), mapTilFastsatteVerdier(besteberegningDto.getNyDagpengeAndel()), periode, forrigePeriode);
            }
        }
        if (nyttBeregningsgrunnlag.getAktivitetStatuser().stream().noneMatch(status -> AktivitetStatus.DAGPENGER.equals(status.getAktivitetStatus()))) {
            beregningsgrunnlagBuilder
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER));
        }
    }

    private FastsatteVerdierDto mapTilFastsatteVerdier(DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel) {
        FastsatteVerdierForBesteberegningDto fastsatteVerdier = nyDagpengeAndel.getFastsatteVerdier();
        return new FastsatteVerdierDto(fastsatteVerdier.finnFastsattBeløpPrÅr().intValue(), fastsatteVerdier.getInntektskategori(), true);
    }

    private RedigerbarAndelFaktaOmBeregningDto lagRedigerbarAndelDtoForDagpenger() {
        return new RedigerbarAndelFaktaOmBeregningDto(AktivitetStatus.DAGPENGER);
    }

    private RedigerbarAndelFaktaOmBeregningDto mapTilRedigerbarAndel(BesteberegningFødendeKvinneAndelDto dtoAndel) {
        return new RedigerbarAndelFaktaOmBeregningDto(false, dtoAndel.getAndelsnr(), dtoAndel.getLagtTilAvSaksbehandler());
    }

    private FastsatteVerdierDto mapTilFastsatteVerdier(BesteberegningFødendeKvinneAndelDto dtoAndel) {
        FastsatteVerdierForBesteberegningDto fastsatteVerdier = dtoAndel.getFastsatteVerdier();
        return new FastsatteVerdierDto(fastsatteVerdier.finnFastsattBeløpPrÅr().intValue(), fastsatteVerdier.getInntektskategori(), fastsatteVerdier.getSkalHaBesteberegning());
    }

}
