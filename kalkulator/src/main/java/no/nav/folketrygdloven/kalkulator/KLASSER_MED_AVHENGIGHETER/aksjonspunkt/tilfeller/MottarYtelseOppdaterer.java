package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.ArbeidstakerandelUtenIMMottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.MottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.VurderMottarYtelseTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;

@ApplicationScoped
@FaktaOmBeregningTilfelleRef("VURDER_MOTTAR_YTELSE")
public class MottarYtelseOppdaterer implements FaktaOmBeregningTilfelleOppdaterer {

    MottarYtelseOppdaterer() {
        // For CDI
    }

    @Override
    public void oppdater(FaktaBeregningLagreDto dto,
                         Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        MottarYtelseDto mottarYtelseDto = dto.getMottarYtelse();
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        if (VurderMottarYtelseTjeneste.erFrilanser(beregningsgrunnlag) && mottarYtelseDto.getFrilansMottarYtelse() != null) {
            settMottarYtelseForFrilans(beregningsgrunnlag, mottarYtelseDto);
        }
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
                .forEach(andel -> settMottarYtelseForAndel(mottarYtelseDto, beregningsgrunnlag, andel));
    }

    private void settMottarYtelseForAndel(MottarYtelseDto mottarYtelseDto, BeregningsgrunnlagDto nyttBeregningsgrunnlag, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        Optional<Boolean> mottarYtelseVerdiForAndel = mottarYtelseDto.getArbeidstakerUtenIMMottarYtelse().stream()
            .filter(mottarYtelseAndel -> mottarYtelseAndel.getAndelsnr() == andel.getAndelsnr())
            .findFirst().map(ArbeidstakerandelUtenIMMottarYtelseDto::getMottarYtelse);
        mottarYtelseVerdiForAndel
            .ifPresent(mottarYtelse -> settMottarYtelseVerdiForAndelerMedArbeidsforholdUtenIM(nyttBeregningsgrunnlag, andel, mottarYtelse));
    }

    private void settMottarYtelseVerdiForAndelerMedArbeidsforholdUtenIM(BeregningsgrunnlagDto nyttBeregningsgrunnlag, BeregningsgrunnlagPrStatusOgAndelDto andel, boolean mottarYtelse) {
        nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(nyAndel -> nyAndel.gjelderSammeArbeidsforhold(andel))
            .forEach(nyAndel -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(nyAndel)
                .medMottarYtelse(mottarYtelse, nyAndel.getAktivitetStatus()));
    }

    private void settMottarYtelseForFrilans(BeregningsgrunnlagDto nyttBeregningsgrunnlag, MottarYtelseDto mottarYtelseDto) {
        nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .filter(andel -> andel.getAktivitetStatus().erFrilanser())
            .forEach(andel -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel)
                .medMottarYtelse(mottarYtelseDto.getFrilansMottarYtelse(), andel.getAktivitetStatus()));
    }

}
