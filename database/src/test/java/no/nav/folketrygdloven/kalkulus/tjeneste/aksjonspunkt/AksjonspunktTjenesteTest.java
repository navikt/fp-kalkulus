package no.nav.folketrygdloven.kalkulus.tjeneste.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.aksjonspunkt.AksjonspunktEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.aksjonspunkt.AksjonspunktKontrollTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AksjonspunktStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.tjeneste.extensions.JpaExtension;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class AksjonspunktTjenesteTest extends EntityManagerAwareTest {
    private AksjonspunktRepository aksjonspunktRepository;
    private KoblingRepository koblingRepository;
    private AksjonspunktKontrollTjeneste aksjonspunktKontrollTjeneste = new AksjonspunktKontrollTjeneste();
    private KoblingEntitet kobling;
    private AksjonspunktTjeneste aksjonspunktTjeneste;

    @BeforeEach
    public void setup() {
        AktørId aktørId = new AktørId("1234123412341");
        KoblingReferanse koblingReferanse = new KoblingReferanse(UUID.randomUUID());
        Saksnummer saksnummer = new Saksnummer("1234");

        kobling = new KoblingEntitet(koblingReferanse, YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_NÆRSTÅENDE, saksnummer, aktørId);
        aksjonspunktRepository = new AksjonspunktRepository(getEntityManager());
        koblingRepository = new KoblingRepository(getEntityManager());
        koblingRepository.lagre(kobling);
        aksjonspunktTjeneste = new AksjonspunktTjeneste(aksjonspunktRepository, koblingRepository, aksjonspunktKontrollTjeneste, true);
    }

    @Test
    public void skal_opprette_lagre_og_hente_aksjonspunkt() {
        List<AksjonspunktDefinisjon> aksjonspunkter = Collections.singletonList(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);

        aksjonspunktTjeneste.lagreAksjonspunktresultater(kobling.getId(), aksjonspunkter);

        AksjonspunktEntitet lagretAksjonspunkt = aksjonspunktTjeneste.hentAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS)
                .orElse(null);

        assertThat(lagretAksjonspunkt).isNotNull();
        assertThat(lagretAksjonspunkt.getDefinisjon()).isEqualTo(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
        assertThat(lagretAksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);
        assertThat(lagretAksjonspunkt.getStegFunnet()).isEqualTo(BeregningSteg.FORS_BERGRUNN);
    }

    @Test
    public void skal_opprette_og_løse_aksjonspunkt() {
        List<AksjonspunktDefinisjon> aksjonspunkter = Collections.singletonList(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);

        // Opprett
        aksjonspunktTjeneste.lagreAksjonspunktresultater(kobling.getId(), aksjonspunkter);
        // Løs
        aksjonspunktTjeneste.løsAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, "Dette er en begrunnelse");

        AksjonspunktEntitet løstAksjonspunkt = aksjonspunktTjeneste.hentAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS)
                .orElse(null);
        assertThat(løstAksjonspunkt).isNotNull();
        assertThat(løstAksjonspunkt.getDefinisjon()).isEqualTo(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
        assertThat(løstAksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(løstAksjonspunkt.getStegFunnet()).isEqualTo(BeregningSteg.FORS_BERGRUNN);
        assertThat(løstAksjonspunkt.getBegrunnelse()).isEqualTo("Dette er en begrunnelse");
    }

    @Test
    public void skal_avbryte_aksjonspunkter_fra_og_med_oppgitt_aksjonspunkt_ved_tilbakerulling() {
        // Opprett
        aksjonspunktTjeneste.lagreAksjonspunktresultater(kobling.getId(), Collections.singletonList(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN));
        aksjonspunktTjeneste.lagreAksjonspunktresultater(kobling.getId(), Collections.singletonList(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS));
        aksjonspunktTjeneste.lagreAksjonspunktresultater(kobling.getId(), Collections.singletonList(AksjonspunktDefinisjon.FORDEL_BEREGNINGSGRUNNLAG));

        // Løs
        aksjonspunktTjeneste.løsAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN, "Dette er en begrunnelse");
        aksjonspunktTjeneste.løsAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, "Dette er en begrunnelse");
        aksjonspunktTjeneste.løsAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FORDEL_BEREGNINGSGRUNNLAG, "Dette er en begrunnelse");

        // Rull tilbake fra og med avvikaksjonspunkt
        aksjonspunktTjeneste.avbrytAlleAksjonspunktEtter(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);

        List<AksjonspunktEntitet> aksjonspunkter = aksjonspunktRepository.hentAksjonspunkterforKobling(kobling);

        assertThat(aksjonspunkter).hasSize(3);
        AksjonspunktEntitet faktaAP = aksjonspunkter.stream().filter(ap -> ap.getDefinisjon().equals(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN)).findFirst().orElse(null);
        assertThat(faktaAP).isNotNull();
        assertThat(faktaAP.getDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        assertThat(faktaAP.getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(faktaAP.getStegFunnet()).isEqualTo(BeregningSteg.KOFAKBER);

        AksjonspunktEntitet avvikAP = aksjonspunkter.stream().filter(ap -> ap.getDefinisjon().equals(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS)).findFirst().orElse(null);
        assertThat(avvikAP).isNotNull();
        assertThat(avvikAP.getDefinisjon()).isEqualTo(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
        assertThat(avvikAP.getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(avvikAP.getStegFunnet()).isEqualTo(BeregningSteg.FORS_BERGRUNN);

        AksjonspunktEntitet fordelAP = aksjonspunkter.stream().filter(ap -> ap.getDefinisjon().equals(AksjonspunktDefinisjon.FORDEL_BEREGNINGSGRUNNLAG)).findFirst().orElse(null);
        assertThat(fordelAP).isNotNull();
        assertThat(fordelAP.getDefinisjon()).isEqualTo(AksjonspunktDefinisjon.FORDEL_BEREGNINGSGRUNNLAG);
        assertThat(fordelAP.getStatus()).isEqualTo(AksjonspunktStatus.AVBRUTT);
        assertThat(fordelAP.getStegFunnet()).isEqualTo(BeregningSteg.FORDEL_BERGRUNN);
    }

    @Test
    public void skal_kaste_feil_om_vi_prøver_å_løse_aksjonspunkt_som_ikke_er_utledet() {
        assertThatThrownBy(() ->
                aksjonspunktTjeneste.løsAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, "Dette er en begrunnelse"))
                .isInstanceOf(TekniskException.class);
    }

    @Test
    public void skal_kaste_feil_hvis_vi_prøver_å_løse_allerede_løst_aksjonspunkt() {
        List<AksjonspunktDefinisjon> aksjonspunkter = Collections.singletonList(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);

        // Opprett
        aksjonspunktTjeneste.lagreAksjonspunktresultater(kobling.getId(), aksjonspunkter);
        // Løs
        aksjonspunktTjeneste.løsAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, "Dette er en begrunnelse");

        AksjonspunktEntitet løstAksjonspunkt = aksjonspunktTjeneste.hentAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS)
                .orElse(null);
        assertThat(løstAksjonspunkt).isNotNull();
        assertThat(løstAksjonspunkt.getDefinisjon()).isEqualTo(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
        assertThat(løstAksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.UTFØRT);
        assertThat(løstAksjonspunkt.getStegFunnet()).isEqualTo(BeregningSteg.FORS_BERGRUNN);
        assertThat(løstAksjonspunkt.getBegrunnelse()).isEqualTo("Dette er en begrunnelse");

        // Prøv å løse igjen
        assertThatThrownBy(() ->
                aksjonspunktTjeneste.løsAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, "Dette er en begrunnelse"))
                .isInstanceOf(TekniskException.class);

    }


}
