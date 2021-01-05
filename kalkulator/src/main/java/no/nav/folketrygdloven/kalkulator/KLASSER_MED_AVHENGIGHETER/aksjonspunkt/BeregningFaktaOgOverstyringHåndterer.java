package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.OverstyrBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller.FaktaOmBeregningTilfellerOppdaterer;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

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

    public BeregningsgrunnlagGrunnlagDto håndter(HåndterBeregningsgrunnlagInput input, FaktaBeregningLagreDto faktaDto) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());

        Optional<BeregningsgrunnlagDto> forrigeBg = input.getForrigeGrunnlagFraHåndteringTilstand().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);

        faktaOmBeregningTilfellerOppdaterer.oppdater(faktaDto, forrigeBg, input, grunnlagBuilder);
        return grunnlagBuilder.build(input.getHåndteringTilstand());
    }


    public BeregningsgrunnlagGrunnlagDto håndterMedOverstyring(HåndterBeregningsgrunnlagInput input, OverstyrBeregningsgrunnlagDto dto) {
        // Overstyring kan kun gjøres på grunnlaget fra 98-steget
        BeregningsgrunnlagGrunnlagDto aktivtGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(aktivtGrunnlag);

        Optional<BeregningsgrunnlagDto> forrigeBg = input.getForrigeGrunnlagFraHåndteringTilstand().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlag);

        FaktaBeregningLagreDto fakta = dto.getFakta();
        if (fakta != null) {
            faktaOmBeregningTilfellerOppdaterer.oppdater(fakta, forrigeBg, input, grunnlagBuilder);
        }
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = grunnlagBuilder.getBeregningsgrunnlagBuilder().medOverstyring(true);
        overstyrInntekterPrPeriode(beregningsgrunnlagBuilder.getBeregningsgrunnlag(), forrigeBg, dto.getOverstyrteAndeler());
        finnManglendeAktivitetstatuser(beregningsgrunnlagBuilder.getBeregningsgrunnlag(), dto.getOverstyrteAndeler())
                .forEach(as -> BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(as).build(beregningsgrunnlagBuilder.getBeregningsgrunnlag()));
        return grunnlagBuilder.build(input.getHåndteringTilstand());
    }

    private List<AktivitetStatus> finnManglendeAktivitetstatuser(BeregningsgrunnlagDto bg, List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler) {
        return overstyrteAndeler.stream().flatMap(a -> a.getAktivitetStatus().stream())
                .filter(as -> bg.getAktivitetStatuser().stream().map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus).noneMatch(as::equals))
                .collect(Collectors.toList());
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
                            FastsettFaktaOmBeregningVerdierTjeneste.fastsettVerdierForAndel(andelDto, andelDto.getFastsatteVerdier(), bgPeriode, forrigeBgPeriode));
        }
    }
}
