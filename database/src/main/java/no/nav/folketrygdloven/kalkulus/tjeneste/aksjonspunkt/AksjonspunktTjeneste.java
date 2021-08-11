package no.nav.folketrygdloven.kalkulus.tjeneste.aksjonspunkt;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.TekniskException;

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

    public void lagreAksjonspunktresultater(Long koblingid, List<AksjonspunktDefinisjon> aksjonspunkter) {
        KoblingEntitet kobling = koblingRepository.hentKoblingMedId(koblingid).orElseThrow();
        verifiserAlleNyeAksjonspunktErUnike(aksjonspunkter, kobling.getId());
        aksjonspunkter.forEach(utledetAP -> håndterUtledetAksjonspunkt(kobling, utledetAP));
    }

    public void opprettEllerGjennopprettAksjonspunkt(Long koblingId, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        KoblingEntitet koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        Optional<AksjonspunktEntitet> eksisterendeAP = aksjonspunktRepository.hentAksjonspunktforKobling(koblingEntitet, aksjonspunktDefinisjon);
        if (eksisterendeAP.isEmpty()) {
            håndterUtledetAksjonspunkt(koblingEntitet, aksjonspunktDefinisjon);
        } else {
            reaktiverAksjonspunkt(eksisterendeAP.get());
        }
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

    public void avbrytAndreAksjonspunkterISammeSteg(Long koblingId, AksjonspunktDefinisjon definisjon) {
        hentAlleAksjonspunkterForKobling(koblingId).forEach(ap -> {
            if (!definisjon.equals(ap.getDefinisjon()) && ap.getStegFunnet().equals(definisjon.getStegFunnet())) {
                avbrytAksjonspunkt(koblingId, ap);
            }
        });
    }

    /**
     *
     * @param stegSomSkalBeregnes beregningsteget vi nå skal beregne
     * @param koblingId koblingen som skal beregnes
     * Alle aksjonspunkter som er funnet før steget vi nå skal til
     * må være løst eller avbrutt før vi kan fortsette beregningen
     */
    public void validerIngenAksjonspunktFørStegÅpne(BeregningSteg stegSomSkalBeregnes, Long koblingId) {
        List<AksjonspunktEntitet> åpneAksjonspunktFørSteg = hentAlleAksjonspunkterForKobling(koblingId).stream()
                .filter(ap -> ap.getStatus().equals(AksjonspunktStatus.OPPRETTET))
                .filter(ap -> ap.getStegFunnet().erFør(stegSomSkalBeregnes))
                .collect(Collectors.toList());
        if (!åpneAksjonspunktFørSteg.isEmpty()) {
            throw new TekniskException("FT-406874",
                    String.format("Det finnes aksjonspunkt for kobling %s som må løses før beregning kan fortsette til steg %s, listen med åpne aksjonspunkter er %s",
                            koblingId, stegSomSkalBeregnes, åpneAksjonspunktFørSteg));
        }
    }

    /**
     *
     * @param koblingId
     * Skal sjekke at det kun finnes avbrutte aksjonspunkter (eller ingen aksjonspunkter) for en kobling
     */
    public void validerIngenÅpneAksjonspunkterPåKobling(Long koblingId) {
        List<AksjonspunktEntitet> alleAksjonspunkterIkkeAvbrutt = hentAlleAksjonspunkterForKobling(koblingId).stream()
                .filter(ap -> !AksjonspunktStatus.AVBRUTT.equals(ap.getStatus()))
                .collect(Collectors.toList());
        if (!alleAksjonspunkterIkkeAvbrutt.isEmpty()) {
            throw new TekniskException("FT-406875",
                    String.format("Det finnes aksjonspunkt for kobling %s som ikke er avbrutt, listen med aksjonspunkter i ugyldig tilstand er %s",
                            koblingId, alleAksjonspunkterIkkeAvbrutt));
        }
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


    private void håndterUtledetAksjonspunkt(KoblingEntitet kobling, AksjonspunktDefinisjon utledetAP) {
        Optional<AksjonspunktEntitet> eksisterendeAP = aksjonspunktRepository.hentAksjonspunktforKobling(kobling, utledetAP);
        if (eksisterendeAP.isEmpty()) {
            AksjonspunktEntitet aksjonspunktEntitet = aksjonspunktKontrollTjeneste.opprettForKobling(kobling, utledetAP);
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
        Set<AksjonspunktDefinisjon> set = new HashSet<>(aksjonspunkter);
        if (set.size() != aksjonspunkter.size()) {
            throw new TekniskException("FT-406870",
                    String.format("Det er utledet duplikate aksjonspunkter for kobling %s. Listen med aksjonspunkter er %s", koblingId, aksjonspunkter));
        }
    }

    private List<AksjonspunktEntitet> hentAlleAksjonspunkterForKobling(Long koblingId) {
        KoblingEntitet koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        return aksjonspunktRepository.hentAksjonspunkterforKobling(koblingEntitet);
    }

    public void avbrytAlleAksjonspunktEtter(Long koblingId, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        KoblingEntitet koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        List<AksjonspunktEntitet> alleApPåKobling = aksjonspunktRepository.hentAksjonspunkterforKobling(koblingEntitet);
        Optional<AksjonspunktEntitet> gjeldendeAP = alleApPåKobling.stream().filter(ap -> ap.getDefinisjon().equals(aksjonspunktDefinisjon)).findFirst();
        if (gjeldendeAP.isEmpty()) {
            kastManglendeAksjonspunktFeil(koblingId, aksjonspunktDefinisjon);
        }
        alleApPåKobling.forEach(ap -> {
            if (aksjonspunktErFunnetEtter(ap, gjeldendeAP.get())) {
                avbrytAksjonspunkt(koblingId, ap);
            }
        });
    }

    private boolean aksjonspunktErFunnetEtter(AksjonspunktEntitet ap, AksjonspunktEntitet aksjonspunktEntitet) {
        return ap.getStegFunnet().erEtter(aksjonspunktEntitet.getStegFunnet());
    }

    private void avbrytAksjonspunkt(Long koblingId, AksjonspunktEntitet ap) {
        LOG.info("Avbryter aksjonspunkt {} for kobling {}", ap.getDefinisjon(), koblingId);
        aksjonspunktKontrollTjeneste.avbryt(ap);
        aksjonspunktRepository.lagre(ap);
    }

    private void kastManglendeAksjonspunktFeil(Long koblingId, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        throw new TekniskException("FT-406876",
                String.format("Aksjonspunkt med definisjon %s på koblingen med id %s finnes sikke på koblingen.", aksjonspunktDefinisjon, koblingId));
    }

    public void settOpprettetVedBehov(Long koblingId, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        KoblingEntitet koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        Optional<AksjonspunktEntitet> eksisterendeAP = aksjonspunktRepository.hentAksjonspunktforKobling(koblingEntitet, aksjonspunktDefinisjon);
        if (eksisterendeAP.isEmpty()) {
            kastManglendeAksjonspunktFeil(koblingId, aksjonspunktDefinisjon);
        }
        AksjonspunktEntitet ap = eksisterendeAP.get();
        reaktiverAksjonspunkt(ap);
    }
}
