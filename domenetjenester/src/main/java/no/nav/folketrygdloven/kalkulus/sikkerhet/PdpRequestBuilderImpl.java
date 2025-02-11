package no.nav.folketrygdloven.kalkulus.sikkerhet;

import static no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys.BEHANDLING_STATUS;
import static no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys.FAGSAK_STATUS;

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
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
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

    private final KoblingRepository koblingRepository;

    @Inject
    public PdpRequestBuilderImpl(KoblingRepository koblingRepository) {
        this.koblingRepository = koblingRepository;
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        Set<String> saksnumre = dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);
        saksnumre.stream().findFirst().ifPresent(s -> LOG_CONTEXT.add("fagsak", s));
        Set<UUID> behandlinger = dataAttributter.getVerdier(StandardAbacAttributtType.BEHANDLING_UUID);
        behandlinger.stream().findFirst().ifPresent(b -> LOG_CONTEXT.add("behandling", b));

        var aktørerFraBehandlinger = behandlinger.stream()
            .map(KoblingReferanse::new)
            .map(koblingRepository::hentForKoblingReferanse)
            .flatMap(Optional::stream)
            .map(KoblingEntitet::getAktørId)
            .map(AktørId::getId)
            .collect(Collectors.toSet());

        var aktørerFraSaker = saksnumre.stream()
            .map(Saksnummer::new)
            .map(koblingRepository::hentAlleKoblingerForSaksnummer)
            .flatMap(Collection::stream)
            .map(KoblingEntitet::getAktørId)
            .map(AktørId::getId)
            .collect(Collectors.toSet());

        return AppRessursData.builder()
            .leggTilAktørIdSet(aktørerFraBehandlinger)
            .leggTilAktørIdSet(aktørerFraSaker)
            .leggTilAktørIdSet(dataAttributter.getVerdier(StandardAbacAttributtType.AKTØR_ID)) // Attributt ikke i bruk men for ordens skyld
            .leggTilFødselsnumre(dataAttributter.getVerdier(StandardAbacAttributtType.FNR)) // Attributt ikke i bruk men for ordens skyld
            // TODO: Hente fra pip-tjenesten? arv fra tidligere... men nå er 2 pips aktuelle ....
            .leggTilRessurs(FAGSAK_STATUS, PipFagsakStatus.UNDER_BEHANDLING)
            .leggTilRessurs(BEHANDLING_STATUS, PipBehandlingStatus.UTREDES)
            .build();
    }
}
