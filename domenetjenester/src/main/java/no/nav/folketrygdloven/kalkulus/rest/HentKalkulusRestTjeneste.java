package no.nav.folketrygdloven.kalkulus.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
import jakarta.ws.rs.core.Response.Status;

import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagGuiTjeneste;
import no.nav.folketrygdloven.kalkulator.guitjenester.KalkulatorGuiInterface;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.beregning.GUIBeregningsgrunnlagInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.mapTilKontrakt.MapDetaljertBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulus.mappers.MapIAYTilKalulator;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoForGUIRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagDtoListeForGUIRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagListeRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentBeregningsgrunnlagRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.HentForSakRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.AktiveReferanser;
import no.nav.folketrygdloven.kalkulus.response.v1.EksternReferanseDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.BeregningsgrunnlagPrReferanse;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagListe;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
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
    @Path("/grunnlag/bolk")
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response hentAktiveBeregningsgrunnlagGrunnlag(@NotNull @Valid HentBeregningsgrunnlagListeRequestAbacDto spesifikasjon) {
        if (spesifikasjon.getRequestPrReferanse().isEmpty()) {
            return Response.noContent().build();
        }
        var ytelseTyper = spesifikasjon.getRequestPrReferanse().stream()
                .map(HentBeregningsgrunnlagRequest::getYtelseSomSkalBeregnes).collect(Collectors.toSet());
        if (ytelseTyper.size() != 1) {
            return Response.status(Status.BAD_REQUEST).entity("Feil input, alle requests må ha samme ytelsetype. Fikk: " + ytelseTyper).build();
        }
        var ytelseType = ytelseTyper.iterator().next();
        var koblingReferanser = spesifikasjon.getRequestPrReferanse().stream().map(v -> new KoblingReferanse(v.getKoblingReferanse()))
                .collect(Collectors.toList());
        var kobling = koblingTjeneste.hentKoblinger(koblingReferanser, ytelseType).stream().findFirst();
        var beregningsgrunnlagGrunnlagDto = kobling.flatMap(k -> beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(k.getId()))
            .map(MapDetaljertBeregningsgrunnlag::map);
        return beregningsgrunnlagGrunnlagDto.isEmpty() ? Response.noContent().build() : Response.ok(Collections.singletonList(beregningsgrunnlagGrunnlagDto)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent beregningsgrunnlagDto for angitt behandling som brukes frontend", summary = ("Returnerer beregningsgrunnlagDto for behandling."), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    @Path("/beregningsgrunnlagListe")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBeregningsgrunnlagDtoListe(@NotNull @Valid HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto spesifikasjon) {
        List<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner = spesifikasjon.getRequestPrReferanse();
        Map<Long, BeregningsgrunnlagGUIInput> inputResultat;
        try {
            inputResultat = finnInputForGenereringAvDtoTilGUI(spesifikasjoner);
        } catch (UgyldigInputException e) {
            return Response.ok(new BeregningsgrunnlagListe(true)).build();
        }

        List<BeregningsgrunnlagPrReferanse<BeregningsgrunnlagDto>> dtoPrReferanse = hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(
                inputResultat, spesifikasjoner).entrySet()
                .stream()
                .map(e -> new BeregningsgrunnlagPrReferanse<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        return Response.ok(new BeregningsgrunnlagListe(dtoPrReferanse)).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter aktive referanser", summary = ("Henter aktive referanser for sak"), tags = "beregningsgrunnlag")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    @Path("/aktive-referanser")
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentAktiveReferanser(@NotNull @Valid HentForSakRequestAbacDto spesifikasjon) {
        var koblinger = koblingTjeneste.hentKoblingerForSak(new Saksnummer(spesifikasjon.getSaksnummer().verdi()));

        var koblingIderMedAktiveGrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntiteter(koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet()))
                .stream().filter(BeregningsgrunnlagGrunnlagEntitet::erAktivt)
                .map(BeregningsgrunnlagGrunnlagEntitet::getKoblingId)
                .collect(Collectors.toSet());
        return Response.ok(new AktiveReferanser(koblinger.stream()
                .filter(k -> koblingIderMedAktiveGrunnlag.contains(k.getId()))
                .map(KoblingEntitet::getKoblingReferanse).map(KoblingReferanse::getReferanse)
                .map(EksternReferanseDto::new).toList())).build();
    }

    private Map<UUID, BeregningsgrunnlagDto> hentBeregningsgrunnlagDtoForGUIForSpesifikasjon(Map<Long, BeregningsgrunnlagGUIInput> inputPrKobling, List<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner) {
        return inputPrKobling.values()
                .stream().collect(Collectors.toMap(input -> input.getKoblingReferanse().getKoblingUuid(), input -> mapTilDto(spesifikasjoner, input)));
    }

    private Map<Long, BeregningsgrunnlagGUIInput> finnInputForGenereringAvDtoTilGUI(Collection<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner) {
        var koblingReferanser = spesifikasjoner.stream().map(HentBeregningsgrunnlagDtoForGUIRequest::getKoblingReferanse)
                .map(KoblingReferanse::new)
                .collect(Collectors.toSet());

        var ytelseSomSkalBeregnes = spesifikasjoner.stream()
                .map(HentBeregningsgrunnlagDtoForGUIRequest::getYtelseSomSkalBeregnes)
                .collect(Collectors.toSet());

        if (ytelseSomSkalBeregnes.isEmpty()) {
            return Map.of();
        } else if (ytelseSomSkalBeregnes.size() != 1) {
            throw new IllegalArgumentException("Støtter kun at alle har samme ytelse type. Fikk: " + ytelseSomSkalBeregnes);
        }
        var ytelseType = ytelseSomSkalBeregnes.iterator().next();
        var koblinger = koblingTjeneste.hentKoblinger(koblingReferanser, ytelseType);
        var koblingIds = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toList());
        var koblingRelasjoner = koblingTjeneste.hentKoblingRelasjoner(koblingIds);

        return guiInputTjeneste.lagInputForKoblinger(koblingIds, koblingRelasjoner);
    }

    private BeregningsgrunnlagDto mapTilDto(Collection<HentBeregningsgrunnlagDtoForGUIRequest> spesifikasjoner, BeregningsgrunnlagGUIInput input) {
        MDC.put("prosess_koblingId", input.getKoblingReferanse().getKoblingId().toString());
        var spesifikasjon = spesifikasjoner.stream().filter(s -> s.getKoblingReferanse().equals(input.getKoblingReferanse().getKoblingUuid()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Ingen match blant koblinger"));
        input.oppdaterArbeidsgiverinformasjon(MapIAYTilKalulator.mapArbeidsgiverReferanser(spesifikasjon.getReferanser()));
        BeregningsgrunnlagDto beregningsgrunnlagDto = dtoTjeneste.lagBeregningsgrunnlagDto(input);
        beregningsgrunnlagDto.setVilkårsperiodeFom(spesifikasjon.getVilkårsperiodeFom());
        MDC.remove("prosess_koblingId");
        return beregningsgrunnlagDto;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagListeRequestAbacDto extends HentBeregningsgrunnlagListeRequest implements AbacDto {


        public HentBeregningsgrunnlagListeRequestAbacDto() {
            // For Json deserialisering
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            return abacDataAttributter;
        }
    }


    @Deprecated(forRemoval = true, since = "1.0")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagRequestAbacDto extends HentBeregningsgrunnlagRequest implements AbacDto {

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getKoblingReferanse());
            return abacDataAttributter;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentForSakRequestAbacDto extends HentForSakRequest implements AbacDto {

        public HentForSakRequestAbacDto() {
            // For Json deserialisering
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
            return abacDataAttributter;
        }
    }


    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    public static class HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto extends HentBeregningsgrunnlagDtoListeForGUIRequest implements AbacDto {


        public HentBeregningsgrunnlagDtoListeForGUIRequestAbacDto() {
            // For Json deserialisering
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            abacDataAttributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
            return abacDataAttributter;
        }
    }

}
