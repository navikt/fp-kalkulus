package no.nav.folketrygdloven.kalkulus.forvaltning;

import static no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper.mapGrunnlag;
import static no.nav.folketrygdloven.kalkulus.mappers.MapFraKalkulator.mapFraKalkulatorInputTilBeregningsgrunnlagInput;
import static no.nav.folketrygdloven.kalkulus.mappers.MapTilGUIInputFraKalkulator.mapFraKalkulatorInput;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag.YtelsespesifiktGrunnlagTjenesteOMP;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt.AvklaringsbehovUtlederFastsettBeregningsaktiviteter;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelBeregningsgrunnlagTilfelleInput;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelBeregningsgrunnlagTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.Resultat;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovKontrollTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAggregatEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaAktørEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.FaktaArbeidsforholdEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.mapFraEntitet.BehandlingslagerTilKalkulusMapper;
import no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator;
import no.nav.folketrygdloven.kalkulus.request.v1.migrerAksjonspunkt.MigrerAksjonspunktRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.OmsorgspengeGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@ApplicationScoped
public class AksjonspunktMigreringTjeneste {

    private static final Logger LOGGER = LoggerFactory.getLogger(AksjonspunktMigreringTjeneste.class);

    private KoblingTjeneste koblingTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private AvklaringsbehovKontrollTjeneste avklaringsbehovKontrollTjeneste;
    private Instance<AvklaringsbehovUtlederFastsettBeregningsaktiviteter> apUtlederFastsettAktiviteter;
    private YtelsespesifiktGrunnlagTjenesteOMP ytelsespesifiktGrunnlagTjenesteOMP;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private AvklaringsbehovRepository avklaringsbehovRepository;

    public AksjonspunktMigreringTjeneste() {
        // CDI
    }

    @Inject
    public AksjonspunktMigreringTjeneste(KoblingTjeneste koblingTjeneste,
                                         @Any Instance<AvklaringsbehovUtlederFastsettBeregningsaktiviteter> apUtlederFastsettAktiviteter,
                                         KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                         BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                         AvklaringsbehovKontrollTjeneste avklaringsbehovKontrollTjeneste,
                                         AvklaringsbehovTjeneste avklaringsbehovTjeneste,
                                         AvklaringsbehovRepository avklaringsbehovRepository) {
        this.koblingTjeneste = koblingTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.avklaringsbehovKontrollTjeneste = avklaringsbehovKontrollTjeneste;
        this.apUtlederFastsettAktiviteter = apUtlederFastsettAktiviteter;
        this.avklaringsbehovRepository = avklaringsbehovRepository;
        this.ytelsespesifiktGrunnlagTjenesteOMP = new YtelsespesifiktGrunnlagTjenesteOMP();
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }

    public void fordelBeregningsgrunnlagMigrering(List<MigrerAksjonspunktRequest> aksjonspunktdata) {
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.FORDEL_BEREGNINGSGRUNNLAG;
        for (MigrerAksjonspunktRequest data : aksjonspunktdata) {
            List<KoblingEntitet> koblinger = finnKoblingerUtenAksjonspunkt(data, avklaringsbehovDefinisjon);
            List<KoblingEntitet> koblingerMedFordelTilfelle = finnKoblingerMedFordelAksjonspunkt(koblinger);
            lagAvklaringsbehovForKoblinger(data, koblingerMedFordelTilfelle, avklaringsbehovDefinisjon);
        }
    }

    public void vurderVarigEndringMigrering(List<MigrerAksjonspunktRequest> aksjonspunktdata) {
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE;
        for (MigrerAksjonspunktRequest data : aksjonspunktdata) {
            List<KoblingEntitet> koblinger = finnKoblingerUtenAksjonspunkt(data, avklaringsbehovDefinisjon);
            List<KoblingEntitet> koblingerMedAvvik = finnKoblingerMedAvvik(koblinger);
            lagAvklaringsbehovForKoblinger(data, koblingerMedAvvik, avklaringsbehovDefinisjon);
        }
    }

    public void fastsettForTidsbegrensetMigrering(List<MigrerAksjonspunktRequest> aksjonspunktdata) {
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD;
        for (MigrerAksjonspunktRequest data : aksjonspunktdata) {
            List<KoblingEntitet> koblinger = finnKoblingerUtenAksjonspunkt(data, avklaringsbehovDefinisjon);
            List<KoblingEntitet> koblingerNyIArbeidslivet = finnKoblingerMedTidsbegrensetAksjonspunkt(koblinger);
            lagAvklaringsbehovForKoblinger(data, koblingerNyIArbeidslivet, avklaringsbehovDefinisjon);
        }
    }

