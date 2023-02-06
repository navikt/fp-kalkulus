package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.TilkommetInntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.util.BeregningsgrunnlagUtil;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@ApplicationScoped
public class MapBeregningsgrunnlagFraVLTilRegel {
    private Instance<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper;

    public MapBeregningsgrunnlagFraVLTilRegel() {
        // CDI
    }

    @Inject
    public MapBeregningsgrunnlagFraVLTilRegel(@Any Instance<YtelsesspesifikkRegelMapper> ytelsesSpesifikkMapper) {
        this.ytelsesSpesifikkMapper = ytelsesSpesifikkMapper;
    }

    public no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag map(BeregningsgrunnlagInput input,
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

        List<BeregningsgrunnlagPeriode> perioder = mapBeregningsgrunnlagPerioder(beregningsgrunnlag, input);

        var builder = no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag.builder();

        return builder
                .medAktivitetStatuser(aktivitetStatuser)
                .medBeregningsgrunnlagPerioder(perioder)
                .medGrunnbeløp(beregningsgrunnlag.getGrunnbeløp().getVerdi())
                .medYtelsesdagerIEtÅr(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getYtelsesdagerIÅr())
                .medYtelsesSpesifiktGrunnlag(mapYtelsesSpesifiktGrunnlag(input, beregningsgrunnlag))
                .medAntallGØvreGrenseverdi(KonfigTjeneste.forYtelse(input.getFagsakYtelseType()).getAntallGØvreGrenseverdi())
                .medMidlertidigInaktivType(mapMidlertidigInaktivType(input))
                .leggTilToggle("GRADERING_MOT_INNTEKT", KonfigurasjonVerdi.get("GRADERING_MOT_INNTEKT", false))
                .build();
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

    private YtelsesSpesifiktGrunnlag mapYtelsesSpesifiktGrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        return FagsakYtelseTypeRef.Lookup.find(ytelsesSpesifikkMapper, input.getFagsakYtelseType())
                .map(mapper -> mapper.map(beregningsgrunnlag, input)).orElse(null);
    }

    private Dekningsgrad finnDekningsgrad(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, LocalDate periodeFom) {
        if (ytelsespesifiktGrunnlag instanceof FrisinnGrunnlag frisinngrunnlag && periodeFom != null) {
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

    private List<BeregningsgrunnlagPeriode> mapBeregningsgrunnlagPerioder(BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                                          BeregningsgrunnlagInput input) {
        var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(input.getForlengelseperioder(), vlBeregningsgrunnlag);
        List<BeregningsgrunnlagPeriode> perioder = new ArrayList<>();
        YtelsespesifiktGrunnlag ytelsesgrunnlag = input.getYtelsespesifiktGrunnlag();
        vlBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> perioderTilVurderingTjeneste.erTilVurdering(p.getPeriode()))
                .forEach(vlBGPeriode -> {
                    Dekningsgrad dekningsgradPeriode = ytelsesgrunnlag == null ? null : finnDekningsgrad(ytelsesgrunnlag, vlBGPeriode.getBeregningsgrunnlagPeriodeFom());
                    final BeregningsgrunnlagPeriode.Builder regelBGPeriode = BeregningsgrunnlagPeriode.builder()
                            .medPeriode(Periode.of(vlBGPeriode.getBeregningsgrunnlagPeriodeFom(), vlBGPeriode.getBeregningsgrunnlagPeriodeTom()))
                            .medDekningsgrad(dekningsgradPeriode)
                            .leggTilTilkommetInntektsforhold(mapTilkomneInntekter(vlBGPeriode));

                    List<BeregningsgrunnlagPrStatus> beregningsgrunnlagPrStatus = mapVLBGPrStatus(vlBGPeriode, input);
                    beregningsgrunnlagPrStatus.forEach(regelBGPeriode::medBeregningsgrunnlagPrStatus);
                    perioder.add(regelBGPeriode.build());
                });

        return perioder;
    }

    private List<TilkommetInntekt> mapTilkomneInntekter(BeregningsgrunnlagPeriodeDto vlBGPeriode) {
        return vlBGPeriode.getTilkomneInntekter().stream().filter(TilkommetInntektDto::skalRedusereUtbetaling).map(this::mapTilkommetInntekt).toList();
    }

    private TilkommetInntekt mapTilkommetInntekt(TilkommetInntektDto ti) {
        return new TilkommetInntekt(AktivitetStatus.valueOf(ti.getAktivitetStatus().getKode()),
                MapArbeidsforholdFraVLTilRegel.arbeidsforholdFor(ti.getAktivitetStatus(), ti.getArbeidsgiver(), OpptjeningAktivitetType.UDEFINERT, ti.getArbeidsforholdRef().getReferanse()),
                ti.getTilkommetInntektPrÅr());
    }

    private List<BeregningsgrunnlagPrStatus> mapVLBGPrStatus(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                             BeregningsgrunnlagInput input) {
        List<BeregningsgrunnlagPrStatus> liste = new ArrayList<>();
        BeregningsgrunnlagPrStatus bgpsATFL = null;
        for (BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
            if (AktivitetStatus.ATFL.equals(regelAktivitetStatus) || AktivitetStatus.AT.equals(regelAktivitetStatus)) {
                if (bgpsATFL == null) {  // Alle ATFL håndteres samtidig her
                    bgpsATFL = mapVLBGPStatusForATFL(vlBGPeriode, regelAktivitetStatus, input);
                    liste.add(bgpsATFL);
                }
            } else {
                BeregningsgrunnlagPrStatus bgps = mapVLBGPStatusForAlleAktivietetStatuser(vlBGPStatus, input);
                liste.add(bgps);
            }
        }

        return liste;
    }

    // Ikke ATFL, de har separat mapping
    private BeregningsgrunnlagPrStatus mapVLBGPStatusForAlleAktivietetStatuser(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus,
                                                                               BeregningsgrunnlagInput input) {
        final AktivitetStatus regelAktivitetStatus = mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus());
        return BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(regelAktivitetStatus)
                .medBruttoPrÅr(vlBGPStatus.getBruttoPrÅr())
                .medInntektsgrunnlagPrÅr(vlBGPStatus.getGrunnlagPrÅr().getBruttoUtenFordelt() != null ? vlBGPStatus.getGrunnlagPrÅr().getBruttoUtenFordelt() : BigDecimal.ZERO)
                .medAndelNr(vlBGPStatus.getAndelsnr())
                .medUtbetalingsprosent(finnUtbetalingsgradForAndel(vlBGPStatus, vlBGPStatus.getBeregningsgrunnlagPeriode().getPeriode(), input.getYtelsespesifiktGrunnlag(), false))
                .build();
    }

