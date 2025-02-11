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
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagGuiTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.KalkulatorGuiInterface;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.beregning.GUIBeregningsgrunnlagInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapDetaljertBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.request.v1.enkel.EnkelFpkalkulusRequestDto;
import no.nav.folketrygdloven.kalkulus.request.v1.enkel.EnkelHentBeregningsgrunnlagGUIRequest;
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
    public Response hentAktiveBeregningsgrunnlagGrunnlag(@TilpassetAbacAttributt(supplierClass = EnkelFpkalkulusRequestAbacSupplier.class) @NotNull @Valid EnkelFpkalkulusRequestDto request) {
        var koblingReferanse = new KoblingReferanse(request.behandlingUuid());
        var kobling = koblingTjeneste.hentKoblingOptional(koblingReferanse);
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
    public Response hentBeregningsgrunnlagDto(@TilpassetAbacAttributt(supplierClass = HentBeregningsgrunnlagGUIRequestAbacSupplier.class) @NotNull @Valid EnkelHentBeregningsgrunnlagGUIRequest request) {
        var koblingEntitet = koblingTjeneste.hentKoblingOptional(new KoblingReferanse(request.behandlingUuid()));
        var guiInput = koblingEntitet.flatMap(k -> finnInputForGenereringAvDtoTilGUI(k, request.kalkulatorInput()));
        if (guiInput.isEmpty()) {
            return Response.noContent().build();
        }
        var guiDto = hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(guiInput.get());
        return Response.ok(guiDto).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beteberegningsgrunnlag for angitt referanse", summary = ("Returnerer aktive besteberegningsgrunnlag for angitt kobling referanse."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    @Path("/grunnlag/besteberegning")
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response hentBesteberegnetGrunnlag(@TilpassetAbacAttributt(supplierClass = EnkelFpkalkulusRequestAbacSupplier.class) @NotNull @Valid EnkelFpkalkulusRequestDto request) {
        var koblingReferanse = new KoblingReferanse(request.behandlingUuid());
        var kobling = koblingTjeneste.hentKoblingOptional(koblingReferanse);
        var besteberegnetGrunnlagDto = kobling.flatMap(k -> beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(k.getId()))
            .flatMap(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag)
            .flatMap(BeregningsgrunnlagEntitet::getBesteberegninggrunnlag)
            .map(MapDetaljertBeregningsgrunnlag::mapBesteberegningsgrunlag);
        return besteberegnetGrunnlagDto.map(Response::ok).orElseGet(Response::noContent).build();
    }

    private BeregningsgrunnlagDto hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(BeregningsgrunnlagGUIInput guiInput) {
        return mapTilDto(guiInput);
    }

    private Optional<BeregningsgrunnlagGUIInput> finnInputForGenereringAvDtoTilGUI(KoblingEntitet kobling, KalkulatorInputDto input) {
        var originalKoblingEntitet = kobling.getOriginalKoblingReferanse().map(kr -> koblingTjeneste.hentKobling(kr));
        return guiInputTjeneste.lagInputForKoblinger(kobling, originalKoblingEntitet, input);
    }

    private BeregningsgrunnlagDto mapTilDto(BeregningsgrunnlagGUIInput input) {
        MDC.put("prosess_koblingId", input.getKoblingReferanse().getKoblingId().toString());
        BeregningsgrunnlagDto beregningsgrunnlagDto = dtoTjeneste.lagBeregningsgrunnlagDto(input);
        MDC.remove("prosess_koblingId");
        return beregningsgrunnlagDto;
    }

    public static class HentBeregningsgrunnlagGUIRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (EnkelHentBeregningsgrunnlagGUIRequest) o;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid())
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.saksnummer().verdi());
        }
    }

    public static class EnkelFpkalkulusRequestAbacSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (EnkelFpkalkulusRequestDto) o;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid())
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.saksnummer().verdi());
        }
    }

}
