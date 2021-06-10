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

        aksjonspunktTjeneste.lagreAksjonspunktresultater(kobling.getId(), BeregningSteg.FORS_BERGRUNN, aksjonspunkter);

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
        aksjonspunktTjeneste.lagreAksjonspunktresultater(kobling.getId(), BeregningSteg.FORS_BERGRUNN, aksjonspunkter);
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
    public void skal_kaste_feil_om_vi_prøver_å_løse_aksjonspunkt_som_ikke_er_utledet() {
        assertThatThrownBy(() ->
                aksjonspunktTjeneste.løsAksjonspunkt(kobling.getId(), AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS, "Dette er en begrunnelse"))
                .isInstanceOf(TekniskException.class);
    }

    @Test
    public void skal_kaste_feil_hvis_vi_prøver_å_løse_allerede_løst_aksjonspunkt() {
        List<AksjonspunktDefinisjon> aksjonspunkter = Collections.singletonList(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);

        // Opprett
        aksjonspunktTjeneste.lagreAksjonspunktresultater(kobling.getId(), BeregningSteg.FORS_BERGRUNN, aksjonspunkter);
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
