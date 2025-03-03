package no.nav.folketrygdloven.kalkulus.rest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;
import no.nav.folketrygdloven.kalkulus.forvaltning.GrunnbeløpreguleringTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.GrunnbeløpReguleringStatus;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapBeregningSats;
import no.nav.folketrygdloven.kalkulus.request.v1.HentGrunnbeløpRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.KontrollerGrunnbeløpRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.GrunnbeløpReguleringRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "grunnbelop"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class GrunnbeløpRestTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private GrunnbeløpreguleringTjeneste grunnbeløpreguleringTjeneste;

    public GrunnbeløpRestTjeneste() {
        // for CDI
    }

    @Inject
    public GrunnbeløpRestTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                  GrunnbeløpreguleringTjeneste grunnbeløpreguleringTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.grunnbeløpreguleringTjeneste = grunnbeløpreguleringTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent grunnbeløp for angitt dato", summary = ("Returnerer grunnbeløp for dato."), tags = "grunnbelop")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    @Path("/grunnbelop")
    public Response hentGrunnbeløp(@NotNull @Valid HentGrunnbeløpRequestAbacDto spesifikasjon) {
        BeregningSats grunnbeløp = beregningsgrunnlagRepository.finnGrunnbeløp(spesifikasjon.getDato());
        final Response response = Response.ok(MapBeregningSats.map(grunnbeløp)).build();
        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Verifiserer grunnbeløpet på et grunnlag, og kontrollerer at grunnbeløpet som er brukt fortsatt er korrekt.",
            summary = ("Returnerer en liste over koblinger og status for gregulering for hver kobling."), tags = "grunnbelop")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    @Path("/kontrollerGregulering")
    public Response hentGrunnbeløp(@NotNull @Valid KontrollerGrunnbeløpRequestAbacDto spesifikasjon) {
        List<UUID> referanser = spesifikasjon.getKoblinger();
        Map<UUID, GrunnbeløpReguleringStatus> resultat = new HashMap<>();

        for (UUID ref : referanser) {
            if (ref != null) {
                GrunnbeløpReguleringStatus status = grunnbeløpreguleringTjeneste.undersøkBehovForGregulering(new KoblingReferanse(ref), spesifikasjon.getSaksnummer().verdi());
                resultat.put(ref, status);
            }
        }
        GrunnbeløpReguleringRespons respons = new GrunnbeløpReguleringRespons(resultat);
        return Response.ok(respons).build();
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentGrunnbeløpRequestAbacDto extends HentGrunnbeløpRequest implements AbacDto {


        @JsonCreator
        public HentGrunnbeløpRequestAbacDto(@JsonProperty(value = "dato", required = true) @Valid @NotNull LocalDate dato) {
            super(dato);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            return abacDataAttributter;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class KontrollerGrunnbeløpRequestAbacDto extends KontrollerGrunnbeløpRequest implements AbacDto {


        @JsonCreator
        public KontrollerGrunnbeløpRequestAbacDto(@JsonProperty(value = "koblinger", required = true) @Valid @NotNull @Size(min = 1) List<UUID> koblinger,
                                                  @JsonProperty(value = "saksnummer", required = true) @NotNull @Valid Saksnummer saksnummer) {
            super(koblinger, saksnummer);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter;
        }
    }

}
