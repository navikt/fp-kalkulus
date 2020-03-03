package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.OverstyrBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller.FaktaOmBeregningTilfellerOppdaterer;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;

@ApplicationScoped
public class BeregningFaktaOgOverstyringHåndterer {

    private FaktaOmBeregningTilfellerOppdaterer faktaOmBeregningTilfellerOppdaterer;

    public BeregningFaktaOgOverstyringHåndterer() {
        // For CDI
    }

    @Inject
    public BeregningFaktaOgOverstyringHåndterer(FaktaOmBeregningTilfellerOppdaterer faktaOmBeregningTilfellerOppdaterer) {
        this.faktaOmBeregningTilfellerOppdaterer = faktaOmBeregningTilfellerOppdaterer;
    }

    public BeregningsgrunnlagGrunnlagDto håndter(BeregningsgrunnlagInput input, FaktaBeregningLagreDto faktaDto) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());

        Optional<BeregningsgrunnlagDto> forrigeBg = input.hentForrigeBeregningsgrunnlag(BeregningsgrunnlagTilstand.KOFAKBER_UT);

        faktaOmBeregningTilfellerOppdaterer.oppdater(faktaDto, forrigeBg, input, grunnlagBuilder);
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.KOFAKBER_UT);
    }


    public BeregningsgrunnlagGrunnlagDto håndterMedOverstyring(BeregningsgrunnlagInput input, OverstyrBeregningsgrunnlagDto dto) {
        // Overstyring kan kun gjøres på grunnlaget fra 98-steget
        BeregningsgrunnlagGrunnlagDto grunnlagOppdatertMedAndeler = input.hentForrigeBeregningsgrunnlagGrunnlag(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)
            .orElseThrow(() -> new IllegalStateException("Kan ikke overstyre uten et opprettet beregningsgrunnlag"));
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlagOppdatertMedAndeler);

        Optional<BeregningsgrunnlagDto> forrigeBg = input.hentForrigeBeregningsgrunnlag(BeregningsgrunnlagTilstand.KOFAKBER_UT);

        FaktaBeregningLagreDto fakta = dto.getFakta();
        if (fakta != null) {
            faktaOmBeregningTilfellerOppdaterer.oppdater(fakta, forrigeBg, input, grunnlagBuilder);
        }
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = grunnlagBuilder.getBeregningsgrunnlagBuilder().medOverstyring(true);
        overstyrInntekterPrPeriode(beregningsgrunnlagBuilder.getBeregningsgrunnlag(), forrigeBg, dto.getOverstyrteAndeler());
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.KOFAKBER_UT);
    }

    private void overstyrInntekterPrPeriode(BeregningsgrunnlagDto nyttGrunnlag,
                                            Optional<BeregningsgrunnlagDto> forrigeBg,
                                            List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler) {
        List<BeregningsgrunnlagPeriodeDto> bgPerioder = nyttGrunnlag.getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriodeDto bgPeriode : bgPerioder) {
            Optional<BeregningsgrunnlagPeriodeDto> forrigeBgPeriode = MatchBeregningsgrunnlagTjeneste
                .finnOverlappendePeriodeOmKunEnFinnes(bgPeriode, forrigeBg);
            overstyrteAndeler
                .forEach(andelDto ->
                    FastsettBeregningVerdierTjeneste.fastsettVerdierForAndel(mapTilRedigerbarAndelDto(andelDto), andelDto.getFastsatteVerdier(), bgPeriode, forrigeBgPeriode));
        }
    }

    private RedigerbarAndelFaktaOmBeregningDto mapTilRedigerbarAndelDto(FastsettBeregningsgrunnlagAndelDto andelDto) {
        return new RedigerbarAndelFaktaOmBeregningDto(false, andelDto.getAndelsnr(), andelDto.getLagtTilAvSaksbehandler());
    }

}
