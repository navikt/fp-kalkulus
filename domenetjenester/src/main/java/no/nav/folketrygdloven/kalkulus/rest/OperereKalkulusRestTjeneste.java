package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import no.nav.folketrygdloven.kalkulus.beregning.BeregningTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.felles.verktøy.Transaction;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndtererApplikasjonTjeneste;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.StartBeregningRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@OpenAPIDefinition(tags = @Tag(name = "operere-kalkulus"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transaction
public class OperereKalkulusRestTjeneste {


    private KoblingTjeneste koblingTjeneste;
    private BeregningTjeneste beregningTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste;

    public OperereKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public OperereKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste, BeregningTjeneste beregningTjeneste, HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningTjeneste = beregningTjeneste;
        this.håndtererApplikasjonTjeneste = håndtererApplikasjonTjeneste;
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

        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        var aktørId = new AktørId(spesifikasjon.getAktør().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());

        KoblingEntitet koblingEntitet = koblingTjeneste.finnEllerOpprett(koblingReferanse, ytelseTyperKalkulusStøtter, aktørId, saksnummer);
//        kalkulatorInputTjeneste.lagInput(koblingEntitet.getId(), )


        TilstandResponse tilstandResponse = new TilstandResponse(Collections.emptyList());

        return Response.ok(tilstandResponse).build();
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
    public Response håndter(@NotNull @Valid HåndterBeregningRequest spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse);
        håndtererApplikasjonTjeneste.håndter(koblingId, spesifikasjon.getHåndterBeregning());
        TilstandResponse tilstandResponse = new TilstandResponse(Collections.emptyList());
        return Response.ok(tilstandResponse).build();
    }
}
