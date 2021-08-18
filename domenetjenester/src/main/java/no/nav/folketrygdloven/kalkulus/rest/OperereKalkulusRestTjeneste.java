package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.sikkerhet.KalkulusBeskyttetRessursAttributt.BEREGNINGSGRUNNLAG;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulus.beregning.BeregningStegTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.MapHåndteringskodeTilTilstand;
import no.nav.folketrygdloven.kalkulus.beregning.input.HentInputResponsKode;
import no.nav.folketrygdloven.kalkulus.beregning.input.HåndteringInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.Resultat;
import no.nav.folketrygdloven.kalkulus.beregning.input.StegProsessInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndtererApplikasjonTjeneste;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.FortsettBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.StartBeregningListeRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandListeResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringListeRespons;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringPrRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.OppdateringRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;

@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(tags = @Tag(name = "beregn"))
@Path("/kalkulus/v1")
@ApplicationScoped
@Transactional
public class OperereKalkulusRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private BeregningStegTjeneste beregningStegTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private StegProsessInputTjeneste stegInputTjeneste;
    private HåndteringInputTjeneste håndteringInputTjeneste;
    private HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste;
    private RullTilbakeTjeneste rullTilbakeTjeneste;

    public OperereKalkulusRestTjeneste() {
        // for CDI
    }

    @Inject
    public OperereKalkulusRestTjeneste(KoblingTjeneste koblingTjeneste,
                                       BeregningStegTjeneste beregningStegTjeneste,
                                       KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                       StegProsessInputTjeneste stegInputTjeneste,
                                       HåndteringInputTjeneste håndteringInputTjeneste,
                                       HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste,
                                       RullTilbakeTjeneste rullTilbakeTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningStegTjeneste = beregningStegTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.stegInputTjeneste = stegInputTjeneste;
        this.håndteringInputTjeneste = håndteringInputTjeneste;
        this.håndtererApplikasjonTjeneste = håndtererApplikasjonTjeneste;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/start/bolk")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Starter en beregning basert på gitt input."), responses = {
            @ApiResponse(description = "Liste med avklaringsbehov som har oppstått per angitt eksternReferanse", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = CREATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response beregn(@NotNull @Valid StartBeregningListeRequestAbacDto spesifikasjon) {
        var aktørId = new AktørId(spesifikasjon.getAktør().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtterKontrakt.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        MDC.put("prosess_saksnummer", saksnummer.getVerdi());

        List<TilstandResponse> resultat = new ArrayList<>();

        for (var entry : spesifikasjon.getKalkulatorInputPerKoblingReferanse().entrySet()) {
            var eksternReferanse = entry.getKey();
            var kalkulatorInput = entry.getValue();
            var koblingReferanse = new KoblingReferanse(eksternReferanse);
            MDC.put("prosess_koblingreferanse", eksternReferanse.toString());

            var koblingEntitet = koblingTjeneste.finnEllerOpprett(koblingReferanse, ytelseTyperKalkulusStøtter, aktørId, saksnummer);

            boolean inputHarEndretSeg = kalkulatorInputTjeneste.lagreKalkulatorInput(koblingEntitet.getId(), kalkulatorInput);

            if (inputHarEndretSeg) {
                rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingEntitet.getId(), BeregningsgrunnlagTilstand.OPPRETTET);
            }

            var input = stegInputTjeneste.lagStartInput(koblingEntitet, kalkulatorInput);
            var tilstandResponse = beregningStegTjeneste.fastsettBeregningsaktiviteter(input);
            resultat.add(tilstandResponse);
        }

        return Response.ok(new TilstandListeResponse(resultat)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/fortsett/bolk")
    @Operation(description = "Utfører beregning basert på reqest", tags = "beregn", summary = ("Fortsetter en beregning basert på stegInput."), responses = {
            @ApiResponse(description = "Liste med avklaringsbehov som har oppstått", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TilstandResponse.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    public Response beregnVidere(@NotNull @Valid FortsettBeregningListeRequestAbacDto spesifikasjon) {
        String saksnummer = spesifikasjon.getSaksnummer();
        MDC.put("prosess_saksnummer", saksnummer);
        List<TilstandResponse> resultat = new ArrayList<>();
        Resultat<StegProsesseringInput> inputResultat;
        // Sjekker om request har oppdatert kalkulatorinput
        if (spesifikasjon.getKalkulatorInputPerKoblingReferanse() != null) {
            // kalkulatorinput oppdateres
            var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtterKontrakt.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
            kalkulatorInputTjeneste.lagreKalkulatorInput(ytelseTyperKalkulusStøtter, spesifikasjon.getKalkulatorInputPerKoblingReferanse());
            inputResultat = lagStegInputForKoblinger(spesifikasjon);
        } else {
            inputResultat = lagStegInputForKoblinger(spesifikasjon);
            // Sjekker om kalkulatorinput må oppdateres
            if (inputResultat.getKode() == HentInputResponsKode.ETTERSPØR_NY_INPUT) {
                return Response.ok(new TilstandListeResponse(true)).build();
            }
        }

        for (var inputPrKobling : inputResultat.getResultatPrKobling().entrySet()) {
            MDC.put("prosess_koblingreferanse", inputPrKobling.getKey().toString());
            BeregningSteg stegtype = BeregningSteg.fraKode(spesifikasjon.getStegType().getKode());
            var tilstandResponse = beregningStegTjeneste.beregnFor(stegtype, inputPrKobling.getValue());
            resultat.add(tilstandResponse);
        }

        return Response.ok(new TilstandListeResponse(resultat)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/oppdaterListe")
    @Operation(description = "Oppdaterer beregningsgrunnlag for oppgitt liste", tags = "beregn", summary = ("Oppdaterer beregningsgrunnlag basert på løsning av avklaringsbehov for oppgitt liste."), responses = {
            @ApiResponse(description = "Liste med endringer som ble gjort under oppdatering", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OppdateringListeRespons.class)))
    })
    @BeskyttetRessurs(action = UPDATE, resource = BEREGNINGSGRUNNLAG)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response oppdaterListe(@NotNull @Valid HåndterBeregningListeRequestAbacDto spesifikasjon) {

        // Mapper informasjon om kobling
        var koblingReferanser = spesifikasjon.getHåndterBeregningListe().stream().map(HåndterBeregningRequest::getEksternReferanse)
                .map(KoblingReferanse::new).collect(Collectors.toList());
        var koblinger = koblingTjeneste.hentKoblinger(koblingReferanser);
        var koblingTilDto = spesifikasjon.getHåndterBeregningListe().stream()
                .collect(Collectors.toMap(s -> finnKoblingId(koblinger, s), HåndterBeregningRequest::getHåndterBeregning));

        // Sjekker om request har oppdatert kalkulatorinput
        if (spesifikasjon.getKalkulatorInputPerKoblingReferanse() != null) {
            // kalkulatorinput oppdateres
            kalkulatorInputTjeneste.lagreKalkulatorInput(spesifikasjon.getKalkulatorInputPerKoblingReferanse());
        }

        // Lager Input for oppdatering
        var hentInputResultat = håndteringInputTjeneste.lagKalkulatorInput(koblingTilDto.keySet());
        // Sjekker om kalkulatorinput må oppdateres
        if (hentInputResultat.getKode() == HentInputResponsKode.ETTERSPØR_NY_INPUT) {
            return Response.ok(new OppdateringListeRespons(true)).build();
        }

        var tilstand = finnTilstandFraDto(koblingTilDto);

        // Ruller tilbake hvis det er tilbakehopp
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingTilDto.keySet(), tilstand);

        var håndterInputPrKobling = håndteringInputTjeneste.lagBeregningsgrunnlagInput(koblingTilDto.keySet(),
                hentInputResultat.getResultatPrKobling(), tilstand);

        // Håndterer oppdatering
        var håndterResultat = håndtererApplikasjonTjeneste.håndter(håndterInputPrKobling, koblingTilDto);

        // Lager responsobjekt
        List<OppdateringPrRequest> oppdateringer = håndterResultat.entrySet().stream()
                .sorted(Comparator.comparing(e -> håndterInputPrKobling.get(e.getKey()).getSkjæringstidspunktOpptjening()))
                .map(res -> new OppdateringPrRequest(res.getValue(), finnKoblingUUIDForKoblingId(koblinger, res)))
                .collect(Collectors.toList());
        return Response.ok(Objects.requireNonNullElseGet(new OppdateringListeRespons(oppdateringer), OppdateringRespons::TOM_RESPONS)).build();
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

            // Vi kaster ikkje feil om vi ikkje finner kobling
            // Grunnen til dette er fordi i nokon FRISINN-saker hopper man over beregning ved avslag og dermed vil ikkje beregning vere kjørt
            // Vurderingen er at dette er noko som kan håndteres utan feil sidan målet er å deaktivere (vi vil deaktivere ein beregning som blei avslått
            // før den nådde beregning)
            if (kopt.isPresent()) {
                KoblingEntitet kobling = kopt.get();
                if (!Objects.equals(kobling.getSaksnummer().getVerdi(), saksnummer)) {
                    throw new IllegalArgumentException("Kobling tilhører ikke saksnummer [" + saksnummer + "]: " + kobling);
                }
                rullTilbakeTjeneste.deaktiverAktivtBeregningsgrunnlagOgInput(kobling.getId());
            }
        }

        return Response.ok().build();
    }

    private Resultat<StegProsesseringInput> lagStegInputForKoblinger(@Valid @NotNull FortsettBeregningListeRequestAbacDto spesifikasjon) {
        var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtterKontrakt.fraKode(spesifikasjon.getYtelseSomSkalBeregnes().getKode());
        List<KoblingReferanse> referanser = spesifikasjon.getEksternReferanser().stream().map(KoblingReferanse::new).collect(Collectors.toList());
        var koblinger = koblingTjeneste.hentKoblinger(referanser, ytelseTyperKalkulusStøtter);

        List<KoblingEntitet> koblingUtenSaksnummer = koblinger.stream()
                .filter(k -> !Objects.equals(k.getSaksnummer().getVerdi(), spesifikasjon.getSaksnummer())).collect(Collectors.toList());
        if (!koblingUtenSaksnummer.isEmpty()) {
            throw new IllegalArgumentException("Koblinger tilhører ikke saksnummer [" + spesifikasjon.getSaksnummer() + "]: " + koblingUtenSaksnummer);
        }


        BeregningSteg stegType = BeregningSteg.fraKode(spesifikasjon.getStegType().getKode());
        return stegInputTjeneste.lagFortsettInput(koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toList()), stegType,
                spesifikasjon.getKoblingRelasjon().orElse(Collections.emptyMap()));
    }

    private BeregningsgrunnlagTilstand finnTilstandFraDto(Map<Long, HåndterBeregningDto> håndterBeregningDtoPrKobling) {
        List<BeregningsgrunnlagTilstand> tilstander = håndterBeregningDtoPrKobling.values().stream().map(HåndterBeregningDto::getKode)
                .map(MapHåndteringskodeTilTilstand::map)
                .distinct()
                .collect(Collectors.toList());
        if (tilstander.size() > 1) {
            throw new IllegalStateException("Kan ikke løse avklaringsbehov for flere tilstander samtidig");
        }
        return tilstander.get(0);
    }

    private Long finnKoblingId(List<KoblingEntitet> koblinger, HåndterBeregningRequest s) {
        return koblinger.stream().filter(kobling -> kobling.getKoblingReferanse().getReferanse().equals(s.getEksternReferanse()))
                .findFirst().map(KoblingEntitet::getId).orElse(null);
    }

    private UUID finnKoblingUUIDForKoblingId(List<KoblingEntitet> koblinger, Map.Entry<Long, OppdateringRespons> res) {
        return koblinger.stream().filter(k -> k.getId().equals(res.getKey())).findFirst().map(KoblingEntitet::getKoblingReferanse)
                .map(KoblingReferanse::getReferanse).orElse(null);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class StartBeregningListeRequestAbacDto extends StartBeregningListeRequest implements AbacDto {

        public StartBeregningListeRequestAbacDto() {
        }

        public StartBeregningListeRequestAbacDto(Map<UUID, KalkulatorInputDto> kalkulatorInput, String saksnummer, PersonIdent aktør,
                                                 YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
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

        public FortsettBeregningListeRequestAbacDto(String saksnummer,
                                                    List<UUID> eksternReferanser,
                                                    Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse,
                                                    StegType stegType, YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes) {
            super(saksnummer, eksternReferanser, kalkulatorInputPerKoblingReferanse, ytelseSomSkalBeregnes, stegType);
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
    public static class DeaktiverBeregningsgrunnlagRequestAbacDto extends BeregningsgrunnlagListeRequest implements AbacDto {

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HåndterBeregningListeRequestAbacDto extends HåndterBeregningListeRequest implements AbacDto {

        public HåndterBeregningListeRequestAbacDto() {
            // Jackson
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();

            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            return abacDataAttributter;
        }
    }

}
