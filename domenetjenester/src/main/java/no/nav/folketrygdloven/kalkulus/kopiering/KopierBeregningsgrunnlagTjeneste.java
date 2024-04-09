package no.nav.folketrygdloven.kalkulus.kopiering;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.request.v1.KopierBeregningRequest;
import no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class KopierBeregningsgrunnlagTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;

    public KopierBeregningsgrunnlagTjeneste() {
    }

    @Inject
    public KopierBeregningsgrunnlagTjeneste(KoblingTjeneste koblingTjeneste,
                                            BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                            AvklaringsbehovTjeneste avklaringsbehovTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }


    /**
     * Oppretter ny kobling og kopierer data fra eksisterende kobling
     * @param nyReferanse     Ny referanse som skal opprettes
     * @param saksnummer      Saksnummer
     * @param steg            Definerer steget som vi kopierer beregningsgrunnlag fra
     * @param originalReferanse Referanse vi skal kopiere fra
     */
    public void kopierGrunnlagOgOpprettKoblinger(KoblingReferanse nyReferanse,
                                                 KoblingReferanse originalReferanse,
                                                 Saksnummer saksnummer,
                                                 BeregningSteg steg) {
        var eksisterendeKobling = koblingTjeneste.hentFor(originalReferanse)
            .orElseThrow(() -> new TekniskException("FT-47034", String.format(
                "Pøver å opprette ny kobling %s basert på data fra en referanse som ikke finnes. Kobling med referanse kobling %s finnes ikke.",
                nyReferanse, originalReferanse)));
        var grunnlagSomSkalKopieres = validerOgHentGrunnlag(eksisterendeKobling, saksnummer, steg);
        var nyKobling = opprettNyKobling(eksisterendeKobling, nyReferanse);
        // opprettKoblingrelasjoner(kopiRequests); TODO tsf-5742 koblingrelasjon
        kopierBeregningsgrunnlag(grunnlagSomSkalKopieres, nyKobling, steg);
        kopierAvklaringsbehov(nyKobling, eksisterendeKobling, steg);
    }

    private void opprettKoblingrelasjoner(List<KopierBeregningRequest> kopiRequests) {
        var koblingrelasjoner = kopiRequests.stream()
            .collect(Collectors.toMap(KopierBeregningRequest::getEksternReferanse, f -> List.of(f.getKopierFraReferanse())));
        koblingTjeneste.finnOgOpprettKoblingRelasjoner(koblingrelasjoner);
    }

    private BeregningsgrunnlagGrunnlagEntitet validerOgHentGrunnlag(KoblingEntitet eksisterendeKobling, Saksnummer saksnummer, BeregningSteg steg) {
        if (!eksisterendeKobling.getSaksnummer().equals(saksnummer)) {
            throw new TekniskException("FT-47035", String.format(
                "Prøver å kopiere grunnlag fra en kobling uten matchende saksnummer. Saksnummer på ny kobling er %s mens saksnummer på eksisterende kobling var %S",
                saksnummer, eksisterendeKobling.getSaksnummer()));
        }
        return beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKobling(eksisterendeKobling.getId(),
                MapStegTilTilstand.mapTilStegTilstand(steg), null)
            .orElseThrow(() -> new TekniskException("FT-47036", String.format(
                "Prøver å kopiere grunnlag fra en kobling uten et beregningsgrunnlag i forespurt tilstand. Prøve å kopiere fra kobling %s men fant ikke grunnlag i tilstand %s",
                eksisterendeKobling.getKoblingReferanse(), MapStegTilTilstand.mapTilStegTilstand(steg))));
    }

    private KoblingEntitet opprettNyKobling(KoblingEntitet eksisterendeKobling, KoblingReferanse nyReferanse) {
        return koblingTjeneste.finnEllerOpprett(nyReferanse, eksisterendeKobling.getYtelseType(), eksisterendeKobling.getAktørId(), eksisterendeKobling.getSaksnummer());
    }

    private void kopierBeregningsgrunnlag(BeregningsgrunnlagGrunnlagEntitet grunnlag, KoblingEntitet nyKoblingEntitet, BeregningSteg steg) {
        var kopi = BeregningsgrunnlagGrunnlagBuilder.kopiere(grunnlag);
        beregningsgrunnlagRepository.lagre(nyKoblingEntitet.getId(), kopi, MapStegTilTilstand.mapTilStegTilstand(steg));
    }

    private void kopierAvklaringsbehov(KoblingEntitet nyKobling,
                                       KoblingEntitet eksisterendeKobling,
                                       BeregningSteg steg) {
        var avklaringsbehovSomMåKopieres = finnAvklaringsbehovSomSkalKopieres(eksisterendeKobling.getId(), steg);

        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehov(nyKobling.getId());
        // Kopierer alle fra eksisterende koblinger til kopi-koblinger
        avklaringsbehovSomMåKopieres.forEach(ab -> {
            avklaringsbehovTjeneste.kopierAvklaringsbehov(nyKobling, ab);
        });
    }

    private Set<AvklaringsbehovEntitet> finnAvklaringsbehovSomSkalKopieres(Long eksisterendeKoblingId, BeregningSteg steg) {
        return avklaringsbehovTjeneste.hentAlleAvklaringsbehovForKobling(eksisterendeKoblingId)
            .stream()
            .filter(ap -> ap.getStegFunnet().erFør(steg))
            .filter(ap -> ap.getStatus().equals(AvklaringsbehovStatus.UTFØRT))
            .collect(Collectors.toSet());
    }
}
