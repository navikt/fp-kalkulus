package no.nav.folketrygdloven.kalkulus.håndtering;

import static no.nav.folketrygdloven.kalkulus.håndtering.HåndteringApplikasjonFeil.FACTORY;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.beregning.MapHåndteringskodeTilTilstand;
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

    public HåndtererApplikasjonTjeneste() {
        // CDI
    }

    @Inject
    public HåndtererApplikasjonTjeneste(RullTilbakeTjeneste rullTilbakeTjeneste, BeregningsgrunnlagRepository beregningsgrunnlagRepository) {
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
    }

    public Map<Long, OppdateringRespons> håndter(Map<Long, HåndterBeregningsgrunnlagInput> håndterBeregningInputPrKobling, Map<Long, HåndterBeregningDto> håndterBeregningDtoPrKobling) {

        Map<Long, OppdateringRespons> resultatPrKobling = new HashMap<>();

        for (Map.Entry<Long, HåndterBeregningsgrunnlagInput> hånteringInputPrKobling : håndterBeregningInputPrKobling.entrySet()) {
            HåndterBeregningDto håndterBeregningDto = håndterBeregningDtoPrKobling.get(hånteringInputPrKobling.getKey());
            HåndteringKode håndteringKode = håndterBeregningDto.getKode();
            BeregningsgrunnlagTilstand tilstand = MapHåndteringskodeTilTilstand.map(håndteringKode);
            HåndteringResultat resultat = håndterOgLagre(hånteringInputPrKobling, håndterBeregningDto, håndteringKode, tilstand);
            if (resultat.getEndring() != null) {
                resultatPrKobling.put(hånteringInputPrKobling.getKey(), resultat.getEndring());
            }
        }
        return resultatPrKobling;
    }

    private HåndteringResultat håndterOgLagre(Map.Entry<Long, HåndterBeregningsgrunnlagInput> hånteringInputPrKobling, HåndterBeregningDto håndterBeregningDto, HåndteringKode håndteringKode, BeregningsgrunnlagTilstand tilstand) {
        rullTilbakeVedBehov(hånteringInputPrKobling.getKey(), tilstand);
        BeregningHåndterer<HåndterBeregningDto> beregningHåndterer = finnBeregningHåndterer(håndterBeregningDto.getClass(), håndteringKode.getKode());
        HåndteringResultat resultat = beregningHåndterer.håndter(håndterBeregningDto, hånteringInputPrKobling.getValue());
        var beregningsgrunnlagGrunnlagBuilder = KalkulatorTilEntitetMapper.mapGrunnlag(resultat.getNyttGrunnlag());
        beregningsgrunnlagRepository.lagre(hånteringInputPrKobling.getKey(), beregningsgrunnlagGrunnlagBuilder, tilstand);
        return resultat;
    }


    private void rullTilbakeVedBehov(Long koblingId, BeregningsgrunnlagTilstand tilstand) {
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
