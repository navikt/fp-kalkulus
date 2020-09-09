package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.KortvarigeArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;

@ApplicationScoped
public class KortvarigeArbeidsforholdDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    @Override
    public void lagDto(BeregningsgrunnlagRestInput input,
                       FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        if (!beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD)) {
            return;
        }
        List<KortvarigeArbeidsforholdDto> arbeidsforholdDto = lagKortvarigeArbeidsforholdDto(input.getKoblingReferanse(), beregningsgrunnlag, input.getIayGrunnlag());
        faktaOmBeregningDto.setKortvarigeArbeidsforhold(arbeidsforholdDto);
    }

    private List<KortvarigeArbeidsforholdDto> lagKortvarigeArbeidsforholdDto(KoblingReferanse ref, BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(ref.getAktørId(), beregningsgrunnlag, inntektArbeidYtelseGrunnlag);
        return kortvarige.entrySet().stream()
            .map(entry -> mapFraYrkesaktivitet(finnRestDtoForAndel(entry, beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()), inntektArbeidYtelseGrunnlag))
            .collect(Collectors.toList());
    }

    private BeregningsgrunnlagPrStatusOgAndelDto finnRestDtoForAndel(Map.Entry<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> entry, List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
            .filter(a -> a.getAndelsnr().equals(entry.getKey().getAndelsnr()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikkje matchende blant REST-andeler"));
    }

    private KortvarigeArbeidsforholdDto mapFraYrkesaktivitet(BeregningsgrunnlagPrStatusOgAndelDto prStatusOgAndel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        KortvarigeArbeidsforholdDto beregningArbeidsforhold = new KortvarigeArbeidsforholdDto();
        beregningArbeidsforhold.setErTidsbegrensetArbeidsforhold(prStatusOgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getErTidsbegrensetArbeidsforhold).orElse(null));
        beregningArbeidsforhold.setAndelsnr(prStatusOgAndel.getAndelsnr());
        Optional<BeregningsgrunnlagArbeidsforholdDto> arbDto = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(prStatusOgAndel, Optional.empty(), inntektArbeidYtelseGrunnlag);
        arbDto.ifPresent(beregningArbeidsforhold::setArbeidsforhold);
        return beregningArbeidsforhold;
    }
}
