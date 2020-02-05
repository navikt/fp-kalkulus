package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAktivitetAggregat;
import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapAndel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FordelBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.rest.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.rest.VisningsnavnForAktivitetTjeneste;
import no.nav.folketrygdloven.kalkulator.rest.dto.AndelForFaktaOmBeregningDto;

@ApplicationScoped
public class AndelerForFaktaOmBeregningTjeneste {

    List<AndelForFaktaOmBeregningDto> lagAndelerForFaktaOmBeregning(BeregningsgrunnlagRestInput input) {
        BeregningsgrunnlagGrunnlagRestDto gjeldendeGrunnlag;
        if (input.getFaktaOmBeregningPreutfyllingsgrunnlag().isPresent()) {
            gjeldendeGrunnlag = input.getFaktaOmBeregningPreutfyllingsgrunnlag().get();
        } else {
            gjeldendeGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        }
        var beregningAktivitetAggregat = gjeldendeGrunnlag.getGjeldendeAktiviteter();
        BeregningsgrunnlagRestDto beregningsgrunnlag = gjeldendeGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Må ha beregningsgrunnlag her"));;
        List<BeregningsgrunnlagPrStatusOgAndelRestDto> andelerIFørstePeriode = beregningsgrunnlag
            .getBeregningsgrunnlagPerioder()
            .get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(a -> !FordelBeregningsgrunnlagTjeneste.erNyttArbeidsforhold(mapAndel(a), mapAktivitetAggregat(beregningAktivitetAggregat), input.getSkjæringstidspunktForBeregning()))
            .collect(Collectors.toList());
        return andelerIFørstePeriode.stream()
            .map(andel -> mapTilAndelIFaktaOmBeregning(input, andel))
            .collect(Collectors.toList());

    }

    private AndelForFaktaOmBeregningDto mapTilAndelIFaktaOmBeregning(BeregningsgrunnlagRestInput input, BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        var ref = input.getBehandlingReferanse();
        var inntektsmeldinger = input.getInntektsmeldinger();
        Optional<InntektsmeldingDto> inntektsmeldingForAndel = BeregningInntektsmeldingTjeneste.finnInntektsmeldingForAndel(mapAndel(andel), inntektsmeldinger, input.getSkjæringstidspunktForBeregning());
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
