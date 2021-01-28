package no.nav.folketrygdloven.kalkulator.aksjonspunkt.tilfeller;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.ArbeidstakerandelUtenIMMottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.aksjonspunkt.dto.MottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;

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
        var andelListe = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        FaktaAggregatDto.Builder faktaBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        if  (mottarYtelseDto.getFrilansMottarYtelse() != null) {
            settMottarYtelseForFrilans(mottarYtelseDto, faktaBuilder);
        }
        mottarYtelseDto.getArbeidstakerUtenIMMottarYtelse()
                .forEach(arbMottarYtelse -> settMottarYtelseForArbeid(andelListe, faktaBuilder, arbMottarYtelse));
        grunnlagBuilder.medFaktaAggregat(faktaBuilder.build());
    }

    private void settMottarYtelseForFrilans(MottarYtelseDto mottarYtelseDto, FaktaAggregatDto.Builder faktaBuilder) {
        FaktaAktørDto.Builder faktaAktørBuilder = faktaBuilder.getFaktaAktørBuilder();
        faktaAktørBuilder.medHarFLMottattYtelse(mottarYtelseDto.getFrilansMottarYtelse());
        faktaBuilder.medFaktaAktør(faktaAktørBuilder.build());
    }

    private void settMottarYtelseForArbeid(List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, FaktaAggregatDto.Builder faktaBuilder, ArbeidstakerandelUtenIMMottarYtelseDto arbMottarYtelse) {
        andelListe.stream()
                .filter(a -> a.getAndelsnr().equals(arbMottarYtelse.getAndelsnr()))
                .findFirst()
                .flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                .ifPresent(arb -> settMottarYtelseForArbeidsforhold(faktaBuilder, arbMottarYtelse, arb));
    }

    private void settMottarYtelseForArbeidsforhold(FaktaAggregatDto.Builder faktaBuilder, ArbeidstakerandelUtenIMMottarYtelseDto arbMottarYtelse, BGAndelArbeidsforholdDto arb) {
        FaktaArbeidsforholdDto.Builder faktaArbBuilder = faktaBuilder.getFaktaArbeidsforholdBuilderFor(arb.getArbeidsgiver(), arb.getArbeidsforholdRef())
                .medHarMottattYtelse(arbMottarYtelse.getMottarYtelse());
        faktaBuilder.erstattEksisterendeEllerLeggTil(faktaArbBuilder.build());
    }

}
