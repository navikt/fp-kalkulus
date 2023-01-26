package no.nav.folketrygdloven.kalkulator.guitjenester;


import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.InntektsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderInntektsforholdPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderNyttInntektsforholdDto;

class VurderNyeInntektsforholdDtoTjeneste {

    public static VurderNyttInntektsforholdDto lagDto(BeregningsgrunnlagGUIInput input) {

        if (input.getAvklaringsbehov().stream().noneMatch(a -> a.getDefinisjon().equals(AvklaringsbehovDefinisjon.VURDER_NYTT_INNTKTSFRHLD))) {
            return null;
        }

        var iayGrunnlag = input.getIayGrunnlag();
        var bgPerioder = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();

        var periodeListe = bgPerioder.stream()
                .filter(it -> !it.getTilkomneInntekter().isEmpty())
                .map(it -> mapPeriode(iayGrunnlag, it))
                .collect(Collectors.toList());

        if (!periodeListe.isEmpty()) {
            return new VurderNyttInntektsforholdDto(periodeListe);
        }

        return null;
    }

    private static VurderInntektsforholdPeriodeDto mapPeriode(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                              BeregningsgrunnlagPeriodeDto periode) {
        var innteksforholdListe = mapInntektforholdDtoListe(iayGrunnlag, periode);
        return new VurderInntektsforholdPeriodeDto(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), innteksforholdListe.stream().toList());
    }

    private static Set<InntektsforholdDto> mapInntektforholdDtoListe(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                     BeregningsgrunnlagPeriodeDto periode) {
        return periode.getTilkomneInntekter()
                .stream()
                .map(a -> mapTilInntektsforhold(a, iayGrunnlag))
                .collect(Collectors.toCollection(HashSet::new));
    }


    private static InntektsforholdDto mapTilInntektsforhold(TilkommetInntektDto tilkommetInntektDto, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var inntektsmelding = iayGrunnlag.getInntektsmeldinger().stream()
                .flatMap(it -> it.getAlleInntektsmeldinger().stream())
                .filter(im -> Objects.equals(im.getArbeidsgiver(), tilkommetInntektDto.getArbeidsgiver().orElse(null))
                        && tilkommetInntektDto.getArbeidsforholdRef().gjelderFor(im.getArbeidsforholdRef()))
                .findFirst();

        return new InntektsforholdDto(
                tilkommetInntektDto.getAktivitetStatus(),
                tilkommetInntektDto.getArbeidsgiver().map(Arbeidsgiver::getIdentifikator).orElse(null),
                tilkommetInntektDto.getArbeidsforholdRef().getReferanse(),
                finnEksternArbeidsforholdId(tilkommetInntektDto.getArbeidsgiver(), tilkommetInntektDto.getArbeidsforholdRef(), iayGrunnlag).map(EksternArbeidsforholdRef::getReferanse).orElse(null),
                finnBruttoPrÅr(tilkommetInntektDto, inntektsmelding),
                tilkommetInntektDto.skalRedusereUtbetaling(),
                inntektsmelding.isPresent());
    }

    private static Integer finnBruttoPrÅr(TilkommetInntektDto tilkommetInntektDto, Optional<InntektsmeldingDto> inntektsmelding) {
        return tilkommetInntektDto.getBruttoInntektPrÅr() != null ? Integer.valueOf(tilkommetInntektDto.getBruttoInntektPrÅr().intValue()) :
                inntektsmelding.map(im -> im.getInntektBeløp().getVerdi().multiply(BigDecimal.valueOf(12))).map(BigDecimal::intValue).orElse(null);
    }

    private static Optional<EksternArbeidsforholdRef> finnEksternArbeidsforholdId(Optional<Arbeidsgiver> arbeidsgiver,
                                                                                  InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (arbeidsgiver.isEmpty() || arbeidsforholdRef.getReferanse() == null) {
            return Optional.empty();
        }
        return iayGrunnlag.getArbeidsforholdInformasjon()
                .map(d -> d.finnEkstern(arbeidsgiver.get(), arbeidsforholdRef));
    }

}
