package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.util.BeregningsgrunnlagUtil;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapInntektskategoriFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapPeriodeÅrsakFraVlTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FullføreBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.VurderBeregningsgrunnlagvilkårInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeÅrsakDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

@ApplicationScoped
public class MapBeregningsgrunnlagFraVLTilRegel {
    private static final String TOGGLE = "fpsak.splitteSammenligningATFL";
    private static final Map<SammenligningsgrunnlagType, AktivitetStatus> SAMMENLIGNINGSGRUNNLAGTYPE_AKTIVITETSTATUS_MAP = Map.of(
            SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN, AktivitetStatus.ATFL_SN,
            SammenligningsgrunnlagType.SAMMENLIGNING_AT, AktivitetStatus.AT,
            SammenligningsgrunnlagType.SAMMENLIGNING_FL, AktivitetStatus.FL,
            SammenligningsgrunnlagType.SAMMENLIGNING_SN, AktivitetStatus.SN
    );
    private Instance<MapInntektsgrunnlagVLTilRegel> alleInntektMappere;
    private Instance<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper;

    public MapBeregningsgrunnlagFraVLTilRegel() {
        // CDI
    }

    @Inject
    public MapBeregningsgrunnlagFraVLTilRegel(@Any Instance<MapInntektsgrunnlagVLTilRegel> inntektsmapper,
                                              @Any Instance<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper) {
        this.alleInntektMappere = inntektsmapper;
        this.ytelsesSpesifikkMapper = ytelsesSpesifikkMapper;
    }

    public List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode> mapTilFordelingsregel(KoblingReferanse referanse,
                                                                                                                                BeregningsgrunnlagDto Beregningsgrunnlag, BeregningsgrunnlagInput input) {
        Objects.requireNonNull(referanse, "BehandlingReferanse kan ikke være null!");
        Objects.requireNonNull(Beregningsgrunnlag, "Beregningsgrunnlag kan ikke være null!");
        return mapBeregningsgrunnlagPerioder(Beregningsgrunnlag, input);
    }

    public no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag map(BeregningsgrunnlagInput input,
                                                                                                 BeregningsgrunnlagGrunnlagDto oppdatertGrunnlag) {
        var ref = input.getKoblingReferanse();
        Objects.requireNonNull(ref, "BehandlingReferanse kan ikke være null!");
        Objects.requireNonNull(oppdatertGrunnlag, "BeregningsgrunnlagGrunnlag kan ikke være null");
        BeregningsgrunnlagDto beregningsgrunnlag = oppdatertGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "Beregningsgrunnlag kan ikke være null!");

        List<AktivitetStatusMedHjemmel> aktivitetStatuser = beregningsgrunnlag.getAktivitetStatuser().stream()
                .map(this::mapVLAktivitetStatusMedHjemmel)
                .sorted()
                .collect(Collectors.toList());

