package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.DRIFT;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.forvaltning.AksjonspunktMigreringTjeneste;
import no.nav.folketrygdloven.kalkulus.request.v1.regelinput.KomprimerRegelInputRequest;
import no.nav.folketrygdloven.kalkulus.tjeneste.kobling.KoblingRepository;
import no.nav.folketrygdloven.kalkulus.tjeneste.sporing.RegelsporingRepository;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "regelinputForvaltning"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class KomprimerJsonInputRestTjeneste {


    private RegelsporingRepository regelsporingRepository;
    private KoblingRepository koblingRepository;

    public KomprimerJsonInputRestTjeneste() {
        // for CDI
    }

    @Inject
    public KomprimerJsonInputRestTjeneste(RegelsporingRepository regelsporingRepository, KoblingRepository koblingRepository) {
        this.regelsporingRepository = regelsporingRepository;
        this.koblingRepository = koblingRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("komprimerRegelSporing")
    @Operation(description = "Komprimerer alle perioderegelsporinger for sak", tags = "regelinputForvaltning", summary = ("Komprimerer regelsporinger"))
    @BeskyttetRessurs(action = CREATE, property = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response komprimerRegelsporing(@NotNull @Valid KomprimerRegelInputRequestAbacDto spesifikasjon) {
        if (!KonfigurasjonVerdi.get("REKURSIV_TASK_KOMPRIMERING_ENABLED", true)) {
            return Response.noContent().build();
        }
        var koblinger = koblingRepository.hentAlleKoblingerForSaksnummer(new Saksnummer(spesifikasjon.getSaksnummer()));
        koblinger.forEach(k -> regelsporingRepository.hashAlleForKobling(k.getId()));
        var nesteKoblingId = regelsporingRepository.finnKoblingUtenKomprimering();
        if (nesteKoblingId.isEmpty()) {
            return Response.noContent().build();
        }
        var kobling = koblingRepository.hentKoblingerFor(List.of(nesteKoblingId.get()));
        if (kobling.isEmpty()) {
            return Response.noContent().build();
        } else {
            return Response.ok(new no.nav.folketrygdloven.kalkulus.response.v1.regelinput.Saksnummer(kobling.get(0).getSaksnummer().getVerdi())).build();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class KomprimerRegelInputRequestAbacDto extends KomprimerRegelInputRequest implements AbacDto {

        public KomprimerRegelInputRequestAbacDto() {
            // Jackson
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            return abacDataAttributter;
        }
    }


}
