package no.nav.folketrygdloven.kalkulus.domene.sikkerhet;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.tjeneste.kobling.KoblingRepository;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;


/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@Dependent
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");
    private static final boolean LOCAL = Environment.current().isLocal();

    private final KoblingRepository koblingRepository;

    @Inject
    public PdpRequestBuilderImpl(KoblingRepository koblingRepository) {
        this.koblingRepository = koblingRepository;
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        Set<String> saksnumre = dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);
        Set<UUID> behandlinger = dataAttributter.getVerdier(StandardAbacAttributtType.BEHANDLING_UUID);
        setLogContext(saksnumre, behandlinger);

        // Tester kjører uten FPSAK, kun mot VTP
        if (LOCAL) {
            return minimalbuilder()
                .leggTilIdenter(identerFraFagsak(saksnumre))
                .leggTilIdenter(identerFraBehandlinger(behandlinger))
                .build();
        } else if (saksnumre.stream().findFirst().isPresent()) {
            return minimalbuilder().medSaksnummer(saksnumre.stream().findFirst().orElseThrow()).build();
        } else {
            // Bør ikke være nødvendig
            return minimalbuilder().leggTilIdenter(identerFraBehandlinger(behandlinger)).build();
        }
    }

    @Override
    public AppRessursData lagAppRessursDataForSystembruker(AbacDataAttributter dataAttributter) {
        Set<String> saksnumre = dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);
        Set<UUID> behandlinger = dataAttributter.getVerdier(StandardAbacAttributtType.BEHANDLING_UUID);
        setLogContext(saksnumre, behandlinger);
        return minimalbuilder().build();
    }

    private void setLogContext(Set<String> saksnumre, Set<UUID> behandlinger) {
        saksnumre.stream().findFirst().ifPresent(s -> LOG_CONTEXT.add("fagsak", s));
        behandlinger.stream().findFirst().map(UUID::toString).ifPresent(b -> LOG_CONTEXT.add("behandling", b));
    }


    private AppRessursData.Builder minimalbuilder() {
        return AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES);
    }

    private Set<String> identerFraFagsak(Set<String> saksnumre) {
        return saksnumre.stream()
            .map(Saksnummer::new)
            .map(koblingRepository::hentAlleKoblingerForSaksnummer)
            .flatMap(Collection::stream)
            .map(KoblingEntitet::getAktørId)
            .map(AktørId::getId)
            .collect(Collectors.toSet());
    }

    private Set<String> identerFraBehandlinger(Set<UUID> behandlinger) {
        return behandlinger.stream()
            .map(KoblingReferanse::new)
            .map(koblingRepository::hentForKoblingReferanse)
            .flatMap(Optional::stream)
            .map(KoblingEntitet::getAktørId)
            .map(AktørId::getId)
            .collect(Collectors.toSet());
    }
}