    // Felles mapping av alle statuser som mapper til ATFL
    private BeregningsgrunnlagPrStatus mapVLBGPStatusForATFL(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                             AktivitetStatus regelAktivitetStatus,
                                                             BeregningsgrunnlagInput input) {

        BeregningsgrunnlagPrStatus.Builder regelBGPStatusATFL = BeregningsgrunnlagPrStatus.builder().medAktivitetStatus(regelAktivitetStatus);

        for (BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus : vlBGPeriode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            if (regelAktivitetStatus.equals(mapVLAktivitetStatus(vlBGPStatus.getAktivitetStatus()))) {
                BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold = byggAndel(vlBGPStatus, input);
                regelBGPStatusATFL.medArbeidsforhold(regelArbeidsforhold);
            }
        }
        return regelBGPStatusATFL.build();
    }

    private BeregningsgrunnlagPrArbeidsforhold byggAndel(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus,
                                                         BeregningsgrunnlagInput input) {
        BeregningsgrunnlagPrArbeidsforhold.Builder builder = BeregningsgrunnlagPrArbeidsforhold.builder();
        builder
                .medBruttoPrÅr(vlBGPStatus.getBruttoPrÅr())
                .medInntektsgrunnlagPrÅr(vlBGPStatus.getGrunnlagPrÅr().getBruttoUtenFordelt() != null ? vlBGPStatus.getGrunnlagPrÅr().getBruttoUtenFordelt() : BigDecimal.ZERO)
                .medAndelNr(vlBGPStatus.getAndelsnr())
                .medArbeidsforhold(MapArbeidsforholdFraVLTilRegel.arbeidsforholdFor(vlBGPStatus))
                .medUtbetalingsprosent(finnUtbetalingsgradForAndel(vlBGPStatus, vlBGPStatus.getBeregningsgrunnlagPeriode().getPeriode(), input.getYtelsespesifiktGrunnlag(), false));
        vlBGPStatus.getBgAndelArbeidsforhold().ifPresent(bga ->
                builder
                        .medNaturalytelseBortfaltPrÅr(bga.getNaturalytelseBortfaltPrÅr().orElse(null))
                        .medNaturalytelseTilkommetPrÅr(bga.getNaturalytelseTilkommetPrÅr().orElse(null))
                        .medRefusjonPrÅr(bga.getGjeldendeRefusjonPrÅr()));

        return builder.build();
    }

}