    public void nyIArbeidslivetSNMigrering(List<MigrerAksjonspunktRequest> aksjonspunktdata) {
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET;
        for (MigrerAksjonspunktRequest data : aksjonspunktdata) {
            List<KoblingEntitet> koblinger = finnKoblingerUtenAksjonspunkt(data, avklaringsbehovDefinisjon);
            List<KoblingEntitet> koblingerNyIArbeidslivet = finnKoblingerMedNyIArbeidslivetAksjonspunkt(koblinger);
            lagAvklaringsbehovForKoblinger(data, koblingerNyIArbeidslivet, avklaringsbehovDefinisjon);
        }
    }

    public void vurderFastsettVedAvvikATFLMigrering(List<MigrerAksjonspunktRequest> aksjonspunktdata) {
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS;
        for (MigrerAksjonspunktRequest data : aksjonspunktdata) {
            List<KoblingEntitet> koblinger = finnKoblingerUtenAksjonspunkt(data, avklaringsbehovDefinisjon);
            List<KoblingEntitet> koblingerMedAvvik = finnKoblingerMedAvviksvurdering(data, koblinger);
            lagAvklaringsbehovForKoblinger(data, koblingerMedAvvik, avklaringsbehovDefinisjon);
        }
    }

    public void vurderFaktaBeregningMigrering(List<MigrerAksjonspunktRequest> aksjonspunktdata) {
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.VURDER_FAKTA_FOR_ATFL_SN;
        for (MigrerAksjonspunktRequest data : aksjonspunktdata) {
            List<KoblingEntitet> koblinger = finnKoblingerUtenAksjonspunkt(data, avklaringsbehovDefinisjon);
            List<KoblingEntitet> koblingerMedTilfeller = finnKoblingerMedFaktaBeregningAksjonspunkt(koblinger);
            lagAvklaringsbehovForKoblinger(data, koblingerMedTilfeller, avklaringsbehovDefinisjon);
        }
    }

    public void avklarAktiviteterMigrering(List<MigrerAksjonspunktRequest> aksjonspunktdata) {
        AvklaringsbehovDefinisjon avklaringsbehovDefinisjon = AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER;
        for (MigrerAksjonspunktRequest data : aksjonspunktdata) {
            List<KoblingEntitet> koblinger = finnKoblingerUtenAksjonspunkt(data, avklaringsbehovDefinisjon);
            List<KoblingEntitet> koblingerMedAvklaringsbehov = finnKoblingerMedAvklarAktiveterAksjonspunkt(data, koblinger);
            lagAvklaringsbehovForKoblinger(data, koblingerMedAvklaringsbehov, avklaringsbehovDefinisjon);
        }
    }

