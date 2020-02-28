package no.nav.folketrygdloven.kalkulator.rest.fakta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.fordeling.FordelTilkommetArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.rest.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.rest.VisningsnavnForAktivitetTjeneste;
import no.nav.folketrygdloven.kalkulator.rest.dto.AndelForFaktaOmBeregningDto;

@ApplicationScoped
public class AndelerForFaktaOmBeregningTjeneste {

    List<AndelForFaktaOmBeregningDto> lagAndelerForFaktaOmBeregning(BeregningsgrunnlagRestInput input) {
        BeregningsgrunnlagGrunnlagDto gjeldendeGrunnlag;
        if (input.getFaktaOmBeregningPreutfyllingsgrunnlag().isPresent()) {
            gjeldendeGrunnlag = input.getFaktaOmBeregningPreutfyllingsgrunnlag().get();
        } else {
            gjeldendeGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        }
        var beregningAktivitetAggregat = gjeldendeGrunnlag.getGjeldendeAktiviteter();
        BeregningsgrunnlagDto beregningsgrunnlag = gjeldendeGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Må ha beregningsgrunnlag her"));;
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIFørstePeriode = beregningsgrunnlag
            .getBeregningsgrunnlagPerioder()
            .get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(a -> !FordelTilkommetArbeidsforholdTjeneste.erNyttArbeidsforhold(a, beregningAktivitetAggregat, input.getSkjæringstidspunktForBeregning()))
            .collect(Collectors.toList());
        return andelerIFørstePeriode.stream()
            .map(andel -> mapTilAndelIFaktaOmBeregning(input, andel))
            .collect(Collectors.toList());

    }

    private AndelForFaktaOmBeregningDto mapTilAndelIFaktaOmBeregning(BeregningsgrunnlagRestInput input, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        var ref = input.getBehandlingReferanse();
        var inntektsmeldinger = input.getInntektsmeldinger();
        Optional<InntektsmeldingDto> inntektsmeldingForAndel = BeregningInntektsmeldingTjeneste.finnInntektsmeldingForAndel(andel, inntektsmeldinger);
        AndelForFaktaOmBeregningDto andelDto = new AndelForFaktaOmBeregningDto();
        andelDto.setFastsattBelop(FinnInntektForVisning.finnInntektForPreutfylling(andel));
        andelDto.setInntektskategori(andel.getInntektskategori());
        andelDto.setAndelsnr(andel.getAndelsnr());
        andelDto.setAktivitetStatus(andel.getAktivitetStatus());
        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag = input.getIayGrunnlag();
        andelDto.setVisningsnavn(VisningsnavnForAktivitetTjeneste.lagVisningsnavn(ref, inntektArbeidYtelseGrunnlag, andel));
        andelDto.setSkalKunneEndreAktivitet(SkalKunneEndreAktivitet.skalKunneEndreAktivitet(andel));
        andelDto.setLagtTilAvSaksbehandler(andel.getLagtTilAvSaksbehandler());
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag).ifPresent(andelDto::setArbeidsforhold);
        finnRefusjonskravFraInntektsmelding(inntektsmeldingForAndel).ifPresent(andelDto::setRefusjonskrav);
        FinnInntektForVisning.finnInntektForKunLese(ref, andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag,
            input.getBeregningsgrunnlag().getFaktaOmBeregningTilfeller()).ifPresent(andelDto::setBelopReadOnly);
        return andelDto;
    }

    private Optional<BigDecimal> finnRefusjonskravFraInntektsmelding(Optional<InntektsmeldingDto> inntektsmeldingForAndel) {
        return inntektsmeldingForAndel
            .map(InntektsmeldingDto::getRefusjonBeløpPerMnd)
            .map(Beløp::getVerdi);
    }
}
