package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.felles.BeregningInntektsmeldingTjeneste.finnInntektsmeldingForAndel;
import static no.nav.folketrygdloven.kalkulator.guitjenester.VisningsnavnForAktivitetTjeneste.lagVisningsnavn;
import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FinnInntektForVisning.finnInntektForKunLese;
import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.FinnInntektForVisning.finnInntektForPreutfylling;
import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.SkalKunneEndreAktivitet.skalKunneEndreAktivitet;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde.PROSESS_START;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde.SAKSBEHANDLER_KOFAKBER;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AndelForFaktaOmBeregningDto;

public class AndelerForFaktaOmBeregningTjeneste {

    private AndelerForFaktaOmBeregningTjeneste() {
        // Skjul
    }

    public static List<AndelForFaktaOmBeregningDto> lagAndelerForFaktaOmBeregning(BeregningsgrunnlagGUIInput input) {
        return input.getFaktaOmBeregningBeregningsgrunnlagGrunnlag()
                .orElse(input.getBeregningsgrunnlagGrunnlag())
                .getBeregningsgrunnlag()
                .map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder)
                .filter(Objects::nonNull)
                .filter(c -> !c.isEmpty())
                .map(b -> b.get(0))
                .map(g -> g.getBeregningsgrunnlagPrStatusOgAndelList())
                .orElseThrow()
                .stream()
                .filter(a -> a.getKilde().equals(PROSESS_START) || a.getKilde().equals(SAKSBEHANDLER_KOFAKBER))
                .map(andel -> mapTilAndelIFaktaOmBeregning(input, andel))
                .collect(Collectors.toList());
    }

    private static AndelForFaktaOmBeregningDto mapTilAndelIFaktaOmBeregning(BeregningsgrunnlagGUIInput input,
            BeregningsgrunnlagPrStatusOgAndelDto andel) {
        var ref = input.getKoblingReferanse();
        var inntektsmeldinger = input.getInntektsmeldinger();
        var inntektsmeldingForAndel = finnInntektsmeldingForAndel(andel, inntektsmeldinger);
        var dto = new AndelForFaktaOmBeregningDto();
        dto.setFastsattBelop(finnInntektForPreutfylling(andel));
        dto.setInntektskategori(Inntektskategori.fraKode(andel.getInntektskategori().getKode()));
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setAktivitetStatus(AktivitetStatus.fraKode(andel.getAktivitetStatus().getKode()));
        var inntektArbeidYtelseGrunnlag = input.getIayGrunnlag();
        dto.setVisningsnavn(lagVisningsnavn(ref, inntektArbeidYtelseGrunnlag, andel));
        dto.setSkalKunneEndreAktivitet(skalKunneEndreAktivitet(andel, input.getBeregningsgrunnlag().isOverstyrt()));
        dto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag)
                .ifPresent(dto::setArbeidsforhold);
        finnRefusjonskravFraInntektsmelding(inntektsmeldingForAndel).ifPresent(dto::setRefusjonskrav);
        finnInntektForKunLese(ref, andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag,
                input.getBeregningsgrunnlag().getFaktaOmBeregningTilfeller())
                        .ifPresent(dto::setBelopReadOnly);
        return dto;
    }

    private static Optional<BigDecimal> finnRefusjonskravFraInntektsmelding(Optional<InntektsmeldingDto> inntektsmeldingForAndel) {
        return inntektsmeldingForAndel
                .map(InntektsmeldingDto::getRefusjonBeløpPerMnd)
                .map(Beløp::getVerdi);
    }
}