    private List<KoblingEntitet> finnKoblingerUtenAksjonspunkt(MigrerAksjonspunktRequest data, AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        List<KoblingReferanse> koblingreferanser = getKoblingReferanser(data);
        List<KoblingEntitet> koblinger = koblingTjeneste.hentKoblinger(koblingreferanser);
        Map<Long, List<AvklaringsbehovEntitet>> avklaringsbehovPrKobling = avklaringsbehovTjeneste.hentAlleAvklaringsbehovForKoblinger(koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toList()))
                .stream().collect(Collectors.groupingBy(AvklaringsbehovEntitet::getKoblingId));
        List<KoblingEntitet> koblingerUtenAksjonspunkt = koblinger.stream()
                .filter(k -> avklaringsbehovPrKobling.getOrDefault(k.getId(), Collections.emptyList())
                        .stream()
                        .noneMatch(ab -> ab.getDefinisjon().equals(avklaringsbehovDefinisjon)))
                .collect(Collectors.toList());
        logKoblingerMedAksjonspunkt(avklaringsbehovDefinisjon, koblinger, avklaringsbehovPrKobling, koblingerUtenAksjonspunkt);
        return koblingerUtenAksjonspunkt;
    }

    private void logKoblingerMedAksjonspunkt(AvklaringsbehovDefinisjon avklaringsbehovDefinisjon, List<KoblingEntitet> koblinger, Map<Long, List<AvklaringsbehovEntitet>> avklaringsbehovPrKobling, List<KoblingEntitet> koblingerUtenAksjonspunkt) {
        if (koblingerUtenAksjonspunkt.size() != koblinger.size()) {
            List<KoblingEntitet> koblingerMedAksjonspunkt = koblinger.stream()
                    .filter(k -> avklaringsbehovPrKobling.getOrDefault(k.getId(), Collections.emptyList())
                            .stream()
                            .anyMatch(ab -> ab.getDefinisjon().equals(avklaringsbehovDefinisjon)))
                    .collect(Collectors.toList());
            List<UUID> referanser = koblingerMedAksjonspunkt.stream()
                    .map(KoblingEntitet::getKoblingReferanse).map(KoblingReferanse::getReferanse)
                    .collect(Collectors.toList());
            LOGGER.info("Følgende koblinger hadde allerede aksjonspunkt " + avklaringsbehovDefinisjon.getKode() + ": " + referanser);
        }
    }

    private List<KoblingEntitet> finnKoblingerMedFordelAksjonspunkt(List<KoblingEntitet> koblinger) {
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKoblinger(koblingIder, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        var inputRespons = kalkulatorInputTjeneste.hentForKoblinger(koblingIder);

        Map<Long, InntektsmeldingAggregatDto> inntektsmeldingerPrKobling = inputRespons.getResultatPrKobling().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> MapIAYTilKalulator.mapInntektsmelding(e.getValue().getIayGrunnlag().getInntektsmeldingDto())
                ));

        var bgPrKobling = beregningsgrunnlagGrunnlagEntiteter.stream()
                .filter(gr -> gr.getBeregningsgrunnlag().isPresent())
                .collect(
                        Collectors.toMap(BeregningsgrunnlagGrunnlagEntitet::getKoblingId,
                                gr -> gr.getBeregningsgrunnlag()
                                        .map(BehandlingslagerTilKalkulusMapper::mapBeregningsgrunnlag).orElseThrow()));


        var fordelingInputPrKobling = bgPrKobling.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new FordelBeregningsgrunnlagTilfelleInput(e.getValue(),
                                AktivitetGradering.INGEN_GRADERING,
                                inntektsmeldingerPrKobling.get(e.getKey()).getAlleInntektsmeldinger()))
                );

        var koblingerMedFordelTilfelle = fordelingInputPrKobling.entrySet().stream()
                .filter(e -> FordelBeregningsgrunnlagTilfelleTjeneste.harTilfelleForFordeling(e.getValue()))
                .map(Map.Entry::getKey)
                .map(id -> finnKobling(koblinger, id))
                .collect(Collectors.toList());
        return koblingerMedFordelTilfelle;
    }

    private List<KoblingEntitet> finnKoblingerMedTidsbegrensetAksjonspunkt(List<KoblingEntitet> koblinger) {
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        List<KoblingEntitet> koblingerTidsbegrenset = beregningsgrunnlagGrunnlagEntiteter.stream()
                .filter(this::erTidsbegrenset)
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .map(id -> finnKobling(koblinger, id))
                .collect(Collectors.toList());
        List<KoblingEntitet> koblingerMedAvvik = beregningsgrunnlagGrunnlagEntiteter.stream()
                .filter(gr -> koblingerTidsbegrenset.stream().map(KoblingEntitet::getId).anyMatch(id -> id.equals(gr.getKoblingId())))
                .filter(this::harAvvik)
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .map(id -> finnKobling(koblinger, id))
                .collect(Collectors.toList());
        return koblingerMedAvvik;
    }

    private List<KoblingEntitet> finnKoblingerMedNyIArbeidslivetAksjonspunkt(List<KoblingEntitet> koblinger) {
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        List<KoblingEntitet> koblingerNyIArbeidslivet = beregningsgrunnlagGrunnlagEntiteter.stream()
                .filter(this::erNyIArbeidslivet)
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .map(id -> finnKobling(koblinger, id))
                .collect(Collectors.toList());
        return koblingerNyIArbeidslivet;
    }

    private List<KoblingEntitet> finnKoblingerMedAvviksvurdering(MigrerAksjonspunktRequest data, List<KoblingEntitet> koblinger) {
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        var grunnlagEntitetMap = beregningsgrunnlagGrunnlagEntiteter
                .stream().collect(Collectors.toMap(BeregningsgrunnlagGrunnlagEntitet::getKoblingId, Function.identity()));
        var inputRespons = kalkulatorInputTjeneste.hentForKoblinger(koblingIder);
        Map<Long, BeregningsgrunnlagGUIInput> guiInputMap = koblinger.stream().collect(
                Collectors.toMap(KoblingEntitet::getId,
                        k -> lagInput(grunnlagEntitetMap, inputRespons, k)));
        List<KoblingEntitet> koblingerMedAvvik = beregningsgrunnlagGrunnlagEntiteter.stream()
                .filter(this::harAvvik)
                .filter(gr -> {
                    if (data.getYtelseSomSkalBeregnes().equals(YtelseTyperKalkulusStøtterKontrakt.OMSORGSPENGER)) {
                        return skalAvviksvurdere(guiInputMap, gr);
                    }
                    return true;
                })
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .map(id -> finnKobling(koblinger, id))
                .collect(Collectors.toList());
        return koblingerMedAvvik;
    }

    private BeregningsgrunnlagGUIInput lagInput(Map<Long, BeregningsgrunnlagGrunnlagEntitet> grunnlagEntitetMap, Resultat<KalkulatorInputDto> inputRespons, KoblingEntitet kobling) {
        KalkulatorInputDto kalkulatorInputDto = inputRespons.getResultatPrKobling().get(kobling.getId());
        BeregningsgrunnlagGrunnlagDto mappetGrunnlag = mapGrunnlag(grunnlagEntitetMap.get(kobling.getId()));
        return mapFraKalkulatorInput(kobling,
                kalkulatorInputDto,
                Optional.of(grunnlagEntitetMap.get(kobling.getId()))).medBeregningsgrunnlagGrunnlag(mappetGrunnlag);
    }

    private List<KoblingEntitet> finnKoblingerMedAvvik(List<KoblingEntitet> koblinger) {
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        return beregningsgrunnlagGrunnlagEntiteter.stream()
                .filter(this::harAvvik)
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .map(id -> finnKobling(koblinger, id))
                .collect(Collectors.toList());
    }

    private List<KoblingEntitet> finnKoblingerMedFaktaBeregningAksjonspunkt(List<KoblingEntitet> koblinger) {
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var beregningsgrunnlagGrunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder);
        List<KoblingEntitet> koblingerMedTilfeller = beregningsgrunnlagGrunnlagEntiteter.stream()
                .filter(gr -> !gr.getBeregningsgrunnlag().map(BeregningsgrunnlagEntitet::getFaktaOmBeregningTilfeller).orElse(Collections.emptyList()).isEmpty())
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .map(id -> finnKobling(koblinger, id))
                .collect(Collectors.toList());
        return koblingerMedTilfeller;
    }

    private Boolean erTidsbegrenset(BeregningsgrunnlagGrunnlagEntitet gr) {
        return gr.getFaktaAggregat().stream().flatMap(fa -> fa.getFaktaArbeidsforhold().stream())
                .anyMatch(FaktaArbeidsforholdEntitet::getErTidsbegrenset);
    }

    private List<KoblingEntitet> finnKoblingerMedAvklarAktiveterAksjonspunkt(MigrerAksjonspunktRequest data, List<KoblingEntitet> koblinger) {
        var koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var inputRespons = kalkulatorInputTjeneste.hentForKoblinger(koblingIder);
        var grunnlagEntiteter = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblingIder)
                .stream().collect(Collectors.toMap(BeregningsgrunnlagGrunnlagEntitet::getKoblingId, Function.identity()));
        var koblingStegInputMap = grunnlagEntiteter.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> mapFraKalkulatorInputTilBeregningsgrunnlagInput(finnKobling(koblinger, e.getKey()), inputRespons.getResultatPrKobling().get(e.getKey()), Optional.of(e.getValue())))
                );
        var utleder = finnImplementasjonForYtelseType(FagsakYtelseType.fraKode(data.getYtelseSomSkalBeregnes().getKode()), apUtlederFastsettAktiviteter);

        List<KoblingEntitet> koblingerMedAvklaringsbehov = koblingStegInputMap.keySet().stream()
                .filter(beregningsgrunnlagInput -> harAvklarAktiviteterAksjonspunkt(koblingStegInputMap, utleder, beregningsgrunnlagInput))
                .map(id -> finnKobling(koblinger, id))
                .collect(Collectors.toList());
        return koblingerMedAvklaringsbehov;
    }

    private Boolean erNyIArbeidslivet(BeregningsgrunnlagGrunnlagEntitet gr) {
        return gr.getFaktaAggregat().flatMap(FaktaAggregatEntitet::getFaktaAktør)
                .map(FaktaAktørEntitet::getErNyIArbeidslivetSN).orElse(false);
    }

    private List<KoblingReferanse> getKoblingReferanser(MigrerAksjonspunktRequest data) {
        return data.getEksternReferanseListe().stream()
                .map(KoblingReferanse::new).collect(Collectors.toList());
    }

    private boolean harAvvik(BeregningsgrunnlagGrunnlagEntitet gr) {
        Optional<Sammenligningsgrunnlag> sammenligningsgrunnlag = gr.getBeregningsgrunnlag().flatMap(BeregningsgrunnlagEntitet::getSammenligningsgrunnlag);
        return sammenligningsgrunnlag.filter(sg -> sg.getAvvikPromilleNy().compareTo(BigDecimal.valueOf(250)) > 0).isPresent();
    }

    private boolean skalAvviksvurdere(Map<Long, BeregningsgrunnlagGUIInput> guiInputMap, BeregningsgrunnlagGrunnlagEntitet gr) {
        return ytelsespesifiktGrunnlagTjenesteOMP.map(guiInputMap.get(gr.getKoblingId()))
                .map(ytelsespesifiktGrunnlagDto -> (OmsorgspengeGrunnlagDto) ytelsespesifiktGrunnlagDto)
                .filter(OmsorgspengeGrunnlagDto::getSkalAvviksvurdere)
                .isPresent();
    }

    private boolean harAvklarAktiviteterAksjonspunkt(Map<Long, BeregningsgrunnlagInput> koblingStegInputMap, AvklaringsbehovUtlederFastsettBeregningsaktiviteter utleder, Long beregningsgrunnlagInput) {
        var input = koblingStegInputMap.get(beregningsgrunnlagInput);
        var regelResultat = new BeregningsgrunnlagRegelResultat(input.getBeregningsgrunnlag(), List.of());
        return !utleder.utledAvklaringsbehov(regelResultat, input, false).isEmpty();
    }

    private void lagAvklaringsbehovForKoblinger(MigrerAksjonspunktRequest data, List<KoblingEntitet> koblinger, AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {

        if (koblinger.isEmpty()) {
            throw new IllegalStateException("Ingen koblinger hadde aksjonspunkt " + avklaringsbehovDefinisjon.getKode() + " for saksnummer " + data.getSaksnummer());
        }

        List<UUID> referanser = koblinger.stream().map(KoblingEntitet::getKoblingReferanse).map(KoblingReferanse::getReferanse).collect(Collectors.toList());
        LOGGER.info("Lagrer ned avklaringsbehov " + avklaringsbehovDefinisjon.getKode()
                + " for saksnummer " + data.getSaksnummer()
                + " og koblinger " + referanser);

        var avklaringsbehovEntiteter = koblinger.stream()
                .map(kobling -> avklaringsbehovKontrollTjeneste.opprettForKobling(kobling, avklaringsbehovDefinisjon))
                .collect(Collectors.toList());

        var status = AvklaringsbehovStatus.fraKode(data.getAvklaringsbehovStatus());

        if (status.equals(AvklaringsbehovStatus.UTFØRT)) {
            avklaringsbehovEntiteter.forEach(ab -> avklaringsbehovKontrollTjeneste.løs(ab, data.getBegrunnelse()));
        } else if (status.equals(AvklaringsbehovStatus.AVBRUTT)) {
            avklaringsbehovEntiteter.forEach(ab -> avklaringsbehovKontrollTjeneste.avbryt(ab));
        }
        avklaringsbehovEntiteter.forEach(ab -> avklaringsbehovRepository.lagre(ab));
    }

    private KoblingEntitet finnKobling(List<KoblingEntitet> koblinger, Long id) {
        return koblinger.stream().filter(k -> k.getId().equals(id)).findFirst().orElseThrow(() -> new IllegalStateException("Fant ikkje kobling"));
    }

    private <T> T finnImplementasjonForYtelseType(FagsakYtelseType fagsakYtelseType, Instance<T> instanser) {
        return FagsakYtelseTypeRef.Lookup.find(instanser, fagsakYtelseType)
                .orElseThrow(() -> new IllegalStateException("Finner ikke implementasjon for ytelse " + fagsakYtelseType.getKode()));
    }

}
