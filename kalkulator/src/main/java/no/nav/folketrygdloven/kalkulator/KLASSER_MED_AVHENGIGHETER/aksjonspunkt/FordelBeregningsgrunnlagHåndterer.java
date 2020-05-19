package no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FastsettBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.aksjonspunkt.dto.FordelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrgNummer;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public class FordelBeregningsgrunnlagHåndterer {

    private FordelBeregningsgrunnlagHåndterer() {
        // skjul
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(FordelBeregningsgrunnlagDto dto, BeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagPeriodeDto> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (FastsettBeregningsgrunnlagPeriodeDto endretPeriode : dto.getEndretBeregningsgrunnlagPerioder()) {
            fastsettVerdierForPeriode(input, perioder, endretPeriode);
        }
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FASTSATT_INN);
    }


    private static void fastsettVerdierForPeriode(BeregningsgrunnlagInput input,
                                                  List<BeregningsgrunnlagPeriodeDto> perioder, FastsettBeregningsgrunnlagPeriodeDto endretPeriode) {
        BeregningsgrunnlagPeriodeDto korrektPeriode = getKorrektPeriode(input, perioder, endretPeriode);
        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> refusjonMap = FordelRefusjonTjeneste.getRefusjonPrÅrMap(input, endretPeriode, korrektPeriode);
        BeregningsgrunnlagPeriodeDto uendretPeriode = BeregningsgrunnlagPeriodeDto.builder(korrektPeriode).buildForKopi();
        BeregningsgrunnlagPeriodeDto.Builder perioderBuilder = BeregningsgrunnlagPeriodeDto.oppdater(korrektPeriode)
            .fjernAlleBeregningsgrunnlagPrStatusOgAndeler();
        // Må sortere med eksisterende først for å sette andelsnr på disse først
        List<FastsettBeregningsgrunnlagAndelDto> sorted = sorterMedNyesteSist(endretPeriode);
        for (FastsettBeregningsgrunnlagAndelDto endretAndel : sorted) {
            perioderBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(fastsettVerdierForAndel(uendretPeriode, refusjonMap, endretAndel));
        }
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder fastsettVerdierForAndel(BeregningsgrunnlagPeriodeDto korrektPeriode,
                                                                                        Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> refusjonMap,
                                                                                        FastsettBeregningsgrunnlagAndelDto endretAndel) {
        FastsatteVerdierDto fastsatteVerdier = endretAndel.getFastsatteVerdier();
        FastsatteVerdierDto verdierMedJustertRefusjon = lagVerdierMedFordeltRefusjon(refusjonMap, endretAndel, fastsatteVerdier);
        return byggAndel(korrektPeriode, endretAndel, verdierMedJustertRefusjon);
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder byggAndel(BeregningsgrunnlagPeriodeDto korrektPeriode, FastsettBeregningsgrunnlagAndelDto endretAndel, FastsatteVerdierDto verdierMedJustertRefusjon) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = lagBuilderMedInntektOgInntektskategori(endretAndel);
        if (gjelderArbeidsforhold(endretAndel)) {
            byggArbeidsforhold(korrektPeriode, endretAndel, verdierMedJustertRefusjon, andelBuilder);
        }
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> korrektAndel = finnAndelMedMatchendeAndelsnr(korrektPeriode, endretAndel);
        if (korrektAndel.isPresent()) {
            if (gjelderAAPEllerDagpenger(endretAndel)) {
                mapFelterForYtelse(korrektAndel.get(), andelBuilder);
            }
            if (!endretAndel.getNyAndel()) {
                mapBeregnetOgOverstyrt(korrektAndel.get(), andelBuilder);
            }
        }
        return andelBuilder;
    }

    private static void mapBeregnetOgOverstyrt(BeregningsgrunnlagPrStatusOgAndelDto korrektAndel, BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder) {
        andelBuilder.medBeregnetPrÅr(korrektAndel.getBeregnetPrÅr());
        andelBuilder.medOverstyrtPrÅr(korrektAndel.getOverstyrtPrÅr());
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelMedMatchendeAndelsnr(BeregningsgrunnlagPeriodeDto korrektPeriode, FastsettBeregningsgrunnlagAndelDto endretAndel) {
        return korrektPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getAndelsnr().equals(endretAndel.getAndelsnr())).findFirst();
    }

    private static void mapFelterForYtelse(BeregningsgrunnlagPrStatusOgAndelDto korrektAndel, BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder) {
        andelBuilder.medOrginalDagsatsFraTilstøtendeYtelse(korrektAndel.getOrginalDagsatsFraTilstøtendeYtelse());
        andelBuilder.medÅrsbeløpFraTilstøtendeYtelse(korrektAndel.getÅrsbeløpFraTilstøtendeYtelseVerdi());
    }

    private static boolean gjelderAAPEllerDagpenger(FastsettBeregningsgrunnlagAndelDto endretAndel) {
        return endretAndel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSAVKLARINGSPENGER) || endretAndel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER);
    }

    private static boolean gjelderArbeidsforhold(FastsettBeregningsgrunnlagAndelDto endretAndel) {
        return endretAndel.getArbeidsgiverId() != null;
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder lagBuilderMedInntektOgInntektskategori(FastsettBeregningsgrunnlagAndelDto endretAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny();

        if (endretAndel.getBeregningsperiodeFom() != null) {
            builder.medBeregningsperiode(endretAndel.getBeregningsperiodeFom(), endretAndel.getBeregningsperiodeTom());
        }
        return builder
            .medAndelsnr(endretAndel.getNyAndel() ? null : endretAndel.getAndelsnr()) // Opprettholder andelsnr som andel ble lagret med forrige gang
            .medAktivitetStatus(endretAndel.getAktivitetStatus())
            .medInntektskategori(endretAndel.getFastsatteVerdier().getInntektskategori())
            .medFordeltPrÅr(endretAndel.getFastsatteVerdier().finnEllerUtregnFastsattBeløpPrÅr())
            .medLagtTilAvSaksbehandler(endretAndel.getLagtTilAvSaksbehandler())
            .medArbforholdType(endretAndel.getArbeidsforholdType())
            .medFastsattAvSaksbehandler(true);
    }

    private static void byggArbeidsforhold(BeregningsgrunnlagPeriodeDto korrektPeriode,
                                           FastsettBeregningsgrunnlagAndelDto endretAndel,
                                           FastsatteVerdierDto verdierMedJustertRefusjon,
                                           BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder) {
        var arbeidsgiver = finnArbeidsgiver(endretAndel);
        Optional<BGAndelArbeidsforholdDto> arbeidsforholdOpt = finnRiktigArbeidsforholdFraGrunnlag(korrektPeriode, endretAndel);
        if (arbeidsforholdOpt.isPresent()) {
            var arbeidsforhold = arbeidsforholdOpt.get();
            BGAndelArbeidsforholdDto.Builder abeidsforholdBuilder = andelBuilder.getBgAndelArbeidsforholdDtoBuilder().medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(endretAndel.getArbeidsforholdId())
                .medRefusjonskravPrÅr(verdierMedJustertRefusjon.getRefusjonPrÅr() != null ? BigDecimal.valueOf(verdierMedJustertRefusjon.getRefusjonPrÅr()) : BigDecimal.ZERO)
                .medArbeidsperiodeFom(arbeidsforhold.getArbeidsperiodeFom())
                .medArbeidsperiodeTom(arbeidsforhold.getArbeidsperiodeTom().orElse(null));
            if (!endretAndel.getLagtTilAvSaksbehandler() && endretAndel.getFastsatteVerdier().getFastsattÅrsbeløpInklNaturalytelse() == null) {
                mapNaturalytelse(arbeidsforhold, abeidsforholdBuilder);
            }
            andelBuilder.medBGAndelArbeidsforhold(abeidsforholdBuilder);
        }
    }

    private static void mapNaturalytelse(BGAndelArbeidsforholdDto arbeidsforhold, BGAndelArbeidsforholdDto.Builder abeidsforholdBuilder) {
        abeidsforholdBuilder
            .medNaturalytelseTilkommetPrÅr(arbeidsforhold.getNaturalytelseTilkommetPrÅr().orElse(null))
            .medNaturalytelseBortfaltPrÅr(arbeidsforhold.getNaturalytelseBortfaltPrÅr().orElse(null));
    }

    private static Optional<BGAndelArbeidsforholdDto> finnRiktigArbeidsforholdFraGrunnlag(BeregningsgrunnlagPeriodeDto korrektPeriode, FastsettBeregningsgrunnlagAndelDto endretAndel) {
        return korrektPeriode
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(a -> a.getArbeidsgiver().getIdentifikator().equals(endretAndel.getArbeidsgiverId()) && a.getArbeidsforholdRef().gjelderFor(endretAndel.getArbeidsforholdId()))
            .findFirst();
    }

    private static Arbeidsgiver finnArbeidsgiver(FastsettBeregningsgrunnlagAndelDto endretAndel) {
        Arbeidsgiver arbeidsgiver;
        if (OrgNummer.erGyldigOrgnr(endretAndel.getArbeidsgiverId())) {
            arbeidsgiver = Arbeidsgiver.virksomhet(endretAndel.getArbeidsgiverId());
        } else {
            arbeidsgiver = Arbeidsgiver.person(new AktørId(endretAndel.getArbeidsgiverId()));
        }
        return arbeidsgiver;
    }

    private static FastsatteVerdierDto lagVerdierMedFordeltRefusjon(Map<FastsettBeregningsgrunnlagAndelDto, BigDecimal> refusjonMap,
                                                                    FastsettBeregningsgrunnlagAndelDto endretAndel, FastsatteVerdierDto fastsatteVerdier) {
        return FastsatteVerdierDto.Builder.oppdater(fastsatteVerdier)
                .medRefusjonPrÅr(refusjonMap.get(endretAndel) != null ? refusjonMap.get(endretAndel).intValue() : null)
                .build();
    }

    private static BeregningsgrunnlagPeriodeDto getKorrektPeriode(BeregningsgrunnlagInput input, List<BeregningsgrunnlagPeriodeDto> perioder,
                                                                  FastsettBeregningsgrunnlagPeriodeDto endretPeriode) {
        return perioder.stream()
            .filter(periode -> periode.getBeregningsgrunnlagPeriodeFom().equals(endretPeriode.getFom()))
            .findFirst()
            .orElseThrow(() -> FordelBeregningsgrunnlagHåndtererFeil.FACTORY.finnerIkkePeriodeFeil(input.getBehandlingReferanse().getBehandlingId()).toException());
    }

    private static List<FastsettBeregningsgrunnlagAndelDto> sorterMedNyesteSist(FastsettBeregningsgrunnlagPeriodeDto endretPeriode) {
        Comparator<FastsettBeregningsgrunnlagAndelDto> fastsettBeregningsgrunnlagAndelDtoComparator = (a1, a2) -> {
            if (a1.getNyAndel()) {
                return 1;
            }
            if (a2.getNyAndel()) {
                return -1;
            }
            return 0;
        };
        return endretPeriode.getAndeler().stream().sorted(fastsettBeregningsgrunnlagAndelDtoComparator).collect(Collectors.toList());
    }
    private interface FordelBeregningsgrunnlagHåndtererFeil extends DeklarerteFeil {


        FordelBeregningsgrunnlagHåndtererFeil FACTORY = FeilFactory.create(FordelBeregningsgrunnlagHåndtererFeil.class);

        @TekniskFeil(feilkode = "FT-401647", feilmelding = "Finner ikke periode for eksisterende grunnlag. Behandling  %s", logLevel = LogLevel.WARN)
        Feil finnerIkkePeriodeFeil(long behandlingId);
    }

}
