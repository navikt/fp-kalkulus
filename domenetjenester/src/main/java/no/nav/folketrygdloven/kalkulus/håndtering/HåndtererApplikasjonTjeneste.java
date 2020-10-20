package no.nav.folketrygdloven.kalkulus.håndtering;

import static no.nav.folketrygdloven.kalkulus.håndtering.HåndteringApplikasjonFeil.FACTORY;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulus.beregning.MapHåndteringskodeTilTilstand;
import no.nav.folketrygdloven.kalkulus.beregning.input.HåndteringInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;

@ApplicationScoped
public class HåndtererApplikasjonTjeneste {

    private RullTilbakeTjeneste rullTilbakeTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private HåndteringInputTjeneste håndteringInputTjeneste;

    public HåndtererApplikasjonTjeneste() {
        // CDI
    }

    @Inject
    public HåndtererApplikasjonTjeneste(RullTilbakeTjeneste rullTilbakeTjeneste, BeregningsgrunnlagRepository beregningsgrunnlagRepository, HåndteringInputTjeneste håndteringInputTjeneste) {
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.håndteringInputTjeneste = håndteringInputTjeneste;
    }

    public OppdateringRespons håndter(Long koblingId, HåndterBeregningDto håndterBeregningDto) {
        rullTilbakeVedBehov(koblingId, håndterBeregningDto);
        HåndteringKode håndteringKode = håndterBeregningDto.getKode();
        BeregningHåndterer<HåndterBeregningDto> beregningHåndterer = finnBeregningHåndterer(håndterBeregningDto.getClass(), håndteringKode.getKode());
        HåndteringResultat resultat = beregningHåndterer.håndter(håndterBeregningDto, håndteringInputTjeneste.lagInput(koblingId, håndteringKode));
        var beregningsgrunnlagGrunnlagBuilder = KalkulatorTilEntitetMapper.mapGrunnlag(resultat.getNyttGrunnlag());
        mapFaktaBeregning(håndteringKode, resultat, beregningsgrunnlagGrunnlagBuilder);
        beregningsgrunnlagRepository.lagre(koblingId, beregningsgrunnlagGrunnlagBuilder, MapHåndteringskodeTilTilstand.map(håndteringKode));
        return resultat.getEndring();
    }

    // TODO: Fjernes når domenemodellen er utvidet med ny faktastruktur (TSF-1340)
    private void mapFaktaBeregning(HåndteringKode håndteringKode, HåndteringResultat resultat, BeregningsgrunnlagGrunnlagBuilder beregningsgrunnlagGrunnlagBuilder) {
        if (håndteringKode.equals(HåndteringKode.FAKTA_OM_BEREGNING)) {
            beregningsgrunnlagGrunnlagBuilder.medFaktaAggregat(KalkulatorTilEntitetMapper.mapFaktaAggregat(resultat.getNyttGrunnlag().getBeregningsgrunnlag()
                    .orElseThrow(() -> new IllegalStateException("Forventer beregningsgrunnlag"))).orElse(null));
        }
    }

    private void rullTilbakeVedBehov(Long koblingId, HåndterBeregningDto håndterBeregningDto) {
        HåndteringKode kode = håndterBeregningDto.getKode();
        BeregningsgrunnlagTilstand tilstand = MapHåndteringskodeTilTilstand.map(kode);
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingId, tilstand);
    }

    @SuppressWarnings("unchecked")
    private BeregningHåndterer<HåndterBeregningDto> finnBeregningHåndterer(Class<? extends HåndterBeregningDto> dtoClass,
                                                                                        String hånterKode) {
        Instance<Object> instance = finnAdapter(dtoClass, BeregningHåndterer.class);
        if (instance.isUnsatisfied()) {
            throw FACTORY.kanIkkeFinneHåndterer(hånterKode).toException();
        } else {
            Object minInstans = instance.get();
            if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                        "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
            }
            return (BeregningHåndterer<HåndterBeregningDto>) minInstans;
        }
    }

    private Instance<Object> finnAdapter(Class<?> cls, final Class<?> targetAdapter) {
        CDI<Object> cdi = CDI.current();
        Instance<Object> instance = cdi.select(new DtoTilServiceAdapter.Literal(cls, targetAdapter));
        // hvis unsatisfied, søk parent
        while (instance.isUnsatisfied() && !Objects.equals(Object.class, cls)) {
            cls = cls.getSuperclass();
            instance = cdi.select(new DtoTilServiceAdapter.Literal(cls, targetAdapter));
            if (!instance.isUnsatisfied()) {
                return instance;
            }
        }

        return instance;
    }


}
