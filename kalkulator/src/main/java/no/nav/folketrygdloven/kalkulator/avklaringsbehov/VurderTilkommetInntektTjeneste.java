package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
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
        vurderteInntektsforholdPerioder.forEach(p -> leggTilInntektsforhold(input, bgBuilder, perioder, p));
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FASTSATT_INN);
    }

    private static void leggTilInntektsforhold(HåndterBeregningsgrunnlagInput input,
                                               BeregningsgrunnlagDto.Builder bgBuilder,
                                               List<BeregningsgrunnlagPeriodeDto> perioder,
                                               VurderTilkomneInntektsforholdPeriodeDto p) {
        var bgPerioder = perioder.stream().filter(bgp -> bgp.getPeriode().overlapper(Intervall.fraOgMedTilOgMed(p.getFom(), p.getTom()))).toList();
        if (bgPerioder.size() > 1) {
            throw new IllegalStateException("Forventer maksimalt 1 periode fant " + bgPerioder);
        }
        if (!bgPerioder.isEmpty()) {
            var bgPeriode = bgPerioder.get(0);
            var periodeBuilder = bgBuilder.getPeriodeBuilderFor(bgPeriode.getPeriode()).orElseThrow();
            p.getTilkomneInntektsforhold().stream()
                    .map(i -> {
                        var tilkommetInntektDto = finnTilkommetInntektTilVurdering(bgPeriode.getTilkomneInntekter(), i).orElseThrow();
                        return new TilkommetInntektDto(tilkommetInntektDto.getAktivitetStatus(),
                                tilkommetInntektDto.getArbeidsgiver().orElse(null),
                                tilkommetInntektDto.getArbeidsforholdRef(),
                                i.getSkalRedusereUtbetaling() ? finnBruttoPrÅr(i, input.getIayGrunnlag()) : null,
                                i.getSkalRedusereUtbetaling() ? utledTilkommetFraBrutto(i, bgPeriode.getPeriode(), input.getYtelsespesifiktGrunnlag()) : null,
                                i.getSkalRedusereUtbetaling());
                    }).
                    forEach(periodeBuilder::leggTilTilkommetInntekt);


            if (bgPeriode.getTilkomneInntekter().stream()
                    .anyMatch(it -> it.skalRedusereUtbetaling() == null)) {
                throw new IllegalStateException(String.format("Periode %s har tilkomne inntektsforhold som ikke har blitt vurdert", bgPeriode.getPeriode()));
            }
        }
    }

    private static Optional<TilkommetInntektDto> finnTilkommetInntektTilVurdering(Collection<TilkommetInntektDto> vurderInntektsforhold, NyttInntektsforholdDto i) {
        return vurderInntektsforhold.stream().filter(v ->
                v.getAktivitetStatus().equals(i.getAktivitetStatus()) &&
                        (ingenHarArbeidsgiver(i, v) || harLikArbeidsgiver(i, v)) &&
                        v.getArbeidsforholdRef().gjelderFor(InternArbeidsforholdRefDto.ref(i.getArbeidsforholdId()))).findFirst();
    }

    private static boolean harLikArbeidsgiver(NyttInntektsforholdDto i, TilkommetInntektDto v) {
        return v.getArbeidsgiver().isPresent() && v.getArbeidsgiver().get().getIdentifikator().equals(i.getArbeidsgiverIdentifikator());
    }

    private static boolean ingenHarArbeidsgiver(NyttInntektsforholdDto i, TilkommetInntektDto v) {
        return v.getArbeidsgiver().isEmpty() && i.getArbeidsgiverIdentifikator() == null;
    }


    private static BigDecimal finnBruttoPrÅr(NyttInntektsforholdDto i, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (i.getBruttoInntektPrÅr() != null) {
            return BigDecimal.valueOf(i.getBruttoInntektPrÅr());
        }
        var inntektsmelding = finnInntektsmelding(i, iayGrunnlag);
        return inntektsmelding.map(VurderTilkommetInntektTjeneste::mapTilÅrsinntekt).orElse(null);
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
        var tilVurderingTjeneste = new PerioderTilVurderingTjeneste(input.getForlengelseperioder(), input.getBeregningsgrunnlag());
        return input.getBeregningsgrunnlagGrunnlag()
                .getBeregningsgrunnlag()
                .stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(p -> tilVurderingTjeneste.erTilVurdering(p.getPeriode()))
                .toList();
    }

}
