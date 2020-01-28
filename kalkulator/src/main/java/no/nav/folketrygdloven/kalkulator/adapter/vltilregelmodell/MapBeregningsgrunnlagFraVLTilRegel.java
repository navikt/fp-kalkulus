package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.adapter.util.BeregningsgrunnlagUtil;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapInntektskategoriFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeÅrsakDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;

public class MapBeregningsgrunnlagFraVLTilRegel {
    private static final String TOGGLE = "fpsak.splitteSammenligningATFL";

    private static final Map<SammenligningsgrunnlagType, AktivitetStatus> SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP = Map.of(
        SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN, AktivitetStatus.ATFL_SN,
        SammenligningsgrunnlagType.SAMMENLIGNING_AT, AktivitetStatus.AT,
        SammenligningsgrunnlagType.SAMMENLIGNING_FL, AktivitetStatus.FL,
        SammenligningsgrunnlagType.SAMMENLIGNING_SN, AktivitetStatus.SN
    );

    private MapBeregningsgrunnlagFraVLTilRegel() {
        // skjul meg
    }

    public static List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode> mapTilFordelingsregel(BehandlingReferanse referanse,
                                                                                                                               BeregningsgrunnlagDto Beregningsgrunnlag, BeregningsgrunnlagInput input) {
        Objects.requireNonNull(referanse, "BehandlingReferanse kan ikke være null!");
        Objects.requireNonNull(Beregningsgrunnlag, "Beregningsgrunnlag kan ikke være null!");
        return mapBeregningsgrunnlagPerioder(Beregningsgrunnlag, input);
    }

    public static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag map(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto oppdatertGrunnlag) {
        var ref = input.getBehandlingReferanse();
        Objects.requireNonNull(ref, "BehandlingReferanse kan ikke være null!");
        Objects.requireNonNull(oppdatertGrunnlag, "BeregningsgrunnlagGrunnlag kan ikke være null");
        BeregningsgrunnlagDto beregningsgrunnlag = oppdatertGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "Beregningsgrunnlag kan ikke være null!");

        List<AktivitetStatusMedHjemmel> aktivitetStatuser = beregningsgrunnlag.getAktivitetStatuser().stream()
            .map(MapBeregningsgrunnlagFraVLTilRegel::mapVLAktivitetStatusMedHjemmel)
            .sorted()
            .collect(Collectors.toList());

        Inntektsgrunnlag inntektsgrunnlag = MapInntektsgrunnlagVLTilRegel.map(ref, input, beregningsgrunnlag.getSkjæringstidspunkt());
        List<BeregningsgrunnlagPeriode> perioder = mapBeregningsgrunnlagPerioder(beregningsgrunnlag, input);
        //Sammenligningsgrunnlaget blir alltid satt inne i regel
        EnumMap<AktivitetStatus, SammenligningsGrunnlag> sammenligningsgrunnlagMap = mapSammenligningsgrunnlagPrStatus(beregningsgrunnlag);
        SammenligningsGrunnlag sammenligningsgrunnlag = beregningsgrunnlag.getSammenligningsgrunnlag() != null ?
            mapSammenligningsGrunnlag(beregningsgrunnlag.getSammenligningsgrunnlag()) : null;
        Dekningsgrad dekningsgrad = finnDekningsgrad(input);

        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();

        boolean erMilitærIOpptjeningsperioden = harHattMilitærIOpptjeningsperioden(oppdatertGrunnlag.getGjeldendeAktiviteter());

