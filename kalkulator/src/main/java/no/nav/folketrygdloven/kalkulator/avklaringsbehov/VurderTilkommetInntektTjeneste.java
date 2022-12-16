package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.NyttInntektsforholdDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderTilkommetInntektHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderTilkomneInntektsforholdPeriodeDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

public class VurderTilkommetInntektTjeneste {

    public static BeregningsgrunnlagGrunnlagDto løsAvklaringsbehov(VurderTilkommetInntektHåndteringDto vurderDto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        var bgBuilder = grunnlagBuilder.getBeregningsgrunnlagBuilder();
        var perioder = finnBgPerioder(input);
        var vurderteInntektsforholdPerioder = vurderDto.getTilkomneInntektsforholdPerioder();
        perioder.forEach(p -> leggTilTilkommetInntektDersomRelevant(
                input,
                bgBuilder,
                finnVurdertPeriode(vurderteInntektsforholdPerioder, p),
                p
        ));
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FASTSATT_INN);
    }

    private static Optional<VurderTilkomneInntektsforholdPeriodeDto> finnVurdertPeriode(List<VurderTilkomneInntektsforholdPeriodeDto> vurderteInntektsforholdPerioder, BeregningsgrunnlagPeriodeDto p) {
        return vurderteInntektsforholdPerioder.stream()
                .filter(vp -> Intervall.fraOgMedTilOgMed(vp.getFom(), vp.getTom()).overlapper(p.getPeriode()))
                .findFirst();
    }

    private static void leggTilTilkommetInntektDersomRelevant(HåndterBeregningsgrunnlagInput input,
                                                              BeregningsgrunnlagDto.Builder bgBuilder,
                                                              Optional<VurderTilkomneInntektsforholdPeriodeDto> vurdertPeriode,
                                                              BeregningsgrunnlagPeriodeDto p) {
        if (vurdertPeriode.isEmpty()) {
            return;
        }

        var vurderteInntektsforhold = vurdertPeriode.get().getTilkomneInntektsforhold();
        var periodeBuilder = bgBuilder.getPeriodeBuilderFor(p.getPeriode()).orElseThrow();
        var vurderInntektsforhold = TilkommetInntektsforholdTjeneste.finnTilkomneInntektsforhold(input.getSkjæringstidspunktForBeregning(),
                input.getIayGrunnlag().getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter).orElse(Collections.emptyList()),
                p.getBeregningsgrunnlagPrStatusOgAndelList(),
                p.getPeriode(),
                input.getYtelsespesifiktGrunnlag());
        vurderteInntektsforhold.stream()
                .filter(i -> skalVurderesForPeriode(vurderInntektsforhold, i))
                .map(i -> mapTilkommetInntekt(input, p, i))
                .forEach(periodeBuilder::leggTilTilkommetInntekt);
    }

    private static boolean skalVurderesForPeriode(Collection<TilkommetInntektsforholdTjeneste.StatusOgArbeidsgiver> vurderInntektsforhold, NyttInntektsforholdDto i) {
        return vurderInntektsforhold.stream().anyMatch(v ->
                v.aktivitetStatus().equals(i.getAktivitetStatus()) &&
                        v.arbeidsgiver().getIdentifikator().equals(i.getArbeidsgiverIdentifikator()));
    }

    private static TilkommetInntektDto mapTilkommetInntekt(HåndterBeregningsgrunnlagInput input, BeregningsgrunnlagPeriodeDto p, NyttInntektsforholdDto i) {
        return new TilkommetInntektDto(
                i.getAktivitetStatus(),
                mapArbeidsgiver(i),
                InternArbeidsforholdRefDto.ref(i.getArbeidsforholdId()),
                finnBruttoPrÅr(i, input.getIayGrunnlag()),
                utledTilkommetFraBrutto(i, p.getPeriode(), input.getYtelsespesifiktGrunnlag()),
                i.getSkalRedusereUtbetaling()
        );
    }

    private static BigDecimal finnBruttoPrÅr(NyttInntektsforholdDto i, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var inntektsmelding = finnInntektsmelding(i, iayGrunnlag);
        return inntektsmelding.map(VurderTilkommetInntektTjeneste::mapTilÅrsinntekt)
                .orElseGet(() -> i.getBruttoInntektPrÅr() != null ? BigDecimal.valueOf(i.getBruttoInntektPrÅr()) : null);
    }

    private static Optional<InntektsmeldingDto> finnInntektsmelding(NyttInntektsforholdDto i, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return iayGrunnlag.getInntektsmeldinger().stream()
                .flatMap(ims -> ims.getAlleInntektsmeldinger().stream())
                .filter(im -> Objects.equals(im.getArbeidsgiver().getIdentifikator(), i.getArbeidsgiverIdentifikator())
                        && InternArbeidsforholdRefDto.ref(i.getArbeidsforholdId()).gjelderFor(im.getArbeidsforholdRef()))
                .findFirst();
    }

    private static BigDecimal mapTilÅrsinntekt(InntektsmeldingDto inntektsmeldingDto) {
        return inntektsmeldingDto.getInntektBeløp().getVerdi().multiply(BigDecimal.valueOf(12));
    }

    private static Arbeidsgiver mapArbeidsgiver(NyttInntektsforholdDto i) {
        if (i.getArbeidsgiverIdentifikator() == null) {
            return null;
        }
        return OrgNummer.erGyldigOrgnr(i.getArbeidsgiverIdentifikator()) ? Arbeidsgiver.virksomhet(i.getArbeidsgiverIdentifikator()) :
                Arbeidsgiver.person(new AktørId(i.getArbeidsgiverIdentifikator()));
    }

    private static BigDecimal utledTilkommetFraBrutto(NyttInntektsforholdDto inntektsforhold, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            if (inntektsforhold.getAktivitetStatus().erArbeidstaker() && inntektsforhold.getArbeidsgiverIdentifikator() != null) {
                var utbetalingsgradProsent = UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(
                        mapArbeidsgiver(inntektsforhold),
                        InternArbeidsforholdRefDto.ref(inntektsforhold.getArbeidsforholdId()),
                        periode,
                        ytelsespesifiktGrunnlag,
                        true);
                var utbetalingsgrad = utbetalingsgradProsent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                return BigDecimal.valueOf(inntektsforhold.getBruttoInntektPrÅr()).multiply(BigDecimal.ONE.subtract(utbetalingsgrad));
            } else {
                var utbetalingsgradProsent = UtbetalingsgradTjeneste.finnUtbetalingsgradForStatus(
                        inntektsforhold.getAktivitetStatus(),
                        periode,
                        ytelsespesifiktGrunnlag);
                var utbetalingsgrad = utbetalingsgradProsent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                return BigDecimal.valueOf(inntektsforhold.getBruttoInntektPrÅr()).multiply(BigDecimal.ONE.subtract(utbetalingsgrad));
            }
        }
        throw new IllegalStateException("Kun gyldig ved utbetalingsgradgrunnlag");
    }

    private static List<BeregningsgrunnlagPeriodeDto> finnBgPerioder(HåndterBeregningsgrunnlagInput input) {
        return input.getBeregningsgrunnlagGrunnlag()
                .getBeregningsgrunnlag()
                .map(BeregningsgrunnlagDto::getBeregningsgrunnlagPerioder)
                .orElse(Collections.emptyList());
    }

}
