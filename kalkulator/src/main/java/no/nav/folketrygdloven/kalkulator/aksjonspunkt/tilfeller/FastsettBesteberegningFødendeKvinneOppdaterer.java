package no.nav.folketrygdloven.kalkulator.aksjonspunkt.tilfeller;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.FastsettFaktaOmBeregningVerdierTjeneste;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.BesteberegningFødendeKvinneAndelDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.BesteberegningFødendeKvinneDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.DagpengeAndelLagtTilBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.FastsatteVerdierForBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;


@ApplicationScoped
@FaktaOmBeregningTilfelleRef("FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE")
public class FastsettBesteberegningFødendeKvinneOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto,
                         Optional<BeregningsgrunnlagDto> forrigeBg,
                         BeregningsgrunnlagInput input,
                         BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        BesteberegningFødendeKvinneDto besteberegningDto = dto.getBesteberegningAndeler();
        List<BesteberegningFødendeKvinneAndelDto> andelListe = besteberegningDto.getBesteberegningAndelListe();
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = grunnlagBuilder.getBeregningsgrunnlagBuilder();
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = beregningsgrunnlagBuilder.getBeregningsgrunnlag();
        for (var periode : nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = forrigeBg
                    .flatMap(beregningsgrunnlag -> beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                            .filter(periode1 -> periode1.getPeriode().overlapper(periode.getPeriode())).findFirst());
            andelListe.forEach(dtoAndel -> FastsettFaktaOmBeregningVerdierTjeneste.fastsettVerdierForAndel(mapTilRedigerbarAndel(dtoAndel), mapTilFastsatteVerdier(dtoAndel), periode, forrigePeriode));
            if (besteberegningDto.getNyDagpengeAndel() != null) {
                FastsettFaktaOmBeregningVerdierTjeneste.fastsettVerdierForAndel(lagRedigerbarAndelDtoForDagpenger(), mapTilFastsatteVerdier(besteberegningDto.getNyDagpengeAndel()), periode, forrigePeriode);
            }
        }
        if (nyttBeregningsgrunnlag.getAktivitetStatuser().stream().noneMatch(status -> AktivitetStatus.DAGPENGER.equals(status.getAktivitetStatus()))) {
            beregningsgrunnlagBuilder
                    .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER));
        }

        // Setter fakta aggregat
        FaktaAggregatDto.Builder faktaBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        FaktaAktørDto.Builder faktaAktørBuilder = faktaBuilder.getFaktaAktørBuilder();
        boolean skalBesteberegnes = besteberegningDto.getBesteberegningAndelListe().stream().anyMatch(a -> a.getFastsatteVerdier().getSkalHaBesteberegning());
        faktaAktørBuilder.medSkalBesteberegnes(skalBesteberegnes);
        faktaBuilder.medFaktaAktør(faktaAktørBuilder.build());
        grunnlagBuilder.medFaktaAggregat(faktaBuilder.build());
    }

    private FastsatteVerdierDto mapTilFastsatteVerdier(DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel) {
        FastsatteVerdierForBesteberegningDto fastsatteVerdier = nyDagpengeAndel.getFastsatteVerdier();
        return FastsatteVerdierDto.Builder.ny()
                .medFastsattBeløpPrÅr(fastsatteVerdier.finnFastsattBeløpPrÅr().intValue())
                .medInntektskategori(fastsatteVerdier.getInntektskategori())
                .medSkalHaBesteberegning(true)
                .build();
    }

    private RedigerbarAndelFaktaOmBeregningDto lagRedigerbarAndelDtoForDagpenger() {
        return new RedigerbarAndelFaktaOmBeregningDto(AktivitetStatus.DAGPENGER);
    }

    private RedigerbarAndelFaktaOmBeregningDto mapTilRedigerbarAndel(BesteberegningFødendeKvinneAndelDto dtoAndel) {
        return new RedigerbarAndelFaktaOmBeregningDto(false, dtoAndel.getAndelsnr(), dtoAndel.getLagtTilAvSaksbehandler());
    }

    private FastsatteVerdierDto mapTilFastsatteVerdier(BesteberegningFødendeKvinneAndelDto dtoAndel) {
        FastsatteVerdierForBesteberegningDto fastsatteVerdier = dtoAndel.getFastsatteVerdier();
        return FastsatteVerdierDto.Builder.ny()
                .medFastsattBeløpPrÅr(fastsatteVerdier.finnFastsattBeløpPrÅr().intValue())
                .medInntektskategori(fastsatteVerdier.getInntektskategori())
                .medSkalHaBesteberegning(fastsatteVerdier.getSkalHaBesteberegning())
                .build();
    }

}
