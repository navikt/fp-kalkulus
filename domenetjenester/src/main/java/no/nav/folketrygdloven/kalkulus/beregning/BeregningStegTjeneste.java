package no.nav.folketrygdloven.kalkulus.beregning;

import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FASTSATT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FASTSATT_INN;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FORESLÅTT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.FORESLÅTT_UT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.KOFAKBER_UT;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AksjonspunktMedTilstandDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAksjonspunkt;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;

@ApplicationScoped
public class BeregningStegTjeneste {
    private static final String UTVIKLER_FEIL_SKAL_HA_BEREGNINGSGRUNNLAG_HER = "Utvikler-feil: skal ha beregningsgrunnlag her";
    private static final Supplier<IllegalStateException> INGEN_BG_EXCEPTION_SUPPLIER = () -> new IllegalStateException(UTVIKLER_FEIL_SKAL_HA_BEREGNINGSGRUNNLAG_HER);

    private BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste;
    private BeregningsgrunnlagRepository repository;
    private RullTilbakeTjeneste rullTilbakeTjeneste;

    BeregningStegTjeneste() {
        // CDI
    }

    @Inject
    public BeregningStegTjeneste(BeregningsgrunnlagTjeneste beregningsgrunnlagTjeneste, BeregningsgrunnlagRepository repository, RullTilbakeTjeneste rullTilbakeTjeneste) {
        this.beregningsgrunnlagTjeneste = beregningsgrunnlagTjeneste;
        this.repository = repository;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
    }

    /** FastsettBeregningsaktiviteter
     *  Steg 1. FASTSETT_STP_BER
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link TilstandResponse}
     */
    public TilstandResponse fastsettBeregningsaktiviteter(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat resultat = beregningsgrunnlagTjeneste.fastsettBeregningsaktiviteter(input);
        return mapTilstandResponse(lagreOgKopier(input.getBehandlingReferanse(), resultat));
    }

