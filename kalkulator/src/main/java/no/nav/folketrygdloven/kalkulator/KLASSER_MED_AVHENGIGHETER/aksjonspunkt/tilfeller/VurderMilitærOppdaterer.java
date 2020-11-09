package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.VurderMilitærDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;


@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_MILITÆR_SIVILTJENESTE")
public class VurderMilitærOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    @Override
    public void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderMilitærDto militærDto = dto.getVurderMilitaer();
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        if (militærDto.getHarMilitaer()) {
            leggTilMilitærstatusOgAndelHvisIkkeFinnes(beregningsgrunnlag);
        } else {
            slettMilitærStatusOgAndelHvisFinnes(beregningsgrunnlag);
        }

        // Setter fakta aggregat
        FaktaAggregatDto.Builder faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        FaktaAktørDto.Builder faktaAktørBuilder = faktaAggregatBuilder.getFaktaAktørBuilder();
        faktaAktørBuilder.medErMilitærSiviltjeneste(militærDto.getHarMilitaer());
        faktaAggregatBuilder.medFaktaAktør(faktaAktørBuilder.build());
        grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
    }

    private void slettMilitærStatusOgAndelHvisFinnes(BeregningsgrunnlagDto nyttBeregningsgrunnlag) {
        BeregningsgrunnlagDto.Builder grunnlagUtenMilitærBuilder = BeregningsgrunnlagDto.Builder.oppdater(Optional.of(nyttBeregningsgrunnlag));
        if (harMilitærstatus(nyttBeregningsgrunnlag)) {
            grunnlagUtenMilitærBuilder.fjernAktivitetstatus(AktivitetStatus.MILITÆR_ELLER_SIVIL);
        }
        BeregningsgrunnlagDto grunnlagUtenMilitær = grunnlagUtenMilitærBuilder.build();
        grunnlagUtenMilitær.getBeregningsgrunnlagPerioder().forEach(periode -> {
            if (harMilitærandel(periode)) {
                fjernMilitærFraPeriode(grunnlagUtenMilitær, periode);
            }
        });
    }

    private void fjernMilitærFraPeriode(BeregningsgrunnlagDto grunnlagUtenMilitær, BeregningsgrunnlagPeriodeDto periode) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> alleMilitærandeler = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.MILITÆR_ELLER_SIVIL))
            .collect(Collectors.toList());
        BeregningsgrunnlagPeriodeDto.Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.oppdater(periode);
        alleMilitærandeler.forEach(periodeBuilder::fjernBeregningsgrunnlagPrStatusOgAndel);
        periodeBuilder.build(grunnlagUtenMilitær);
    }

    private void leggTilMilitærstatusOgAndelHvisIkkeFinnes(BeregningsgrunnlagDto nyttBeregningsgrunnlag) {
        nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(this::leggTilMilitærAndelOmDenIkkeFinnes);
        if (!harMilitærstatus(nyttBeregningsgrunnlag)) {
            BeregningsgrunnlagAktivitetStatusDto.Builder aktivitetBuilder = BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL).medHjemmel(Hjemmel.F_14_7);
            BeregningsgrunnlagDto.Builder.oppdater(Optional.of(nyttBeregningsgrunnlag)).leggTilAktivitetStatus(aktivitetBuilder);
        }
    }

    private void leggTilMilitærAndelOmDenIkkeFinnes(BeregningsgrunnlagPeriodeDto periode) {
        if (!harMilitærandel(periode)) {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build(periode);
        }
    }

    private boolean harMilitærandel(BeregningsgrunnlagPeriodeDto førstePeriode) {
        return førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .anyMatch(andel -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(andel.getAktivitetStatus()));
    }

    private boolean harMilitærstatus(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().stream()
            .anyMatch(status -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(status.getAktivitetStatus()));
    }

}
