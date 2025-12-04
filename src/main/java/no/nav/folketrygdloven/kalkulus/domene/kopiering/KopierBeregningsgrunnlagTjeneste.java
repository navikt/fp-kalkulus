package no.nav.folketrygdloven.kalkulus.domene.kopiering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.beregning.MapStegTilTilstand;
import no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagBuilder;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.domene.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.avklaringsbehov.AvklaringsbehovTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class KopierBeregningsgrunnlagTjeneste {
    private static final Set<BeregningSteg> STØTTEDE_STARTPUNKT = Set.of(BeregningSteg.FORS_BERGRUNN);

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
     * Oppretter ny kobling og kopierer data angitt av steget fra eksisterende kobling.
     * Brukes når en ny referanse skal starte beregningen sin midt i prosessen.
     * Da skal siste grunnlag før angitt steg kopieres fra originalReferanse
     *
     * @param nyReferanse           Ny referanse som skal opprettes
     * @param nyReferanseSaksnummer Saksnummer for ny referanse (Brukes til å validere at vi ikke går på tvers av saksnummer)
     * @param steg                  Definerer steget som vi kopierer til. Siste grunnlag før dette steget skal kopieres,
     *                              fordi ny beregning skal kjøres fra angitt steg
     * @param originalReferanse     Referanse vi skal kopiere fra
     * @param input                 BeregningsgrunnlagInput, trengs i enkelte tilfeller
     */
    public void kopierBeregningsgrunlagForStartISteg(KoblingReferanse nyReferanse,
                                                     KoblingReferanse originalReferanse,
                                                     Saksnummer nyReferanseSaksnummer,
                                                     BeregningSteg steg,
                                                     KalkulatorInputDto input) {
        validerGyldigStartpunkt(steg, nyReferanse);
        var eksisterendeKobling = hentKoblingOgValiderTilstand(originalReferanse);
        validerSaksnummer(nyReferanseSaksnummer, eksisterendeKobling);
        var nyKobling = opprettNyKobling(eksisterendeKobling, nyReferanse);
        if (steg.equals(BeregningSteg.FORS_BERGRUNN)) {
            kopierForGregulering(input, eksisterendeKobling, nyKobling);
        }
    }

    /**
     * Oppretter ny kobling og kopierer fastsatt grunnlag fra eksisterende kobling.
     * Brukes når behandlingen skal hoppe over beregning og kun skal kopiere grunnlag fra forrige beregning.
     *
     * @param nyReferanse           Ny referanse som skal opprettes
     * @param nyReferanseSaksnummer Saksnummer for ny referanse (Brukes til å validere at vi ikke går på tvers av saksnummer)
     * @param originalReferanse     Referanse vi skal kopiere fra
     */
    public void kopierFastsattBeregningsgrunnlag(KoblingReferanse nyReferanse, KoblingReferanse originalReferanse, Saksnummer nyReferanseSaksnummer) {
        var eksisterendeKobling = hentKoblingOgValiderTilstand(originalReferanse);
        validerSaksnummer(nyReferanseSaksnummer, eksisterendeKobling);
        var nyKobling = opprettNyKobling(eksisterendeKobling, nyReferanse);
        var grunnlagSomSkalKopieres = validerOgHentGrunnlag(eksisterendeKobling, nyKobling.getSaksnummer(), BeregningsgrunnlagTilstand.FASTSATT);
        kopierBeregningsgrunnlag(grunnlagSomSkalKopieres, nyKobling, BeregningSteg.FAST_BERGRUNN);
        kopierAvklaringsbehov(nyKobling, eksisterendeKobling, BeregningSteg.FAST_BERGRUNN);
    }

    private void kopierForGregulering(KalkulatorInputDto input, KoblingEntitet eksisterendeKobling, KoblingEntitet nyKobling) {
        var grunnbeløpsbestemmendeDato = finnGrunnbeløpsbestemmendeDato(input);
        var grunnbeløp = beregningsgrunnlagRepository.finnGrunnbeløp(grunnbeløpsbestemmendeDato);
        Arrays.stream(BeregningsgrunnlagTilstand.values()).filter(s -> s.erFør(BeregningsgrunnlagTilstand.FORESLÅTT)).forEach(s -> {
            var grunnlagSomSkalKopieres = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKobling(
                eksisterendeKobling.getId(), s, null);
            grunnlagSomSkalKopieres.ifPresent(gr -> kopierBeregningsgrunnlagMedNyG(gr, nyKobling, s, BigDecimal.valueOf(grunnbeløp.getVerdi())));
        });
        kopierAvklaringsbehov(nyKobling, eksisterendeKobling, BeregningSteg.FORS_BERGRUNN);
    }


    private void validerSaksnummer(Saksnummer nyReferanseSaksnummer, KoblingEntitet eksisterendeKobling) {
        if (!nyReferanseSaksnummer.equals(eksisterendeKobling.getSaksnummer())) {
            throw new TekniskException("FT-47035", String.format(
                "Prøver å kopiere grunnlag fra en kobling uten matchende saksnummer. Saksnummer på ny kobling er %s mens saksnummer på eksisterende kobling var %S",
                nyReferanseSaksnummer, eksisterendeKobling.getSaksnummer()));
        }
    }

    private KoblingEntitet hentKoblingOgValiderTilstand(KoblingReferanse originalReferanse) {
        var kobling = koblingTjeneste.hentKoblingOptional(originalReferanse)
            .orElseThrow(() -> new TekniskException("FT-47037",
                String.format("Prøver å kopiere fra en referanse som ikke finnes. Kobling med referanse kobling %s finnes ikke.",
                    originalReferanse)));
        if (!kobling.getErAvsluttet()) {
            throw new TekniskException("FT-47038",
                String.format("Prøver å kopiere fra en referanse som ikke er avsluttet. Kobling med referanse kobling %s.", originalReferanse));
        }
        return kobling;
    }

    private void validerGyldigStartpunkt(BeregningSteg steg, KoblingReferanse nyReferanse) {
        if (!STØTTEDE_STARTPUNKT.contains(steg)) {
            throw new TekniskException("FT-47039", String.format("Ugyldig startpukt %s angitt for ny kobling %s.", steg, nyReferanse));
        }
    }

    private LocalDate finnGrunnbeløpsbestemmendeDato(KalkulatorInputDto input) {
        if (input.getYtelsespesifiktGrunnlag() instanceof ForeldrepengerGrunnlag fg) {
            return fg.getFørsteUttaksdato();
        }
        return input.getSkjæringstidspunkt();
    }

    private BeregningsgrunnlagGrunnlagEntitet validerOgHentGrunnlag(KoblingEntitet eksisterendeKobling,
                                                                    Saksnummer saksnummer,
                                                                    BeregningsgrunnlagTilstand tilstand) {
        if (!eksisterendeKobling.getSaksnummer().equals(saksnummer)) {
            throw new TekniskException("FT-47035", String.format(
                "Prøver å kopiere grunnlag fra en kobling uten matchende saksnummer. Saksnummer på ny kobling er %s mens saksnummer på eksisterende kobling var %S",
                saksnummer, eksisterendeKobling.getSaksnummer()));
        }
        return beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitetForKobling(eksisterendeKobling.getId(), tilstand, null)
            .orElseThrow(() -> new TekniskException("FT-47036", String.format(
                "Prøver å kopiere grunnlag fra en kobling uten et beregningsgrunnlag i forespurt tilstand. Prøve å kopiere fra kobling %s men fant ikke grunnlag i tilstand %s",
                eksisterendeKobling.getKoblingReferanse(), tilstand)));
    }

    private KoblingEntitet opprettNyKobling(KoblingEntitet eksisterendeKobling, KoblingReferanse nyReferanse) {
        return koblingTjeneste.finnEllerOpprett(nyReferanse, eksisterendeKobling.getYtelseType(), eksisterendeKobling.getAktørId(),
            eksisterendeKobling.getSaksnummer(), Optional.of(eksisterendeKobling.getKoblingReferanse()));
    }

    private void kopierBeregningsgrunnlag(BeregningsgrunnlagGrunnlagEntitet grunnlag, KoblingEntitet nyKoblingEntitet, BeregningSteg steg) {
        var kopi = BeregningsgrunnlagGrunnlagBuilder.kopiere(grunnlag);
        beregningsgrunnlagRepository.lagre(nyKoblingEntitet.getId(), kopi, MapStegTilTilstand.mapTilStegTilstand(steg));
    }

    private void kopierBeregningsgrunnlagMedNyG(BeregningsgrunnlagGrunnlagEntitet grunnlag,
                                                KoblingEntitet nyKoblingEntitet,
                                                BeregningsgrunnlagTilstand tilstand,
                                                BigDecimal nyttGrunnbeløp) {
        var kopi = BeregningsgrunnlagGrunnlagBuilder.kopiere(grunnlag);
        var gammeltGrunnbeløp = grunnlag.getBeregningsgrunnlag().map(BeregningsgrunnlagEntitet::getGrunnbeløp).orElse(null);
        var skalGRegulere = gammeltGrunnbeløp != null && nyttGrunnbeløp.compareTo(gammeltGrunnbeløp.getVerdi()) > 0;
        if (skalGRegulere) {
            grunnlag.getBeregningsgrunnlag()
                .ifPresent(bg -> kopi.medBeregningsgrunnlag(BeregningsgrunnlagEntitet.kopiere(bg).medGrunnbeløp(nyttGrunnbeløp).build()));
        }
        beregningsgrunnlagRepository.lagre(nyKoblingEntitet.getId(), kopi, tilstand);
    }

    private void kopierAvklaringsbehov(KoblingEntitet nyKobling, KoblingEntitet eksisterendeKobling, BeregningSteg steg) {
        var avklaringsbehovSomMåKopieres = finnAvklaringsbehovSomSkalKopieres(eksisterendeKobling.getId(), steg);

        avklaringsbehovTjeneste.avbrytAlleAvklaringsbehov(nyKobling.getId());
        // Kopierer alle fra eksisterende koblinger til kopi-koblinger
        avklaringsbehovSomMåKopieres.forEach(ab -> avklaringsbehovTjeneste.kopierAvklaringsbehov(nyKobling, ab));
    }

    private Set<AvklaringsbehovEntitet> finnAvklaringsbehovSomSkalKopieres(Long eksisterendeKoblingId, BeregningSteg steg) {
        return avklaringsbehovTjeneste.hentAlleAvklaringsbehovForKobling(eksisterendeKoblingId)
            .stream()
            .filter(ap -> ap.getStegFunnet().erFør(steg))
            .filter(ap -> ap.getStatus().equals(AvklaringsbehovStatus.UTFØRT))
            .collect(Collectors.toSet());
    }
}
