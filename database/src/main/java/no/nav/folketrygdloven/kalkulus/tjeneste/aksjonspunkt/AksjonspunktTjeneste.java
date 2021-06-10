package no.nav.folketrygdloven.kalkulus.tjeneste.aksjonspunkt;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.aksjonspunkt.AksjonspunktEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.aksjonspunkt.AksjonspunktKontrollTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class AksjonspunktTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(AksjonspunktTjeneste.class);

    private AksjonspunktRepository aksjonspunktRepository;
    private KoblingRepository koblingRepository;
    private AksjonspunktKontrollTjeneste aksjonspunktKontrollTjeneste;
    private boolean skalLagreAksjonspunktIKalkulus;

    AksjonspunktTjeneste() {
        // CDI
    }

    @Inject
    public AksjonspunktTjeneste(AksjonspunktRepository aksjonspunktRepository,
                                KoblingRepository koblingRepository,
                                AksjonspunktKontrollTjeneste aksjonspunktKontrollTjeneste,
                                @KonfigVerdi(value = "LAGRE_AKSJONSPUNKT_I_KALKULUS", defaultVerdi = "false", required = false) boolean skalLagreAksjonspunktIKalkulus) {
        this.aksjonspunktRepository = aksjonspunktRepository;
        this.koblingRepository = koblingRepository;
        this.aksjonspunktKontrollTjeneste = aksjonspunktKontrollTjeneste;
        this.skalLagreAksjonspunktIKalkulus = skalLagreAksjonspunktIKalkulus;
    }

    public boolean skalLagreAksjonspunktIKalkulus() {
        return skalLagreAksjonspunktIKalkulus;
    }

    public void lagreAksjonspunktresultater(Long koblingid, BeregningSteg steg, List<AksjonspunktDefinisjon> aksjonspunkter) {
        KoblingEntitet kobling = koblingRepository.hentKoblingMedId(koblingid).orElseThrow();
        verifiserAlleNyeAksjonspunktErUnike(aksjonspunkter, kobling.getId());
        aksjonspunkter.forEach(utledetAP -> håndterUtledetAksjonspunkt(steg, kobling, utledetAP));
    }

    public Optional<AksjonspunktEntitet> hentAksjonspunkt(Long koblingId, AksjonspunktDefinisjon definisjon) {
        KoblingEntitet kobling = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        return aksjonspunktRepository.hentAksjonspunktforKobling(kobling, definisjon);
    }

    public void løsAksjonspunkt(Long koblingId, AksjonspunktDefinisjon definisjon, String begrunnelse) {
        // Guard mot å løse aksjonspunkter i ugyldig state
        validerAtAPKanLøses(definisjon, koblingId);

        LOG.info("Løser aksjonspunkt {} for kobling {}", definisjon, koblingId);
        KoblingEntitet kobling = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        AksjonspunktEntitet aksjonspunktEntitet = aksjonspunktRepository.hentAksjonspunktforKobling(kobling, definisjon).orElseThrow();
        aksjonspunktKontrollTjeneste.løs(aksjonspunktEntitet, begrunnelse);
        aksjonspunktRepository.lagre(aksjonspunktEntitet);
    }

    private void validerAtAPKanLøses(AksjonspunktDefinisjon definisjon, Long koblingId) {
        if (skalLagreAksjonspunktIKalkulus()) {
            Optional<AksjonspunktEntitet> aksjonspunktEntitet = hentAksjonspunkt(koblingId, definisjon);
            if (aksjonspunktEntitet.isEmpty()) {
                throw new TekniskException("FT-406872",
                        String.format("Prøver å løse aksjonspunkt %s for kobling %s uten at dette er utledet for koblingen", definisjon, koblingId));
            } else if (!AksjonspunktStatus.OPPRETTET.equals(aksjonspunktEntitet.get().getStatus())) {
                throw new TekniskException("FT-406873",
                        String.format("Prøver å løse aksjonspunkt %s for kobling %s men det har ugyldig status %s", definisjon, koblingId, aksjonspunktEntitet.get().getStatus()));
            }
        }
    }


    private void håndterUtledetAksjonspunkt(BeregningSteg steg, KoblingEntitet kobling, AksjonspunktDefinisjon utledetAP) {
        Optional<AksjonspunktEntitet> eksisterendeAP = aksjonspunktRepository.hentAksjonspunktforKobling(kobling, utledetAP);
        if (eksisterendeAP.isEmpty()) {
            AksjonspunktEntitet aksjonspunktEntitet = aksjonspunktKontrollTjeneste.opprettForKobling(kobling, steg, utledetAP);
            aksjonspunktRepository.lagre(aksjonspunktEntitet);
        } else if (AksjonspunktStatus.AVBRUTT.equals(eksisterendeAP.get().getStatus())) {
            reaktiverAksjonspunkt(eksisterendeAP.get());
        } else if (AksjonspunktStatus.OPPRETTET.equals(eksisterendeAP.get().getStatus())) {
            throw new TekniskException("FT-406871",
                    String.format("Det er utledet aksjonspunkt med definisjon %s som allerede finnes på koblingen med id %s.", utledetAP, kobling.getId()));
        }
    }

    private void reaktiverAksjonspunkt(AksjonspunktEntitet aksjonspunktEntitet) {
        aksjonspunktKontrollTjeneste.gjennopprett(aksjonspunktEntitet);
        aksjonspunktRepository.lagre(aksjonspunktEntitet);
    }

    private void verifiserAlleNyeAksjonspunktErUnike(List<AksjonspunktDefinisjon> aksjonspunkter, Long koblingId) {
        Set<AksjonspunktDefinisjon> set = new HashSet<AksjonspunktDefinisjon>(aksjonspunkter);
        if (set.size() != aksjonspunkter.size()) {
            throw new TekniskException("FT-406870",
                    String.format("Det er utledet duplikate aksjonspunkter for kobling %s. Listen med aksjonspunkter er %s", koblingId, aksjonspunkter));
        }
    }
}
