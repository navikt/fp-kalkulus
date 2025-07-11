package no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovKontrollTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class AvklaringsbehovTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(AvklaringsbehovTjeneste.class);

    private AvklaringsbehovRepository avklaringsbehovRepository;
    private KoblingRepository koblingRepository;
    private AvklaringsbehovKontrollTjeneste avklaringsbehovKontrollTjeneste;

    AvklaringsbehovTjeneste() {
        // CDI
    }

    @Inject
    public AvklaringsbehovTjeneste(AvklaringsbehovRepository avklaringsbehovRepository,
                                   KoblingRepository koblingRepository) {
        this.avklaringsbehovRepository = avklaringsbehovRepository;
        this.koblingRepository = koblingRepository;
        this.avklaringsbehovKontrollTjeneste = new AvklaringsbehovKontrollTjeneste();
    }

    public void lagreAvklaringsresultater(Long koblingid, List<AvklaringsbehovDefinisjon> avklaringsbehov) {
        KoblingEntitet kobling = koblingRepository.hentKoblingMedId(koblingid).orElseThrow();
        verifiserAlleNyeAvklaringsbehovErUnike(avklaringsbehov, kobling.getId());
        avklaringsbehov.forEach(utledetAB -> håndterUtledetAvklaringsbehov(kobling, utledetAB));
    }

    public void opprettEllerGjennopprettAvklaringsbehov(Long koblingId, AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        KoblingEntitet koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        Optional<AvklaringsbehovEntitet> eksisterendeAP = avklaringsbehovRepository.hentAvklaringsbehovForKobling(koblingEntitet, avklaringsbehovDefinisjon);
        if (eksisterendeAP.isEmpty()) {
            håndterUtledetAvklaringsbehov(koblingEntitet, avklaringsbehovDefinisjon);
        } else {
            reaktiverAvklaringsbehov(eksisterendeAP.get());
        }
    }

    public void kopierAvklaringsbehov(KoblingEntitet nyKobling, AvklaringsbehovEntitet avklaringsbehov) {
        Optional<AvklaringsbehovEntitet> eksisterendeAP = avklaringsbehovRepository.hentAvklaringsbehovForKobling(nyKobling, avklaringsbehov.getDefinisjon());
        AvklaringsbehovEntitet kopiert;
        if (eksisterendeAP.isEmpty()) {
            kopiert = avklaringsbehovKontrollTjeneste.opprettForKoblingLikEksisterende(nyKobling, avklaringsbehov);
        } else {
            kopiert = avklaringsbehovKontrollTjeneste.kopierDataFraAvklaringsbehov(eksisterendeAP.get(), avklaringsbehov);
        }
        avklaringsbehovRepository.lagre(kopiert);
    }

    public Optional<AvklaringsbehovEntitet> hentAvklaringsbehov(Long koblingId, AvklaringsbehovDefinisjon definisjon) {
        KoblingEntitet kobling = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        return avklaringsbehovRepository.hentAvklaringsbehovForKobling(kobling, definisjon);
    }

    public void løsAvklaringsbehov(Long koblingId, AvklaringsbehovDefinisjon definisjon, String begrunnelse) {
        // Guard mot å løse avklaringsbehov i ugyldig state
        validerAtABKanLøses(definisjon, koblingId);

        LOG.info("Løser avklaringsbehov {} for kobling {}", definisjon, koblingId);
        KoblingEntitet kobling = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        AvklaringsbehovEntitet avklaringsbehovEntitet = avklaringsbehovRepository.hentAvklaringsbehovForKobling(kobling, definisjon).orElseThrow();
        avklaringsbehovKontrollTjeneste.løs(avklaringsbehovEntitet, begrunnelse);
        avklaringsbehovRepository.lagre(avklaringsbehovEntitet);
    }

    public void løsAndreAvklaringsbehovISammeSteg(Long koblingId, AvklaringsbehovDefinisjon definisjon, String begrunnelse) {
        hentAlleAvklaringsbehovForKobling(koblingId).forEach(ap -> {
            if (erÅpentAksjonspunktISammeSteg(definisjon, ap)) {
                løsAvklaringsbehov(koblingId, ap.getDefinisjon(), begrunnelse);
            }
        });
    }

    private static boolean erÅpentAksjonspunktISammeSteg(AvklaringsbehovDefinisjon definisjon, AvklaringsbehovEntitet ap) {
        return !definisjon.equals(ap.getDefinisjon()) && ap.getStegFunnet().equals(definisjon.getStegFunnet()) && ap.getStatus().equals(AvklaringsbehovStatus.OPPRETTET);
    }

    /**
     * @param stegSomSkalBeregnes beregningsteget vi nå skal beregne
     * @param koblingId           koblingen som skal beregnes
     *                            Alle avklaringsbehov som er funnet før steget vi nå skal til
     *                            må være løst eller avbrutt før vi kan fortsette beregningen
     */
    public void validerIngenAvklaringsbehovFørStegÅpne(BeregningSteg stegSomSkalBeregnes, Long koblingId) {
        List<AvklaringsbehovEntitet> åpneAvklaringsbehovFørSteg = hentAlleAvklaringsbehovForKobling(koblingId).stream()
                .filter(ap -> ap.getStatus().equals(AvklaringsbehovStatus.OPPRETTET))
                .filter(ap -> ap.getStegFunnet().erFør(stegSomSkalBeregnes))
                .toList();
        if (!åpneAvklaringsbehovFørSteg.isEmpty()) {
            throw new TekniskException("FT-406874",
                    String.format("Det finnes avklaringsbehov for kobling %s som må løses før beregning kan fortsette til steg %s, listen med åpne avklaringsbehov er %s",
                            koblingId, stegSomSkalBeregnes, åpneAvklaringsbehovFørSteg));
        }
    }

    /**
     * @param koblingId Skal sjekke at det kun finnes avbrutte avklaringsbehov (eller ingen avklaringsbehov) for en kobling
     */
    public void validerIngenÅpneAvklaringsbehovPåKobling(Long koblingId) {
        List<AvklaringsbehovEntitet> alleAvklaringsbehovIkkeAvbrutt = hentAlleAvklaringsbehovForKobling(koblingId).stream()
                .filter(ap -> !AvklaringsbehovStatus.AVBRUTT.equals(ap.getStatus()))
                .filter(ap -> !ap.getDefinisjon().erOverstyring())
                .toList();
        if (!alleAvklaringsbehovIkkeAvbrutt.isEmpty()) {
            throw new TekniskException("FT-406875",
                    String.format("Det finnes avklaringsbehov for kobling %s som ikke er avbrutt, listen med avklaringsbehov i ugyldig tilstand er %s",
                            koblingId, alleAvklaringsbehovIkkeAvbrutt));
        }
    }

    private void validerAtABKanLøses(AvklaringsbehovDefinisjon definisjon, Long koblingId) {
        Optional<AvklaringsbehovEntitet> avklaringsbehovEntitet = hentAvklaringsbehov(koblingId, definisjon);
        if (avklaringsbehovEntitet.isEmpty()) {
            throw new TekniskException("FT-406872",
                    String.format("Prøver å løse avklaringsbehov %s for kobling %s uten at dette er utledet for koblingen", definisjon, koblingId));
        } else if (!AvklaringsbehovStatus.OPPRETTET.equals(avklaringsbehovEntitet.get().getStatus())) {
            throw new TekniskException("FT-406873",
                    String.format("Prøver å løse avklaringsbehov %s for kobling %s men det har ugyldig status %s", definisjon, koblingId, avklaringsbehovEntitet.get().getStatus()));
        }
    }


    private void håndterUtledetAvklaringsbehov(KoblingEntitet kobling, AvklaringsbehovDefinisjon utledetAP) {
        Optional<AvklaringsbehovEntitet> eksisterendeAP = avklaringsbehovRepository.hentAvklaringsbehovForKobling(kobling, utledetAP);
        if (eksisterendeAP.isEmpty()) {
            AvklaringsbehovEntitet avklaringsbehovEntitet = avklaringsbehovKontrollTjeneste.opprettForKobling(kobling, utledetAP);
            avklaringsbehovRepository.lagre(avklaringsbehovEntitet);
        } else if (AvklaringsbehovStatus.AVBRUTT.equals(eksisterendeAP.get().getStatus())) {
            reaktiverAvklaringsbehov(eksisterendeAP.get());
        } else if (AvklaringsbehovStatus.OPPRETTET.equals(eksisterendeAP.get().getStatus()) && !eksisterendeAP.get().getDefinisjon().erOverstyring()) {
            throw new TekniskException("FT-406871",
                    String.format("Det er utledet avklaringsbehov med definisjon %s som allerede finnes på koblingen med id %s.", utledetAP, kobling.getId()));
        }
    }

    private void reaktiverAvklaringsbehov(AvklaringsbehovEntitet avklaringsbehovEntitet) {
        avklaringsbehovKontrollTjeneste.gjennopprett(avklaringsbehovEntitet);
        avklaringsbehovRepository.lagre(avklaringsbehovEntitet);
    }

    private void verifiserAlleNyeAvklaringsbehovErUnike(List<AvklaringsbehovDefinisjon> avklaringsbehov, Long koblingId) {
        Set<AvklaringsbehovDefinisjon> set = new HashSet<>(avklaringsbehov);
        if (set.size() != avklaringsbehov.size()) {
            throw new TekniskException("FT-406870",
                    String.format("Det er utledet duplikate avklaringsbehov for kobling %s. Listen med avklaringsbehov er %s", koblingId, avklaringsbehov));
        }
    }

    public List<AvklaringsbehovEntitet> hentAlleAvklaringsbehovForKobling(Long koblingId) {
        KoblingEntitet koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        return avklaringsbehovRepository.hentAvklaringsbehovForKobling(koblingEntitet);
    }

    public void avbrytAlleAvklaringsbehovEtterEllerISteg(Long koblingId, BeregningSteg steg, boolean skalKjøreSteget) {
        List<AvklaringsbehovEntitet> alleApPåKobling = avklaringsbehovRepository.hentAvklaringsbehovforKobling(koblingId);
        alleApPåKobling.forEach(ap -> {
                    if (skalKjøreSteget && !ap.getStegFunnet().erFør(steg)) {
                        avbrytAvklaringsbehov(ap.getKoblingId(), ap);
                    } else if (!skalKjøreSteget && ap.getStegFunnet().erEtter(steg)) {
                        avbrytAvklaringsbehov(ap.getKoblingId(), ap);
                    }

                });
    }

    public void avbrytAlleAvklaringsbehovEtter(Long koblingId, AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        KoblingEntitet koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        List<AvklaringsbehovEntitet> alleApPåKobling = avklaringsbehovRepository.hentAvklaringsbehovForKobling(koblingEntitet);
        Optional<AvklaringsbehovEntitet> gjeldendeAP = alleApPåKobling.stream().filter(ap -> ap.getDefinisjon().equals(avklaringsbehovDefinisjon)).findFirst();
        if (gjeldendeAP.isEmpty()) {
            kastManglendeAvklaringsbehovFeil(koblingId, avklaringsbehovDefinisjon);
        }
        alleApPåKobling.forEach(ap -> {
            if (avklaringsbehovErFunnetEtter(ap, gjeldendeAP.get())) {
                avbrytAvklaringsbehov(koblingId, ap);
            }
        });
    }

    /**
     * Avbryter alle avklaringsbehov på kobling
     *
     * @param koblingId KoblingId
     */
    public void avbrytAlleAvklaringsbehov(Long koblingId) {
        KoblingEntitet koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        List<AvklaringsbehovEntitet> alleApPåKobling = avklaringsbehovRepository.hentAvklaringsbehovForKobling(koblingEntitet);
        alleApPåKobling.forEach(ap -> avbrytAvklaringsbehov(koblingId, ap));
    }

    private boolean avklaringsbehovErFunnetEtter(AvklaringsbehovEntitet ap, AvklaringsbehovEntitet avklaringsbehovEntitet) {
        return ap.getStegFunnet().erEtter(avklaringsbehovEntitet.getStegFunnet());
    }

    public void avbrytAvklaringsbehov(Long koblingId, AvklaringsbehovEntitet ap) {
        LOG.info("Avbryter avklaringsbehov {} for kobling {}", ap.getDefinisjon(), koblingId);
        avklaringsbehovKontrollTjeneste.avbryt(ap);
        avklaringsbehovRepository.lagre(ap);
    }

    public void trekkOverstyring(Long koblingId, AvklaringsbehovEntitet ap) {
        LOG.info("Trekker overstyring {} for kobling {}", ap.getDefinisjon(), koblingId);
        avklaringsbehovKontrollTjeneste.trekkOverstyring(ap);
        avklaringsbehovRepository.lagre(ap);
    }

    private void kastManglendeAvklaringsbehovFeil(Long koblingId, AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        throw new TekniskException("FT-406876",
                String.format("Avklaringsbehov med definisjon %s på koblingen med id %s finnes sikke på koblingen.", avklaringsbehovDefinisjon, koblingId));
    }

    public void settOpprettetVedBehov(Long koblingId, AvklaringsbehovDefinisjon avklaringsbehovDefinisjon) {
        KoblingEntitet koblingEntitet = koblingRepository.hentKoblingMedId(koblingId).orElseThrow();
        Optional<AvklaringsbehovEntitet> eksisterendeAP = avklaringsbehovRepository.hentAvklaringsbehovForKobling(koblingEntitet, avklaringsbehovDefinisjon);
        if (eksisterendeAP.isEmpty()) {
            kastManglendeAvklaringsbehovFeil(koblingId, avklaringsbehovDefinisjon);
        }
        AvklaringsbehovEntitet ap = eksisterendeAP.get();
        reaktiverAvklaringsbehov(ap);
    }
}