        var builder = no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder();
        sammenligningsgrunnlagMap.forEach(builder::medSammenligningsgrunnlagPrStatus);
        return builder
            .medInntektsgrunnlag(inntektsgrunnlag)
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medAktivitetStatuser(aktivitetStatuser)
            .medBeregningsgrunnlagPerioder(perioder)
            .medSammenligningsgrunnlag(sammenligningsgrunnlag)
            .medDekningsgrad(dekningsgrad)
            .medGrunnbeløp(beregningsgrunnlag.getGrunnbeløp().getVerdi())
            .medGrunnbeløpSatser(input.getGrunnbeløpsatser())
            .medMilitærIOpptjeningsperioden(erMilitærIOpptjeningsperioden)
            .medAntallGrunnbeløpMilitærHarKravPå(finnGrunnbeløpMilitærHarKravPå(input))
            .build();
    }

    private static List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak> mapPeriodeÅrsak(List<BeregningsgrunnlagPeriodeÅrsakDto> beregningsgrunnlagPeriodeÅrsaker) {
        if (beregningsgrunnlagPeriodeÅrsaker.isEmpty()) {
            return Collections.emptyList();
        }
        List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak> periodeÅrsakerMapped = new ArrayList<>();
        beregningsgrunnlagPeriodeÅrsaker.forEach(bgPeriodeÅrsak -> {
            if (!PeriodeÅrsak.UDEFINERT.equals(bgPeriodeÅrsak.getPeriodeÅrsak())) {
                try {
                    periodeÅrsakerMapped.add(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.valueOf(bgPeriodeÅrsak.getPeriodeÅrsak().getKode()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Ukjent PeriodeÅrsak: (" + bgPeriodeÅrsak.getPeriodeÅrsak().getKode() + ").", e);
                }
            }
        });
        return periodeÅrsakerMapped;
    }

    private static int finnGrunnbeløpMilitærHarKravPå(BeregningsgrunnlagInput input) {
        return input.getYtelsespesifiktGrunnlag().getGrunnbeløpMilitærHarKravPå();
    }

    private static Dekningsgrad finnDekningsgrad(BeregningsgrunnlagInput input) {
        return Dekningsgrad.fra(input.getYtelsespesifiktGrunnlag().getDekningsgrad());
    }

    private static AktivitetStatusMedHjemmel mapVLAktivitetStatusMedHjemmel(final BeregningsgrunnlagAktivitetStatusDto vlBGAktivitetStatus) {
        BeregningsgrunnlagHjemmel hjemmel = null;
        if (!Hjemmel.UDEFINERT.equals(vlBGAktivitetStatus.getHjemmel())) {
            try {
                hjemmel = BeregningsgrunnlagHjemmel.valueOf(vlBGAktivitetStatus.getHjemmel().getKode());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Ukjent Hjemmel: (" + vlBGAktivitetStatus.getHjemmel().getKode() + ").", e);
            }
        }
        AktivitetStatus as = mapVLAktivitetStatus(vlBGAktivitetStatus.getAktivitetStatus());
        return new AktivitetStatusMedHjemmel(as, hjemmel);
    }

    private static AktivitetStatus mapVLAktivitetStatus(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.kodeverk.AktivitetStatus vlBGAktivitetStatus) {
        if (BeregningsgrunnlagUtil.erATFL(vlBGAktivitetStatus)) {
            return AktivitetStatus.ATFL;
        }

        try {
            return AktivitetStatus.valueOf(vlBGAktivitetStatus.getKode());
        } catch (IllegalArgumentException e) {
            if (BeregningsgrunnlagUtil.erATFL_SN(vlBGAktivitetStatus)) {
                return AktivitetStatus.ATFL_SN;
            }
            throw new IllegalStateException("Ukjent AktivitetStatus: (" + vlBGAktivitetStatus.getKode() + ").", e);
        }
    }

    private static SammenligningsGrunnlag mapSammenligningsGrunnlag(SammenligningsgrunnlagDto sammenligningsgrunnlag) {
        return SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(new Periode(
                sammenligningsgrunnlag.getSammenligningsperiodeFom(),
                sammenligningsgrunnlag.getSammenligningsperiodeTom()))
            .medRapportertPrÅr(sammenligningsgrunnlag.getRapportertPrÅr())
            .medAvvikProsentFraPromilleNy(sammenligningsgrunnlag.getAvvikPromilleNy())
            .build();
    }

    private static List<BeregningsgrunnlagPeriode> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagDto vlBeregningsgrunnlag, BeregningsgrunnlagInput input) {
        List<BeregningsgrunnlagPeriode> perioder = new ArrayList<>();
        vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(vlBGPeriode -> {
            final BeregningsgrunnlagPeriode.Builder regelBGPeriode = BeregningsgrunnlagPeriode.builder()
                .medPeriode(Periode.of(vlBGPeriode.getBeregningsgrunnlagPeriodeFom(), vlBGPeriode.getBeregningsgrunnlagPeriodeTom()))
                .medskalSplitteATFL(input.isEnabled(TOGGLE, false))
                .leggTilPeriodeÅrsaker(mapPeriodeÅrsak(vlBGPeriode.getBeregningsgrunnlagPeriodeÅrsaker()));

            List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = mapVLBGPrStatus(vlBGPeriode, vlBeregningsgrunnlag.getFaktaOmBeregningTilfeller());
            beregningsgrunnlagPrStatus.forEach(regelBGPeriode::medBeregningsgrunnlagPrStatus);
            perioder.add(regelBGPeriode.build());
        });

        return perioder;
    }

    private static List<BeregningsgrunnlagPrStatus> mapVLBGPrStatus(BeregningsgrunnlagPeriodeDto vlBGPeriode, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        List<BeregningsgrunnlagPrStatus> liste = new ArrayList<>();
        BeregningsgrunnlagPrStatus bgpsATFL = null;

        for (BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
            if (AktivitetStatus.ATFL.equals(regelAktivitetStatus) || AktivitetStatus.AT.equals(regelAktivitetStatus)) {
                if (bgpsATFL == null) {  // Alle ATFL håndteres samtidig her
                    bgpsATFL = mapVLBGPStatusForATFL(vlBGPeriode, regelAktivitetStatus, faktaOmBeregningTilfeller);
                    liste.add(bgpsATFL);
                }
            } else {
                BeregningsgrunnlagPrStatus bgps = mapVLBGPStatusForAlleAktivietetStatuser(vlBGPStatus);
                liste.add(bgps);
            }
        }
        return liste;
    }

    private static EnumMap<AktivitetStatus, SammenligningsGrunnlag> mapSammenligningsgrunnlagPrStatus(BeregningsgrunnlagDto Beregningsgrunnlag){
        EnumMap<AktivitetStatus, SammenligningsGrunnlag> sammenligningsGrunnlagMap = new EnumMap<>(AktivitetStatus.class);
        for (SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagPrStatus : Beregningsgrunnlag.getSammenligningsgrunnlagPrStatusListe()){
            SammenligningsGrunnlag sammenligningsGrunnlag = SammenligningsGrunnlag.builder()
                .medSammenligningsperiode(new Periode(
                    sammenligningsgrunnlagPrStatus.getSammenligningsperiodeFom(),
                    sammenligningsgrunnlagPrStatus.getSammenligningsperiodeTom()))
                .medRapportertPrÅr(sammenligningsgrunnlagPrStatus.getRapportertPrÅr())
                .medAvvikProsentFraPromilleNy(sammenligningsgrunnlagPrStatus.getAvvikPromilleNy())
                .build();
            sammenligningsGrunnlagMap.put(SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP.get(sammenligningsgrunnlagPrStatus.getSammenligningsgrunnlagType()), sammenligningsGrunnlag);
        }
        return sammenligningsGrunnlagMap;
    }

    // Ikke ATFL og TY, de har separat mapping
    private static BeregningsgrunnlagPrStatus mapVLBGPStatusForAlleAktivietetStatuser(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
        List<BigDecimal> pgi = (vlBGPStatus.getPgiSnitt() == null ? new ArrayList<>() :
            Arrays.asList(vlBGPStatus.getPgi1(), vlBGPStatus.getPgi2(), vlBGPStatus.getPgi3()));
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(regelAktivitetStatus)
            .medBeregningsperiode(beregningsperiodeFor(vlBGPStatus))
            .medBeregnetPrÅr(vlBGPStatus.getBeregnetPrÅr())
            .medOverstyrtPrÅr(vlBGPStatus.getOverstyrtPrÅr())
            .medFordeltPrÅr(vlBGPStatus.getFordeltPrÅr())
            .medGjennomsnittligPGI(vlBGPStatus.getPgiSnitt())
            .medPGI(pgi)
            .medÅrsbeløpFraTilstøtendeYtelse(vlBGPStatus.getÅrsbeløpFraTilstøtendeYtelseVerdi())
            .medErNyIArbeidslivet(vlBGPStatus.getNyIArbeidslivet())
            .medAndelNr(vlBGPStatus.getAndelsnr())
            .medInntektskategori(MapInntektskategoriFraVLTilRegel.map(vlBGPStatus.getInntektskategori()))
            .medFastsattAvSaksbehandler(vlBGPStatus.getFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(vlBGPStatus.getLagtTilAvSaksbehandler())
            .medBesteberegningPrÅr(vlBGPStatus.getBesteberegningPrÅr())
            .medOrginalDagsatsFraTilstøtendeYtelse(vlBGPStatus.getOrginalDagsatsFraTilstøtendeYtelse())
            .build();
    }

    private static Periode beregningsperiodeFor(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        if (vlBGPStatus.getBeregningsperiodeFom() == null && vlBGPStatus.getBeregningsperiodeTom() == null) {
            return null;
        }
        return Periode.of(vlBGPStatus.getBeregningsperiodeFom(), vlBGPStatus.getBeregningsperiodeTom());
    }

    // Felles mapping av alle statuser som mapper til ATFL
    private static BeregningsgrunnlagPrStatus mapVLBGPStatusForATFL(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                                    AktivitetStatus regelAktivitetStatus, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {

        BeregningsgrunnlagPrStatus.Builder regelBGPStatusATFL = BeregningsgrunnlagPrStatus.builder().medAktivitetStatus(regelAktivitetStatus)
            .medFlOgAtISammeOrganisasjon(faktaOmBeregningTilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON));

        for (BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            if (regelAktivitetStatus.equals(mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus()))) {
                BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold = byggAndel(vlBGPStatus);
                regelBGPStatusATFL.medArbeidsforhold(regelArbeidsforhold);
            }
        }
        return regelBGPStatusATFL.build();
    }

    private static BeregningsgrunnlagPrArbeidsforhold byggAndel(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        BeregningsgrunnlagPrArbeidsforhold.Builder builder = BeregningsgrunnlagPrArbeidsforhold.builder();
        builder
            .medInntektskategori(MapInntektskategoriFraVLTilRegel.map(vlBGPStatus.getInntektskategori()))
            .medBeregnetPrÅr(vlBGPStatus.getBeregnetPrÅr())
            .medBeregningsperiode(beregningsperiodeFor(vlBGPStatus))
            .medFastsattAvSaksbehandler(vlBGPStatus.getFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(vlBGPStatus.getLagtTilAvSaksbehandler())
            .medAndelNr(vlBGPStatus.getAndelsnr())
            .medOverstyrtPrÅr(vlBGPStatus.getOverstyrtPrÅr())
            .medFordeltPrÅr(vlBGPStatus.getFordeltPrÅr())
            .medArbeidsforhold(MapArbeidsforholdFraVLTilRegel.arbeidsforholdFor(vlBGPStatus));

        vlBGPStatus.getBgAndelArbeidsforhold().ifPresent(bga ->
            builder
                .medNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().orElse(null))
                .medNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().orElse(null))
                .medErTidsbegrensetArbeidsforhold(bga.getErTidsbegrensetArbeidsforhold())
                .medRefusjonskravPrÅr(bga.getRefusjonskravPrÅr()));

        return builder.build();
    }

    private static boolean harHattMilitærIOpptjeningsperioden(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        Objects.requireNonNull(beregningAktivitetAggregat, "beregningAktivitetAggregat");
        return beregningAktivitetAggregat.getBeregningAktiviteter().stream()
            .map(BeregningAktivitetDto::getOpptjeningAktivitetType)
            .anyMatch(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE::equals);
    }
}
