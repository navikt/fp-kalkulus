package no.nav.folketrygdloven.kalkulus.app.sikkerhet;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.k9.felles.sikkerhet.abac.AbacAttributtSamling;
import no.nav.k9.felles.sikkerhet.abac.PdpKlient;
import no.nav.k9.felles.sikkerhet.abac.PdpRequest;
import no.nav.k9.felles.sikkerhet.abac.PdpRequestBuilder;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@Dependent
@Alternative
@Priority(2)
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(PdpRequestBuilderImpl.class);
    private String abacDomain;
    private KoblingTjeneste koblingTjeneste;

    public PdpRequestBuilderImpl() {
    }

    @Inject
    public PdpRequestBuilderImpl(@KonfigVerdi(value = "abac.domain") String abacDomain, KoblingTjeneste koblingTjeneste) {
        this.abacDomain = abacDomain;
        this.koblingTjeneste = koblingTjeneste;
    }

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(AbacAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_DOMENE, abacDomain);
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());

        if (attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID).isEmpty()) {
            Optional<AktørId> aktørId = utledAktørId(attributter);
            aktørId.map(AktørId::getAktørId).map(List::of).ifPresent(id -> pdpRequest.put(AbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, id));
        } else {
            pdpRequest.put(AbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID));
        }

        var aksjonspunktTyper = attributter.getVerdier(StandardAbacAttributtType.AKSJONSPUNKT_KODE).stream()
                .map(AvklaringsbehovDefinisjon::fraKode)
                .map(AvklaringsbehovDefinisjon::getAvklaringsbehovType)
                .map(AvklaringsbehovType::getNavn) // Antar her at AvklaringsbehovType stemmer overens med offisiellKode fra AksjonspunktType
                .collect(Collectors.toSet());

        if (!aksjonspunktTyper.isEmpty()) {
            LOG.info(aksjonspunktTyper.toString());
            pdpRequest.put(AbacAttributter.RESOURCE_K9_SAK_AKSJONSPUNKT_TYPE, aksjonspunktTyper);
        }


        // TODO: Gå over til å hente fra pip-tjenesten når alle kall inkluderer behandlinguuid?
        pdpRequest.put(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, AbacBehandlingStatus.UTREDES.getEksternKode());
        pdpRequest.put(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, AbacFagsakStatus.UNDER_BEHANDLING.getEksternKode());
        return pdpRequest;
    }

    private Optional<AktørId> utledAktørId(AbacAttributtSamling attributter) {
        Set<String> saksnummer = attributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);
        if (saksnummer.isEmpty()) {
            return Optional.empty();
        }
        return koblingTjeneste.hentAktørIdForSak(new Saksnummer(saksnummer.iterator().next()));
    }
}