    /** KontrollerFaktaBeregningsgrunnlag
     * Steg 2. KOFAKBER
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse kontrollerFaktaBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.kontrollerFaktaBeregningsgrunnlag(input);
        Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeBekreftetGrunnlag = finnForrigeGrunnlagFraTilstand(input, KOFAKBER_UT);
        BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag = KalkulatorTilEntitetMapper.mapGrunnlag(beregningResultatAggregat.getBeregningsgrunnlagGrunnlag());
        BeregningsgrunnlagEntitet nyttBg = nyttGrunnlag.getBeregningsgrunnlag().orElseThrow(INGEN_BG_EXCEPTION_SUPPLIER);
        lagreOgKopier(input, beregningResultatAggregat, forrigeBekreftetGrunnlag, nyttGrunnlag, nyttBg);
        return mapTilstandResponse(beregningResultatAggregat.getBeregningAksjonspunktResultater());
    }

    /** ForeslåBeregningsgrunnlag
     * Steg 3. FORS_BERGRUNN
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse foreslåBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(input);
        Optional<BeregningsgrunnlagEntitet> forrigeBekreftetBeregningsgrunnlag = finnForrigeBgFraTilstand(input, FORESLÅTT_UT);
        BeregningsgrunnlagEntitet nyttBg = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningResultatAggregat.getBeregningsgrunnlag());
        lagreOgKopier(input, beregningResultatAggregat, forrigeBekreftetBeregningsgrunnlag, nyttBg, FORESLÅTT, FORESLÅTT_UT);
        return mapTilstandResponse(beregningResultatAggregat.getBeregningAksjonspunktResultater());
    }


    /** FordelBeregningsgrunnlag
     * Steg 4. FORDEL_BERGRUNN
     * @param input {@link BeregningsgrunnlagInput}
     * @return {@link BeregningAksjonspunktResultat}
     */
    public TilstandResponse fordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.fordelBeregningsgrunnlag(input);
        Optional<BeregningsgrunnlagEntitet> forrigeBekreftetBeregningsgrunnlag = finnForrigeBgFraTilstand(input, FASTSATT_INN);
        BeregningsgrunnlagEntitet nyttBg = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningResultatAggregat.getBeregningsgrunnlag());
        lagreOgKopier(input, beregningResultatAggregat, forrigeBekreftetBeregningsgrunnlag, nyttBg, OPPDATERT_MED_REFUSJON_OG_GRADERING, FASTSATT_INN);
        //TODO(OJR) hva skal vi gjøre her ESPEN?
        //BeregningsgrunnlagVilkårOgAkjonspunktResultat beregningsgrunnlagVilkårOgAkjonspunktResultat = new BeregningsgrunnlagVilkårOgAkjonspunktResultat(beregningResultatAggregat.getBeregningAksjonspunktResultater());
        //beregningsgrunnlagVilkårOgAkjonspunktResultat.setVilkårOppfylt(getVilkårResultat(beregningResultatAggregat), getRegelEvalueringVilkårvurdering(beregningResultatAggregat), getRegelInputVilkårvurdering(beregningResultatAggregat));
        return mapTilstandResponse(beregningResultatAggregat.getBeregningAksjonspunktResultater());
    }

    /** FastsettBeregningsgrunnlagSteg
     * Steg 5. FAST_BERGRUNN
     * @param input {@link BeregningsgrunnlagInput}
     */
    public TilstandResponse fastsettBeregningsaktiviteters(BeregningsgrunnlagInput input) {
        BeregningResultatAggregat beregningResultatAggregat = beregningsgrunnlagTjeneste.fastsettBeregningsgrunnlag(input);
        Long koblingId = input.getBehandlingReferanse().getKoblingId();
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag = beregningResultatAggregat.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag();

        if (beregningsgrunnlag.isPresent()) {
            BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = KalkulatorTilEntitetMapper.mapBeregningsgrunnlag(beregningsgrunnlag.get());
            repository.lagre(koblingId, beregningsgrunnlagEntitet, FASTSATT);
        }
        return TilstandResponse.TOM_RESPONSE();
    }

    private List<BeregningAksjonspunktResultat> lagreOgKopier(BehandlingReferanse ref, BeregningResultatAggregat resultat) {
        BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag = KalkulatorTilEntitetMapper.mapGrunnlag(resultat.getBeregningsgrunnlagGrunnlag());
        Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeBekreftetGrunnlag = repository.hentSisteBeregningsgrunnlagGrunnlagEntitet(
                ref.getBehandlingId(),
                BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        List<BeregningAksjonspunktResultat> beregningAksjonspunktResultater = resultat.getBeregningAksjonspunktResultater();
        boolean kanKopiereGrunnlag = KopierBeregningsgrunnlag.kanKopiereFraForrigeBekreftetGrunnlag(
                beregningAksjonspunktResultater,
                nyttGrunnlag,
                repository.hentSisteBeregningsgrunnlagGrunnlagEntitet(ref.getBehandlingId(), BeregningsgrunnlagTilstand.OPPRETTET),
                forrigeBekreftetGrunnlag
        );
        repository.lagre(ref.getBehandlingId(), BeregningsgrunnlagGrunnlagBuilder.oppdatere(nyttGrunnlag), BeregningsgrunnlagTilstand.OPPRETTET);
        if (kanKopiereGrunnlag) {
            forrigeBekreftetGrunnlag.ifPresent(gr -> repository.lagre(ref.getBehandlingId(), BeregningsgrunnlagGrunnlagBuilder.oppdatere(gr), BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER));
        }
        return beregningAksjonspunktResultater;
    }

    private void lagreOgKopier(BeregningsgrunnlagInput input,
                               BeregningResultatAggregat beregningResultatAggregat,
                               Optional<BeregningsgrunnlagGrunnlagEntitet> forrigeBekreftetGrunnlag,
                               BeregningsgrunnlagGrunnlagEntitet nyttGrunnlag,
                               BeregningsgrunnlagEntitet nyttBg) {
        BehandlingReferanse ref = input.getBehandlingReferanse();
        Long behandlingId = ref.getBehandlingId();
        boolean kanKopiereFraBekreftet = KopierBeregningsgrunnlag.kanKopiereFraForrigeBekreftetGrunnlag(
                beregningResultatAggregat.getBeregningAksjonspunktResultater(),
                nyttBg,
                finnForrigeBgFraTilstand(input, OPPDATERT_MED_ANDELER),
                forrigeBekreftetGrunnlag.flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag));
        repository.lagre(behandlingId, nyttBg, OPPDATERT_MED_ANDELER);
        if (kanKopiereFraBekreftet) {
            Optional<BeregningsgrunnlagGrunnlagBuilder> bekreftetGrunnlagBuilder = forrigeBekreftetGrunnlag
                    .map(gr -> {
                                BeregningsgrunnlagGrunnlagBuilder b = BeregningsgrunnlagGrunnlagBuilder.oppdatere(nyttGrunnlag)
                                        .medBeregningsgrunnlag(gr.getBeregningsgrunnlag().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag")));
                                gr.getRefusjonOverstyringer().ifPresent(b::medRefusjonOverstyring);
                                return b;
                            }
                    );
            bekreftetGrunnlagBuilder
                    .ifPresent(b -> repository.lagre(behandlingId, b, KOFAKBER_UT));
        }
    }

    private void lagreOgKopier(BeregningsgrunnlagInput input,
                               BeregningResultatAggregat beregningResultatAggregat,
                               Optional<BeregningsgrunnlagEntitet> forrigeBekreftetBeregningsgrunnlag,
                               BeregningsgrunnlagEntitet nyttBg,
                               BeregningsgrunnlagTilstand tilstand, BeregningsgrunnlagTilstand bekreftetTilstand) {
        BehandlingReferanse ref = input.getBehandlingReferanse();
        Long behandlingId = ref.getBehandlingId();
        boolean kanKopiereBekreftet = KopierBeregningsgrunnlag.kanKopiereFraForrigeBekreftetGrunnlag(
                beregningResultatAggregat.getBeregningAksjonspunktResultater(),
                nyttBg,
                finnForrigeBgFraTilstand(input, tilstand),
                forrigeBekreftetBeregningsgrunnlag
        );
        repository.lagre(behandlingId, nyttBg, tilstand);
        if (kanKopiereBekreftet) {
            forrigeBekreftetBeregningsgrunnlag.ifPresent(bg -> repository.lagre(behandlingId, bg, bekreftetTilstand));
        }
    }

    private Optional<BeregningsgrunnlagGrunnlagEntitet> finnForrigeGrunnlagFraTilstand(BeregningsgrunnlagInput input, BeregningsgrunnlagTilstand tilstandFraSteg) {
        BehandlingReferanse referanse = input.getBehandlingReferanse();
        return repository
                .hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(referanse.getBehandlingId(), referanse.getOriginalBehandlingId(), tilstandFraSteg);
    }

    private Optional<BeregningsgrunnlagEntitet> finnForrigeBgFraTilstand(BeregningsgrunnlagInput input, BeregningsgrunnlagTilstand tilstandFraSteg) {
        BehandlingReferanse behandlingReferanse = input.getBehandlingReferanse();
        return repository
                .hentSisteBeregningsgrunnlagGrunnlagEntitetForBehandlinger(behandlingReferanse.getBehandlingId(), behandlingReferanse.getOriginalBehandlingId(), tilstandFraSteg)
                .flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
    }

    private TilstandResponse mapTilstandResponse(List<BeregningAksjonspunktResultat> resultat) {
        return new TilstandResponse(resultat.stream()
                .map(res -> new AksjonspunktMedTilstandDto(
                        new BeregningAksjonspunkt(res.getBeregningAksjonspunktDefinisjon().getKode()),
                        new BeregningVenteårsak(res.getVenteårsak().getKode()),
                        res.getVentefrist())).collect(Collectors.toList()));
    }

    public TilstandResponse beregnFor(StegType stegType, BeregningsgrunnlagInput input, Long koblingId) {
        rullTilbakeTjeneste.rullTilbakeTilTilstandFørVedBehov(koblingId, MapStegTilTilstand.map(stegType));
        if (stegType.equals(StegType.KOFAKBER)) {
            return kontrollerFaktaBeregningsgrunnlag(input);
        } else if (stegType.equals(StegType.FORS_BERGRUNN)) {
            return foreslåBeregningsgrunnlag(input);
        } else if (stegType.equals(StegType.FORDEL_BERGRUNN)) {
            return fordelBeregningsgrunnlag(input);
        } else if (stegType.equals(StegType.FAST_BERGRUNN)) {
            return fastsettBeregningsaktiviteters(input);
        }
        throw new IllegalStateException("Kan ikke beregne for " + stegType.getKode());
    }
}
