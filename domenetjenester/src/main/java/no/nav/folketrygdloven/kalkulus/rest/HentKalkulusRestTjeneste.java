package no.nav.folketrygdloven.kalkulus.rest;

import java.util.Optional;
import java.util.function.Function;

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

import org.slf4j.MDC;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.HentBeregningsgrunnlagGUIRequest;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.HentBeregningsgrunnlagRequestDto;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagGuiTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.KalkulatorGuiInterface;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.beregning.GUIBeregningsgrunnlagInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapDetaljertBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregningsgrunnlag"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class HentKalkulusRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private GUIBeregningsgrunnlagInputTjeneste guiInputTjeneste;
    private final KalkulatorGuiInterface dtoTjeneste = new BeregningsgrunnlagGuiTjeneste();

    public HentKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public HentKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                    BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                    GUIBeregningsgrunnlagInputTjeneste guiInputTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.guiInputTjeneste = guiInputTjeneste;
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent aktive BeregningsgrunnlagGrunnlag for angitte referanser", summary = ("Returnerer aktive BeregningsgrunnlagGrunnlag for angitte kobling referanser."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    @Path("/grunnlag")
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response hentAktiveBeregningsgrunnlagGrunnlag(@TilpassetAbacAttributt(supplierClass = HentBeregningsgrunnlagRequestAbacSupplier.class) @NotNull @Valid HentBeregningsgrunnlagRequestDto request) {
        var koblingReferanse = new KoblingReferanse(request.behandlingUuid());
        var kobling = koblingTjeneste.hentFor(koblingReferanse);
        var beregningsgrunnlagGrunnlagDto = kobling.flatMap(k -> beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(k.getId()))
            .map(MapDetaljertBeregningsgrunnlag::map);
        return beregningsgrunnlagGrunnlagDto.map(Response::ok).orElseGet(Response::noContent).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlagDto for angitt behandling som brukes frontend", summary = ("Returnerer beregningsgrunnlagDto for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    @Path("/grunnlag/gui")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBeregningsgrunnlagDtoListe(@TilpassetAbacAttributt(supplierClass = HentBeregningsgrunnlagGUIRequestAbacSupplier.class) @NotNull @Valid HentBeregningsgrunnlagGUIRequest request) {
        var koblingEntitet = koblingTjeneste.hentFor(new KoblingReferanse(request.behandlingUuid()));
        var guiInput = koblingEntitet.flatMap(k -> finnInputForGenereringAvDtoTilGUI(k, request.kalkulatorInput()));
        if (guiInput.isEmpty()) {
            return Response.noContent().build();
        }
        var guiDto = hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(guiInput.get());
        return Response.ok(guiDto).build();
    }

    private BeregningsgrunnlagDto hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(BeregningsgrunnlagGUIInput guiInput) {
        return mapTilDto(guiInput);
    }

    private Optional<BeregningsgrunnlagGUIInput> finnInputForGenereringAvDtoTilGUI(KoblingEntitet kobling, KalkulatorInputDto input) {
        return guiInputTjeneste.lagInputForKoblinger(kobling, Optional.empty(), input); // TODO tfp-5742 hent original kobling
    }

    private BeregningsgrunnlagDto mapTilDto(BeregningsgrunnlagGUIInput input) {
        MDC.put("prosess_koblingId", input.getKoblingReferanse().getKoblingId().toString());
        BeregningsgrunnlagDto beregningsgrunnlagDto = dtoTjeneste.lagBeregningsgrunnlagDto(input);
        MDC.remove("prosess_koblingId");
        return beregningsgrunnlagDto;
    }

    public static class HentBeregningsgrunnlagRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (HentBeregningsgrunnlagRequestDto) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }
    }

    public static class HentBeregningsgrunnlagGUIRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (HentBeregningsgrunnlagGUIRequest) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }
    }
}
