package no.nav.folketrygdloven.kalkulus.håndtering;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.beregning.MapHåndteringskodeTilTilstand;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.HåndteringKode;
import no.nav.folketrygdloven.kalkulus.mapTilEntitet.KalkulatorTilEntitetMapper;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.aksjonspunkt.AksjonspunktTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
import no.nav.k9.felles.exception.TekniskException;

@ApplicationScoped
public class HåndtererApplikasjonTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(HåndtererApplikasjonTjeneste.class);

    private RullTilbakeTjeneste rullTilbakeTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private AksjonspunktTjeneste aksjonspunktTjeneste;

    public HåndtererApplikasjonTjeneste() {
        // CDI
    }

    @Inject
    public HåndtererApplikasjonTjeneste(RullTilbakeTjeneste rullTilbakeTjeneste,
                                        BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                        AksjonspunktTjeneste aksjonspunktTjeneste) {
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.aksjonspunktTjeneste = aksjonspunktTjeneste;
    }

    public Map<Long, OppdateringRespons> håndter(Map<Long, HåndterBeregningsgrunnlagInput> håndterBeregningInputPrKobling, Map<Long, HåndterBeregningDto> håndterBeregningDtoPrKobling) {

        Map<Long, OppdateringRespons> resultatPrKobling = new HashMap<>();

        for (Map.Entry<Long, HåndterBeregningsgrunnlagInput> hånteringInputPrKobling : håndterBeregningInputPrKobling.entrySet()) {
            Long koblingId = hånteringInputPrKobling.getKey();
            HåndterBeregningDto håndterBeregningDto = håndterBeregningDtoPrKobling.get(koblingId);

            // Siden vi i starten kan få inn aksjonspunkter som ikke er lagret i kalkulus (gamle saker fra før dette ble introdusert)
            // må vi sjekke på dette intill gamle saker har passert beregning
            boolean skalLøseAksjonspunktEtterOppdatering = false;
            AksjonspunktDefinisjon aksjonspunktdefinisjon = AksjonspunktDefinisjon.fraHåndtering(håndterBeregningDto.getKode());
            if (aksjonspunktTjeneste.skalLagreAksjonspunktIKalkulus()) {
                if (harUtledetAksjonspunkt(koblingId, aksjonspunktdefinisjon)) {
                    skalLøseAksjonspunktEtterOppdatering = true;
                } else if (aksjonspunktdefinisjon.erOverstyring()) {
                    opprettOverstyringAksjonspunkt(koblingId, aksjonspunktdefinisjon);
                    skalLøseAksjonspunktEtterOppdatering = true;
                } else {
                    LOG.info("FT-406871: Prøver å løse aksjonspunkt {} på kobling {} men dette er ikke lagret som utledet i kalkulus", håndterBeregningDto.getKode(), koblingId);
                }
            }

            BeregningsgrunnlagTilstand tilstand = MapHåndteringskodeTilTilstand.map(håndterBeregningDto.getKode());
            HåndteringResultat resultat = håndterOgLagre(hånteringInputPrKobling, håndterBeregningDto, håndterBeregningDto.getKode(), tilstand);
            if (resultat.getEndring() != null) {
                resultatPrKobling.put(koblingId, resultat.getEndring());
            } else {
                resultatPrKobling.put(koblingId, new OppdateringRespons());
            }

            if (skalLøseAksjonspunktEtterOppdatering) {
                løsAksjonspunkt(koblingId, håndterBeregningDto);
            }
        }
        return resultatPrKobling;
    }

    private boolean harUtledetAksjonspunkt(Long koblingId, AksjonspunktDefinisjon aksjonspunktdefinisjon) {
        return aksjonspunktTjeneste.hentAksjonspunkt(koblingId, aksjonspunktdefinisjon).isPresent();
    }

    private void løsAksjonspunkt(Long koblingId, HåndterBeregningDto håndterBeregningDto) {
        if (aksjonspunktTjeneste.skalLagreAksjonspunktIKalkulus()) {
            AksjonspunktDefinisjon aksjonspunkt = AksjonspunktDefinisjon.fraHåndtering(håndterBeregningDto.getKode());
            aksjonspunktTjeneste.avbrytAlleAksjonspunktEtter(koblingId, aksjonspunkt);
            aksjonspunktTjeneste.settOpprettetVedBehov(koblingId, aksjonspunkt);
            aksjonspunktTjeneste.løsAksjonspunkt(koblingId, aksjonspunkt,
                    håndterBeregningDto.getBegrunnelse());
        }
    }

    private void opprettOverstyringAksjonspunkt(Long koblingId, AksjonspunktDefinisjon aksjonspunktdefinisjon) {
        aksjonspunktTjeneste.avbrytAndreAksjonspunkterISammeSteg(koblingId, aksjonspunktdefinisjon);
        aksjonspunktTjeneste.opprettEllerGjennopprettAksjonspunkt(koblingId, aksjonspunktdefinisjon);
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
            throw new TekniskException("FT-770745", String.format("Finner ikke håndtering for aksjonspunkt med kode: %s", hånterKode));
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
