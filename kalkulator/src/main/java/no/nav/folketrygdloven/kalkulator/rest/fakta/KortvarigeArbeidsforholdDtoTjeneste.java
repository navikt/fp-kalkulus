package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapBeregningsgrunnlag;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.rest.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.rest.dto.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.KortvarigeArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

@ApplicationScoped
class KortvarigeArbeidsforholdDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    @Override
    public void lagDto(BeregningsgrunnlagRestInput input,
                       FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagRestDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        if (!beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD)) {
            return;
        }
        List<KortvarigeArbeidsforholdDto> arbeidsforholdDto = lagKortvarigeArbeidsforholdDto(input.getBehandlingReferanse(), beregningsgrunnlag, input.getIayGrunnlag());
        faktaOmBeregningDto.setKortvarigeArbeidsforhold(arbeidsforholdDto);
    }

    private List<KortvarigeArbeidsforholdDto> lagKortvarigeArbeidsforholdDto(BehandlingReferanse ref, BeregningsgrunnlagRestDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(ref.getAktørId(), mapBeregningsgrunnlag(beregningsgrunnlag), inntektArbeidYtelseGrunnlag);
        return kortvarige.entrySet().stream()
            .map(entry -> mapFraYrkesaktivitet(finnRestDtoForAndel(entry, beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()), inntektArbeidYtelseGrunnlag))
            .collect(Collectors.toList());
    }

    private BeregningsgrunnlagPrStatusOgAndelRestDto finnRestDtoForAndel(Map.Entry<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> entry, List<BeregningsgrunnlagPrStatusOgAndelRestDto> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
            .filter(a -> a.getAndelsnr().equals(entry.getKey().getAndelsnr()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikkje matchende blant REST-andeler"));
    }

    private KortvarigeArbeidsforholdDto mapFraYrkesaktivitet(BeregningsgrunnlagPrStatusOgAndelRestDto prStatusOgAndel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        KortvarigeArbeidsforholdDto beregningArbeidsforhold = new KortvarigeArbeidsforholdDto();
        beregningArbeidsforhold.setErTidsbegrensetArbeidsforhold(prStatusOgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdRestDto::getErTidsbegrensetArbeidsforhold).orElse(null));
        beregningArbeidsforhold.setAndelsnr(prStatusOgAndel.getAndelsnr());
        Optional<BeregningsgrunnlagArbeidsforholdDto> arbDto = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(prStatusOgAndel, Optional.empty(), inntektArbeidYtelseGrunnlag);
        arbDto.ifPresent(beregningArbeidsforhold::setArbeidsforhold);
        return beregningArbeidsforhold;
    }
}
