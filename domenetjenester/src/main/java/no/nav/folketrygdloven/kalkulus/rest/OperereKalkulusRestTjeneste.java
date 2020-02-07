package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulus.felles.verktøy.Transaction;
import no.nav.folketrygdloven.kalkulus.request.v1.StartBeregningRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@OpenAPIDefinition(tags = @Tag(name = "operere-kalkulus"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transaction
public class OperereKalkulusRestTjeneste {


    public OperereKalkulusRestTjeneste() {
        // for CDI
    }



    @POST
    @Path("/start-beregn")
    @Operation(description = "Utfører bereninig basert på reqest", tags = "operere-kalkulus", responses = {
            @ApiResponse(description = "Liste med aksjonspunkter som har oppstått",
                    content = @Content(mediaType = "appliaction/json",
                            schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@NotNull @Valid StartBeregningRequest spesifikasjon) {
        TilstandResponse tilstandResponse = new TilstandResponse(Collections.emptyList());

        return Response.ok(tilstandResponse).build();
    }
}
