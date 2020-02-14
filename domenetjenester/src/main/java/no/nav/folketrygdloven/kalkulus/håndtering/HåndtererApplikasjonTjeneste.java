package no.nav.folketrygdloven.kalkulus.håndtering;

import static no.nav.folketrygdloven.kalkulus.håndtering.HåndteringApplikasjonFeil.FACTORY;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.beregning.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilBGMapper;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@ApplicationScoped
public class HåndtererApplikasjonTjeneste {

    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    public HåndtererApplikasjonTjeneste() {
        // CDI
    }

    @Inject
    public HåndtererApplikasjonTjeneste(KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
    }

    public void håndter(Long koblingId, HåndterBeregningDto håndterBeregningDto) {
        BeregningHåndterer<HåndterBeregningDto> beregningHåndterer = finnBeregningHåndterer(håndterBeregningDto.getClass(), håndterBeregningDto.getKode().getKode());
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningHåndterer.håndter(håndterBeregningDto, kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(koblingId));
        BeregningsgrunnlagGrunnlagEntitet beregningsgrunnlagGrunnlagEntitet = KalkulatorTilEntitetMapper.mapGrunnlag(grunnlag);
        beregningsgrunnlagRepository.lagreOgFlush(koblingId, beregningsgrunnlagGrunnlagEntitet);
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
