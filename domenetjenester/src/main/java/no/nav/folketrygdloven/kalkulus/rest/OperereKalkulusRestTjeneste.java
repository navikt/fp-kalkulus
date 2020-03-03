package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Collections;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulus.UuidDto;
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
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.FortsettBeregningRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.StartBeregningRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregn"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transaction
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/start")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn",
            summary = ("Starter en beregning basert på gitt input."),
            responses = {@ApiResponse(description = "Liste med aksjonspunkter som har oppstått",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@NotNull @Valid StartBeregningRequestAbacDto spesifikasjon) {

        var koblingReferanse = new KoblingReferanse(spesifikasjon.getEksternReferanse().getReferanse());
        var aktørId = new AktørId(spesifikasjon.getAktør().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());

        KoblingEntitet koblingEntitet = koblingTjeneste.finnEllerOpprett(koblingReferanse, ytelseTyperKalkulusStøtter, aktørId, saksnummer);

        boolean inputHarEndretSeg = kalkulatorInputTjeneste.lagreKalkulatorInput(koblingEntitet.getId(), spesifikasjon.getKalkulatorInput());

        BeregningsgrunnlagInput input = kalkulatorInputTjeneste.lagInput(koblingEntitet.getId());

        if (inputHarEndretSeg) {
            rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingEntitet.getId(), BeregningsgrunnlagTilstand.OPPRETTET);
        }

        TilstandResponse tilstandResponse = beregningStegTjeneste.fastsettBeregningsaktiviteter(input);
        return Response.ok(tilstandResponse).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/fortsett")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn",
            summary = ("Fortsetter en beregning basert på stegInput."),
            responses = {@ApiResponse(description = "Liste med aksjonspunkter som har oppstått",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response beregnVidere(@NotNull @Valid FortsettBeregningRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getEksternReferanse());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtter.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());

        KoblingEntitet koblingEntitet = koblingTjeneste.hentFor(koblingReferanse, ytelseTyperKalkulusStøtter);
        BeregningsgrunnlagInput input = kalkulatorInputTjeneste.lagInputMedBeregningsgrunnlag(koblingEntitet.getId());
        TilstandResponse tilstandResponse = beregningStegTjeneste.beregnFor(spesifikasjon.getStegType(), input, koblingEntitet.getId());

        if (tilstandResponse.getAksjonspunktMedTilstandDto().isEmpty()) {
            return Response.noContent().build();
        } else {
            return Response.ok(tilstandResponse).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/oppdater")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn",
            summary = ("Oppdaterer beregningsgrunnlag basert på løsning av aksjonspunkt."),
            responses = {@ApiResponse(description = "Liste med aksjonspunkter som har oppstått",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response håndter(@NotNull @Valid HåndterBeregningRequestAbacDto spesifikasjon) {
        var koblingReferanse = new KoblingReferanse(spesifikasjon.getEksternReferanse());
        Long koblingId = koblingTjeneste.hentKoblingId(koblingReferanse);

        håndtererApplikasjonTjeneste.håndter(koblingId, spesifikasjon.getHåndterBeregning());
        TilstandResponse tilstandResponse = new TilstandResponse(Collections.emptyList());
        return Response.ok(tilstandResponse).build();
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class StartBeregningRequestAbacDto extends StartBeregningRequest implements AbacDto {

        @JsonCreator
        public StartBeregningRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UuidDto eksternReferanse,
                                            @JsonProperty(value = "saksnummer", required = true) @NotNull @Pattern(regexp = "^[0-9_.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'") @Valid String saksnummer,
                                            @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                            @JsonProperty(value = "kalkulatorInput", required = true) @NotNull @Valid KalkulatorInputDto kalkulatorInput,
                                            @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(eksternReferanse, saksnummer, aktør, ytelseSomSkalBeregnes, kalkulatorInput);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();

            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getEksternReferanse());
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getAktør().getIdent());
        }
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class FortsettBeregningRequestAbacDto extends FortsettBeregningRequest implements AbacDto {


        @JsonCreator
        public FortsettBeregningRequestAbacDto(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                               @JsonProperty(value = "stegType", required = true) @NotNull @Valid StegType stegType,
                                               @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) @NotNull @Valid YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(eksternReferanse, ytelseSomSkalBeregnes, stegType);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getEksternReferanse());
            return abacDataAttributter;
        }
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HåndterBeregningRequestAbacDto extends HåndterBeregningRequest implements AbacDto {


        @JsonCreator
        public HåndterBeregningRequestAbacDto(@JsonProperty(value = "håndterBeregning", required = true) @NotNull @Valid HåndterBeregningDto håndterBeregning,
                                              @JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse) {
            super(håndterBeregning, eksternReferanse);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();

            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getEksternReferanse());
            return abacDataAttributter;
        }
    }
}
