package no.nav.folketrygdloven.kalkulator.rest.fakta;

import static no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene.mapBeregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.ArbeidstakerUtenInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.VurderMottarYtelseTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.rest.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.rest.dto.ArbeidstakerUtenInntektsmeldingAndelDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.VurderMottarYtelseDto;

@ApplicationScoped
public class VurderMottarYtelseDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    @Override
    public void lagDto(BeregningsgrunnlagRestInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagRestDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        if (beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE)) {
            LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
            var ref = input.getBehandlingReferanse();
            var iayGrunnlag = input.getIayGrunnlag();
            AktørId aktørId = ref.getAktørId();
            byggVerdier(aktørId, iayGrunnlag, beregningsgrunnlag, faktaOmBeregningDto, skjæringstidspunkt);
        }
    }

    private void byggVerdier(AktørId aktørId, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, BeregningsgrunnlagRestDto beregningsgrunnlag,
                             FaktaOmBeregningDto faktaOmBeregningDto, LocalDate skjæringstidspunkt) {
        VurderMottarYtelseDto vurderMottarYtelseDto = new VurderMottarYtelseDto();
        if (VurderMottarYtelseTjeneste.erFrilanser(mapBeregningsgrunnlag(beregningsgrunnlag))) {
            lagFrilansDel(aktørId, beregningsgrunnlag, inntektArbeidYtelseGrunnlag, vurderMottarYtelseDto, skjæringstidspunkt);
            if (faktaOmBeregningDto.getFrilansAndel() == null) {
                FaktaOmBeregningAndelDtoTjeneste.lagFrilansAndelDto(beregningsgrunnlag, inntektArbeidYtelseGrunnlag).ifPresent(faktaOmBeregningDto::setFrilansAndel);
            }
        }
        lagArbeidstakerUtenInntektsmeldingDel(inntektArbeidYtelseGrunnlag, aktørId, beregningsgrunnlag,
            vurderMottarYtelseDto, skjæringstidspunkt);
        faktaOmBeregningDto.setVurderMottarYtelse(vurderMottarYtelseDto);
    }

    private void lagArbeidstakerUtenInntektsmeldingDel(InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                       AktørId aktørId,
                                                       BeregningsgrunnlagRestDto beregningsgrunnlag, VurderMottarYtelseDto vurderMottarYtelseDto,
                                                       LocalDate skjæringstidspunkt) {

        var filter = new InntektFilterDto(inntektArbeidYtelseGrunnlag.getAktørInntektFraRegister(aktørId)).før(skjæringstidspunkt);
        var andeler = ArbeidstakerUtenInntektsmeldingTjeneste.finnArbeidstakerAndelerUtenInntektsmelding(mapBeregningsgrunnlag(beregningsgrunnlag), inntektArbeidYtelseGrunnlag);
        andeler.forEach(andelUtenIM -> {
            var dto = new ArbeidstakerUtenInntektsmeldingAndelDto();
            BeregningsgrunnlagPrStatusOgAndelRestDto andel = finnRestAndel(andelUtenIM, beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
            beregnOgSettInntektPrMnd(filter, andel, dto);
            dto.setAndelsnr(andelUtenIM.getAndelsnr());
            dto.setInntektskategori(andelUtenIM.getInntektskategori());
            BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag).ifPresent(dto::setArbeidsforhold);
            andelUtenIM.mottarYtelse().ifPresent(dto::setMottarYtelse);
            vurderMottarYtelseDto.leggTilArbeidstakerAndelUtenInntektsmelding(dto);
        });
    }

    private BeregningsgrunnlagPrStatusOgAndelRestDto finnRestAndel(BeregningsgrunnlagPrStatusOgAndelDto andelUtenIM, List<BeregningsgrunnlagPrStatusOgAndelRestDto> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
            .filter(a -> a.getAndelsnr().equals(andelUtenIM.getAndelsnr()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikke matchende andel"));
    }

    private void beregnOgSettInntektPrMnd(InntektFilterDto filter, BeregningsgrunnlagPrStatusOgAndelRestDto andel, ArbeidstakerUtenInntektsmeldingAndelDto dto) {
        BigDecimal snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittinntektForArbeidstakerIBeregningsperioden(filter, andel);
        dto.setInntektPrMnd(snittIBeregningsperioden);
    }

    private void lagFrilansDel(AktørId aktørId,
                               BeregningsgrunnlagRestDto beregningsgrunnlag,
                               InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                               VurderMottarYtelseDto vurderMottarYtelseDto,
                               LocalDate skjæringstidspunkt) {
        vurderMottarYtelseDto.setErFrilans(VurderMottarYtelseTjeneste.erFrilanser(mapBeregningsgrunnlag(beregningsgrunnlag)));
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getAktivitetStatus().erFrilanser()).findFirst()
            .ifPresent(frilansAndel -> {
                vurderMottarYtelseDto.setFrilansMottarYtelse(frilansAndel.mottarYtelse().orElse(null));
                InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(aktørId, inntektArbeidYtelseGrunnlag, frilansAndel, skjæringstidspunkt)
                    .ifPresent(vurderMottarYtelseDto::setFrilansInntektPrMnd);
            });
    }

}
