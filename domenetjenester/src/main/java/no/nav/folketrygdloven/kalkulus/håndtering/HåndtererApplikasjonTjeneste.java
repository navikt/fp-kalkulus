package no.nav.folketrygdloven.kalkulus.håndtering;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand;
import no.nav.folketrygdloven.kalkulus.felles.feil.TekniskException;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@ApplicationScoped
public class HåndtererApplikasjonTjeneste {
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;

    public HåndtererApplikasjonTjeneste() {
        // CDI
    }

    @Inject
    public HåndtererApplikasjonTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                        AvklaringsbehovTjeneste avklaringsbehovTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }

    public OppdateringRespons håndter(HåndterBeregningsgrunnlagInput håndterBeregningInput, HåndterBeregningDto håndterBeregningDto) {
        Long koblingId = håndterBeregningInput.getKoblingId();
        var avklaringsbehovdefinisjon = håndterBeregningDto.getAvklaringsbehovDefinisjon();
        if (avklaringsbehovdefinisjon.erOverstyring()) {
            if (håndterBeregningDto.skalAvbrytes()) {
                var overstyring = avklaringsbehovTjeneste.hentAvklaringsbehov(koblingId, avklaringsbehovdefinisjon);
                overstyring.ifPresent(o -> avklaringsbehovTjeneste.trekkOverstyring(koblingId, o));
                return OppdateringRespons.TOM_RESPONS();
            } else {
                opprettOverstyringAvklaringsbehov(koblingId, avklaringsbehovdefinisjon, håndterBeregningDto.getBegrunnelse());
            }
        }
        BeregningsgrunnlagTilstand tilstand = MapStegTilTilstand.mapTilStegUtTilstand(håndterBeregningDto.getAvklaringsbehovDefinisjon().getStegFunnet()).orElseThrow();
        HåndteringResultat resultat = håndterOgLagre(håndterBeregningInput, håndterBeregningDto, håndterBeregningDto.getAvklaringsbehovDefinisjon(), tilstand);
        løsAvklaringsbehov(koblingId, håndterBeregningDto);
        if (resultat.getEndring() != null) {
            return new OppdateringRespons(resultat.getEndring(), håndterBeregningInput.getKoblingReferanse().getKoblingUuid());
        } else {
            return new OppdateringRespons();
        }
    }

    private void løsAvklaringsbehov(Long koblingId, HåndterBeregningDto håndterBeregningDto) {
        var avklaringsbehov = håndterBeregningDto.getAvklaringsbehovDefinisjon();
        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehovEtter(koblingId, avklaringsbehov);
        avklaringsbehovTjeneste.settOpprettetVedBehov(koblingId, avklaringsbehov);
        avklaringsbehovTjeneste.løsAvklaringsbehov(koblingId, avklaringsbehov,
                håndterBeregningDto.getBegrunnelse());
    }

    private void opprettOverstyringAvklaringsbehov(Long koblingId, AvklaringsbehovDefinisjon avklaringsbehovdefinisjon, String begrunnelse) {
        if (!KonfigurasjonVerdi.instance().get("TREKKE_OVERSTYRING_ENABLED", false)) {
            avklaringsbehovTjeneste.avbrytAndreAvklaringsbehovISammeSteg(koblingId, avklaringsbehovdefinisjon);
        } else {
            avklaringsbehovTjeneste.løsAndreAvklaringsbehovISammeSteg(koblingId, avklaringsbehovdefinisjon, begrunnelse);

        }
        avklaringsbehovTjeneste.opprettEllerGjennopprettAvklaringsbehov(koblingId, avklaringsbehovdefinisjon);
    }


    private HåndteringResultat håndterOgLagre(HåndterBeregningsgrunnlagInput hånteringInput, HåndterBeregningDto håndterBeregningDto, AvklaringsbehovDefinisjon avklaringsbehovDefinisjon, BeregningsgrunnlagTilstand tilstand) {
        BeregningHåndterer<HåndterBeregningDto> beregningHåndterer = finnBeregningHåndterer(håndterBeregningDto.getClass(), avklaringsbehovDefinisjon.getKode());
        HåndteringResultat resultat = beregningHåndterer.håndter(håndterBeregningDto, hånteringInput);
        var beregningsgrunnlagGrunnlagBuilder = KalkulatorTilEntitetMapper.mapGrunnlag(resultat.getNyttGrunnlag());
        beregningsgrunnlagRepository.lagre(hånteringInput.getKoblingId(), beregningsgrunnlagGrunnlagBuilder, tilstand);
        return resultat;
    }

    @SuppressWarnings("unchecked")
    private BeregningHåndterer<HåndterBeregningDto> finnBeregningHåndterer(Class<? extends HåndterBeregningDto> dtoClass,
                                                                           String hånterKode) {
        Instance<Object> instance = finnAdapter(dtoClass, BeregningHåndterer.class);
        if (instance.isUnsatisfied()) {
            throw new TekniskException("FT-770745", String.format("Finner ikke håndtering for avklaringsbehov med kode: %s", hånterKode));
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
