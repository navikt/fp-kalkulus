package no.nav.folketrygdloven.kalkulus.rest.forvaltning;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.BEREGNINGSGRUNNLAG;
import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributtMiljøvariabel.DRIFT;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.forvaltning.ResetGrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kopiering.KopierBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulus.request.v1.KopierBeregningRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.KopierOgResettBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.ResettBeregningRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.KopiResponse;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;


@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "reset"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class KopierOgResetRestTjeneste {

    private KopierBeregningsgrunnlagTjeneste kopierTjeneste;
    private ResetGrunnlagTjeneste resetGrunnlagTjeneste;

    public KopierOgResetRestTjeneste() {
        // for CDI
    }

    @Inject
    public KopierOgResetRestTjeneste(KopierBeregningsgrunnlagTjeneste kopierTjeneste, ResetGrunnlagTjeneste resetGrunnlagTjeneste) {
        this.kopierTjeneste = kopierTjeneste;
        this.resetGrunnlagTjeneste = resetGrunnlagTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/kopierOgResett/bolk")
    @Operation(description = "Kopierer beregning fra eksisterende referanse til ny referanse og resetter original til forrige fastsatte.",
            tags = "beregn",
            summary = ("Kopierer en beregning."), responses = {
            @ApiResponse(description = "Liste med kopierte referanser dersom alle koblinger er kopiert", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = KopiResponse.class)))
    })
    @BeskyttetRessurs(action = UPDATE, property = BEREGNINGSGRUNNLAG)
    public Response kopierOgResett(@NotNull @Valid KopierOgResettBeregningListeRequestAbacDto spesifikasjon) {
        MDC.put("prosess_saksnummer", spesifikasjon.getSaksnummer().verdi());
        kopierTjeneste.kopierGrunnlagOgOpprettKoblinger(
                spesifikasjon.getKopierBeregningListe(),
                spesifikasjon.getYtelseSomSkalBeregnes(),
                new Saksnummer(spesifikasjon.getSaksnummer().verdi()),
                spesifikasjon.getStegType() == null ? BeregningSteg.FAST_BERGRUNN : spesifikasjon.getStegType(),
                spesifikasjon.getBehandlingAvsluttetTid());
        resetGrunnlagTjeneste.resetGrunnlag(spesifikasjon.getKopierBeregningListe().stream().map(KopierBeregningRequest::getKopierFraReferanse).toList(), spesifikasjon.getOriginalBehandlingAvsluttetTid());
        return Response.ok(spesifikasjon.getKopierBeregningListe()
                .stream()
                .map(KopierBeregningRequest::getEksternReferanse)
                .map(KopiResponse::new).collect(Collectors.toList())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/resett")
    @Operation(description = "Resetter original til forrige fastsatte.",
            tags = "beregn",
            summary = ("Resetter et grunnlag."))
    @BeskyttetRessurs(action = CREATE, property = DRIFT)
    public Response resett(@NotNull @Valid ResettBeregningRequestAbacDto spesifikasjon) {
        resetGrunnlagTjeneste.resetGrunnlag(List.of(spesifikasjon.getEksternReferanse()), spesifikasjon.getBehandlingAvsluttetTid());
        return Response.ok().build();
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class KopierOgResettBeregningListeRequestAbacDto extends KopierOgResettBeregningListeRequest implements AbacDto {

        public KopierOgResettBeregningListeRequestAbacDto() {
        }

        public KopierOgResettBeregningListeRequestAbacDto(no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer saksnummer,
                                                          UUID behandlingUuid,
                                                          FagsakYtelseType ytelseSomSkalBeregnes,
                                                          List<KopierBeregningRequest> kopierBeregningListe, BeregningSteg stegType,
                                                          LocalDateTime originalBehandlingAvsluttetTid,
                                                          LocalDateTime behandlingAvsluttetTid) {
            super(saksnummer, behandlingUuid, ytelseSomSkalBeregnes, stegType, kopierBeregningListe, originalBehandlingAvsluttetTid, behandlingAvsluttetTid);
        }


        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            if (getBehandlingUuid() != null) {
                abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            }
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class ResettBeregningRequestAbacDto extends ResettBeregningRequest implements AbacDto {

        public ResettBeregningRequestAbacDto() {
        }

        public ResettBeregningRequestAbacDto(UUID eksternReferanse,
                                             LocalDateTime behandlingAvsluttetTid) {
            super(eksternReferanse, behandlingAvsluttetTid);
        }


        @Override
        public AbacDataAttributter abacAttributter() {
            return new AbacDataAttributter();
        }
    }


}
