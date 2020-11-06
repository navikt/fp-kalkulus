package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.BeregningInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.guitjenester.VisningsnavnForAktivitetTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AndelForFaktaOmBeregningDto;

public class AndelerForFaktaOmBeregningTjeneste {

    private AndelerForFaktaOmBeregningTjeneste() {
        // Skjul
    }

    public static List<AndelForFaktaOmBeregningDto> lagAndelerForFaktaOmBeregning(BeregningsgrunnlagGUIInput input) {
        BeregningsgrunnlagGrunnlagDto gjeldendeGrunnlag;
        if (input.getFaktaOmBeregningBeregningsgrunnlagGrunnlag().isPresent()) {
            gjeldendeGrunnlag = input.getFaktaOmBeregningBeregningsgrunnlagGrunnlag().get();
        } else {
            gjeldendeGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        }
        BeregningsgrunnlagDto beregningsgrunnlag = gjeldendeGrunnlag.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Må ha beregningsgrunnlag her"));
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIFørstePeriode = beregningsgrunnlag
            .getBeregningsgrunnlagPerioder()
            .get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START) || a.getKilde().equals(AndelKilde.SAKSBEHANDLER_KOFAKBER))
            .collect(Collectors.toList());
        return andelerIFørstePeriode.stream()
            .map(andel -> mapTilAndelIFaktaOmBeregning(input, andel))
            .collect(Collectors.toList());

    }

    private static AndelForFaktaOmBeregningDto mapTilAndelIFaktaOmBeregning(BeregningsgrunnlagGUIInput input, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        var ref = input.getKoblingReferanse();
        var inntektsmeldinger = input.getInntektsmeldinger();
        Optional<InntektsmeldingDto> inntektsmeldingForAndel = BeregningInntektsmeldingTjeneste.finnInntektsmeldingForAndel(andel, inntektsmeldinger);
        AndelForFaktaOmBeregningDto andelDto = new AndelForFaktaOmBeregningDto();
        andelDto.setFastsattBelop(FinnInntektForVisning.finnInntektForPreutfylling(andel));
        andelDto.setInntektskategori(new Inntektskategori(andel.getInntektskategori().getKode()));
        andelDto.setAndelsnr(andel.getAndelsnr());
        andelDto.setAktivitetStatus(new AktivitetStatus(andel.getAktivitetStatus().getKode()));
        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag = input.getIayGrunnlag();
        andelDto.setVisningsnavn(VisningsnavnForAktivitetTjeneste.lagVisningsnavn(ref, inntektArbeidYtelseGrunnlag, andel));
        andelDto.setSkalKunneEndreAktivitet(SkalKunneEndreAktivitet.skalKunneEndreAktivitet(andel));
        andelDto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag).ifPresent(andelDto::setArbeidsforhold);
        finnRefusjonskravFraInntektsmelding(inntektsmeldingForAndel).ifPresent(andelDto::setRefusjonskrav);
        FinnInntektForVisning.finnInntektForKunLese(ref, andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag,
            input.getBeregningsgrunnlag().getFaktaOmBeregningTilfeller()).ifPresent(andelDto::setBelopReadOnly);
        return andelDto;
    }

    private static Optional<BigDecimal> finnRefusjonskravFraInntektsmelding(Optional<InntektsmeldingDto> inntektsmeldingForAndel) {
        return inntektsmeldingForAndel
            .map(InntektsmeldingDto::getRefusjonBeløpPerMnd)
            .map(Beløp::getVerdi);
    }
}
