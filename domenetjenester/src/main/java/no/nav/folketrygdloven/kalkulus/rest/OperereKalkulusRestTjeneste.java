package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.beregning.BeregningStegTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.YtelseTyperKalkulusStøtter;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndtererApplikasjonTjeneste;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.FortsettBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.StartBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandListeResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringListeRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringPrRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;
import no.nav.folketrygdloven.kalkulus.rest.abac.FortsettBeregningRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.HentBeregningsgrunnlagRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.HåndterBeregningListeRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.HåndterBeregningRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.rest.abac.StartBeregningRequestAbacDto;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregn"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class OperereKalkulusRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningStegTjeneste beregningStegTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste;
    private RullTilbakeTjeneste rullTilbakeTjeneste;

    public OperereKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public OperereKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                       BeregningStegTjeneste beregningStegTjeneste,
                                       KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                       HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste,
                                       RullTilbakeTjeneste rullTilbakeTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningStegTjeneste = beregningStegTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.håndtererApplikasjonTjeneste = håndtererApplikasjonTjeneste;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
    }

    /**
     * @deprecated bytt til {@link #beregn(StartBeregningListeRequestAbacDto)}
     */
    @Deprecated(forRemoval = true, since = "1.0")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/start")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Starter en beregning basert på gitt input."), responses = {
            @ApiResponse(description = "Liste med aksjonspunkter som har oppstått", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = CREATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@NotNull @Valid StartBeregningRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getEksternReferanse().getReferanse());
        var aktørId = new AktørId(spesifikasjon.getAktør().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());
        MDC.put("prosess_koblingreferanse", koblingReferanse.getReferanse().toString());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());

        KoblingEntitet koblingEntitet = koblingTjeneste.finnEllerOpprett(koblingReferanse, ytelseTyperKalkulusStøtter, aktørId, saksnummer);

        boolean inputHarEndretSeg = kalkulatorInputTjeneste.lagreKalkulatorInput(koblingEntitet.getId(), spesifikasjon.getKalkulatorInput());

        BeregningsgrunnlagInput input = kalkulatorInputTjeneste.lagInput(koblingEntitet.getId(), Optional.empty());

        if (inputHarEndretSeg) {
            rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingEntitet.getId(), BeregningsgrunnlagTilstand.OPPRETTET);
        }

        TilstandResponse tilstandResponse = beregningStegTjeneste.fastsettBeregningsaktiviteter(input);

        return Response.ok(tilstandResponse).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/start/bolk")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Starter en beregning basert på gitt input."), responses = {
            @ApiResponse(description = "Liste med aksjonspunkter som har oppstått per angitt eksternReferanse", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = CREATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@NotNull @Valid StartBeregningListeRequestAbacDto spesifikasjon) {
        var aktørId = new AktørId(spesifikasjon.getAktør().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());

        List<TilstandResponse> resultat = new ArrayList<>();

        for (var entry : spesifikasjon.getKalkulatorInputPerKoblingReferanse().entrySet()) {
            var eksternReferanse = entry.getKey();
            var kalkulatorInput = entry.getValue();
            var koblingReferanse = new KoblingReferanse(eksternReferanse);
            MDC.put("prosess_koblingreferanse", eksternReferanse.toString());

            var koblingEntitet = koblingTjeneste.finnEllerOpprett(koblingReferanse, ytelseTyperKalkulusStøtter, aktørId, saksnummer);

            boolean inputHarEndretSeg = kalkulatorInputTjeneste.lagreKalkulatorInput(koblingEntitet.getId(), kalkulatorInput);

            var input = kalkulatorInputTjeneste.lagInput(koblingEntitet.getId(), Optional.empty());

            if (inputHarEndretSeg) {
                rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingEntitet.getId(), BeregningsgrunnlagTilstand.OPPRETTET);
            }

            var tilstandResponse = beregningStegTjeneste.fastsettBeregningsaktiviteter(input);
            resultat.add(tilstandResponse);
        }

        return Response.ok(new TilstandListeResponse(resultat)).build();
    }

    /**
     * @deprecated bytt til {@link #beregnVidere(FortsettBeregningListeRequestAbacDto)}
     */
    @Deprecated(forRemoval = true, since = "1.0")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/fortsett")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Fortsetter en beregning basert på stegInput."), responses = {
            @ApiResponse(description = "Liste med aksjonspunkter som har oppstått", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    public Response beregnVidere(@NotNull @Valid FortsettBeregningRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getEksternReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        KoblingEntitet koblingEntitet = koblingTjeneste.hentFor(koblingReferanse, ytelseTyperKalkulusStøtter);
        MDC.put("prosess_saksnummer", koblingEntitet.getSaksnummer().getVerdi());
        MDC.put("prosess_koblingreferanse", koblingReferanse.getReferanse().toString());
        BeregningsgrunnlagInput input = kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(koblingEntitet.getId());
        TilstandResponse tilstandResponse = beregningStegTjeneste.beregnFor(spesifikasjon.getStegType(), input, koblingEntitet.getId());

        return Response.ok(tilstandResponse).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/fortsett/bolk")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Fortsetter en beregning basert på stegInput."), responses = {
            @ApiResponse(description = "Liste med aksjonspunkter som har oppstått", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    public Response beregnVidere(@NotNull @Valid FortsettBeregningListeRequestAbacDto spesifikasjon) {
        String saksnummer = spesifikasjon.getSaksnummer();
        MDC.put("prosess_saksnummer", saksnummer);
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());

        List<TilstandResponse> resultat = new ArrayList<>();

        for (var eksternReferanse : spesifikasjon.getEksternReferanser()) {
            MDC.put("prosess_koblingreferanse", eksternReferanse.toString());
            var koblingReferanse = new KoblingReferanse(eksternReferanse);
            var kobling = koblingTjeneste.hentFor(koblingReferanse, ytelseTyperKalkulusStøtter);
            if(!Objects.equals(kobling.getSaksnummer().getVerdi(), saksnummer)){
                throw new IllegalArgumentException("Kobling tilhører ikke saksnummer [" +saksnummer+"]: " + kobling);
            }
            
            var input = kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(kobling.getId());
            var tilstandResponse = beregningStegTjeneste.beregnFor(spesifikasjon.getStegType(), input, kobling.getId());

            resultat.add(tilstandResponse);
        }

        return Response.ok(new TilstandListeResponse(resultat)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/oppdaterListe")
    @Operation(description = "Oppdaterer beregningsgrunnlag for oppgitt liste", tags = "beregn", summary = ("Oppdaterer beregningsgrunnlag basert på løsning av aksjonspunkt for oppgitt liste."), responses = {
            @ApiResponse(description = "Liste med endringer som ble gjort under oppdatering", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OppdateringListeRespons.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response oppdaterListe(@NotNull @Valid HåndterBeregningListeRequestAbacDto spesifikasjon) {
        List<OppdateringPrRequest> oppdateringer = spesifikasjon.getHåndterBeregningListe().stream()
                .map(request -> {
                    var oppdatering = håndterForKobling(request);
                    return new OppdateringPrRequest(oppdatering, request.getEksternReferanse());
                })
                .collect(Collectors.toList());

        return Response.ok(Objects.requireNonNullElseGet(new OppdateringListeRespons(oppdateringer), OppdateringRespons::TOM_RESPONS)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/oppdater")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Oppdaterer beregningsgrunnlag basert på løsning av aksjonspunkt."), responses = {
            @ApiResponse(description = "Endringer som ble gjort under oppdatering", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OppdateringRespons.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response håndter(@NotNull @Valid HåndterBeregningRequestAbacDto spesifikasjon) {
        OppdateringRespons respons = håndterForKobling(spesifikasjon);
        return Response.ok(Objects.requireNonNullElseGet(respons, OppdateringRespons::TOM_RESPONS)).build();
    }

    /**
     * @deprecated bytt til {@link #deaktiver(DeaktiverBeregningsgrunnlagRequestAbacDto)}
     */
    @Deprecated(forRemoval = true, since = "1.0")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deaktiver")
    @Operation(description = "Deaktiverer aktivt beregningsgrunnlag. Nullstiller beregning.", tags = "deaktiver", summary = ("Deaktiverer aktivt beregningsgrunnlag."))
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response deaktiverBeregningsgrunnlag(@NotNull @Valid HentBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse);
        rullTilbakeTjeneste.deaktiverAktivtBeregningsgrunnlagOgInput(koblingId);
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deaktiver/bolk")
    @Operation(description = "Deaktiverer aktivt beregningsgrunnlag. Nullstiller beregning.", tags = "deaktiver", summary = ("Deaktiverer aktivt beregningsgrunnlag."))
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response deaktiverBeregningsgrunnlag(@NotNull @Valid DeaktiverBeregningsgrunnlagRequestAbacDto spesifikasjon) {
        String saksnummer = spesifikasjon.getSaksnummer();
        MDC.put("prosess_saksnummer", saksnummer);
        for (var k : spesifikasjon.getRequestPrReferanse()) {
            var koblingReferanse = new KoblingReferanse(k.getKoblingReferanse());
            MDC.put("prosess_koblingreferanse", k.toString());
            var kopt = koblingTjeneste.hentFor(koblingReferanse);

            if (kopt.isEmpty()) {
                throw new IllegalArgumentException("Kan ikke finne kobling: " + k.getKoblingReferanse());
            }
            KoblingEntitet kobling = kopt.get();
            if(!Objects.equals(kobling.getSaksnummer().getVerdi(), saksnummer)){
                throw new IllegalArgumentException("Kobling tilhører ikke saksnummer [" +saksnummer+"]: " + kobling);
            }
            rullTilbakeTjeneste.deaktiverAktivtBeregningsgrunnlagOgInput(kobling.getId());
        }

        return Response.ok().build();
    }

    private OppdateringRespons håndterForKobling(@NotNull @Valid HåndterBeregningRequest spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getEksternReferanse());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse);
        koblingTjeneste.hentFor(koblingReferanse).map(KoblingEntitet::getSaksnummer)
            .ifPresent(saksnummer -> MDC.put("prosess_saksnummer", saksnummer.getVerdi()));
        return håndtererApplikasjonTjeneste.håndter(koblingId, spesifikasjon.getHåndterBeregning());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class StartBeregningListeRequestAbacDto extends StartBeregningListeRequest implements AbacDto {

        public StartBeregningListeRequestAbacDto() {
        }
        
        public StartBeregningListeRequestAbacDto(Map<UUID, KalkulatorInputDto> kalkulatorInput, String saksnummer, PersonIdent aktør, YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(kalkulatorInput, saksnummer, aktør, ytelseSomSkalBeregnes);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            var abacDataAttributter = AbacDataAttributter.opprett();

            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKalkulatorInputPerKoblingReferanse().keySet());
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getAktør().getIdent());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class FortsettBeregningListeRequestAbacDto extends FortsettBeregningListeRequest implements AbacDto {

        public FortsettBeregningListeRequestAbacDto() {
        }

        public FortsettBeregningListeRequestAbacDto(String saksnummer, List<UUID> eksternReferanser, StegType stegType, YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(saksnummer, eksternReferanser, ytelseSomSkalBeregnes, stegType);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getEksternReferanser());
            return abacDataAttributter;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class DeaktiverBeregningsgrunnlagRequestAbacDto extends BeregningsgrunnlagListeRequest implements no.nav.vedtak.sikkerhet.abac.AbacDto {

        public DeaktiverBeregningsgrunnlagRequestAbacDto() {
        }

        public DeaktiverBeregningsgrunnlagRequestAbacDto(String saksnummer, List<BeregningsgrunnlagRequest> requestPrReferanse) {
            super(saksnummer, requestPrReferanse);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            var abacDataAttributter = AbacDataAttributter.opprett();
            List<UUID> eksternReferanser = getRequestPrReferanse().stream().map(BeregningsgrunnlagRequest::getKoblingReferanse).collect(Collectors.toList());
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, eksternReferanser);
            return abacDataAttributter;
        }
    }
}
