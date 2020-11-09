package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.tilfeller;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.ArbeidstakerandelUtenIMMottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.MottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.VurderMottarYtelseTjeneste;

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
        var andelListe = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        andelListe.forEach(andel -> settMottarYtelseForAndel(mottarYtelseDto, beregningsgrunnlag, andel));

        // Setter fakta aggregat
        FaktaAggregatDto.Builder faktaBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        FaktaAktørDto.Builder faktaAktørBuilder = faktaBuilder.getFaktaAktørBuilder();
        faktaAktørBuilder.medHarFLMottattYtelse(mottarYtelseDto.getFrilansMottarYtelse());
        faktaBuilder.medFaktaAktør(faktaAktørBuilder.build());
        mottarYtelseDto.getArbeidstakerUtenIMMottarYtelse()
                .forEach(arbMottarYtelse ->
                        andelListe.stream()
                                .filter(a -> a.getAndelsnr().equals(arbMottarYtelse.getAndelsnr()))
                                .findFirst()
                                .flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                                .ifPresent(arb ->
                                {
                                    FaktaArbeidsforholdDto.Builder faktaArbBuilder = faktaBuilder.getFaktaArbeidsforholdBuilderFor(arb.getArbeidsgiver(), arb.getArbeidsforholdRef())
                                            .medHarMottattYtelse(arbMottarYtelse.getMottarYtelse());
                                    faktaBuilder.erstattEksisterendeEllerLeggTil(faktaArbBuilder.build());
                                }));
        grunnlagBuilder.medFaktaAggregat(faktaBuilder.build());
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
                .forEach(nyAndel -> {
                    BeregningsgrunnlagPrStatusOgAndelDto.Builder oppdatere = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(nyAndel);
                    oppdatere.medBeregningsgrunnlagArbeidstakerAndel(oppdatere.getBeregningsgrunnlagArbeidstakerAndelBuilder().medMottarYtelse(mottarYtelse).build());
                });
    }

    private void settMottarYtelseForFrilans(BeregningsgrunnlagDto nyttBeregningsgrunnlag, MottarYtelseDto mottarYtelseDto) {
        nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .filter(andel -> andel.getAktivitetStatus().erFrilanser())
                .forEach(andel -> {
                    BeregningsgrunnlagPrStatusOgAndelDto.Builder oppdatere = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel);
                    oppdatere.medBeregningsgrunnlagFrilansAndel(oppdatere.getBeregningsgrunnlagFrilansAndelBuilder().medMottarYtelse(mottarYtelseDto.getFrilansMottarYtelse()).build());
                });
    }

}