        MapInntektsgrunnlagVLTilRegel inntektMapper = FagsakYtelseTypeRef.Lookup.find(alleInntektMappere, ref.getFagsakYtelseType()).orElseThrow();
        Inntektsgrunnlag inntektsgrunnlag = inntektMapper.map(input, beregningsgrunnlag.getSkjæringstidspunkt());
        List<BeregningsgrunnlagPeriode> perioder = mapBeregningsgrunnlagPerioder(beregningsgrunnlag, input);
        //Sammenligningsgrunnlaget blir alltid satt inne i regel
        EnumMap<AktivitetStatus, SammenligningsGrunnlag> sammenligningsgrunnlagMap = mapSammenligningsgrunnlagPrStatus(beregningsgrunnlag);
        SammenligningsGrunnlag sammenligningsgrunnlag = beregningsgrunnlag.getSammenligningsgrunnlag() != null ?
                mapSammenligningsGrunnlag(beregningsgrunnlag.getSammenligningsgrunnlag()) : null;

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
                .medGrunnbeløp(beregningsgrunnlag.getGrunnbeløp().getVerdi())
                .medMilitærIOpptjeningsperioden(erMilitærIOpptjeningsperioden)
                .medYtelsesdagerIEtÅr(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getYtelsesdagerIÅr())
                .medAvviksgrenseProsent(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getAvviksgrenseProsent())
                .medYtelsesSpesifiktGrunnlag(mapYtelsesSpesifiktGrunnlag(input, beregningsgrunnlag))
                // Verdier som kun brukes av FORESLÅ (skille ut i egen mapping?)
                .medAntallGMilitærHarKravPå(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getAntallGMilitærHarKravPå().intValue())
                .medAntallGMinstekravVilkår(getAntallGForOppfyltVilkår(input))
                .medAntallGØvreGrenseverdi(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getAntallGØvreGrenseverdi())
                .medUregulertGrunnbeløp(mapUregulertGrunnbeløp(input, beregningsgrunnlag))
                .medMidlertidigInaktivType(mapMidlertidigInaktivType(input))
                .medGrunnbeløpSatser(grunnbeløpSatser(input))
                .build();
    }

    private List<Grunnbeløp> grunnbeløpSatser(BeregningsgrunnlagInput input) {
        if (input instanceof ForeslåBeregningsgrunnlagInput) {
            return ((ForeslåBeregningsgrunnlagInput) input).getGrunnbeløpsatser();
        } else if (input instanceof FortsettForeslåBeregningsgrunnlagInput) {
            return ((FortsettForeslåBeregningsgrunnlagInput) input).getGrunnbeløpsatser();
        }
        return Collections.emptyList();
    }

    private BigDecimal getAntallGForOppfyltVilkår(BeregningsgrunnlagInput input) {
        if (input.getBeregningsgrunnlag() != null && erMidlertidigInaktiv(input)) {
            return KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getAntallGForOppfyltVilkårInaktiv();
        }
        return KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getAntallGForOppfyltVilkår();
    }

    private MidlertidigInaktivType mapMidlertidigInaktivType(BeregningsgrunnlagInput input) {
        if (input.getOpptjeningAktiviteter() == null) {
            return null;
        }
        var midlertidigInaktivType = input.getOpptjeningAktiviteter().getMidlertidigInaktivType();
        return midlertidigInaktivType != null ?
                MidlertidigInaktivType.valueOf(midlertidigInaktivType.name()) :
                null;
    }

    private boolean erMidlertidigInaktiv(BeregningsgrunnlagInput input) {
        return input.getBeregningsgrunnlag().getAktivitetStatuser().stream().anyMatch(a -> a.getAktivitetStatus().equals(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.MIDLERTIDIG_INAKTIV));
    }

    private BigDecimal mapUregulertGrunnbeløp(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        if ((input instanceof VurderBeregningsgrunnlagvilkårInput)) {
            return ((VurderBeregningsgrunnlagvilkårInput) input).getUregulertGrunnbeløp().map(Beløp::getVerdi).orElse(beregningsgrunnlag.getGrunnbeløp().getVerdi());
        }
        if ((input instanceof VurderRefusjonBeregningsgrunnlagInput)) {
            return ((VurderRefusjonBeregningsgrunnlagInput) input).getUregulertGrunnbeløp().map(Beløp::getVerdi).orElse(beregningsgrunnlag.getGrunnbeløp().getVerdi());
        }
        if ((input instanceof FordelBeregningsgrunnlagInput)) {
            return ((FordelBeregningsgrunnlagInput) input).getUregulertGrunnbeløp().map(Beløp::getVerdi).orElse(beregningsgrunnlag.getGrunnbeløp().getVerdi());
        }
        if ((input instanceof FullføreBeregningsgrunnlagInput)) {
            return ((FullføreBeregningsgrunnlagInput) input).getUregulertGrunnbeløp().map(Beløp::getVerdi).orElse(beregningsgrunnlag.getGrunnbeløp().getVerdi());
        }
        return null;
    }

    private YtelsesSpesifiktGrunnlag mapYtelsesSpesifiktGrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        return FagsakYtelseTypeRef.Lookup.find(ytelsesSpesifikkMapper, input.getFagsakYtelseType())
                .map(mapper -> mapper.map(beregningsgrunnlag, input)).orElse(null);
    }

    private List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak> mapPeriodeÅrsak(List<BeregningsgrunnlagPeriodeÅrsakDto> beregningsgrunnlagPeriodeÅrsaker) {
        return beregningsgrunnlagPeriodeÅrsaker.stream().map(BeregningsgrunnlagPeriodeÅrsakDto::getPeriodeÅrsak).map(MapPeriodeÅrsakFraVlTilRegel::map).collect(Collectors.toList());
    }

    private Dekningsgrad finnDekningsgrad(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, LocalDate periodeFom, BeregningsgrunnlagDto vlBeregningsgrunnlag, OpptjeningAktiviteterDto dto) {
        if (ytelsespesifiktGrunnlag instanceof FrisinnGrunnlag && periodeFom != null) {
            FrisinnGrunnlag frisinngrunnlag = (FrisinnGrunnlag) ytelsespesifiktGrunnlag;
            return Dekningsgrad.fra(frisinngrunnlag.getDekningsgradForDato(periodeFom));
        }

        return Dekningsgrad.fra(ytelsespesifiktGrunnlag.getDekningsgrad());
    }

    private AktivitetStatusMedHjemmel mapVLAktivitetStatusMedHjemmel(final BeregningsgrunnlagAktivitetStatusDto vlBGAktivitetStatus) {
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

    private AktivitetStatus mapVLAktivitetStatus(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus vlBGAktivitetStatus) {
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

    private SammenligningsGrunnlag mapSammenligningsGrunnlag(SammenligningsgrunnlagDto sammenligningsgrunnlag) {
        return SammenligningsGrunnlag.builder()
                .medSammenligningsperiode(new Periode(
                        sammenligningsgrunnlag.getSammenligningsperiodeFom(),
                        sammenligningsgrunnlag.getSammenligningsperiodeTom()))
                .medRapportertPrÅr(sammenligningsgrunnlag.getRapportertPrÅr())
                .medAvvikProsentFraPromilleNy(sammenligningsgrunnlag.getAvvikPromilleNy())
                .build();
    }

    private List<BeregningsgrunnlagPeriode> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                                          BeregningsgrunnlagInput input) {
        List<BeregningsgrunnlagPeriode> perioder = new ArrayList<>();
        vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(vlBGPeriode -> {
            YtelsespesifiktGrunnlag ytelsesgrunnlag = input.getYtelsespesifiktGrunnlag();

            Dekningsgrad dekningsgradPeriode = ytelsesgrunnlag == null ? null : finnDekningsgrad(ytelsesgrunnlag, vlBGPeriode.getBeregningsgrunnlagPeriodeFom(), vlBeregningsgrunnlag, input.getOpptjeningAktiviteter());
            final BeregningsgrunnlagPeriode.Builder regelBGPeriode = BeregningsgrunnlagPeriode.builder()
                    .medPeriode(Periode.of(vlBGPeriode.getBeregningsgrunnlagPeriodeFom(), vlBGPeriode.getBeregningsgrunnlagPeriodeTom()))
                    .medDekningsgrad(dekningsgradPeriode)
                    .leggTilPeriodeÅrsaker(mapPeriodeÅrsak(vlBGPeriode.getBeregningsgrunnlagPeriodeÅrsaker()));

            List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = mapVLBGPrStatus(vlBGPeriode, vlBeregningsgrunnlag.getFaktaOmBeregningTilfeller(), input);
            beregningsgrunnlagPrStatus.forEach(regelBGPeriode::medBeregningsgrunnlagPrStatus);
            perioder.add(regelBGPeriode.build());
        });

        return perioder;
    }

    private List<BeregningsgrunnlagPrStatus> mapVLBGPrStatus(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                             List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
                                                             BeregningsgrunnlagInput input) {
        List<BeregningsgrunnlagPrStatus> liste = new ArrayList<>();
        BeregningsgrunnlagPrStatus bgpsATFL = null;

        for (BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
            if (AktivitetStatus.ATFL.equals(regelAktivitetStatus) || AktivitetStatus.AT.equals(regelAktivitetStatus)) {
                if (bgpsATFL == null) {  // Alle ATFL håndteres samtidig her
                    bgpsATFL = mapVLBGPStatusForATFL(vlBGPeriode, regelAktivitetStatus, faktaOmBeregningTilfeller, input);
                    liste.add(bgpsATFL);
                }
            } else {
                BeregningsgrunnlagPrStatus bgps = mapVLBGPStatusForAlleAktivietetStatuser(vlBGPStatus, input);
                liste.add(bgps);
            }
        }
        return liste;
    }

    private EnumMap<AktivitetStatus, SammenligningsGrunnlag> mapSammenligningsgrunnlagPrStatus(BeregningsgrunnlagDto Beregningsgrunnlag) {
        EnumMap<AktivitetStatus, SammenligningsGrunnlag> sammenligningsGrunnlagMap = new EnumMap<>(AktivitetStatus.class);
        for (SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagPrStatus : Beregningsgrunnlag.getSammenligningsgrunnlagPrStatusListe()) {
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
    private BeregningsgrunnlagPrStatus mapVLBGPStatusForAlleAktivietetStatuser(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus,
                                                                               BeregningsgrunnlagInput input) {
        final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
        List<BigDecimal> pgi = (vlBGPStatus.getPgiSnitt() == null ? new ArrayList<>() :
                Arrays.asList(vlBGPStatus.getPgi1(), vlBGPStatus.getPgi2(), vlBGPStatus.getPgi3()));
        Optional<FaktaAktørDto> faktaAktørDto = input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat().flatMap(FaktaAggregatDto::getFaktaAktør);
        return BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(regelAktivitetStatus)
                .medBeregningsperiode(beregningsperiodeFor(vlBGPStatus))
                .medBeregnetPrÅr(vlBGPStatus.getBeregnetPrÅr())
                .medOverstyrtPrÅr(vlBGPStatus.getOverstyrtPrÅr())
                .medFordeltPrÅr(vlBGPStatus.getManueltFordeltPrÅr() != null ? vlBGPStatus.getManueltFordeltPrÅr() : vlBGPStatus.getFordeltPrÅr()) // Midlertidig løsning til vi lager egen mapping for fastsett vl til regel
                .medBesteberegningPrÅr(vlBGPStatus.getBesteberegningPrÅr())
                .medGjennomsnittligPGI(vlBGPStatus.getPgiSnitt())
                .medPGI(pgi)
                .medÅrsbeløpFraTilstøtendeYtelse(vlBGPStatus.getÅrsbeløpFraTilstøtendeYtelseVerdi())
                .medErNyIArbeidslivet(faktaAktørDto.map(FaktaAktørDto::getErNyIArbeidslivetSNVurdering).orElse(null))
                .medAndelNr(vlBGPStatus.getAndelsnr())
                .medInntektskategori(MapInntektskategoriFraVLTilRegel.map(vlBGPStatus.getGjeldendeInntektskategori()))
                .medFastsattAvSaksbehandler(vlBGPStatus.getFastsattAvSaksbehandler())
                .medLagtTilAvSaksbehandler(vlBGPStatus.erLagtTilAvSaksbehandler())
                .medBesteberegningPrÅr(vlBGPStatus.getBesteberegningPrÅr())
                .medOrginalDagsatsFraTilstøtendeYtelse(vlBGPStatus.getOrginalDagsatsFraTilstøtendeYtelse())
                .medUtbetalingsprosentSVP(finnUtbetalingsgradForAndel(vlBGPStatus, vlBGPStatus.getBeregningsgrunnlagPeriode().getPeriode(), input.getYtelsespesifiktGrunnlag(), false))
                .build();
    }


    private Periode beregningsperiodeFor(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        if (vlBGPStatus.getBeregningsperiodeFom() == null && vlBGPStatus.getBeregningsperiodeTom() == null) {
            return null;
        }
        return Periode.of(vlBGPStatus.getBeregningsperiodeFom(), vlBGPStatus.getBeregningsperiodeTom());
    }

    // Felles mapping av alle statuser som mapper til ATFL
    private BeregningsgrunnlagPrStatus mapVLBGPStatusForATFL(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                             AktivitetStatus regelAktivitetStatus,
                                                             List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
                                                             BeregningsgrunnlagInput input) {

        BeregningsgrunnlagPrStatus.Builder regelBGPStatusATFL = BeregningsgrunnlagPrStatus.builder().medAktivitetStatus(regelAktivitetStatus)
                .medFlOgAtISammeOrganisasjon(faktaOmBeregningTilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON));

        for (BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            if (regelAktivitetStatus.equals(mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus()))) {
                BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold = byggAndel(vlBGPStatus, vlBGPeriode.getPeriode(), input);
                regelBGPStatusATFL.medArbeidsforhold(regelArbeidsforhold);
            }
        }
        return regelBGPStatusATFL.build();
    }

    private BeregningsgrunnlagPrArbeidsforhold byggAndel(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus,
                                                         Intervall periode, BeregningsgrunnlagInput input) {
        BeregningsgrunnlagPrArbeidsforhold.Builder builder = BeregningsgrunnlagPrArbeidsforhold.builder();
        builder
                .medInntektskategori(MapInntektskategoriFraVLTilRegel.map(vlBGPStatus.getGjeldendeInntektskategori()))
                .medBeregnetPrÅr(vlBGPStatus.getBeregnetPrÅr())
                .medBeregningsperiode(beregningsperiodeFor(vlBGPStatus))
                .medFastsattAvSaksbehandler(vlBGPStatus.getFastsattAvSaksbehandler())
                .medLagtTilAvSaksbehandler(vlBGPStatus.erLagtTilAvSaksbehandler())
                .medAndelNr(vlBGPStatus.getAndelsnr())
                .medOverstyrtPrÅr(vlBGPStatus.getOverstyrtPrÅr())
                .medFordeltPrÅr(vlBGPStatus.getManueltFordeltPrÅr() != null ? vlBGPStatus.getManueltFordeltPrÅr() : vlBGPStatus.getFordeltPrÅr()) // Midlertidig løsning til vi lager egen mapping for fastsettsteget vl til regel
                .medBesteberegningPrÅr(vlBGPStatus.getBesteberegningPrÅr())
                .medArbeidsforhold(MapArbeidsforholdFraVLTilRegel.arbeidsforholdFor(vlBGPStatus))
                .medUtbetalingsprosentSVP(finnUtbetalingsgradForAndel(vlBGPStatus, vlBGPStatus.getBeregningsgrunnlagPeriode().getPeriode(), input.getYtelsespesifiktGrunnlag(), false));
        Optional<Boolean> erTidsbegrenset = input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat().flatMap(fa -> fa.getFaktaArbeidsforhold(vlBGPStatus))
                .map(FaktaArbeidsforholdDto::getErTidsbegrensetVurdering);
        vlBGPStatus.getBgAndelArbeidsforhold().ifPresent(bga ->
                builder
                        .medNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().orElse(null))
                        .medNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().orElse(null))
                        .medErTidsbegrensetArbeidsforhold(erTidsbegrenset.orElse(null))
                        .medGjeldendeRefusjonPrÅr(bga.getGjeldendeRefusjonPrÅr()));

        return builder.build();
    }

    private boolean harHattMilitærIOpptjeningsperioden(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        Objects.requireNonNull(beregningAktivitetAggregat, "beregningAktivitetAggregat");
        return beregningAktivitetAggregat.getBeregningAktiviteter().stream()
                .map(BeregningAktivitetDto::getOpptjeningAktivitetType)
                .anyMatch(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE::equals);
    }

}
